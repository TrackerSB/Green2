/*
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.menu;

import bayern.steinbrecher.green2.Controller;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.contribution.Contribution;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.green2.generator.AddressGenerator;
import bayern.steinbrecher.green2.generator.BirthdayGenerator;
import bayern.steinbrecher.green2.generator.sepa.SepaPain00800302XMLGenerator;
import bayern.steinbrecher.green2.generator.sepa.SequenceType;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Originator;
import bayern.steinbrecher.green2.query.Query;
import bayern.steinbrecher.green2.result.ResultDialog;
import bayern.steinbrecher.green2.selection.Selection;
import bayern.steinbrecher.green2.selection.SelectionGroup;
import bayern.steinbrecher.green2.sepaform.SepaForm;
import bayern.steinbrecher.green2.utility.SupplyingMap;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.SepaUtility;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Controller for Menu.fxml.
 *
 * @author Stefan Huber
 */
public class MainMenuController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    /**
     * Maps resource keys ({@link EnvironmentHandler#getResourceValue(java.lang.String, java.lang.Object...)} to
     * functions generating checks.
     */
    private final Map<String, Callable<List<String>>> checkFunctions = Map.of(
            "iban", () -> checkIbans(),
            "bic", () -> checkBics(),
            "birthdays", () -> checkDates(m -> m.getPerson().getBirthday()),
            "columnMandatSigned", () -> checkDates(m -> m.getAccountHolder().getMandateSigned()),
            "contributions", () -> checkContributions()
    );
    private DBConnection dbConnection;
    private final ObjectProperty<Optional<LocalDateTime>> dataLastUpdated
            = new SimpleObjectProperty<>(Optional.empty());
    private final BooleanProperty honoringsAvailable = new SimpleBooleanProperty(this, "honoringsAvailable");
    private final Map<Integer, CompletableFuture<List<Member>>> memberBirthday = new SupplyingMap<>(year -> {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBirthdayMember(year);
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                return null;
            }
        });
    });
    private final CompletableFutureProperty<Set<Member>> member = new CompletableFutureProperty<>();
    private final CompletableFutureProperty<Set<Member>> memberNonContributionfree = new CompletableFutureProperty<>();
    private final CompletableFutureProperty<Map<String, String>> nicknames = new CompletableFutureProperty<>();
    private final BooleanProperty allDataAvailable = new SimpleBooleanProperty(this, "allDataAvailable");
    private final BooleanProperty activateBirthdayFeatures
            = new SimpleBooleanProperty(this, "activateBirthdayFeatures", true);

    @FXML
    private MenuItem generateAddressesBirthdayItem;
    @FXML
    private MenuItem generateBirthdayInfosItem;
    @FXML
    private CheckedIntegerSpinner yearSpinner;
    @FXML
    private CheckedIntegerSpinner yearSpinner2;
    @FXML
    private CheckedIntegerSpinner yearSpinner3;
    @FXML
    private javafx.scene.control.Menu honorings;
    @FXML
    private javafx.scene.control.Menu licensesMenu;
    @FXML
    private Label dataLastUpdatedLabel;
    @FXML
    private Rectangle overlayBackground;

    /**
     * Binds the textual representation of the year spinners to text properties of elements in the menu.
     */
    private void bindYearSpinnerTo() {
        StringBinding yearBinding = Bindings.createStringBinding(
                () -> yearSpinner.isValid() ? yearSpinner.getValue().toString() : "?",
                yearSpinner.validProperty(), yearSpinner.valueProperty());

        generateBirthdayInfosItem.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("groupedBirthdayMember") + " ")
                        .concat(yearBinding));
        generateAddressesBirthdayItem.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("birthdayExpression") + " ")
                        .concat(yearBinding));
    }

    private void bindAvailabilityInformations() {
        allDataAvailable.bind(member.availableProperty()
                .and(memberNonContributionfree.availableProperty())
                .and(nicknames.availableProperty()));
        dataLastUpdatedLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            Optional<LocalDateTime> dataLastUpdatedOptional = getDataLastUpdated();
            String text;
            if (dataLastUpdatedOptional.isPresent()) {
                if (dataLastUpdatedOptional.get().equals(LocalDateTime.MIN)) {
                    text = EnvironmentHandler.getResourceValue("dataQueryFailed");
                } else {
                    text = EnvironmentHandler.getResourceValue(
                            "dataLastUpdated", dataLastUpdatedOptional.get()
                                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
                }
            } else {
                text = EnvironmentHandler.getResourceValue("noData");
            }
            return text;
        }, dataLastUpdatedProperty()));
    }

    private void bindHonoringsAvailable() {
        member.availableProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        Platform.runLater(() -> {
                            try {
                                honoringsAvailable.set(!member.get()
                                        .get()
                                        .stream()
                                        .map(Member::getHonorings)
                                        .allMatch(Map::isEmpty));
                            } catch (InterruptedException | ExecutionException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                                honoringsAvailable.set(false);
                            }
                        });
                    }
                });
    }

    private void showHonorings(int yearsOfMembership) {
        if (yearSpinner.isValid()) {
            List<List<String>> result = new ArrayList<>();
            result.add(List.of(
                    EnvironmentHandler.getResourceValue("membershipNumber"),
                    EnvironmentHandler.getResourceValue("prename"),
                    EnvironmentHandler.getResourceValue("lastname"),
                    EnvironmentHandler.getResourceValue("memberSince"),
                    EnvironmentHandler.getResourceValue("isActive")
            ));
            try {
                result.addAll(member.get()
                        .get()
                        .stream()
                        .filter(m -> !m.getHonorings().getOrDefault(yearsOfMembership, Boolean.FALSE))
                        .filter(m -> yearSpinner.getValue() - m.getMemberSince().getYear() >= yearsOfMembership)
                        .map(
                                m -> List.of(Integer.toString(m.getMembershipnumber()),
                                        m.getPerson().getPrename(),
                                        m.getPerson().getLastname(),
                                        m.getMemberSince().toString(),
                                        m.isActive().map(b -> b.toString()).orElse("")))
                        .collect(Collectors.toList())
                );
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            Stage resultStage = new Stage();
            new ResultDialog(result)
                    .start(resultStage);
            resultStage.show();
        }
    }

    private void generateHonoringsMenu() {
        member.availableProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        try {
                            member.get()
                                    .get()
                                    .stream()
                                    .map(Member::getHonorings)
                                    .flatMap(h -> h.keySet().stream())
                                    .distinct()
                                    .sorted()
                                    .forEach(year -> {
                                        String membershipTitle
                                                = EnvironmentHandler.getResourceValue("yearsMembership", year);
                                        MenuItem membershipItem = new MenuItem(membershipTitle);
                                        membershipItem.setOnAction(aevt -> showHonorings(year));
                                        membershipItem.disableProperty()
                                                .bind(yearSpinner.validProperty().not());
                                        Platform.runLater(() -> {
                                            honorings.getItems()
                                                    .add(membershipItem);
                                        });
                                    });
                        } catch (InterruptedException | ExecutionException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
                });
    }

    private void generateLicensesMenu() {
        EnvironmentHandler.getLicenses().stream().forEach(license -> {
            MenuItem item = new MenuItem(license.getName());
            item.setOnAction(aevt -> {
                try {
                    license.setWritable(false, false);
                    Desktop.getDesktop().open(license);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not open license", ex);
                }
            });
            licensesMenu.getItems().add(item);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        yearSpinner2.valueFactoryProperty().bind(yearSpinner.valueFactoryProperty());
        yearSpinner3.valueFactoryProperty().bind(yearSpinner.valueFactoryProperty());
        yearSpinner.getValueFactory().setValue(CURRENT_YEAR + 1);

        bindYearSpinnerTo();
        bindAvailabilityInformations();
        bindHonoringsAvailable();

        //Bind activateBirthdayFeatures
        activateBirthdayFeatures.bind(Bindings.createBooleanBinding(
                () -> EnvironmentHandler.getProfile().get(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES),
                EnvironmentHandler.loadedProfileProperty(),
                EnvironmentHandler.getProfile().getProperty(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES)));

        generateHonoringsMenu();
        generateLicensesMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStage(Stage stage) {
        overlayBackground.widthProperty().bind(stage.widthProperty());
        overlayBackground.heightProperty().bind(stage.heightProperty());
        super.setStage(stage);
    }

    /**
     * Sets the connection to use for querying data and queries immediatly for new data using the given connection.
     *
     * @param dbConnection The connection to use for querying data.
     */
    public void setDbConnection(DBConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("The connection must not be null.");
        }
        this.dbConnection = dbConnection;
        queryData();
    }

    private List<Member> getBirthdayMember(int year) throws InterruptedException, ExecutionException {
        return member.get().get()
                .parallelStream()
                .filter(m -> BirthdayGenerator.getsNotified(m, year))
                .collect(Collectors.toList());
    }

    private void showNoMemberForOutputDialog() {
        String noMemberForOutput = EnvironmentHandler.getResourceValue("noMemberForOutput");
        DialogUtility.showAndWait(DialogUtility.createInfoAlert(getStage(), noMemberForOutput, noMemberForOutput));
    }

    private void generateAddresses(Collection<Member> requestedMember, File outputFile)
            throws IOException, InterruptedException, ExecutionException {
        if (requestedMember.isEmpty()) {
            throw new IllegalArgumentException("Passed empty list to generateAddresses(...)");
        }
        IOStreamUtility.printContent(
                AddressGenerator.generateAddressData(requestedMember, nicknames.get().get()), outputFile, true);
    }

    /**
     * Generates a file Serienbrief_alle.csv containing addresses of all member.
     */
    public void generateAddressesAll() {
        try {
            Set<Member> memberList = this.member.get().get();
            if (memberList.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                Optional<File> path = EnvironmentHandler.askForSavePath(getStage(), "serialLetterAll", "csv");
                if (path.isPresent()) {
                    generateAddresses(memberList, path.get());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not generate addresses.", ex);
        }
    }

    /**
     * Generates a file Serienbrief_Geburtstag_{@code year}.csv containing addresses of all member who get a birthday
     * notification in year {@code year}.
     *
     * @param year The year to look for member.
     */
    public void generateAddressesBirthday(int year) {
        try {
            List<Member> memberBirthdayList = memberBirthday.get(year).get();
            if (memberBirthdayList.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                Optional<File> path
                        = EnvironmentHandler.askForSavePath(getStage(), "serialLetterBirthday", "csv", year);
                if (path.isPresent()) {
                    generateAddresses(memberBirthdayList, path.get());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not generate addresses.", ex);
        }
    }

    //TODO Where to place this method? How to generlize it for all optional columns?
    private boolean isContributionColumnEnabled() throws ExecutionException, InterruptedException {
        return member.get()
                .get()
                .stream()
                .anyMatch(m -> m.getContribution().isPresent());
    }

    @SuppressWarnings("unchecked")
    private void generateSepa(Future<Set<Member>> memberToSelectFuture, boolean useMemberContributions,
            SequenceType sequenceType) {
        try {
            Set<Member> memberToSelect = memberToSelectFuture.get();

            if (memberToSelect.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                boolean askForContribution = !(useMemberContributions && isContributionColumnEnabled());

                WizardPage<Optional<Originator>> sepaFormPage = new SepaForm().getWizardPage();
                sepaFormPage.setNextFunction(() -> askForContribution ? "contribution" : "selection");
                WizardPage<Optional<BiMap<Double, Color>>> contributionPage = new Contribution().getWizardPage();
                WizardPage<Optional<Set<Member>>> selectionPage = new Selection<>(memberToSelect).getWizardPage();
                selectionPage.setFinish(true);

                Map<String, WizardPage<?>> pages = new HashMap<>();
                pages.put(WizardPage.FIRST_PAGE_KEY, sepaFormPage);
                pages.put("contribution", contributionPage);
                pages.put("selection", selectionPage);
                Wizard wizard = new Wizard(pages);
                contributionPage.setNextFunction(() -> {
                    WizardPage<Optional<Map<Member, Double>>> selectionGroupPage
                            = new SelectionGroup<>(new HashSet<>(memberToSelect),
                                    contributionPage.getResultFunction().call().orElse(HashBiMap.create()))
                                    .getWizardPage();
                    selectionGroupPage.setFinish(true);
                    wizard.put("selectionGroup", selectionGroupPage);
                    return "selectionGroup";
                });
                Stage wizardStage = new Stage();
                wizardStage.initOwner(getStage());
                wizardStage.setTitle(EnvironmentHandler.getResourceValue("generateSepa"));
                wizardStage.setResizable(false);
                wizardStage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
                wizard.start(wizardStage);
                wizard.finishedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        Map<String, ?> results = wizard.getResults().get();
                        Set<Member> selectedMember;
                        //TODO Is there any way to avoid explicit casts?
                        if (askForContribution) {
                            Map<Member, Double> groupedMember
                                    = ((Optional<Map<Member, Double>>) results.get("selectionGroup")).get();
                            selectedMember = groupedMember.entrySet().stream()
                                    .map(entry -> {
                                        return new Member.Builder(entry.getKey())
                                                .setContribution(Optional.ofNullable(entry.getValue()))
                                                .generate();
                                    })
                                    .collect(Collectors.toSet());
                        } else {
                            selectedMember = ((Optional<Set<Member>>) results.get("selection")).get();
                        }
                        Originator originator = ((Optional<Originator>) results.get(WizardPage.FIRST_PAGE_KEY)).get();

                        EnvironmentHandler.askForSavePath(getStage(), "sepa", "xml")
                                .ifPresent(file -> {
                                    try {
                                        List<Member> invalidMember
                                                = SepaPain00800302XMLGenerator.createXMLFile(selectedMember, originator,
                                                        sequenceType, file, EnvironmentHandler.getProfile()
                                                                .getOrDefault(ProfileSettings.SEPA_USE_BOM, true));
                                        String message = invalidMember.stream()
                                                .map(Member::toString)
                                                .collect(Collectors.joining("\n"));
                                        if (!message.isEmpty()) {
                                            Alert alert = DialogUtility.createErrorAlert(getStage(), message + "\n"
                                                    + EnvironmentHandler.getResourceValue("haveBadAccountInformation"));
                                            Platform.runLater(() -> alert.show());
                                        }
                                    } catch (IOException ex) {
                                        LOGGER.log(Level.SEVERE, "The sepa xml file could not be created.", ex);
                                    }
                                });
                    }
                });
                wizardStage.getScene().getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
                wizardStage.showAndWait();
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            String noSepaDebit = EnvironmentHandler.getResourceValue("noSepaDebit");
            DialogUtility.showAndWait(DialogUtility.createStacktraceAlert(getStage(), ex, noSepaDebit, noSepaDebit));
        }
    }

    /**
     * Disables the node which {@code aevt} is belonging to, runs {@code run} and enables it again.
     *
     * @param aevt The event of the control which calls {@code run}.
     * @param run The method to call.
     */
    private void callOnDisabled(EventObject aevt, Runnable run) {
        Object sourceObj = aevt.getSource();
        Class<?> sourceClass = sourceObj.getClass();
        if (sourceClass.isAssignableFrom(Node.class) || sourceClass.isAssignableFrom(MenuItem.class)) {
            try {
                Method disablePropertyMethod = sourceClass.getMethod("disableProperty");
                Method isBoundMethod = disablePropertyMethod.getReturnType().getMethod("isBound");
                if ((boolean) isBoundMethod.invoke(disablePropertyMethod.invoke(sourceObj))) {
                    //When getting here: sourceObj.disableProperty().isBound() == true
                    LOGGER.log(Level.WARNING,
                            "Cannot disable control {0} since its DisableProperty is bound.", sourceObj);
                    run.run();
                } else {
                    Method setDisableMethod = sourceClass.getMethod("setDisable", Boolean.TYPE);
                    setDisableMethod.invoke(sourceObj, true);
                    run.run();
                    setDisableMethod.invoke(sourceObj, false);
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            LOGGER.log(
                    Level.WARNING, "The source of the ActionEvent is no Node and no MenuItem. It can´t be disabled.");
            run.run();
        }
    }

    @FXML
    private void queryData() {
        member.set(CompletableFuture.supplyAsync(() -> dbConnection.getAllMember()));
        nicknames.set(CompletableFuture.supplyAsync(() -> dbConnection.getAllNicknames()));
        CompletableFuture.allOf(member.get(), nicknames.get())
                .whenCompleteAsync((result, throwable) -> {
                    LocalDateTime datetime;
                    if (throwable == null) {
                        datetime = LocalDateTime.now();
                    } else {
                        LOGGER.log(Level.SEVERE, "Retrieving the data failed.", throwable);
                        datetime = null; //NOPMD - Make sure datetime is initialized.
                    }
                    Platform.runLater(() -> dataLastUpdated.set(Optional.ofNullable(datetime)));
                });
        memberNonContributionfree.set(member.get().thenApplyAsync(
                ml -> ml.parallelStream()
                        .filter(m -> !m.isContributionfree())
                        .collect(Collectors.toSet())));

        //Precalculate memberBirthday for commonly used years
        int currentYear = LocalDate.now().getYear();
        IntStream.rangeClosed(currentYear - 1, currentYear + 1)
                .forEach(memberBirthday::get);
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void openQueryDialog(ActionEvent aevt) {
        callOnDisabled(aevt, () -> {
            try {
                Map<String, WizardPage<?>> pages = new HashMap<>();
                WizardPage<Optional<List<List<String>>>> queryDialogPage = new Query(dbConnection).getWizardPage();
                pages.put(WizardPage.FIRST_PAGE_KEY, queryDialogPage);
                Wizard queryWizard = new Wizard(pages);
                queryDialogPage.setNextFunction(() -> {
                    WizardPage<Optional<Void>> queryResultPage
                            = new ResultDialog(queryDialogPage.getResultFunction().call().orElse(new ArrayList<>()))
                                    .getWizardPage();
                    queryResultPage.setFinish(true);
                    queryWizard.put("queryResult", queryResultPage);
                    return "queryResult";
                });
                Stage wizardStage = new Stage();
                wizardStage.initOwner(getStage());
                wizardStage.setTitle(EnvironmentHandler.getResourceValue("queryData"));
                wizardStage.setResizable(true);
                wizardStage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
                queryWizard.start(wizardStage);
                wizardStage.getScene().getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
                wizardStage.showAndWait();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                DialogUtility.createStacktraceAlert(
                        getStage(), ex, EnvironmentHandler.getResourceValue("noQueryDialog"))
                        .showAndWait();
            }
        });
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void generateContributionSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(memberNonContributionfree.get(), true, SequenceType.RCUR));
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void generateUniversalSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(member.get(), false, SequenceType.RCUR));
    }

    private List<String> checkIbans() throws InterruptedException, ExecutionException {
        String noIban = EnvironmentHandler.getResourceValue("noIban");
        return member.get().get().parallelStream()
                .filter(m -> !SepaUtility.isValidIban(m.getAccountHolder().getIban()))
                .map(m -> {
                    String iban = m.getAccountHolder().getIban();
                    return m + ": \"" + (iban.isEmpty() ? noIban : iban) + "\"";
                })
                .collect(Collectors.toList());
    }

    private List<String> checkBics() throws InterruptedException, ExecutionException {
        String noBic = EnvironmentHandler.getResourceValue("noBic");
        return member.get().get().parallelStream()
                .filter(m -> !SepaUtility.isValidBic(m.getAccountHolder().getBic()))
                .map(m -> {
                    String bic = m.getAccountHolder().getBic();
                    return m + ": \"" + (bic.isEmpty() ? noBic : bic) + "\"";
                })
                .collect(Collectors.toList());
    }

    private List<String> checkDates(Function<Member, LocalDate> dateFunction)
            throws ExecutionException, InterruptedException {
        return member.get().get().parallelStream()
                .filter(m -> dateFunction.apply(m) == null)
                .map(m -> m.toString() + ": \"" + dateFunction.apply(m) + "\"")
                .collect(Collectors.toList());
    }

    private List<String> checkContributions() throws InterruptedException, ExecutionException {
        List<String> invalidContributions;
        if (isContributionColumnEnabled()) {
            invalidContributions = member.get().get().parallelStream()
                    .filter(m -> {
                        Optional<Double> contribution = m.getContribution();
                        return !contribution.isPresent() || contribution.get() < 0
                                || contribution.get() == 0 && !m.isContributionfree();
                    })
                    .map(m -> m.toString() + ": " + m.getContribution())
                    .collect(Collectors.toList());
        } else {
            invalidContributions = new ArrayList<>();
        }
        return invalidContributions;
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void checkData(ActionEvent aevt) {
        callOnDisabled(aevt, () -> {
            Map<String, List<String>> reports = new HashMap<>();
            checkFunctions.entrySet().stream()
                    .forEach(entry -> {
                        List<String> messages;
                        try {
                            messages = entry.getValue().call();
                        } catch (Exception ex) { //NOPMD - Make sure the checks are not abborted.
                            LOGGER.log(Level.SEVERE, null, ex);
                            messages = Arrays.asList(ex.getLocalizedMessage().split("\n"));
                        }
                        reports.put(EnvironmentHandler.getResourceValue(entry.getKey()), messages);
                    });

            Stage reportsStage = new Stage();
            reportsStage.setTitle(EnvironmentHandler.getResourceValue("checkData"));
            DialogUtility.createCheckReportDialog(getStage(), reportsStage, reports);
            reportsStage.showAndWait();
        });
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void generateAddressesAll(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateAddressesAll());
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void generateAddressesBirthday(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> generateAddressesBirthday(yearSpinner.getValue()));
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void generateBirthdayInfos(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> {
                Integer year = yearSpinner.getValue();
                try {
                    List<Member> birthdayList = memberBirthday.get(year).get();
                    if (birthdayList.isEmpty()) {
                        showNoMemberForOutputDialog();
                    } else {
                        Optional<File> path = EnvironmentHandler.askForSavePath(getStage(), "birthdays", "csv", year);
                        if (path.isPresent()) {
                            IOStreamUtility.printContent(
                                    BirthdayGenerator.createGroupedOutput(birthdayList, year), path.get(), true);
                        }
                    }
                } catch (InterruptedException | ExecutionException | IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not generate birthday infos.", ex);
                }
            });
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void showCredits() {
        String credits = EnvironmentHandler.getResourceValue("credits");
        Alert alert = DialogUtility.createMessageAlert(
                getStage(), EnvironmentHandler.getResourceValue("creditsContent"), null, credits, credits);
        Platform.runLater(() -> alert.show());
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void showVersion() {
        String version = EnvironmentHandler.getResourceValue("version");
        Alert alert = DialogUtility.createInfoAlert(getStage(), EnvironmentHandler.VERSION, version, version, version);
        Platform.runLater(() -> alert.show());
    }

    /**
     * Returns the property containing the date when the date was last updated.
     *
     * @return The property containing the date when the date was last updated.
     * @see #getDataLastUpdated()
     */
    public ReadOnlyObjectProperty<Optional<LocalDateTime>> dataLastUpdatedProperty() {
        return dataLastUpdated;
    }

    /**
     * Returns an {@code Optional} containing the timestamp when the data was last updated.
     *
     * @return An {@code Optional} containing the timestamp when the data was last updated. Returns
     * {@code Optional.empty()} if the data is not yet received.
     */
    public Optional<LocalDateTime> getDataLastUpdated() {
        return dataLastUpdated.get();
    }

    /**
     * Returns the property holding whether all data needed by the functions of the menu is available.
     *
     * @return The property holding whether all data needed by the functions of the menu is available.
     */
    public ReadOnlyBooleanProperty allDataAvailableProperty() {
        return allDataAvailable;
    }

    /**
     * Checks whether all data needed by the functions is available.
     *
     * @return {@code true} only if all data needed by the functions of the menu is available.
     */
    public boolean isAllDataAvailable() {
        return allDataAvailable.get();
    }

    /**
     * Returns the property holding whether the currently loaded profile has activated the birthday features.
     *
     * @return The property holding whether the currently loaded profile has activated the birthday features.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated(forRemoval = false, since = "2u13")
    public ReadOnlyProperty<Boolean> activateBirthdayFeaturesProperty() {
        return activateBirthdayFeatures;
    }

    /**
     * Checks whether the birthday features are activated according to the currently loaded profile.
     *
     * @return {@code true} only if the birthday features are activated according to the currently loaded profile.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated(forRemoval = false, since = "2u13")
    public boolean isActivateBirthdayFeatures() {
        return activateBirthdayFeaturesProperty().getValue();
    }

    /**
     * Returns the property holding whether any member has associated honorings.
     *
     * @return The property holding whether any member has associated honorings.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated
    public ReadOnlyBooleanProperty honoringsAvailableProperty() {
        return honoringsAvailable;
    }

    /**
     * Checks whether any member has associated honorings.
     *
     * @return {@code true} only if there is at least one member which has associated honorings.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated(forRemoval = false, since = "2u14")
    public boolean isHonoringsAvailable() {
        return honoringsAvailableProperty().get();
    }

    /**
     * Represents a property holding a {@link CompletableFuture}. It extends {@link SimpleObjectProperty} with the
     * property of availability of the wrapped {@link CompletableFuture}.
     *
     * @param <T> The type of the result of the wrapped {@link CompletableFuture}.
     */
    private static class CompletableFutureProperty<T> extends SimpleObjectProperty<CompletableFuture<T>> {

        private static final Logger LOGGER = Logger.getLogger(CompletableFutureProperty.class.getName());
        private final BooleanProperty available = new SimpleBooleanProperty();

        /**
         * Creates a {@link CompletableFutureProperty} containing {@code null}.
         */
        CompletableFutureProperty() {
            this(null);
        }

        /**
         * Creates a {@link CompletableFutureProperty} containing the given value.
         *
         * @param initialValue The initial value.
         */
        CompletableFutureProperty(CompletableFuture<T> initialValue) {
            super(initialValue);
        }

        private void updateAvailableProperty(CompletableFuture<T> newValue) {
            available.set(false);
            CompletableFuture.runAsync(() -> {
                try {
                    newValue.get();
                } catch (InterruptedException | ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            })
                    .thenRunAsync(() -> available.set(true));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(CompletableFuture<T> newValue) {
            updateAvailableProperty(newValue);
            super.set(newValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(CompletableFuture<T> value) {
            updateAvailableProperty(value);
            super.setValue(value);
        }

        /**
         * This method does the same as {@link SimpleObjectProperty#get()} but throws a {@link IllegalStateException}
         * when trying to get its value but there is no requestable value (means it is {@code null}.
         *
         * @return The value hold by this property.
         * @see SimpleObjectProperty#get()
         */
        @Override
        public CompletableFuture<T> get() {
            CompletableFuture<T> value = super.get();
            if (value == null) {
                throw new IllegalStateException("There is no data queried yet.\n"
                        + "You have to set a connection first before being able to operate on that data.");
            }
            return value;
        }

        /**
         * Returns the property holding whether the data of the contained {@link Future} object is available.
         *
         * @return The property holding whether the data of the contained {@link Future} object is available.
         */
        public ReadOnlyBooleanProperty availableProperty() {
            return available;
        }

        /**
         * Checks whether the data of the contained {@link Future} object is available.
         *
         * @return {@code true} only if the data of the contained {@link Future} object is available.
         */
        public boolean isAvailable() {
            return available.get();
        }
    }
}

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
import bayern.steinbrecher.green2.connection.scheme.Columns;
import bayern.steinbrecher.green2.connection.scheme.Tables;
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
import bayern.steinbrecher.green2.query.QueryResult;
import bayern.steinbrecher.green2.selection.Selection;
import bayern.steinbrecher.green2.selection.SelectionGroup;
import bayern.steinbrecher.green2.sepaform.SepaForm;
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
import java.util.StringJoiner;
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
public class MenuController extends Controller {

    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private final List<Callable<String>> checkFunctions = Arrays.asList(
            () -> checkIbans(),
            () -> checkBics(),
            () -> checkDates(m -> m.getPerson().getBirthday(),
                    EnvironmentHandler.getResourceValue("memberBadBirthday"),
                    EnvironmentHandler.getResourceValue("allBirthdaysCorrect")),
            () -> checkDates(m -> m.getAccountHolder().getMandateSigned(),
                    EnvironmentHandler.getResourceValue("memberBadMandatSigned"),
                    EnvironmentHandler.getResourceValue("allMandatSignedCorrect")),
            () -> checkContributions());
    private DBConnection dbConnection = null;
    private ObjectProperty<Optional<LocalDateTime>> dataLastUpdated = new SimpleObjectProperty<>(Optional.empty());
    private final Map<Integer, CompletableFuture<List<Member>>> memberBirthday = new HashMap<>(3) {
        /**
         * Returns the value hold at key {@code key}. In contrast to {@link HashMap#get(java.lang.Object)} this method
         * never returns {@code null}. When the searched element is not found the appropriate data is generated, put and
         * returned.
         *
         * @param key The key to search for.
         * @return The {@link CompletableFuture} put for the given key.
         * @see HashMap#get(java.lang.Object)
         */
        @Override
        @SuppressWarnings("element-type-mismatch")
        public CompletableFuture<List<Member>> get(Object key) {
            if (super.containsKey(key)) {
                return super.get(key);
            } else if (key instanceof Integer) {
                int year = (Integer) key;
                putIfAbsent(year, CompletableFuture.supplyAsync(() -> {
                    try {
                        return getBirthdayMember(year);
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    }
                }));
                return super.get(key);
            } else {
                throw new IllegalStateException("There is no entry for the given key " + key
                        + " and no entry can be generated due it is no Integer.");
            }
        }
    };
    private final CompletableFutureProperty<Set<Member>> member = new CompletableFutureProperty<>();
    private final CompletableFutureProperty<Set<Member>> memberNonContributionfree = new CompletableFutureProperty<>();
    private final CompletableFutureProperty<Map<String, String>> nicknames = new CompletableFutureProperty<>();
    private BooleanProperty allDataAvailable = new SimpleBooleanProperty(this, "allDataAvailable");
    private final BooleanProperty activateBirthdayFeatures
            = new SimpleBooleanProperty(this, "activateBirthdayFeatures", true);

    @FXML
    private MenuItem generateAddressesBirthday;
    @FXML
    private MenuItem generateBirthdayInfos;
    @FXML
    private CheckedIntegerSpinner yearSpinner;
    @FXML
    private CheckedIntegerSpinner yearSpinner2;
    @FXML
    private javafx.scene.control.Menu licensesMenu;
    @FXML
    private Label dataLastUpdatedLabel;
    @FXML
    private Rectangle overlayBackground;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Bind year spinner
        StringBinding yearBinding = Bindings.createStringBinding(
                () -> yearSpinner.isValid() ? yearSpinner.getValue().toString() : "?",
                yearSpinner.validProperty(), yearSpinner.valueProperty());
        generateBirthdayInfos.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("groupedBirthdayMember") + " ")
                        .concat(yearBinding));
        generateAddressesBirthday.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("birthdayExpression") + " ")
                        .concat(yearBinding));
        yearSpinner2.valueFactoryProperty().bind(yearSpinner.valueFactoryProperty());
        yearSpinner.getValueFactory().setValue(CURRENT_YEAR + 1);

        //Bind availability informations
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

        //Bind activateBirthdayFeatures
        activateBirthdayFeatures.bind(Bindings.createBooleanBinding(
                () -> EnvironmentHandler.getProfile().get(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES),
                EnvironmentHandler.loadedProfileProperty(),
                EnvironmentHandler.getProfile().getProperty(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES)));

        //Load licenses
        EnvironmentHandler.getLicenses().stream().forEach(license -> {
            MenuItem item = new MenuItem(license.getName());
            item.setOnAction(aevt -> {
                try {
                    license.setWritable(false, false);
                    Desktop.getDesktop().open(license);
                } catch (IOException ex) {
                    Logger.getLogger(MenuController.class.getName()).log(Level.WARNING, "Could not open license", ex);
                }
            });
            licensesMenu.getItems().add(item);
        });
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
    public void setConnection(DBConnection dbConnection) {
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
        DialogUtility.showAndWait(DialogUtility.createInfoAlert(stage, noMemberForOutput, noMemberForOutput));
    }

    private void generateAddresses(Collection<Member> member, File outputFile)
            throws IOException, InterruptedException, ExecutionException {
        if (member.isEmpty()) {
            throw new IllegalArgumentException("Passed empty list to generateAddresses(...)");
        }
        IOStreamUtility.printContent(
                AddressGenerator.generateAddressData(member, nicknames.get().get()), outputFile, true);
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
                Optional<File> path = EnvironmentHandler.askForSavePath(
                        stage, LocalDate.now().toString() + "_Serienbrief_alle", "csv");
                if (path.isPresent()) {
                    generateAddresses(memberList, path.get());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, "Could not generate addresses.", ex);
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
                Optional<File> path = EnvironmentHandler.askForSavePath(
                        stage, LocalDate.now().toString() + "_Serienbrief_Geburtstag_" + year, "csv");
                if (path.isPresent()) {
                    generateAddresses(memberBirthdayList, path.get());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, "Could not generate addresses.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void generateSepa(Future<Set<Member>> memberToSelectFuture, boolean useMemberContributions,
            SequenceType sequenceType) {
        try {
            Set<Member> memberToSelect = memberToSelectFuture.get();

            if (memberToSelect.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                boolean askForContribution = !(useMemberContributions
                        && dbConnection.columnExists(Tables.MEMBER, Columns.CONTRIBUTION));

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
                wizardStage.initOwner(stage);
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
                            BiMap<Double, Color> contribution
                                    = ((Optional<BiMap<Double, Color>>) results.get("contribution")).get();
                            Map<Member, Double> groupedMember
                                    = ((Optional<Map<Member, Double>>) results.get("selectionGroup")).get();
                            selectedMember = groupedMember.entrySet().stream()
                                    .map(entry -> {
                                        Member m = entry.getKey();
                                        m.setContribution(entry.getValue());
                                        return m;
                                    })
                                    .collect(Collectors.toSet());
                        } else {
                            selectedMember = ((Optional<Set<Member>>) results.get("selection")).get();
                        }
                        Originator originator = ((Optional<Originator>) results.get(WizardPage.FIRST_PAGE_KEY)).get();

                        EnvironmentHandler.askForSavePath(stage, LocalDate.now().toString() + "_Sepa", "xml")
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
                                            Alert alert = DialogUtility.createErrorAlert(stage, message + "\n"
                                                    + EnvironmentHandler.getResourceValue("haveBadAccountInformation"));
                                            Platform.runLater(() -> alert.show());
                                        }
                                    } catch (IOException ex) {
                                        Logger.getLogger(MenuController.class.getName())
                                                .log(Level.SEVERE, "The sepa xml file could not be created.", ex);
                                    }
                                });
                    }
                });
                wizardStage.getScene().getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
                wizardStage.showAndWait();
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
            String noSepaDebit = EnvironmentHandler.getResourceValue("noSepaDebit");
            DialogUtility.showAndWait(DialogUtility.createErrorAlert(stage, noSepaDebit, noSepaDebit));
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
                    Logger.getLogger(MenuController.class.getName())
                            .log(Level.WARNING, "Cannot disable control. DisableProperty is bound.");
                    run.run();
                } else {
                    Method setDisableMethod = sourceClass.getMethod("setDisable", Boolean.TYPE);
                    setDisableMethod.invoke(sourceObj, true);
                    run.run();
                    setDisableMethod.invoke(sourceObj, false);
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(MenuController.class.getName()).log(
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
                        Logger.getLogger(MenuController.class.getName())
                                .log(Level.SEVERE, "Retrieving the data failed.", throwable);
                        datetime = null;
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
    private void openQueryDialog(ActionEvent aevt) {
        callOnDisabled(aevt, () -> {
            try {
                Map<String, WizardPage<?>> pages = new HashMap<>();
                WizardPage<Optional<List<List<String>>>> queryDialogPage = new Query(dbConnection).getWizardPage();
                pages.put(WizardPage.FIRST_PAGE_KEY, queryDialogPage);
                Wizard queryWizard = new Wizard(pages);
                queryDialogPage.setNextFunction(() -> {
                    WizardPage<Optional<Void>> queryResultPage
                            = new QueryResult(queryDialogPage.getResultFunction().call().orElse(new ArrayList<>()))
                                    .getWizardPage();
                    queryResultPage.setFinish(true);
                    queryWizard.put("queryResult", queryResultPage);
                    return "queryResult";
                });
                Stage wizardStage = new Stage();
                wizardStage.initOwner(stage);
                wizardStage.setTitle(EnvironmentHandler.getResourceValue("queryMemberTitle"));
                wizardStage.setResizable(true);
                wizardStage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
                queryWizard.start(wizardStage);
                wizardStage.getScene().getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
                wizardStage.showAndWait();
            } catch (IOException ex) {
                Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
                DialogUtility.createStacktraceAlert(stage, ex, EnvironmentHandler.getResourceValue("noQueryDialog"))
                        .showAndWait();
            }
        });
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void generateContributionSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(memberNonContributionfree.get(), true, SequenceType.RCUR));
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void generateUniversalSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(member.get(), false, SequenceType.RCUR));
    }

    private String checkIbans() {
        List<Member> badIban = new ArrayList<>();
        try {
            badIban = member.get().get().parallelStream()
                    .filter(m -> !SepaUtility.isValidIban(m.getAccountHolder().getIban()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (badIban.isEmpty()) {
            return EnvironmentHandler.getResourceValue("correctIbans");
        } else {
            String noIban = EnvironmentHandler.getResourceValue("noIban");
            String message = badIban.stream()
                    .map(m -> {
                        String iban = m.getAccountHolder().getIban();
                        return m + ": \"" + (iban.isEmpty() ? noIban : iban) + "\"";
                    })
                    .collect(Collectors.joining("\n"));
            return EnvironmentHandler.getResourceValue("memberBadIban") + "\n" + message;
        }
    }

    private String checkBics() {
        List<Member> badBic = new ArrayList<>();
        try {
            badBic = member.get().get().parallelStream()
                    .filter(m -> !SepaUtility.isValidBic(m.getAccountHolder().getBic()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (badBic.isEmpty()) {
            return EnvironmentHandler.getResourceValue("correctBics");
        } else {
            String noBic = EnvironmentHandler.getResourceValue("noBic");
            String message = badBic.stream()
                    .map(m -> {
                        String bic = m.getAccountHolder().getBic();
                        return m + ": \"" + (bic.isEmpty() ? noBic : bic) + "\"";
                    })
                    .collect(Collectors.joining("\n"));
            return EnvironmentHandler.getResourceValue("memberBadBic") + "\n" + message;
        }
    }

    private String checkDates(Function<Member, LocalDate> dateFunction, String invalidDatesIntro,
            String allCorrectMessage) {
        try {
            String message = member.get().get().parallelStream()
                    .filter(m -> dateFunction.apply(m) == null)
                    .map(m -> m.toString() + ": \"" + dateFunction.apply(m) + "\"")
                    .collect(Collectors.joining("\n"));
            return message.isEmpty() ? allCorrectMessage
                    : invalidDatesIntro + "\n" + message;
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private String checkContributions() {
        List<Member> contributionDefined = new ArrayList<>();
        try {
            contributionDefined = member.get().get().parallelStream()
                    .filter(m -> m.getContribution().isPresent())
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (contributionDefined.isEmpty()) {
            return EnvironmentHandler.getResourceValue("skipCheckingContribution");
        } else {
            String message = contributionDefined.parallelStream()
                    .filter(m -> m.getContribution().get() < 0)
                    .map(m -> m.toString() + ": " + m.getContribution())
                    .collect(Collectors.joining("\n"));
            message += contributionDefined.parallelStream()
                    .filter(m -> !m.isContributionfree())
                    .filter(m -> m.getContribution().get() == 0)
                    .map(m -> EnvironmentHandler.getResourceValue("zeroContribution", m.toString()))
                    .collect(Collectors.joining("\n"));
            if (message.isEmpty()) {
                return EnvironmentHandler.getResourceValue("correctContributions");
            } else {
                return EnvironmentHandler.getResourceValue("memberBadContributions") + "\n" + message;
            }
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void checkData(ActionEvent aevt) {
        callOnDisabled(aevt, () -> {
            StringJoiner messageJoiner = new StringJoiner("\n\n");
            checkFunctions.forEach(cf -> {
                try {
                    String message = cf.call();
                    if (!message.isEmpty()) {
                        messageJoiner.add(message);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            String checkData = EnvironmentHandler.getResourceValue("checkData");
            DialogUtility.showAndWait(
                    DialogUtility.createMessageAlert(stage, messageJoiner.toString(), checkData, checkData));
        });
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void generateAddressesAll(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateAddressesAll());
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void generateAddressesBirthday(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> generateAddressesBirthday(yearSpinner.getValue()));
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void generateBirthdayInfos(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> {
                Integer year = yearSpinner.getValue();
                try {
                    List<Member> birthdayList = memberBirthday.get(year).get();
                    if (birthdayList.isEmpty()) {
                        showNoMemberForOutputDialog();
                    } else {
                        Optional<File> path = EnvironmentHandler.askForSavePath(
                                stage, LocalDate.now().toString() + "_Geburtstag_" + year, "csv");
                        if (path.isPresent()) {
                            IOStreamUtility.printContent(
                                    BirthdayGenerator.createGroupedOutput(birthdayList, year), path.get(), true);
                        }
                    }
                } catch (InterruptedException | ExecutionException | IOException ex) {
                    Logger.getLogger(MenuController.class.getName())
                            .log(Level.SEVERE, "Could not generate birthday infos.", ex);
                }
            });
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void showCredits() {
        String credits = EnvironmentHandler.getResourceValue("credits");
        Alert alert = DialogUtility.createMessageAlert(
                stage, EnvironmentHandler.getResourceValue("creditsContent"), null, credits, credits);
        Platform.runLater(() -> alert.show());
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void showVersion() {
        String version = EnvironmentHandler.getResourceValue("version");
        Alert alert = DialogUtility.createInfoAlert(stage, EnvironmentHandler.VERSION, version, version, version);
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

    private static class CompletableFutureProperty<T> extends SimpleObjectProperty<CompletableFuture<T>> {

        private final BooleanProperty available = new SimpleBooleanProperty();

        /**
         * Creates a {@link CompletableFutureProperty} containing {@code null}.
         */
        public CompletableFutureProperty() {
            this(null);
        }

        /**
         * Creates a {@link CompletableFutureProperty} containing the given value.
         *
         * @param initialValue The initial value.
         */
        public CompletableFutureProperty(CompletableFuture<T> initialValue) {
            super(initialValue);
        }

        private void updateAvailableProperty(CompletableFuture<T> newValue) {
            available.set(false);
            CompletableFuture.runAsync(
                    () -> {
                        try {
                            newValue.get();
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
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
        public void setValue(CompletableFuture<T> v) {
            updateAvailableProperty(v);
            super.setValue(v);
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

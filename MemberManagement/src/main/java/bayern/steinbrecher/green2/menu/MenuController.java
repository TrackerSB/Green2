/* 
 * Copyright (C) 2017 Stefan Huber
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
import bayern.steinbrecher.green2.data.ConfigKey;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.green2.generator.AddressGenerator;
import bayern.steinbrecher.green2.generator.BirthdayGenerator;
import bayern.steinbrecher.green2.generator.sepa.SepaPain00800302XMLGenerator;
import bayern.steinbrecher.green2.generator.sepa.SequenceType;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Originator;
import bayern.steinbrecher.green2.selection.Selection;
import bayern.steinbrecher.green2.sepaform.SepaForm;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.SepaUtility;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
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
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Controller for Menu.fxml.
 *
 * @author Stefan Huber
 */
public class MenuController extends Controller {

    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private DBConnection dbConnection = null;
    private ObjectProperty<Optional<LocalDateTime>> dataLastUpdated = new SimpleObjectProperty<>(Optional.empty());
    //private MemberManagement caller;
    private final Map<Integer, Future<List<Member>>> memberBirthday = new HashMap<>(3);
    private Future<List<Member>> memberNonContributionfree;
    private Future<Map<String, String>> nicknames;
    private final ExecutorService exserv = Executors.newWorkStealingPool();
    private Future<List<Member>> member;

    @FXML
    private MenuItem generateAddressesBirthday;
    @FXML
    private MenuItem generateBirthdayInfos;
    @FXML
    private CheckedIntegerSpinner yearSpinner;
    @FXML
    private javafx.scene.control.Menu licensesMenu;
    @FXML
    private Label dataLastUpdatedLabel;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        StringBinding yearBinding = Bindings.createStringBinding(
                () -> yearSpinner.isValid() ? yearSpinner.getValue().toString() : "?",
                yearSpinner.validProperty(), yearSpinner.valueProperty());
        generateBirthdayInfos.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("groupedBirthdayMember") + " ")
                        .concat(yearBinding));
        generateAddressesBirthday.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("birthdayExpression") + " ")
                        .concat(yearBinding));
        yearSpinner.getValueFactory().setValue(CURRENT_YEAR + 1);

        StringBinding dataLastUpdatedBinding = Bindings.createStringBinding(() -> {
            Optional<LocalDateTime> dataLastUpdatedOptional = getDataLastUpdated();
            String text;
            if (dataLastUpdatedOptional.isPresent()) {
                text = EnvironmentHandler.getResourceValue(
                        "dataLastUpdated", dataLastUpdatedOptional.get()
                                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
            } else {
                text = EnvironmentHandler.getResourceValue("noData");
            }
            return text;
        }, dataLastUpdatedProperty());
        dataLastUpdatedLabel.textProperty().bind(dataLastUpdatedBinding);

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
     * Sets the connection to use for querying data and queries immediatly for new data using the given connection.
     *
     * @param dbConnection The connection to use for querying data.
     */
    public void setConnection(DBConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("The connection must not be null.");
        }
        this.dbConnection = dbConnection;
        executeQueries();
    }

    /**
     * Checks whether all objects are not {@code null}. If any is {@code null} it throws a {@link IllegalStateException}
     * saying that the caller has to call {@link Application#start(Stage)} first.
     *
     * @param obj The objects to test.
     */
    private void checkQueriesInited(Object... obj) {
        if (Arrays.stream(obj).anyMatch(Objects::isNull)) {
            throw new IllegalStateException(
                    "You have to set a connection first before being able to operate on that data.");
        }
    }

    private List<Member> getBirthdayMember(int year)
            throws InterruptedException, ExecutionException {
        return member.get()
                .parallelStream()
                .filter(m -> BirthdayGenerator.getsNotified(m, year))
                .collect(Collectors.toList());
    }

    private void showNoMemberForOutputDialog() {
        String noMemberForOutput = EnvironmentHandler.getResourceValue("noMemberForOutput");
        Alert alert = DialogUtility.createInfoAlert(stage, noMemberForOutput, noMemberForOutput);
        alert.showAndWait();
    }

    private void generateAddresses(List<Member> member, File outputFile) {
        checkQueriesInited(nicknames);
        if (member.isEmpty()) {
            throw new IllegalArgumentException("Passed empty list to generateAddresses(...)");
        }
        try {
            IOStreamUtility.printContent(
                    AddressGenerator.generateAddressData(member, nicknames.get()), outputFile, true);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates a file Serienbrief_alle.csv containing addresses of all member.
     */
    public void generateAddressesAll() {
        checkQueriesInited(member);
        try {
            List<Member> memberList = this.member.get();
            if (memberList.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                EnvironmentHandler.askForSavePath(stage, "Serienbrief_alle", "csv").ifPresent(file -> {
                    generateAddresses(memberList, file);
                });
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates a file Serienbrief_Geburtstag_{@code year}.csv containing addresses of all member who get a birthday
     * notification in year {@code year}.
     *
     * @param year The year to look for member.
     */
    public void generateAddressesBirthday(int year) {
        checkQueriesInited(memberBirthday);
        memberBirthday.putIfAbsent(year, exserv.submit(() -> getBirthdayMember(year)));
        try {
            List<Member> memberBirthdayList = memberBirthday.get(year).get();
            if (memberBirthdayList.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                EnvironmentHandler.askForSavePath(stage, "Serienbrief_Geburtstag_" + year, "csv")
                        .ifPresent(file -> generateAddresses(memberBirthdayList, file));
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void generateSepa(Future<List<Member>> memberToSelectFuture, boolean useMemberContributions,
            SequenceType sequenceType) {
        checkQueriesInited(memberToSelectFuture);
        try {
            List<Member> memberToSelect = memberToSelectFuture.get();

            if (memberToSelect.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                boolean askForContribution = !(useMemberContributions
                        && dbConnection.columnExists(DBConnection.Tables.MEMBER, DBConnection.Columns.CONTRIBUTION));

                WizardPage<Optional<Originator>> sepaFormPage = new SepaForm().getWizardPage();
                sepaFormPage.setNextFunction(() -> "selection");
                WizardPage<Optional<List<Member>>> selectionPage
                        = new Selection<>(memberToSelect).getWizardPage();
                selectionPage.setFinish(!askForContribution);
                if (askForContribution) {
                    selectionPage.setNextFunction(() -> "contribution");
                }
                WizardPage<Optional<Double>> contributionPage = new Contribution().getWizardPage();
                contributionPage.setFinish(true);

                Map<String, WizardPage<?>> pages = new HashMap<>();
                pages.put(WizardPage.FIRST_PAGE_KEY, sepaFormPage);
                pages.put("selection", selectionPage);
                pages.put("contribution", contributionPage);
                Stage wizardStage = new Stage();
                wizardStage.initOwner(stage);
                wizardStage.setTitle(EnvironmentHandler.getResourceValue("generateSepa"));
                wizardStage.setResizable(false);
                wizardStage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
                Wizard wizard = new Wizard(pages);
                wizard.start(wizardStage);
                wizardStage.getScene().getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
                wizardStage.show();
                wizard.finishedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        Map<String, ?> results = wizard.getResults().get();
                        List<Member> selectedMember = ((Optional<List<Member>>) results.get("selection")).get();
                        if (askForContribution) {
                            double contribution = ((Optional<Double>) results.get("contribution")).get();
                            selectedMember.stream().forEach(m -> m.setContribution(contribution));
                        }
                        Originator originator = ((Optional<Originator>) results.get(WizardPage.FIRST_PAGE_KEY)).get();

                        EnvironmentHandler.askForSavePath(stage, "Sepa", "xml").ifPresent(file -> {
                            List<Member> invalidMember
                                    = SepaPain00800302XMLGenerator.createXMLFile(selectedMember, originator,
                                            sequenceType, file,
                                            EnvironmentHandler.getProfile().getOrDefault(ConfigKey.SEPA_USE_BOM, true));
                            String message = invalidMember.stream()
                                    .map(Member::toString)
                                    .collect(Collectors.joining("\n"));
                            if (!message.isEmpty()) {
                                Alert alert = DialogUtility.createErrorAlert(stage, message + "\n"
                                        + EnvironmentHandler.getResourceValue("haveBadAccountInformation"));
                                alert.show();
                            }
                        });
                    }
                });
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
            String noSepaDebit = EnvironmentHandler.getResourceValue("noSepaDebit");
            Alert alert = DialogUtility.createErrorAlert(stage, noSepaDebit, noSepaDebit);
            alert.showAndWait();
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
                    setDisableMethod.invoke(sourceObj, true);
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
    private void executeQueries() {
        member = exserv.submit(() -> dbConnection.getAllMember());
        exserv.submit(() -> {
            try {
                member.get(); //Wait for data to be received.
                Platform.runLater(() -> dataLastUpdated.setValue(Optional.of(LocalDateTime.now())));
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        int currentYear = LocalDate.now().getYear();
        IntStream.rangeClosed(currentYear - 1, currentYear + 1)
                .forEach(y -> memberBirthday.put(y, exserv.submit(() -> getBirthdayMember(y))));
        memberNonContributionfree = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(m -> !m.isContributionfree())
                .collect(Collectors.toList()));
        nicknames = exserv.submit(() -> dbConnection.getAllNicknames());
    }

    @FXML
    private void generateContributionSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(memberNonContributionfree, true, SequenceType.RCUR));
    }

    @FXML
    private void generateUniversalSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(member, false, SequenceType.RCUR));
    }

    private String checkIbans() {
        checkQueriesInited(member);
        List<Member> badIban = new ArrayList<>();
        try {
            badIban = member.get().parallelStream()
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

    private String checkDates(Function<Member, LocalDate> dateFunction, String invalidDatesIntro,
            String allCorrectMessage) {
        try {
            String message = member.get().parallelStream()
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

    @FXML
    private void checkData(ActionEvent aevt) {
        callOnDisabled(aevt, () -> {
            checkQueriesInited(member);
            String message = checkIbans() + "\n\n"
                    + checkDates(m -> m.getPerson().getBirthday(),
                            EnvironmentHandler.getResourceValue("memberBadBirthday"),
                            EnvironmentHandler.getResourceValue("allBirthdaysCorrect"))
                    + "\n\n"
                    + checkDates(m -> m.getAccountHolder().getMandateSigned(),
                            EnvironmentHandler.getResourceValue("memberBadMandatSigned"),
                            EnvironmentHandler.getResourceValue("allMandatSignedCorrect"));
            String checkData = EnvironmentHandler.getResourceValue("checkData");
            Alert alert = DialogUtility.createMessageAlert(stage, message, checkData, checkData);
            alert.showAndWait();
        });
    }

    @FXML
    private void generateAddressesAll(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateAddressesAll());
    }

    @FXML
    private void generateAddressesBirthday(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> generateAddressesBirthday(yearSpinner.getValue()));
        }
    }

    @FXML
    private void generateBirthdayInfos(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> {
                checkQueriesInited(memberBirthday);
                Integer year = yearSpinner.getValue();
                memberBirthday.putIfAbsent(year, exserv.submit(() -> getBirthdayMember(year)));
                try {
                    List<Member> birthdayList = memberBirthday.get(year).get();
                    if (birthdayList.isEmpty()) {
                        showNoMemberForOutputDialog();
                    } else {
                        EnvironmentHandler.askForSavePath(stage, "/Geburtstag_" + year, "csv").ifPresent(file -> {
                            IOStreamUtility.printContent(
                                    BirthdayGenerator.createGroupedOutput(birthdayList, year), file, true);
                        });
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    @FXML
    private void showCredits() {
        String credits = EnvironmentHandler.getResourceValue("credits");
        Alert alert = DialogUtility.createMessageAlert(
                stage, EnvironmentHandler.getResourceValue("creditsContent"), null, credits, credits);
        alert.show();
    }

    @FXML
    private void showVersion() {
        String version = EnvironmentHandler.getResourceValue("version");
        Alert alert = DialogUtility.createInfoAlert(stage, EnvironmentHandler.VERSION, version, version, version);
        alert.show();
    }

    /**
     * Shuts the controller down now.
     */
    void shutdownNow() {
        exserv.shutdownNow();
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
}

/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.main;

import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.connection.DefaultConnection;
import bayern.steinbrecher.green2.connection.SshConnection;
import bayern.steinbrecher.green2.contribution.Contribution;
import bayern.steinbrecher.green2.data.ConfigKey;
import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.ProfileChoice;
import bayern.steinbrecher.green2.elements.Splashscreen;
import bayern.steinbrecher.green2.elements.WaitScreen;
import bayern.steinbrecher.green2.exception.AuthException;
import bayern.steinbrecher.green2.exception.SchemeCreationException;
import bayern.steinbrecher.green2.generator.AddressGenerator;
import bayern.steinbrecher.green2.generator.BirthdayGenerator;
import bayern.steinbrecher.green2.generator.sepa.SepaPain00800302XMLGenerator;
import bayern.steinbrecher.green2.generator.sepa.SequenceType;
import bayern.steinbrecher.green2.login.Login;
import bayern.steinbrecher.green2.login.LoginKey;
import bayern.steinbrecher.green2.login.ssh.SshLogin;
import bayern.steinbrecher.green2.login.standard.DefaultLogin;
import bayern.steinbrecher.green2.menu.Menu;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Originator;
import bayern.steinbrecher.green2.selection.Selection;
import bayern.steinbrecher.green2.sepaform.SepaForm;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.ProgramCaller;
import bayern.steinbrecher.green2.utility.ServiceFactory;
import bayern.steinbrecher.green2.utility.ThreadUtility;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the entry of the hole application.
 *
 * @author Stefan Huber
 */
public class Main extends Application {

    private static final long SPLASHSCREEN_MILLIS = 2500;
    private Profile profile;
    private Stage menuStage;
    private final ExecutorService exserv = Executors.newWorkStealingPool();
    private Future<List<Member>> member;
    private final Map<Integer, Future<List<Member>>> memberBirthday = new HashMap<>(3);
    private Future<List<Member>> memberNonContributionfree;
    private Future<Map<String, String>> nicknames;
    private Future<Optional<Map<Integer, Double>>> individualContributions;
    private DBConnection dbConnection = null;

    /**
     * Default constructor.
     */
    public Main() {
        List<String> availableProfiles = Profile.getAvailableProfiles();
        if (availableProfiles.size() < 1) {
            ProgramCaller.startGreen2ConfigDialog();
        } else if (availableProfiles.size() == 1) {
            profile = DataProvider.loadProfile(availableProfiles.get(0), false);
        } else {
            Optional<Profile> requestedProfile = ProfileChoice.askForProfile(false);
            if (requestedProfile.isPresent()) {
                profile = DataProvider.loadProfile(requestedProfile.get());
            } else {
                Platform.exit();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        menuStage = primaryStage;
        Platform.setImplicitExit(false);
        /*
         * boolean value needed to make sure no other windows shows up because
         * {@code Platform.exit()} is async.
         */
        boolean valid = checkConfigs();
        if (valid) {
            Splashscreen.showSplashscreen(SPLASHSCREEN_MILLIS, new Stage());
            Login login = createLogin();
            WaitScreen waitScreen = createWaitScreen(login);
            createConnectionService(login, waitScreen).start();
        }
    }

    private boolean checkConfigs() {
        boolean valid = profile.isAllConfigurationsSet();
        if (!valid) {
            DialogUtility.createErrorAlert(
                    MessageFormat.format(DataProvider.getResourceValue("badConfigs"), profile.getProfileName()))
                    .showAndWait()
                    .ifPresent(buttontype -> {
                        if (buttontype == ButtonType.OK) {
                            ProgramCaller.startGreen2ConfigDialog();
                        }
                    });
        }
        return valid;
    }

    private Login createLogin() {
        Login login;
        if (profile.getOrDefault(ConfigKey.USE_SSH, true)) {
            login = new SshLogin();
        } else {
            login = new DefaultLogin();
        }
        return login;
    }

    /*
     * This methode creates a {@code WaitScreen}, connects {@code login} to it
     * AND calls {@code start(...)} of login.
     */
    private WaitScreen createWaitScreen(Login login) throws IOException {
        WaitScreen waitScreen = new WaitScreen();
        waitScreen.start(new Stage());
        Stage loginStage = new Stage();
        loginStage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                waitScreen.close();
            } else if (!login.userAbborted()) {
                waitScreen.show();
            } else {
                Platform.exit();
            }
        });
        login.start(loginStage);
        return waitScreen;
    }

    private Service<Optional<DBConnection>> createConnectionService(Login login, WaitScreen waitScreen) {
        Service<Optional<DBConnection>> connectionService = ServiceFactory.createService(() -> {
            return getConnection(login, waitScreen);
        });

        connectionService.setOnSucceeded(wse -> {
            Optional<DBConnection> optDBConnection = connectionService.getValue();
            if (optDBConnection.isPresent()) {
                dbConnection = optDBConnection.get();
                try {
                    dbConnection.createTablesIfNeeded();
                } catch (SchemeCreationException ex) {
                    DialogUtility.createErrorAlert(DataProvider.getResourceValue("couldntCreateScheme")).showAndWait();
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    Platform.exit();
                }

                executeQueries();

                menuStage.showingProperty().addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal) {
                                waitScreen.close();
                            } else {
                                Platform.exit();
                            }
                        });

                try {
                    new Menu(this).start(menuStage);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return connectionService;
    }

    /**
     * This method is called when the application should stop, destroys
     * resources and prepares for application exit.
     */
    @Override
    public void stop() {
        if (dbConnection != null) {
            dbConnection.close();
        }
        exserv.shutdownNow();
    }

    private void handleAuthException(Login login, WaitScreen waitScreen, Exception cause) {
        Platform.runLater(() -> {
            waitScreen.close();

            Stage s = new Stage();
            Alert dialog;
            if (cause instanceof ConnectException) {
                dialog = DialogUtility.createInfoAlert(DataProvider.getResourceValue("checkConnection"));
            } else if (cause instanceof UnknownHostException) {
                dialog = DialogUtility.createStacktraceAlert(cause, DataProvider.getResourceValue("checkConnection"));
            } else if (cause instanceof AuthException) {
                dialog = DialogUtility.createInfoAlert(DataProvider.getResourceValue("checkInput"));
            } else {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Not action specified for: {0}", cause);
                dialog = DialogUtility.createErrorAlert(DataProvider.getResourceValue("unexpectedAbbort"));
            }

            dialog.showingProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    if (dialog.getResult() == ButtonType.OK) {
                        login.reset();
                        synchronized (this) {
                            notifyAll();
                        }
                    } else {
                        Platform.exit();
                    }
                }
            });
            dialog.show();
        });
    }

    /**
     * Asks the user for the needed logindata as long as the inserted data is
     * not correct or the user abborts. This method should NOT be called by
     * JavaFX Application Thread. E.g. use
     * {@code ServiceFactory.createService(...)}.
     *
     * @param login      The loginframe used to ask the user.
     * @param waitScreen The waitscreen to show when trying to connect to the
     *                   server.
     * @return {@code Optional.empty()} only if the connection could not be
     * established. E.g. the user closed the window or the configured connection
     * is not reachable.
     */
    private Optional<DBConnection> getConnection(Login login, WaitScreen waitScreen) {
        DBConnection con = null;

        Optional<Map<LoginKey, String>> loginInfos = login.getLoginInformation();
        if (loginInfos.isPresent()) {
            Map<LoginKey, String> loginValues = loginInfos.get();
            try {
                if (profile.getOrDefault(ConfigKey.USE_SSH, true)) {
                    con = new SshConnection(
                            profile.getOrDefault(ConfigKey.SSH_HOST, "localhost"),
                            loginValues.get(LoginKey.SSH_USERNAME),
                            loginValues.get(LoginKey.SSH_PASSWORD),
                            profile.getOrDefault(ConfigKey.DATABASE_HOST, "localhost"),
                            loginValues.get(LoginKey.DATABASE_USERNAME),
                            loginValues.get(LoginKey.DATABASE_PASSWORD),
                            profile.getOrDefault(ConfigKey.DATABASE_NAME, "dbname"),
                            profile.getOrDefault(ConfigKey.SSH_CHARSET, StandardCharsets.ISO_8859_1));
                } else {
                    con = new DefaultConnection(
                            profile.getOrDefault(ConfigKey.DATABASE_HOST, "localhost"),
                            loginValues.get(LoginKey.DATABASE_USERNAME),
                            loginValues.get(LoginKey.DATABASE_PASSWORD),
                            profile.getOrDefault(ConfigKey.DATABASE_NAME, "dbname"));
                }
            } catch (UnknownHostException | AuthException ex) {
                handleAuthException(login, waitScreen, ex);

                ThreadUtility.waitWhile(this, login.wouldShowBinding().not());

                return getConnection(login, waitScreen);
            }
        }

        return Optional.ofNullable(con);
    }

    private List<Member> getBirthdayMember(int year)
            throws InterruptedException, ExecutionException {
        return member.get()
                .parallelStream()
                .filter(m -> BirthdayGenerator.getsNotified(m, year))
                .collect(Collectors.toList());
    }

    private void executeQueries() {
        member = exserv.submit(() -> dbConnection.getAllMember());
        int currentYear = LocalDate.now().getYear();
        IntStream.rangeClosed(currentYear - 1, currentYear + 1)
                .forEach(y -> memberBirthday.put(y, exserv.submit(() -> getBirthdayMember(y))));
        memberNonContributionfree = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(m -> !m.isContributionfree())
                .collect(Collectors.toList()));
        nicknames = exserv.submit(() -> dbConnection.getAllNicknames());
        individualContributions = exserv.submit(() -> dbConnection.readIndividualContributions());
    }

    /**
     * Checks whether all objects are not {@code null}. If any is {@code null}
     * it throws a {@code IllegalStateException} saying that the caller has to
     * call {@code start(...)} first.
     *
     * @param obj The objects to test.
     */
    private void checkNull(Object... obj) {
        if (Arrays.stream(obj).anyMatch(Objects::isNull)) {
            throw new IllegalStateException("You have to call start(...) first");
        }
    }

    private void generateAddresses(List<Member> member, String filename) {
        checkNull(nicknames);
        if (member.isEmpty()) {
            Alert alert = DialogUtility.createInfoAlert(DataProvider.getResourceValue("noMemberForOutput"));
            alert.initOwner(menuStage);
            alert.showAndWait();
        } else {
            try {
                IOStreamUtility.printContent(
                        AddressGenerator.generateAddressData(member, nicknames.get()), filename, true);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Generates a file Serienbrief_alle.csv containing addresses of all member.
     *
     * @see DataProvider#SAVE_PATH
     */
    public void generateAddressesAll() {
        checkNull(member);
        try {
            generateAddresses(member.get(), DataProvider.SAVE_PATH + "/Serienbrief_alle.csv");
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates a file Serienbrief_Geburtstag_{@code year}.csv containing
     * addresses of all member who get a birthday notification in year
     * {@code year}.
     *
     * @param year The year to look for member.
     * @see DataProvider#SAVE_PATH
     */
    public void generateAddressesBirthday(int year) {
        checkNull(memberBirthday);
        memberBirthday.putIfAbsent(year, exserv.submit(() -> getBirthdayMember(year)));
        try {
            generateAddresses(memberBirthday.get(year).get(),
                    DataProvider.SAVE_PATH + "/Serienbrief_Geburtstag_" + year + ".csv");
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates a file Geburtstag_{@code year}.csv containing all member who
     * get a birthday notification in year {@code year}.
     *
     * @param year The year to look for member.
     * @see DataProvider#SAVE_PATH
     */
    public void generateBirthdayInfos(int year) {
        checkNull(memberBirthday);
        memberBirthday.putIfAbsent(
                year, exserv.submit(() -> getBirthdayMember(year)));
        try {
            List<Member> birthdayList = memberBirthday.get(year).get();
            if (birthdayList.isEmpty()) {
                Alert alert = DialogUtility.createInfoAlert(DataProvider.getResourceValue("noMemberForOutput"));
                alert.initOwner(menuStage);
                alert.showAndWait();
            } else {
                IOStreamUtility.printContent(BirthdayGenerator.createGroupedOutput(birthdayList, year),
                        DataProvider.SAVE_PATH + "/Geburtstag_" + year + ".csv", true);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void generateSepa(Future<List<Member>> memberToSelectFuture, boolean useMemberContributions,
                              SequenceType sequenceType) {
        checkNull(individualContributions, memberToSelectFuture);
        try {
            List<Member> memberToSelect = memberToSelectFuture.get();

            Map<Integer, Double> contributions = new HashMap<>();
            Optional<Map<Integer, Double>> optContributions = individualContributions.get();
            boolean askForContribution = !(useMemberContributions && optContributions.isPresent());
            if (useMemberContributions) {
                optContributions.ifPresent(contributions::putAll);
            }

            WizardPage<Optional<Originator>> sepaFormPage = new SepaForm(menuStage).getWizardPage();
            sepaFormPage.setNextFunction(() -> "selection");
            WizardPage<Optional<List<Member>>> selectionPage
                    = new Selection<>(memberToSelect, menuStage).getWizardPage();
            selectionPage.setFinish(!askForContribution);
            if (askForContribution) {
                selectionPage.setNextFunction(() -> "contribution");
            }
            WizardPage<Optional<Double>> contributionPage = new Contribution(menuStage).getWizardPage();
            contributionPage.setFinish(true);

            Map<String, WizardPage<?>> pages = new HashMap<>();
            pages.put(WizardPage.FIRST_PAGE_KEY, sepaFormPage);
            pages.put("selection", selectionPage);
            pages.put("contribution", contributionPage);
            Wizard wizard = new Wizard(pages);
            Stage wizardStage = new Stage();
            wizardStage.initOwner(menuStage);
            wizardStage.setTitle(DataProvider.getResourceValue("generateSepa"));
            wizardStage.setResizable(false);
            wizardStage.getIcons().add(DataProvider.ImageSet.LOGO.get());
            wizard.start(wizardStage);
            wizardStage.getScene().getStylesheets().add(DataProvider.STYLESHEET_PATH);
            wizard.finishedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Map<String, ?> results = wizard.getResults().get();
                    List<Member> selectedMember = ((Optional<List<Member>>) results.get("selection")).get();
                    if (askForContribution) {
                        double contribution = ((Optional<Double>) results.get("contribution")).get();
                        selectedMember.forEach(m -> contributions.put(m.getMembershipnumber(), contribution));
                    }
                    Originator originator = ((Optional<Originator>) results.get(WizardPage.FIRST_PAGE_KEY)).get();

                    List<Member> invalidMember
                            = SepaPain00800302XMLGenerator.createXMLFile(memberToSelect, contributions, originator,
                            sequenceType, DataProvider.SAVE_PATH + "/Sepa.xml",
                            profile.getOrDefault(ConfigKey.SEPA_USE_BOM, true));
                    String message = invalidMember.stream()
                            .map(Member::toString)
                            .collect(Collectors.joining("\n"));
                    if (!message.isEmpty()) {
                        Alert alert = DialogUtility.createErrorAlert(message + "\n"
                                + DataProvider.getResourceValue("haveBadAccountInformation"));
                        alert.initOwner(menuStage);
                        alert.show();
                    }
                }
            });
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = DialogUtility.createErrorAlert(DataProvider.getResourceValue("noSepaDebit"));
            alert.initOwner(menuStage);
            alert.showAndWait();
        }
    }

    /**
     * Asks for contribution and for member to debit from.
     */
    public void generateUniversalSepa() {
        generateSepa(member, false, SequenceType.FRST);
    }

    /**
     * Asks for contribution and for member (only non-contributionfree are
     * shown) to debit from.
     */
    public void generateContributionSepa() {
        generateSepa(memberNonContributionfree, true, SequenceType.RCUR);
    }

    private String checkIbans() {
        checkNull(member);
        List<Member> badIban = new ArrayList<>();
        try {
            badIban = member.get().parallelStream()
                    .filter(m -> !SepaPain00800302XMLGenerator.hasValidIban(m.getAccountHolder()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (badIban.isEmpty()) {
            return DataProvider.getResourceValue("correctIbans");
        } else {
            String noIban = DataProvider.getResourceValue("noIban");
            String message = badIban.stream()
                    .map(m -> {
                        String iban = m.getAccountHolder().getIban();
                        return m + ": \"" + (iban.isEmpty() ? noIban : iban) + "\"";
                    })
                    .collect(Collectors.joining("\n"));
            return DataProvider.getResourceValue("memberBadIban") + "\n" + message;
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
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    /**
     * Checks the correctness of all data and shows a dialog showing invalid
     * data entries.
     */
    public void checkData() {
        checkNull(member);
        String message = checkIbans() + "\n\n"
                + checkDates(m -> m.getPerson().getBirthday(),
                DataProvider.getResourceValue("memberBadBirthday"),
                DataProvider.getResourceValue("allBirthdaysCorrect"))
                + "\n\n"
                + checkDates(m -> m.getAccountHolder().getMandatSigned(),
                DataProvider.getResourceValue("memberBadMandatSigned"),
                DataProvider.getResourceValue("allMandatSignedCorrect"));
        Alert alert = DialogUtility.createMessageAlert(message);
        alert.initOwner(menuStage);
        alert.showAndWait();
    }

    /**
     * The main method.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
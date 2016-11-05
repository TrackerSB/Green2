/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.connection.DefaultConnection;
import bayern.steinbrecher.gruen2.connection.SshConnection;
import bayern.steinbrecher.gruen2.contribution.Contribution;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.LoginKey;
import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
import bayern.steinbrecher.gruen2.elements.ConfirmDialog;
import bayern.steinbrecher.gruen2.elements.Splashscreen;
import bayern.steinbrecher.gruen2.elements.WaitScreen;
import bayern.steinbrecher.gruen2.exception.AuthException;
import bayern.steinbrecher.gruen2.generator.AddressGenerator;
import bayern.steinbrecher.gruen2.generator.BirthdayGenerator;
import bayern.steinbrecher.gruen2.generator.sepa.SepaPain00800302XMLGenerator;
import bayern.steinbrecher.gruen2.generator.sepa.SequenceType;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.login.ssh.SshLogin;
import bayern.steinbrecher.gruen2.login.standard.DefaultLogin;
import bayern.steinbrecher.gruen2.menu.Menu;
import bayern.steinbrecher.gruen2.people.Member;
import bayern.steinbrecher.gruen2.people.Originator;
import bayern.steinbrecher.gruen2.selection.Selection;
import bayern.steinbrecher.gruen2.sepaform.SepaForm;
import bayern.steinbrecher.gruen2.utility.ServiceFactory;
import bayern.steinbrecher.gruen2.utility.ThreadUtility;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.stage.Stage;

/**
 * Represents the entry of the hole application.
 *
 * @author Stefan Huber
 */
public class Main extends Application {

    private static final long SPLASHSCREEN_MILLIS = 2500;
    private Stage menuStage;
    private final ExecutorService exserv = Executors.newWorkStealingPool();
    private Future<List<Member>> member;
    private final Map<Integer, Future<List<Member>>> memberBirthday
            = new HashMap<>(3);
    private Future<List<Member>> memberNonContributionfree;
    private Future<Map<String, String>> nicknames;
    private Future<Optional<Map<Integer, Double>>> individualContributions;
    private DBConnection dbConnection = null;

    /**
     * Default constructor.
     */
    public Main() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        menuStage = primaryStage;

        Platform.setImplicitExit(false);

        /**
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
        boolean valid = DataProvider.ALL_CONFIGURATIONS_SET;
        if (!valid) {
            ConfirmDialog.createDialog(new Stage(), null,
                    DataProvider.getResourceValue("badConfigs"))
                    .showOnceAndWait();
            Platform.exit();
        }
        return valid;
    }

    private Login createLogin() {
        Login login;
        if (DataProvider.useSsh()) {
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
    private WaitScreen createWaitScreen(Login login) throws Exception {
        WaitScreen waitScreen = new WaitScreen();
        waitScreen.start(new Stage());
        Stage loginStage = new Stage();
        loginStage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                waitScreen.close();
            } else if (login.userConfirmed()) {
                waitScreen.show();
            } else {
                Platform.exit();
            }
        });
        login.start(loginStage);
        return waitScreen;
    }

    private Service<Optional<DBConnection>> createConnectionService(
            Login login, WaitScreen waitScreen) {
        Service<Optional<DBConnection>> connectionService
                = ServiceFactory.createService(() -> {
                    return getConnection(login, waitScreen);
                });

        connectionService.setOnSucceeded(wse -> {
            Optional<DBConnection> optDBConnection
                    = connectionService.getValue();
            if (optDBConnection.isPresent()) {
                dbConnection = optDBConnection.get();
                dbConnection.createTablesIfNeeded();

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
                    Logger.getLogger(Main.class.getName())
                            .log(Level.SEVERE, null, ex);
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

    private void handleAuthException(Login login, WaitScreen waitScreen,
            Exception cause) {
        Platform.runLater(() -> {
            waitScreen.close();

            Stage s = new Stage();
            ConfirmDialog confirm;
            if (cause instanceof ConnectException) {
                confirm = ConfirmDialog.createDialog(s, null,
                        DataProvider.getResourceValue("checkConnection"));
            } else if (cause instanceof UnknownHostException) {
                String message = cause.getMessage();
                confirm = ConfirmDialog.createCheckConnectionDialog(
                        message.substring(message.lastIndexOf(":") + 1).trim(),
                        s, null);
            } else if (cause instanceof AuthException) {
                confirm = ConfirmDialog.createDialog(
                        s, null, DataProvider.getResourceValue("checkInput"));
            } else {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, "Not action specified for: {0}",
                                cause);
                confirm = ConfirmDialog.createDialog(s, null,
                        DataProvider.getResourceValue("unexpectedAbbort"));
            }

            confirm.showOnce(() -> {
                if (confirm.userConfirmed()) {
                    login.reset();
                    synchronized (this) {
                        notifyAll();
                    }
                } else {
                    Platform.exit();
                }
            });
        });
    }

    /**
     * Asks the user for the needed logindata as long as the inserted data is
     * not correct or the user abborts. This method should NOT be called by
     * JavaFX Application Thread. E.g. use
     * {@code ServiceFactory.createService(...)}.
     *
     * @param login The loginframe used to ask the user.
     * @return {@code Optional.empty()} only if the connection could not be
     * established. E.g. the user closed the window or the configured connection
     * is not reachable.
     */
    private Optional<DBConnection> getConnection(
            Login login, WaitScreen waitScreen) {
        DBConnection con = null;

        Optional<Map<LoginKey, String>> loginInfos
                = login.getLoginInformation();
        if (loginInfos.isPresent()) {
            Map<LoginKey, String> loginValues = loginInfos.get();
            try {
                if (DataProvider.useSsh()) {
                    con = new SshConnection(
                            DataProvider.getOrDefault(
                                    ConfigKey.SSH_HOST, "localhost"),
                            loginValues.get(LoginKey.SSH_USERNAME),
                            loginValues.get(LoginKey.SSH_PASSWORD),
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_HOST, "localhost"),
                            loginValues.get(LoginKey.DATABASE_USERNAME),
                            loginValues.get(LoginKey.DATABASE_PASSWORD),
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_NAME, "dbname"));
                } else {
                    con = new DefaultConnection(
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_HOST, "localhost"),
                            loginValues.get(LoginKey.DATABASE_USERNAME),
                            loginValues.get(LoginKey.DATABASE_PASSWORD),
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_NAME, "dbname"));
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
                .forEach(y -> memberBirthday.put(
                        y, exserv.submit(() -> getBirthdayMember(y))));
        memberNonContributionfree = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(m -> !m.isContributionfree())
                .collect(Collectors.toList()));
        nicknames = exserv.submit(() -> dbConnection.getAllNicknames());
        individualContributions = exserv.submit(
                () -> dbConnection.readIndividualContributions());
    }

    /**
     * Checks whether all objects are not {@code null}. If any is {@code null}
     * it throws a {@code IllegalStateException} saying that the caller has to
     * call {@code start(...)} first.
     *
     * @param obj The objects to test.
     */
    private void checkNull(Object... obj) {
        if (Arrays.stream(obj).anyMatch(o -> o == null)) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
    }

    private void generateAddresses(List<Member> member,
            String filename) {
        checkNull(nicknames);
        try {
            if (member.isEmpty()) {
                ConfirmDialog.createDialog(new Stage(), menuStage,
                        DataProvider.getResourceValue("noMemberForOutput"))
                        .showOnceAndWait();
            } else {
                IOStreamUtility.printContent(AddressGenerator.generateAddressData(
                        member, nicknames.get()), filename, true);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
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
            generateAddresses(member.get(),
                    DataProvider.SAVE_PATH + "/Serienbrief_alle.csv");
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
        memberBirthday.putIfAbsent(
                year, exserv.submit(() -> getBirthdayMember(year)));
        try {
            generateAddresses(memberBirthday.get(year).get(),
                    DataProvider.SAVE_PATH
                    + "/Serienbrief_Geburtstag_" + year + ".csv");
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
                ConfirmDialog.createDialog(new Stage(), menuStage,
                        DataProvider.getResourceValue("noMemberForOutput"))
                        .showOnceAndWait();
            } else {
                IOStreamUtility.printContent(
                        BirthdayGenerator.createGroupedOutput(
                                birthdayList, year),
                        DataProvider.SAVE_PATH
                        + "/Geburtstag_" + year + ".csv", true);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void generateSepa(List<Member> memberToSelect,
            Map<Integer, Double> contribution, SequenceType sequenceType) {
        SepaForm sepaForm = new SepaForm(menuStage);
        Optional<Originator> originator;
        try {
            sepaForm.start(new Stage());
            originator = sepaForm.getOriginator();
        } catch (Exception ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            originator = Optional.empty();
        }

        if (originator.isPresent()) {
            try {
                Selection<Member> selection
                        = new Selection<>(memberToSelect, menuStage);
                selection.start(new Stage());
                Optional<List<Member>> selectedMember
                        = selection.getSelection();

                if (selectedMember.isPresent()) {
                    List<Member> invalidMember
                            = SepaPain00800302XMLGenerator.createXMLFile(
                                    selectedMember.get(), contribution,
                                    originator.get(), sequenceType,
                                    DataProvider.SAVE_PATH + "/Sepa.xml",
                                    DataProvider.isSepaWithBom());
                    String message = invalidMember.stream()
                            .map(m -> m.toString())
                            .collect(Collectors.joining("\n"));
                    if (!message.isEmpty()) {
                        ConfirmDialog.createDialog(new Stage(), menuStage,
                                message + "\n"
                                + DataProvider.getResourceValue(
                                        "haveBadAccountInformation"))
                                .showOnceAndWait();
                    }
                }
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Asks for contribution and for member to debit from.
     */
    public void generateUniversalSepa() {
        checkNull(member);
        Contribution contribution = new Contribution(menuStage);
        try {
            contribution.start(new Stage());
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Optional<Double> optContribution = contribution.getContribution();
        if (optContribution.isPresent()) {
            Map<Integer, Double> contributions = new HashMap<>();
            try {
                member.get().stream().forEach(m -> {
                    contributions.put(
                            m.getMembershipnumber(), optContribution.get());
                });
                generateSepa(member.get(), contributions, SequenceType.OOFF);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);
                ConfirmDialog.createDialog(new Stage(), menuStage,
                        DataProvider.getResourceValue("noSepaDebit"))
                        .showOnceAndWait();
            }
        }
    }

    /**
     * Asks for contribution and for member (only non-contributionfree are
     * shown) to debit from.
     */
    public void generateContributionSepa() {
        checkNull(individualContributions, member, memberNonContributionfree);
        try {
            Optional<Map<Integer, Double>> optContributions
                    = individualContributions.get();
            if (optContributions.isPresent()) {
                generateSepa(memberNonContributionfree.get(),
                        optContributions.get(), SequenceType.RCUR);
            } else {
                Contribution contribution = new Contribution(menuStage);
                try {
                    contribution.start(new Stage());
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                Optional<Double> optContribution
                        = contribution.getContribution();
                if (optContribution.isPresent()) {
                    Map<Integer, Double> contributions = new HashMap<>();
                    for (Member m : member.get()) {
                        contributions.put(m.getMembershipnumber(),
                                optContribution.get());
                    }
                    generateSepa(memberNonContributionfree.get(),
                            contributions, SequenceType.RCUR);
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
            ConfirmDialog.createDialog(new Stage(), menuStage,
                    DataProvider.getResourceValue("noSepaDebit"))
                    .showOnceAndWait();
        }
    }

    private String checkIbans() {
        List<Member> badIban = new ArrayList<>();
        try {
            badIban = member.get().parallelStream()
                    .filter(m -> !SepaPain00800302XMLGenerator
                            .hasValidIban(m.getAccountHolder()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        if (badIban.isEmpty()) {
            return DataProvider.getResourceValue("correctIbans");
        } else {
            String noIban = DataProvider.getResourceValue("noIban");
            String message = badIban.stream()
                    .map(m -> {
                        String iban = m.getAccountHolder().getIban();
                        return m + ": \""
                                + (iban.isEmpty() ? noIban : iban)
                                + "\"";
                    })
                    .collect(Collectors.joining("\n"));
            return DataProvider.getResourceValue("memberBadIban") + "\n"
                    + message;
        }
    }

    private String checkDates(Function<Member, LocalDate> dateFunction,
            String invalidDatesIntro, String allCorrectMessage) {
        try {
            String message = member.get().parallelStream()
                    .filter(m -> dateFunction.apply(m) == null)
                    .map(m -> m.toString()
                            + ": \"" + dateFunction.apply(m) + "\"")
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
                        DataProvider.getResourceValue(
                                "allMandatSignedCorrect"));
        ConfirmDialog.createDialog(new Stage(), menuStage, message)
                .showOnceAndWait();
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

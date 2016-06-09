package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.connection.DefaultConnection;
import bayern.steinbrecher.gruen2.connection.SshConnection;
import bayern.steinbrecher.gruen2.contribution.Contribution;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.data.Output;
import bayern.steinbrecher.gruen2.elements.ConfirmDialog;
import bayern.steinbrecher.gruen2.elements.Splashscreen;
import bayern.steinbrecher.gruen2.elements.WaitScreen;
import bayern.steinbrecher.gruen2.exception.AuthException;
import bayern.steinbrecher.gruen2.generator.AddressGenerator;
import bayern.steinbrecher.gruen2.generator.BirthdayGenerator;
import bayern.steinbrecher.gruen2.generator.SepaPain00800302_XML_Generator;
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

    private static final long SPLASHSCREEN_MILLIS = 1500;
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

        if (!DataProvider.hasAllConfigs()) {
            ConfirmDialog.createDialog(
                    new Stage(), null, ConfirmDialog.BAD_CONFIGS)
                    .showOnceAndWait();
            Platform.exit();
        }

        Splashscreen splashScreen = new Splashscreen();
        splashScreen.start(new Stage());
        splashScreen.showSplashscreen(SPLASHSCREEN_MILLIS);

        Login login;
        if (DataProvider.useSsh()) {
            login = new SshLogin();
        } else {
            login = new DefaultLogin();
        }

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
        connectionService.start();
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
                confirm = ConfirmDialog.createDialog(
                        s, null, ConfirmDialog.CHECK_CONNECTION);
            } else if (cause instanceof UnknownHostException) {
                String message = cause.getMessage();
                confirm = ConfirmDialog.createCheckConnectionDialog(
                        message.substring(message.lastIndexOf(":") + 1).trim(),
                        s, null);
            } else if (cause instanceof AuthException) {
                confirm = ConfirmDialog.createDialog(
                        s, null, ConfirmDialog.CHECK_INPUT);
            } else {
                System.err.println("Not action specified for: " + cause);
                confirm = ConfirmDialog.createDialog(
                        s, null, ConfirmDialog.UNEXPECTED_ABBORT);
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
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);

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
            Output.printContent(AddressGenerator.generateAddressData(
                    member, nicknames.get()), filename);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates a file Serienbrief_alle.csv containing addresses of all member.
     *
     * @see DataProvider#getSavepath()
     */
    public void generateAddressesAll() {
        checkNull(member);
        try {
            generateAddresses(member.get(),
                    DataProvider.getSavepath() + "/Serienbrief_alle.csv");
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
     * @see DataProvider#getSavepath()
     */
    public void generateAddressesBirthday(int year) {
        checkNull(memberBirthday);
        memberBirthday.putIfAbsent(
                year, exserv.submit(() -> getBirthdayMember(year)));
        try {
            generateAddresses(memberBirthday.get(year).get(),
                    DataProvider.getSavepath()
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
     * @see DataProvider#getSavepath()
     */
    public void generateBirthdayInfos(int year) {
        checkNull(memberBirthday);
        memberBirthday.putIfAbsent(
                year, exserv.submit(() -> getBirthdayMember(year)));
        try {
            List<Member> birthdayList = memberBirthday.get(year).get();
            if (!birthdayList.isEmpty()) {
                Output.printContent(
                        BirthdayGenerator.createGroupedOutput(
                                birthdayList, year),
                        DataProvider.getSavepath()
                        + "/Geburtstag_" + year + ".csv");
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private void generateSepa(List<Member> memberToSelect,
            Map<Integer, Double> contribution) {
        SepaForm sepaForm = new SepaForm(menuStage);
        Optional<Originator> originator;
        try {
            sepaForm.start(new Stage());
            originator = sepaForm.getOriginator();
        } catch (Exception ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
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
                            = SepaPain00800302_XML_Generator.createXMLFile(
                                    selectedMember.get(), contribution,
                                    originator.get(),
                                    DataProvider.getSavepath() + "/Sepa.xml");
                    Optional<StringBuilder> message = invalidMember.stream()
                            .map(m -> new StringBuilder(m + "\n"))
                            .reduce(StringBuilder::append);
                    if (message.isPresent()) {
                        Stage badAccountDataStage = new Stage();
                        new ConfirmDialog(message.get() + "\n"
                                + DataProvider.getResourceValue(
                                        "haveNoOrInvalidIban"),
                                menuStage).start(badAccountDataStage);
                        badAccountDataStage.showAndWait();
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
            Logger.getLogger(Main.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        Optional<Double> optContribution = contribution.getContribution();
        if (optContribution.isPresent()) {
            Map<Integer, Double> contributions = new HashMap<>();
            try {
                member.get().stream().forEach(m -> {
                    contributions.put(
                            m.getMembershipnumber(), optContribution.get());
                });
                generateSepa(member.get(), contributions);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);
                try {
                    ConfirmDialog.createDialog(
                            new Stage(), menuStage, ConfirmDialog.NO_SEPA_DEBIT)
                            .showOnceAndWait();
                } catch (Exception ex1) {
                    Logger.getLogger(Main.class.getName())
                            .log(Level.SEVERE, null, ex1);
                }
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
                        optContributions.get());
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
                            contributions);
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
            try {
                ConfirmDialog.createDialog(new Stage(), menuStage,
                        ConfirmDialog.NO_SEPA_DEBIT)
                        .showOnceAndWait();
            } catch (Exception ex1) {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex1);
            }
        }
    }

    /**
     * Checks the correctness of all IBANs and shows a dialog showing invalid
     * IBANs.
     */
    public void checkIban() {
        checkNull(member);
        List<Member> badIban = new ArrayList<>();
        try {
            badIban = member.get().parallelStream()
                    .filter(m -> !SepaPain00800302_XML_Generator
                            .hasValidIban(m.getAccountHolder()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        if (badIban.isEmpty()) {
            try {
                ConfirmDialog.createDialog(new Stage(), menuStage,
                        ConfirmDialog.CORRECT_IBANS)
                        .showOnceAndWait();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        } else {
            String message = DataProvider.getResourceValue("memberBadIban")
                    + "\n";
            String noIban = DataProvider.getResourceValue("noIban");
            message += badIban.stream()
                    .map(m -> {
                        String iban = m.getAccountHolder().getIban();
                        return m + ": \""
                                + (iban.isEmpty() ? noIban : iban)
                                + "\"\n";
                    })
                    .reduce(message, String::concat);
            try {
                ConfirmDialog.createDialog(new Stage(), menuStage, message)
                        .showOnceAndWait();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
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

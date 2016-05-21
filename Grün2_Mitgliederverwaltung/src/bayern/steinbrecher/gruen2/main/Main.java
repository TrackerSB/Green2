package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.utility.ServiceFactory;
import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.connection.DefaultConnection;
import bayern.steinbrecher.gruen2.connection.SshConnection;
import bayern.steinbrecher.gruen2.contribution.Contribution;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.data.Output;
import bayern.steinbrecher.gruen2.elements.ConfirmDialog;
import bayern.steinbrecher.gruen2.elements.WaitScreen;
import bayern.steinbrecher.gruen2.generator.AddressGenerator;
import bayern.steinbrecher.gruen2.generator.BirthdayGenerator;
import bayern.steinbrecher.gruen2.generator.MemberGenerator;
import bayern.steinbrecher.gruen2.generator.SepaPain00800302_XML_Generator;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.login.ssh.SshLogin;
import bayern.steinbrecher.gruen2.login.standard.DefaultLogin;
import bayern.steinbrecher.gruen2.menu.Menu;
import bayern.steinbrecher.gruen2.people.Member;
import bayern.steinbrecher.gruen2.people.Originator;
import bayern.steinbrecher.gruen2.selection.Selection;
import bayern.steinbrecher.gruen2.sepaform.SepaForm;
import bayern.steinbrecher.gruen2.utility.ThreadUtility;
import com.jcraft.jsch.JSchException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.SQLException;
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

    /**
     * A list containing all names of needed columns for queries in the member
     * table.
     */
    public static final List<String> COLUMN_LABELS_MEMBER = new ArrayList<>(
            Arrays.asList("mitgliedsnummer", "vorname", "nachname", "titel",
                    "istmaennlich", "istaktiv", "geburtstag", "strasse",
                    "hausnummer", "plz", "ort", "istbeitragsfrei", "iban",
                    "bic", "kontoinhabervorname", "kontoinhabernachname",
                    "mandaterstellt"));
    private static final String ALL_COLUMN_LABELS_MEMBER
            = COLUMN_LABELS_MEMBER.stream()
            .reduce("", (s1, s2) -> s1.concat(s2).concat(","));
    private static final ConfirmDialog BAD_CONFIGS = new ConfirmDialog(
            "Es fehlen Konfigurationseinstellungen oder die "
            + "Konfigurationsdatei konnte nicht gefunden werden.\n"
            + "Frage bei stefan.huber.niedling@outlook.com nach.", null);
    private static final ConfirmDialog CHECK_CONNECTION = new ConfirmDialog(
            "Prüfe, ob die Datenbank "
            + "erreichbar ist, und ob du Grün2 richtig "
            + "konfiguriert hast.", null);
    private static final ConfirmDialog CHECK_INPUT
            = new ConfirmDialog("Prüfe deine Eingaben.", null);
    private Stage menuStage;
    private final ConfirmDialog NO_SEPA_DEBIT
            = new ConfirmDialog("Sepalastschrift konnte nicht erstellt werden",
                    menuStage);
    private final ConfirmDialog CORRECT_IBANS = new ConfirmDialog(
            "Alle IBANs haben eine korrekte Prüfsumme", menuStage);
    private final ExecutorService exserv = Executors.newWorkStealingPool();
    private Future<List<Member>> member;
    private Map<Integer, Future<List<Member>>> memberBirthday
            = new HashMap<>(3);
    private Future<List<Member>> memberNonContributionfree;
    private Future<Map<String, String>> nicknames;
    private Future<Optional<Map<Integer, Double>>> individualContributions;

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
        if (!DataProvider.hasAllConfigs()) {
            Stage badConfigsStage = new Stage();
            BAD_CONFIGS.start(badConfigsStage);
            badConfigsStage.showAndWait();
            throw new IllegalStateException("Invalid configs.");
        }

        Login login;
        if (DataProvider.useSsh()) {
            login = new SshLogin();
        } else {
            login = new DefaultLogin();
        }

        Stage waitScreen = WaitScreen.createWaitScreen();
        Stage loginStage = new Stage();
        loginStage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                waitScreen.close();
            } else if (login.userConfirmed()) {
                waitScreen.show();
            }
        });
        login.start(loginStage);

        Service<Optional<DBConnection>> connectionService
                = ServiceFactory.createService(() -> {
                    try {
                        return getConnection(login, waitScreen);
                    } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                    return Optional.empty();
                });
        connectionService.setOnSucceeded(wse -> {
            Optional<DBConnection> optDBConnection
                    = connectionService.getValue();
            if (optDBConnection.isPresent()) {
                DBConnection dbConnection = optDBConnection.get();
                if (!tablesExist(dbConnection)) {
                    createTables(dbConnection);
                }
                executeQueries(dbConnection);

                menuStage = new Stage();
                menuStage.showingProperty().addListener(
                        (obs, oldVal, newVal) -> {
                    if (newVal) {
                        waitScreen.close();
                    } else {
                        dbConnection.close();
                        exserv.shutdownNow();
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

    private void checkAuth(Login login, Stage waitScreen, Exception ex) {
        Platform.runLater(() -> {
            waitScreen.close();

            Throwable cause = ex.getCause();
            String message = ex.getMessage();
            Stage dialogStage = new Stage();
            try {
                if (cause instanceof ConnectException
                        || cause instanceof UnknownHostException) {
                    CHECK_CONNECTION.start(dialogStage);
                    dialogStage.show();
                } else if (message != null
                        && message.contains("Auth fail")) {
                    CHECK_INPUT.start(dialogStage);
                    dialogStage.showingProperty()
                            .addListener((obs, oldVal, newVal) -> {
                                if (!newVal) {
                                    login.reset();
                                    synchronized (this) {
                                        notifyAll();
                                    }
                                }
                            });
                    dialogStage.show();
                } else {
                    System.err.println(
                            "Not action specified for: " + cause);
                }
            } catch (Exception exc) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, exc);
            }
        });
    }

    /**
     * Asks the user for the needed logindata as long as the inserted data is
     * not correct or the user abborts.
     *
     * @param login The loginframe used to ask the user.
     * @return {@code Optional.empty()} only if the connection could not be
     * established. E.g. the user closed the window or the configured connection
     * is not reachable.
     */
    private Optional<DBConnection> getConnection(Login login, Stage waitScreen) {
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
            } catch (JSchException | SQLException ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);

                checkAuth(login, waitScreen, ex);

                ThreadUtility.waitWhile(this, login.wouldShowBinding().not());

                return getConnection(login, waitScreen);
            }
        }

        return Optional.ofNullable(con);
    }

    private boolean tablesExist(DBConnection dbConnection) {
        try {
            dbConnection.execQuery(
                    "SELECT COUNT(*) FROM Mitglieder, Spitznamen;");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private void createTables(DBConnection dbConnection) {
        try {
            dbConnection.execUpdate("CREATE TABLE Mitglieder ("
                    + "Mitgliedsnummer INTEGER PRIMARY KEY,"
                    + "Titel VARCHAR(255) NOT NULL,"
                    + "Vorname VARCHAR(255) NOT NULL,"
                    + "Nachname VARCHAR(255) NOT NULL,"
                    + "istAktiv BOOLEAN NOT NULL,"
                    + "istMaennlich BOOLEAN NOT NULL,"
                    + "Geburtstag DATE NOT NULL,"
                    + "Strasse VARCHAR(255) NOT NULL,"
                    + "Hausnummer VARCHAR(255) NOT NULL,"
                    + "PLZ VARCHAR(255) NOT NULL,"
                    + "Ort VARCHAR(255) NOT NULL,"
                    + "AusgetretenSeit DATE NOT NULL DEFAULT '0000-00-00',"
                    + "IBAN VARCHAR(255) NOT NULL,"
                    + "BIC VARCHAR(255) NOT NULL,"
                    + "MandatErstellt DATE NOT NULL,"
                    + "KontoinhaberVorname VARCHAR(255) NOT NULL,"
                    + "KontoinhaberNachname VARCHAR(255) NOT NULL,"
                    + "istBeitragsfrei BOOLEAN NOT NULL DEFAULT '0',"
                    + "Beitrag FLOAT NOT NULL);");
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        try {
            dbConnection.execUpdate("CREATE TABLE Spitznamen ("
                    + "Name VARCHAR(255) PRIMARY KEY,"
                    + "Spitzname VARCHAR(255) NOT NULL);");
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a list of all member accessable with {@code dbc}. The list
     * contains all labels hold in {@code COLUMN_LABELS_MEMBER}.
     *
     * @param dbc The connection to use for accessing the data.
     * @return The list with the member.
     */
    public static List<Member> readMember(DBConnection dbc) {
        try {
            return MemberGenerator.generateMemberList(dbc.execQuery(
                    "SELECT " + ALL_COLUMN_LABELS_MEMBER
                    .substring(0, ALL_COLUMN_LABELS_MEMBER.length() - 1)
                    + " FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00';"));
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private List<Member> getBirthdayMember(int year)
            throws InterruptedException, ExecutionException {
        return member.get()
                .parallelStream()
                .filter(m -> BirthdayGenerator.getsNotified(m, year))
                .collect(Collectors.toList());
    }

    private void executeQueries(DBConnection dbConnection) {
        member = exserv.submit(() -> readMember(dbConnection));
        int currentYear = LocalDate.now().getYear();
        IntStream.rangeClosed(currentYear - 1, currentYear + 1)
                .forEach(y -> memberBirthday.put(
                        y, exserv.submit(() -> getBirthdayMember(y))));
        memberNonContributionfree = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(m -> !m.isContributionfree())
                .collect(Collectors.toList()));
        nicknames = exserv.submit(() -> readNicknames(dbConnection));
        individualContributions = exserv.submit(() -> {
            return readIndividualContributions(dbConnection);
        });
    }

    private void generateAddresses(List<Member> member,
            String filename) {
        if (nicknames == null) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
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
        if (member == null) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
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
        if (memberBirthday == null) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
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
        if (memberBirthday == null) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
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

    /**
     * Queries the nickname table of the specified connection.
     *
     * @param dbc The connection to query through.
     * @return A map from prenames to nicknames.
     */
    public static Map<String, String> readNicknames(DBConnection dbc) {
        Map<String, String> mappedNicknames = new HashMap<>();
        try {
            List<List<String>> queriedNicknames
                    = dbc.execQuery("SELECT * FROM Spitznamen;");
            int nameIndex = queriedNicknames.get(0).indexOf("Name");
            int nicknameIndex = queriedNicknames.get(0).indexOf("Spitzname");

            queriedNicknames.parallelStream().skip(1).forEach(row -> {
                mappedNicknames.put(row.get(nameIndex), row.get(nicknameIndex));
            });
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return mappedNicknames;
    }

    /**
     * Reads the individual contributions of every member - if specified.
     *
     * @param dbc The connection to use to query.
     * @return A Optional containing the individual contributions or
     * {@code Optional.empty()} if inidividual contributions are not specified.
     */
    public static Optional<Map<Integer, Double>> readIndividualContributions(
            DBConnection dbc) {
        if (dbc.checkColumn("Mitglieder", "Beitrag")) {
            try {
                List<List<String>> result
                        = dbc.execQuery("SELECT Mitgliedsnummer, Beitrag "
                                + "FROM Mitglieder;");
                Map<Integer, Double> contributions = new HashMap<>();
                result.parallelStream()
                        .skip(1)
                        .forEach(row -> {
                            contributions.put(Integer.parseInt(row.get(0)),
                                    Double.parseDouble(
                                            row.get(1).replaceAll(",", ".")));
                        });
                return Optional.of(contributions);
            } catch (SQLException ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        return Optional.empty();
    }

    private void generateCustomSepa(List<Member> memberToSelect,
            Map<Integer, Double> contribution) {
        SepaForm sepaForm = new SepaForm();
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
                        = new Selection<>(memberToSelect);
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
                        new ConfirmDialog(message.get()
                                + "\nhaben keine bzw. eine ungültige IBAN "
                                + "und/oder keine BIC.",
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
        if (member == null) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
        Contribution contribution = new Contribution();
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
                generateCustomSepa(member.get(), contributions);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);
                try {
                    Stage noSepaDebitStage = new Stage();
                    NO_SEPA_DEBIT.start(noSepaDebitStage);
                    noSepaDebitStage.showAndWait();
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
        if (individualContributions == null || member == null
                || memberNonContributionfree == null) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
        try {
            Optional<Map<Integer, Double>> optContributions
                    = individualContributions.get();
            if (optContributions.isPresent()) {
                generateCustomSepa(memberNonContributionfree.get(),
                        optContributions.get());
            } else {
                Contribution contribution = new Contribution();
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
                    generateCustomSepa(memberNonContributionfree.get(),
                            contributions);
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
            try {
                Stage noSepaDebitStage = new Stage();
                NO_SEPA_DEBIT.start(noSepaDebitStage);
                noSepaDebitStage.showAndWait();
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
        if (member == null) {
            throw new IllegalStateException(
                    "You have to call start(...) first");
        }
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
                Stage correctIbansSTage = new Stage();
                CORRECT_IBANS.start(correctIbansSTage);
                correctIbansSTage.showAndWait();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        } else {
            String message = "Folgende Mitglieder haben keine IBAN oder eine "
                    + "IBAN mit falscher Prüfsumme:\n";
            message = badIban.stream()
                    .map(m -> {
                        String iban = m.getAccountHolder().getIban();
                        return m + ": \""
                                + (iban.isEmpty() ? "Keine IBAN" : iban)
                                + "\"\n";
                    })
                    .reduce(message, String::concat);
            try {
                Stage messageStage = new Stage();
                new ConfirmDialog(message, menuStage).start(messageStage);
                messageStage.showAndWait();
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

package bayern.steinbrecher.gruen2.mainmenu;

import bayern.steinbrecher.gruen2.Output;
import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.connection.DefaultConnection;
import bayern.steinbrecher.gruen2.connection.SshConnection;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.elements.ConfirmDialog;
import bayern.steinbrecher.gruen2.generator.BirthdayGenerator;
import bayern.steinbrecher.gruen2.login.ssh.SshLogin;
import bayern.steinbrecher.gruen2.login.standard.DefaultLogin;
import bayern.steinbrecher.gruen2.selection.Selection;
import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.sepa.Originator;
import bayern.steinbrecher.gruen2.generator.SepaPain00800302_XML_Generator;
import bayern.steinbrecher.gruen2.sepaform.SepaForm;
import bayern.steinbrecher.gruen2.generator.AddressGenerator;
import bayern.steinbrecher.gruen2.generator.MemberGenerator;
import com.jcraft.jsch.JSchException;
import java.net.ConnectException;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the main menu containing the main functions of Grün2.
 *
 * @author Stefan Huber
 */
public class MainMenu extends Application {

    public static final List<String> COLUMN_LABELS_MEMBER = new ArrayList<>(
            Arrays.asList("mitgliedsnummer", "vorname", "nachname", "titel",
                    "istmaennlich", "istaktiv", "geburtstag", "strasse",
                    "hausnummer", "plz", "ort", "istbeitragsfrei", "iban",
                    "bic", "kontoinhabervorname", "kontoinhabernachname",
                    "mandaterstellt"));
    private static final String ALL_COLUMN_LABELS_MEMBER
            = COLUMN_LABELS_MEMBER.stream()
            .reduce("", (s1, s2) -> s1.concat(s2).concat(","));
    private final ExecutorService exserv = Executors.newWorkStealingPool();
    private Future<List<Member>> member;
    private Map<Integer, Future<List<Member>>> memberBirthday
            = new HashMap<>(3);
    private Future<List<Member>> memberContributionfree;
    private Future<Map<String, String>> nicknames;
    private MainMenuController mcontroller;
    private Stage primaryStage;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!DataProvider.hasAllConfigs()) {
            ConfirmDialog.showConfirmDialog(
                    "Es fehlen Konfigurationseinstellungen oder die "
                    + "Konfigurationsdatei konnte nicht gefunden werden.\n"
                    + "Frage bei stefan.huber.niedling@outlook.com nach.",
                    primaryStage);
            throw new IllegalStateException("Invalid configs.");
        }
        this.primaryStage = primaryStage;

        Login login;
        if (DataProvider.useSsh()) {
            login = new SshLogin();
        } else {
            login = new DefaultLogin();
        }
        Stage loginStage = new Stage();
        login.start(loginStage);

        DBConnection dbConnection = getConnection(login);

        if (dbConnection != null) {
            executeQueries(dbConnection);

            primaryStage.showingProperty().addListener(
                    (obs, oldVal, newVal) -> {
                if (!newVal) {
                    dbConnection.close();
                    exserv.shutdownNow();
                }
            });

            FXMLLoader fxmlLoader
                    = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
            Parent root = fxmlLoader.load();
            root.getStylesheets().add(DataProvider.getStylesheetPath());

            mcontroller = fxmlLoader.getController();
            mcontroller.setStage(primaryStage);
            mcontroller.setCaller(this);

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Programm wählen");
            primaryStage.setResizable(false);
            primaryStage.getIcons().add(DataProvider.getIcon());
            primaryStage.show();
        }
    }

    private void executeQueries(DBConnection dbConnection) {
        member = exserv.submit(() -> readMember(dbConnection));
        int currentYear = LocalDate.now().getYear();
        IntStream.rangeClosed(currentYear - 1, currentYear + 1)
                .forEach(y -> memberBirthday.put(
                        y, exserv.submit(() -> getBirthdayMember(y))));
        memberContributionfree = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(m -> !m.isContributionfree())
                .collect(Collectors.toList()));
        nicknames = exserv.submit(() -> readNicknames(dbConnection));
    }

    /**
     * Asks the user for the needed logindata as long as the inserted data is
     * not correct or the user abborts.
     *
     * @param login The loginframe used to ask the user.
     * @return {@code null} only if the connection could not be established.
     * E.g. the user closed the window or the configured connection is not
     * reachable.
     */
    private DBConnection getConnection(Login login) {
        DBConnection con = null;
        Map<LoginKey, String> loginInfos = login.getLoginInformation();
        while (con == null && loginInfos != null) {
            try {
                if (DataProvider.useSsh()) {
                    con = new SshConnection(
                            DataProvider.getOrDefault(
                                    ConfigKey.SSH_HOST, "localhost"),
                            loginInfos.get(LoginKey.SSH_USERNAME),
                            loginInfos.get(LoginKey.SSH_PASSWORD),
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_HOST, "localhost"),
                            loginInfos.get(LoginKey.DATABASE_USERNAME),
                            loginInfos.get(LoginKey.DATABASE_PASSWORD),
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_NAME, "dbname"));
                } else {
                    con = new DefaultConnection(
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_HOST, "localhost"),
                            loginInfos.get(LoginKey.DATABASE_USERNAME),
                            loginInfos.get(LoginKey.DATABASE_PASSWORD),
                            DataProvider.getOrDefault(
                                    ConfigKey.DATABASE_NAME, "dbname"));
                }
            } catch (JSchException | SQLException ex) {
                Logger.getLogger(MainMenu.class.getName())
                        .log(Level.SEVERE, null, ex);

                //Check "auth fail"
                Throwable cause = ex.getCause();
                String message = ex.getMessage();
                if (cause instanceof ConnectException) {
                    ConfirmDialog.showConfirmDialog("Prüfe, ob du "
                            + "Internetverbindung hast, und ob du Grün2 "
                            + "richtig konfiguriert hast.", primaryStage);
                } else if (message != null && message.contains("Auth fail")) {
                    ConfirmDialog.showConfirmDialog(
                            "Prüfe deine Eingaben.", primaryStage);
                    loginInfos = login.getLoginInformation();
                } else {
                    System.err.println("Not action specified for: " + ex);
                }
            }
        }
        return con;
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
                    + "WHERE AusgetretenSeit='0000-00-00'"));
        } catch (SQLException ex) {
            Logger.getLogger(MainMenu.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private List<Member> getBirthdayMember(int year)
            throws InterruptedException, ExecutionException {
        return member.get()
                .parallelStream()
                .filter(m -> BirthdayGenerator.getsNotify(m, year))
                .collect(Collectors.toList());
    }

    private void generateAddresses(Future<List<Member>> member,
            String filename) {
        try {
            Output.printContent(AddressGenerator.generateAddressData(
                    member.get(), nicknames.get()), filename);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MainMenu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    void generateAddressesAll() {
        generateAddresses(member,
                DataProvider.getSavepath() + "/Serienbrief_alle.csv");
    }

    void generateAddressesBirthday(int year) {
        memberBirthday.putIfAbsent(
                year, exserv.submit(() -> getBirthdayMember(year)));
        generateAddresses(memberBirthday.get(year), DataProvider.getSavepath()
                + "/Serienbrief_Geburtstag_" + year + ".csv");
    }

    void generateBirthdayInfos(int year) {
        memberBirthday.putIfAbsent(
                year, exserv.submit(() -> getBirthdayMember(year)));
        try {
            Output.printContent(
                    BirthdayGenerator.createGroupedOutput(
                            memberBirthday.get(year).get(), year),
                    DataProvider.getSavepath()
                    + "/Geburtstag_" + year + ".csv");
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MainMenu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public static Map<String, String> readNicknames(DBConnection dbc) {
        Map<String, String> mappedNicknames = new HashMap<>();
        try {
            List<List<String>> queriedNicknames
                    = dbc.execQuery("SELECT * FROM Spitznamen");
            int nameIndex = queriedNicknames.get(0).indexOf("Name");
            int nicknameIndex = queriedNicknames.get(0).indexOf("Spitzname");

            queriedNicknames.parallelStream().skip(1).forEach(row -> {
                mappedNicknames.put(row.get(nameIndex), row.get(nicknameIndex));
            });
        } catch (SQLException ex) {
            Logger.getLogger(MainMenu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return mappedNicknames;
    }

    private void generateSepa(Future<List<Member>> memberToSelect) {
        SepaForm sepaForm = new SepaForm();
        Originator originator = null;
        try {
            sepaForm.start(new Stage());
            originator = sepaForm.getOriginator();
        } catch (Exception ex) {
            Logger.getLogger(MainMenu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        if (originator != null) {
            try {
                Selection<Member> selection
                        = new Selection<>(memberToSelect.get());
                selection.start(new Stage());
                List<Member> selectedMember = selection.getSelection();
                double contribution = selection.getContribution();

                List<Member> invalidMember
                        = SepaPain00800302_XML_Generator.createXMLFile(
                                selectedMember, contribution, originator,
                                DataProvider.getSavepath() + "/Sepa.xml");
                Optional<String> message = invalidMember.stream()
                        .map(Member::toString)
                        .map(s -> s += '\n')
                        .reduce(String::concat);
                if (message.isPresent()) {
                    ConfirmDialog.showConfirmDialog(message.get()
                            + "\nhaben keine IBAN oder keine BIC.",
                            primaryStage);
                }

            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(MainMenu.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(MainMenu.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    void generateUniversalSepa() {
        generateSepa(member);
    }

    void generateContributionSepa() {
        generateSepa(memberContributionfree);
    }

    /**
     * The starting point of the hole application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

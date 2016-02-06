package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.Output;
import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.connection.DefaultConnection;
import bayern.steinbrecher.gruen2.connection.SshConnection;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.data.LoginKey;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Stefan Huber
 */
public class Main extends Application {

    public static final Predicate<Member> TEST_BIRTHDAY_THIS_YEAR = m -> {
        int age = LocalDate.now().getYear()
                - m.getPerson().getBirthday().getYear();
        return age == 50 || age == 60 || age == 70 || age == 75 || age >= 80;
    };
    public static final Predicate<Member> TEST_BIRTHDAY_NEXT_YEAR = m -> {
        int age = LocalDate.now().getYear()
                - m.getPerson().getBirthday().getYear() + 1;
        return age == 50 || age == 60 || age == 70 || age == 75 || age >= 80;
    };
    private static final List<String> COLUMN_LABELS_MEMBER = new ArrayList<>(
            Arrays.asList("mitgliedsnummer", "vorname", "nachname", "titel",
                    "istmaennlich", "istaktiv", "geburtstag", "strasse",
                    "hausnummer", "plz", "ort", "istbeitragsfrei", "iban",
                    "bic", "kontoinhabervorname", "kontoinhabernachname",
                    "mandaterstellt"));
    private static final String ALL_COLUMN_LABELS_MEMBER
            = COLUMN_LABELS_MEMBER.stream()
            .reduce("", (s1, s2) -> s1.concat(s2).concat(","));
    private MainController mcontroller;
    private final ExecutorService exserv = Executors.newWorkStealingPool();
    private Future<List<Member>> member;
    private Future<List<Member>> memberBirthdayThisYear;
    private Future<List<Member>> memberBirthdayNextYear;
    private Future<List<Member>> memberContributionfree;
    private Future<Map<String, String>> nicknames;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Login login;
        if (DataProvider.useSsh()) {
            login = new SshLogin();
        } else {
            login = new DefaultLogin();
        }
        Stage loginStage = new Stage();
        login.start(loginStage);

        DBConnection dbConnection = getConnection(login, loginStage);

        if (dbConnection != null) {
            primaryStage.showingProperty().addListener(
                    (obs, oldVal, newVal) -> {
                if (!newVal) {
                    dbConnection.close();
                    exserv.shutdownNow();
                }
            });

            FXMLLoader fxmlLoader
                    = new FXMLLoader(getClass().getResource("Main.fxml"));
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

            executeQueries(dbConnection);
        }
    }

    public void executeQueries(DBConnection dbConnection) {
        member = exserv.submit(() -> readMember(dbConnection));
        memberBirthdayThisYear = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(TEST_BIRTHDAY_THIS_YEAR)
                .collect(Collectors.toList()));
        memberBirthdayNextYear = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(TEST_BIRTHDAY_NEXT_YEAR)
                .collect(Collectors.toList()));
        memberContributionfree = exserv.submit(() -> member.get()
                .parallelStream()
                .filter(m -> !m.isContributionfree())
                .collect(Collectors.toList()));
        nicknames = exserv.submit(() -> readNicknames(dbConnection));
    }

    private DBConnection getConnection(Login login, Stage loginStage) {
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
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex);

                //Login invalid
                loginInfos = login.getLoginInformation();
            }
        }
        return con;
    }

    public List<Member> readMember(DBConnection dbc) {
        try {
            return MemberGenerator.generateMemberList(dbc.execQuery(
                    "SELECT " + ALL_COLUMN_LABELS_MEMBER
                    .substring(0, ALL_COLUMN_LABELS_MEMBER.length() - 1)
                    + " FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00'"));
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void generateAddresses(Future<List<Member>> member,
            String filename) {
        try {
            Output.printContent(AddressGenerator.generateAddressData(
                    member.get(), nicknames.get()), filename);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void generateAddressesAll() {
        generateAddresses(member,
                DataProvider.getSavepath() + "/Serienbrief_alle.csv");
    }

    void generateAddressesBirthdayThisYear() {
        int currentYear = LocalDate.now().getYear();
        generateAddresses(memberBirthdayThisYear, DataProvider.getSavepath()
                + "/Serienbrief_Geburtstag_" + currentYear + ".csv");
    }

    void generateAddressesBirthdayNextYear() {
        int nextYear = LocalDate.now().getYear() + 1;
        generateAddresses(memberBirthdayNextYear, DataProvider.getSavepath()
                + "/Serienbrief_Geburtstag_" + nextYear + ".csv");
    }

    void generateBirthdayThisYearInfos() {
        try {
            int currentYear = LocalDate.now().getYear();
            Output.printContent(BirthdayGenerator.createGroupedOutput(
                    memberBirthdayThisYear.get(), currentYear),
                    DataProvider.getSavepath() + "/Geburtstag_" + currentYear
                    + ".csv");
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void generateBirthdayNextYearInfos() {
        try {
            int nextYear = LocalDate.now().getYear() + 1;
            Output.printContent(BirthdayGenerator.createGroupedOutput(
                    memberBirthdayNextYear.get(), nextYear),
                    DataProvider.getSavepath() + "/Geburtstag_" + nextYear
                    + ".csv");
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Map<String, String> readNicknames(DBConnection dbc) {
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
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mappedNicknames;
    }

    private void generateSepa(List<Member> memberToSelect) {
        SepaForm sepaForm = new SepaForm();
        Originator originator = null;
        try {
            sepaForm.start(new Stage());
            originator = sepaForm.getOriginator();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (originator != null) {
            try {
                Selection<Member> selection = new Selection<>(memberToSelect);
                selection.start(new Stage());
                List<Member> selectedMember = selection.getSelection();
                double contribution = selection.getContribution();

                SepaPain00800302_XML_Generator.createXMLFile(selectedMember,
                        contribution, originator,
                        DataProvider.getSavepath() + "/Sepa.xml");
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    void generateContributionSepa() {
        try {
            generateSepa(member.get().parallelStream()
                    .filter(m -> !m.isContributionfree())
                    .collect(Collectors.toList()));
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void generateUniversalSepa() {
        try {
            generateSepa(member.get());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.connection.DefaultConnection;
import bayern.steinbrecher.gruen2.connection.SshConnection;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.login.ssh.SshLogin;
import bayern.steinbrecher.gruen2.login.standard.DefaultLogin;
import bayern.steinbrecher.gruen2.member.AccountHolder;
import bayern.steinbrecher.gruen2.member.Address;
import bayern.steinbrecher.gruen2.selection.Selection;
import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.member.Person;
import bayern.steinbrecher.gruen2.sepa.Originator;
import bayern.steinbrecher.gruen2.sepa.SepaPain00800302_XML_Generator;
import bayern.steinbrecher.gruen2.sepaform.SepaForm;
import bayern.steinbrecher.gruen2.serialLetters.DataForSerialLettersGenerator;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Stefan Huber
 */
public class Main extends Application {

    private MainController mcontroller;
    private DBConnection dbConnection;
    private static final List<String> COLUMN_LABELS_MEMBER = new ArrayList<>(
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

        dbConnection = getConnection(login, loginStage);

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

            member = exserv.submit(() -> readMember(dbConnection));
            nicknames = exserv.submit(() -> readNicknames(dbConnection));
        }
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

    private List<Member> readMember(DBConnection dbc) {
        try {
            return generateMemberList(dbc.execQuery(
                    "SELECT " + ALL_COLUMN_LABELS_MEMBER
                    .substring(0, ALL_COLUMN_LABELS_MEMBER.length() - 1)
                    + " FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00'"));
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private List<Member> generateMemberList(List<List<String>> queryResult) {
        assert COLUMN_LABELS_MEMBER.size() == queryResult.size();
        /*Map<String, Integer> resultToInitialLabel
                = new HashMap<>(COLUMN_LABELS_MEMBER.size());
        List<String> resultLabels = queryResult.get(0).stream()
                .map(String::toLowerCase).collect(Collectors.toList());
        COLUMN_LABELS_MEMBER.stream().forEach(initialLabel -> {
            resultToInitialLabel
                    .put(initialLabel, resultLabels.indexOf(initialLabel));
        });*/

        List<Member> memberList = new ArrayList<>(queryResult.size());
        queryResult.parallelStream().forEach(row -> {
            String prename = row.get(COLUMN_LABELS_MEMBER.indexOf("vorname"));
            String lastname = row.get(COLUMN_LABELS_MEMBER.indexOf("nachname"));
            String title = row.get(COLUMN_LABELS_MEMBER.indexOf("titel"));
            LocalDate birthday = LocalDate.parse(
                    row.get(COLUMN_LABELS_MEMBER.indexOf("geburtstag")));
            boolean isMale
                    = row.get(COLUMN_LABELS_MEMBER.indexOf("istmaennlich"))
                    .equalsIgnoreCase("1");
            String iban = row.get(COLUMN_LABELS_MEMBER.indexOf("iban"));
            String bic = row.get(COLUMN_LABELS_MEMBER.indexOf("bic"));
            LocalDate mandatsigned = LocalDate.parse(
                    row.get(COLUMN_LABELS_MEMBER.indexOf("mandaterstellt")));
            String street = row.get(COLUMN_LABELS_MEMBER.indexOf("strasse"));
            String housenumber
                    = row.get(COLUMN_LABELS_MEMBER.indexOf("hausnummer"));
            String postcode = row.get(COLUMN_LABELS_MEMBER.indexOf("plz"));
            String place = row.get(COLUMN_LABELS_MEMBER.indexOf("ort"));
            boolean isActive = row.get(COLUMN_LABELS_MEMBER.indexOf("istaktiv"))
                    .equalsIgnoreCase("1");
            boolean isContributionfree
                    = row.get(COLUMN_LABELS_MEMBER.indexOf("istbeitragsfrei"))
                    .equalsIgnoreCase("1");
            int membershipnumber = Integer.parseInt(
                    row.get(COLUMN_LABELS_MEMBER.indexOf("mitgliedsnummer")));
            String accountHolderPrename = row.get(
                    COLUMN_LABELS_MEMBER.indexOf("kontoinhabervorname"));
            String accountHolderLastname = row.get(
                    COLUMN_LABELS_MEMBER.indexOf("kontoinhabernachname"));

            Person p = new Person(prename, lastname, title, birthday, isMale);
            //FIXME MandatChanged has not to be always false
            AccountHolder ah = new AccountHolder(iban, bic, mandatsigned, false,
                    accountHolderPrename.isEmpty()
                            ? prename : accountHolderPrename,
                    accountHolderLastname.isEmpty()
                            ? lastname : accountHolderLastname, title, birthday,
                    isMale);
            Address ad = new Address(street, housenumber, postcode, place);
            Member m = new Member(
                    membershipnumber, p, ad, ah, isActive, isContributionfree);
            synchronized (memberList) {
                memberList.add(m);
            }
        });

        return memberList;
    }

    public void generateSerialLetterData() {
        if (dbConnection == null) {
            throw new IllegalStateException(
                    "No connection initialised. Call start(...) first.");
        }
        try {
            DataForSerialLettersGenerator.generateAddressData(
                    member.get(), nicknames.get());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Map<String, String> readNicknames(DBConnection dbc) {
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

    public void startSepa() {
        if (dbConnection == null) {
            throw new IllegalStateException(
                    "No connection initialised. Call start(...) first.");
        }

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
                Selection<Member> sel = new Selection<>(member.get());
                sel.start(new Stage());
                List<Member> selectedMember = sel.getSelection();
                double contribution = sel.getContribution();
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
}

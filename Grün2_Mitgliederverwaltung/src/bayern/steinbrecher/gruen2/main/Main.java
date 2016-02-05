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
import bayern.steinbrecher.gruen2.selection.Selection;
import bayern.steinbrecher.gruen2.sepa.Member;
import bayern.steinbrecher.gruen2.sepa.Originator;
import bayern.steinbrecher.gruen2.sepa.SepaPain00800302_XML_Generator;
import bayern.steinbrecher.gruen2.sepaform.SepaForm;
import bayern.steinbrecher.gruen2.serialLetters.DataForSerialLettersGenerator;
import com.jcraft.jsch.JSchException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
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
    private final ExecutorService exserv = Executors.newWorkStealingPool();
    private Future<List<List<String>>> memberNoSepa;
    private Future<List<List<String>>> memberSepa;
    private Future<Map<String, String>> nicknames;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {Login login;
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

            memberNoSepa = exserv.submit(() -> readMemberNoSepa(dbConnection));
            memberSepa = exserv.submit(() -> readMemberSepa(dbConnection));
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

    private List<List<String>> readMemberNoSepa(DBConnection dbc) {
        try {
            return dbc.execQuery(
                    "SELECT Vorname, Nachname, Strasse, Hausnummer, PLZ, Ort, "
                    + "istMaennlich, Titel "
                    + "FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00'");
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private List<List<String>> readMemberSepa(DBConnection dbc) {
        try {
            return dbc.execQuery(
                    "SELECT Vorname, Nachname, IBAN, BIC, MandatErstellt, "
                    + "Mitgliedsnummer, KontoinhaberVorname, "
                    + "KontoinhaberNachname "
                    + "FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00' "
                    + "AND istBeitragsfrei='0'");
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void generateSerialLetterData() {
        if (dbConnection == null) {
            throw new IllegalStateException(
                    "No connection initialised. Call start(...) first.");
        }
        try {
            DataForSerialLettersGenerator.generateAddressData(
                    memberNoSepa.get(), nicknames.get());
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
                List<Member> memberList = generateMemberList(memberSepa.get());
                Selection<Member> sel = new Selection<>(memberList);
                sel.start(new Stage());
                memberList = sel.getSelection();
                double contribution = sel.getContribution();
                SepaPain00800302_XML_Generator.createXMLFile(
                        memberList, contribution, originator,
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

    private List<Member> generateMemberList(List<List<String>> member) {
        int mitgliedsnummerIndex = -1;
        int mandatErstelltIndex = -1;
        int ibanIndex = -1;
        int bicIndex = -1;
        int vornameIndex = -1;
        int nachnameIndex = -1;
        int kontoinhaberVornameIndex = -1;
        int kontoinhaberNachnameIndex = -1;
        for (int i = 0; i < member.get(0).size(); i++) {
            switch (member.get(0).get(i)) {
            case "Mitgliedsnummer":
                mitgliedsnummerIndex = i;
                break;
            case "MandatErstellt":
                mandatErstelltIndex = i;
                break;
            case "IBAN":
                ibanIndex = i;
                break;
            case "BIC":
                bicIndex = i;
                break;
            case "Vorname":
                vornameIndex = i;
                break;
            case "Nachname":
                nachnameIndex = i;
                break;
            case "KontoinhaberVorname":
                kontoinhaberVornameIndex = i;
                break;
            case "KontoinhaberNachname":
                kontoinhaberNachnameIndex = i;
                break;
            default:
                System.out.println(
                        member.get(0).get(i) + " is not needed for member.");
            }
        }

        List<Member> memberList = new LinkedList<>();
        for (List<String> row : member.subList(1, member.size())) {
            String kiN = row.get(kontoinhaberNachnameIndex);
            String nachname = row.get(nachnameIndex);
            String kiV = row.get(kontoinhaberVornameIndex);
            String vorname = row.get(vornameIndex);
            Member m = new Member(
                    Integer.parseInt(row.get(mitgliedsnummerIndex)),
                    row.get(mandatErstelltIndex), row.get(ibanIndex),
                    row.get(bicIndex), nachname, vorname,
                    kiN.isEmpty() ? nachname : kiN,
                    kiV.isEmpty() ? vorname : kiV, false);
//FIXME MandatChanged is not always false.
            memberList.add(m);
        }

        return memberList;
    }
}

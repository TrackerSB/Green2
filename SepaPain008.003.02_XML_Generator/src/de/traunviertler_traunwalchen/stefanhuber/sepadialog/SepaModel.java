package de.traunviertler_traunwalchen.stefanhuber.sepadialog;

import de.traunviertler_traunwalchen.stefanhuber.sepa.Member;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.DatabaseConnection;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.DatabaseConnectorModel;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.ssh.DatabaseConnectorSSH;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.standard.DatabaseConnectorStandard;
import de.traunviertler_traunwalchen.stefanhuber.dataprovider.DataProvider;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import de.traunviertler_traunwalchen.stefanhuber.memberselectiondialog.MemberSelectionModel;

/**
 * @author stefan
 */
public class SepaModel extends Application {

    /**
     * true = SQL over SSH, false = SQL over JDBC.
     */
    private static final boolean SSH_CONNECTION = true;
    private DatabaseConnection databaseConnection;
    private SepaController sepaController;
    private Callable callable = () -> {
        MemberSelectionModel msm = new MemberSelectionModel(
                readMember(), sepaController.getEigeninfo());
        if (databaseConnection != null) {
            databaseConnection.close();
        }
        msm.start(new Stage());
        return null;
    };

    private void showSepaDialog() {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader
                    = new FXMLLoader(getClass().getResource("Sepa.fxml"));
            Parent root = fxmlLoader.load();
            root.getStylesheets().add(DataProvider.getStylesheetPath());
            sepaController = fxmlLoader.getController();
            sepaController.setModel(this);

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Sepa-Eigeninformationen");
            stage.setResizable(false);
            stage.getIcons().add(DataProvider.getIcon());
            stage.setOnHidden(ev -> {
                if (databaseConnection != null) {
                    databaseConnection.close();
                }
            });
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(
                    SepaModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseConnectorModel dbcModel;
        if (SSH_CONNECTION) {
            dbcModel = new DatabaseConnectorSSH();
        } else {
            dbcModel = new DatabaseConnectorStandard();
        }
        dbcModel.setCaller(() -> {
            databaseConnection = dbcModel.getConnection();
            //Wenn null, dann wurde das Fenster per "X" geschlossen
            if (databaseConnection != null) {
                //Sepa-GUI
                showSepaDialog();
            }
            return null;
        });
        dbcModel.start(new Stage());
    }

    public void callCallable() {
        try {
            callable.call();
        } catch (Exception ex) {
            Logger.getLogger(
                    SepaModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Macht die SQL-Abfrage und liefert eine Tabelle der Mitglieder (mit
     * Spalten&uuml;berschriften)
     *
     * @return Eine Tabelle der Mitglieder.
     * @throws SQLException Tritt auf, wenn der SQL-Code kein Ergebnis erzielte.
     */
    public LinkedList<Member> readMember() throws SQLException {
        LinkedList<Member> member = new LinkedList<>();
        databaseConnection.execQuery("SELECT Mitgliedsnummer, Vorname, "
                + "Nachname, IBAN, BIC, MandatErstellt "
                + "FROM Mitglieder "
                + "WHERE AusgetretenSeit = '0000-00-00' "
                + "AND IstBeitragsfrei = 0").stream().forEach(row -> {
                    member.add(new Member(Integer.parseInt(row[0]),
                            row[5], row[4], row[2], row[1], row[3], false));
                    //TODO Prüfen, ob Mandat geändert wurde
                });
        return member;
    }
}

package de.traunviertler_traunwalchen.stefanhuber.databaseconnector.standard;

import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.DatabaseConnectorModel;
import de.traunviertler_traunwalchen.stefanhuber.dataprovider.DataProvider;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Diese Klasse stellt sowohl den Start-Punkt der Applikation als auch das Model
 * (MVC-Pattern) dar.
 *
 * @author Stefan Huber
 */
public class DatabaseConnectorStandard extends DatabaseConnectorModel {

    /**
     * Konstruktor
     *
     * @param caller Das Callable-Objekt legt in seiner
     * <code>call</code>-Methode fest, was getan werden soll, nachdem eine
     * Verbindung mit der Datenbank aufgebaut wurde. WICHTIG: Sicherstellen,
     * dass das Callable-Objekt die Datenbankverbindung schlie&szlig;t!
     */
    public DatabaseConnectorStandard(Callable caller) {
        super(caller);
    }

    /**
     * Standard-Konstruktor. Nach einer erfolgreichen Verbindung mit der
     * Datenbank, wird die Verbindung einfach wieder geschlossen.
     */
    public DatabaseConnectorStandard() {
        super();
    }

    /**
     * start-Methode. Initialisiert die GUI f&uuml;r die Login-Daten der
     * Datenbank.
     *
     * @param primaryStage Das initiale Stage-Objekt
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                    .getResource("DatabaseConnectorViewStandard.fxml"));

            Parent root = fxmlLoader.load();

            ((DatabaseConnectorControllerStandard) fxmlLoader.getController())
                    .setModel(this);

            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Login");
            primaryStage.setResizable(false);
            primaryStage.getIcons().add(DataProvider.getIcon());
            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(DatabaseConnectorModel.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}

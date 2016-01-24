package de.traunviertler_traunwalchen.stefanhuber.databaseconnector;

import java.util.concurrent.Callable;
import javafx.application.Application;

/**
 * @author Stefan Huber
 * @param <T> The return type of the result the set caller should return.
 */
public abstract class DatabaseConnectorModel<T> extends Application {

    private Callable<T> caller;
    private DatabaseConnection connection = null;

    public DatabaseConnectorModel() {
        this.caller = () -> {
            //Falls existent, Datenbankverbindung schließen.
            if (connection != null) {
                connection.close();
            }
            return null;
        };
    }

    public DatabaseConnectorModel(Callable<T> caller) {
        this.caller = caller;
    }

    /**
     * Setzt das neue Callable-Objekt.
     *
     * @param caller Die Methode <code>call()</code> dieses Objektes legt fest,
     * was ausgef&uuml;hrt wird, nachdem eine Verbindung mit der Datenbank
     * hergestellt wurde. WICHTIG: Sicherstellen, dass das Callable-Objekt die
     * Datenbankverbindung schlie&szlig;t!
     */
    public void setCaller(Callable<T> caller) {
        this.caller = caller;
    }

    /**
     * Setzt die neue Verbindung mit der Datenbank, die im Model zu speichern
     * ist.
     *
     * @param con Die neue Verbindung
     */
    public void update(DatabaseConnection con) {
        this.connection = con;
    }

    /**
     * Liefert das Objekt, das die derzeitige Verbindung darstellt.
     *
     * @return Die aktuelle Verbindung
     */
    public DatabaseConnection getConnection() {
        return connection;
    }

    /**
     * Ruft die <code>call</code>-Methode des Callable-Objektes auf und
     * schlie&szlig:t die Datenbankverbindung.
     *
     * @return The result of the set callable.
     * @throws Exception Forwards a thrown exeception of the set callable.
     */
    public T callCallable() throws Exception {
        return caller.call();
    }
}

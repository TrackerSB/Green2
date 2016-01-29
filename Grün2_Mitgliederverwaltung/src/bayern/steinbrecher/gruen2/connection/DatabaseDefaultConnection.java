package bayern.steinbrecher.gruen2.connection;

import com.mysql.jdbc.Connection;
import bayern.steinbrecher.gruen2.databaseconnector.DatabaseConnection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 */
public final class DatabaseDefaultConnection implements DatabaseConnection {

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private Connection connection;

    /**
     * Constructes a new database connection.
     *
     * @param databaseHost The address of the database host.
     * @param databaseUsername The username for the database.
     * @param databasePasswd The password for the database.
     * @param databaseName The name of the database to connect to.
     * @throws SQLException Is thrown if some username, password or address is
     * wrong.
     */
    public DatabaseDefaultConnection(String databaseHost,
            String databaseUsername, String databasePasswd, String databaseName)
            throws SQLException {
        try {
            Class.forName(DRIVER).newInstance();
            connection = (Connection) DriverManager.getConnection(databaseHost
                    + databaseName, databaseUsername, databasePasswd);
            execQuery("SELECT 1");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DatabaseConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkedList<String[]> execQuery(String sqlCode) throws SQLException {
        ResultSet resultset = connection.prepareStatement(sqlCode)
                .executeQuery();
        LinkedList<String[]> result = new LinkedList<>();
        while (resultset.next()) {
            String[] fields = getFieldsOfCurrentRow(resultset);
            result.add(fields);
        }

        return result;
    }

    private String[] getFieldsOfCurrentRow(ResultSet resultset)
            throws SQLException {
        int columncount = resultset.getMetaData().getColumnCount();
        String[] fields = new String[columncount];
        for (int i = 0; i < columncount; i++) {
            fields[i] = resultset.getString(i + 1);
        }
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}

package bayern.steinbrecher.gruen2.connection;

import bayern.steinbrecher.gruen2.exception.AuthException;
import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a connection not using SSH.
 *
 * @author Stefan Huber
 */
public final class DefaultConnection extends DBConnection {

    /**
     * The name of the used driver.
     */
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    /**
     * The protocol used as the beginning of the location of the database.
     */
    private static final String DRIVER_PROTOCOL = "jdbc:mysql://";
    /**
     * The created connection used to execute queries.
     */
    private Connection connection;

    /**
     * Constructes a new database connection.
     *
     * @param databaseHost The address of the database host.
     * @param databaseUsername The username for the database.
     * @param databasePasswd The password for the database.
     * @param databaseName The name of the database to connect to.
     * @throws AuthException Is thrown if some username, password or address is
     * wrong.
     */
    public DefaultConnection(String databaseHost, String databaseUsername,
            String databasePasswd, String databaseName) throws AuthException {
        try {
            Class.forName(DRIVER);
            if (!databaseHost.endsWith("/")) {
                databaseHost += "/";
            }
            connection = (Connection) DriverManager.getConnection(
                    DRIVER_PROTOCOL + databaseHost
                    + databaseName, databaseUsername, databasePasswd);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DefaultConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new AuthException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<String>> execQuery(String sqlCode) throws SQLException {
        ResultSet resultset = connection.prepareStatement(sqlCode)
                .executeQuery();

        List<List<String>> resultTable = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= resultset.getMetaData().getColumnCount(); i++) {
            labels.add(resultset.getMetaData().getColumnLabel(i));
        }
        resultTable.add(labels);

        while (resultset.next()) {
            List<String> columns = new ArrayList<>();
            for (String l : labels) {
                columns.add(resultset.getString(l));
            }
            resultTable.add(columns);
        }

        return resultTable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execUpdate(String sqlCode) throws SQLException {
        connection.prepareStatement(sqlCode).executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}

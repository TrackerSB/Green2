package bayern.steinbrecher.gruen2.connection;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 */
public final class DefaultConnection implements DBConnection {

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
    public DefaultConnection(String databaseHost, String databaseUsername,
            String databasePasswd, String databaseName) throws SQLException {
        try {
            Class.forName(DRIVER).newInstance();
            connection = (Connection) DriverManager.getConnection(databaseHost
                    + databaseName, databaseUsername, databasePasswd);
            execQuery("SELECT 1"); //TODO SELECT 1 notwendig?
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String>> execQuery(String sqlCode) throws SQLException {
        ResultSet resultset = connection.prepareStatement(sqlCode)
                .executeQuery();

        Map<String, List<String>> mappedResult = new HashMap<>();
        for (int i = 0; i < resultset.getMetaData().getColumnCount(); i++) {
            mappedResult.put(resultset.getMetaData().getColumnLabel(i),
                    new LinkedList<>());
        }

        while (resultset.next()) {
            for(String key: mappedResult.keySet()){
                mappedResult.get(key).add(resultset.getString(key));
            }
        }

        return mappedResult;
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

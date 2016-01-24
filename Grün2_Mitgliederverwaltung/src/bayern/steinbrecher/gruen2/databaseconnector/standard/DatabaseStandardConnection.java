package bayern.steinbrecher.gruen2.databaseconnector.standard;

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
public final class DatabaseStandardConnection implements DatabaseConnection {

    private static final String DATABASE_HOST = "jdbc:mysql://localhost:3306/",
            DATABASE = "frauenverein",
            DRIVER = "com.mysql.jdbc.Driver";
    private Connection connection;

    public DatabaseStandardConnection(String databaseUsername,
            String databasePasswd) throws SQLException {
        try {
            Class.forName(DRIVER).newInstance();
            connection = (Connection) DriverManager.getConnection(
                    DATABASE_HOST + DATABASE, databaseUsername, databasePasswd);
            execQuery("SELECT 1");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DatabaseStandardConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

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

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseStandardConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
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
}

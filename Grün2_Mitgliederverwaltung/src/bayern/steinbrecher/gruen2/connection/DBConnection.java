package bayern.steinbrecher.gruen2.connection;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a database connection.
 *
 * @author Stefan Huber
 */
public abstract class DBConnection implements AutoCloseable {

    /**
     * Closes this connection.
     */
    @Override
    public abstract void close();

    /**
     * Executes a query and returns the result.
     *
     * @param sqlCode The sql code.
     * @return Table containing the results AND the headings of each column.
     * First dimension rows; second columns.
     * @throws SQLException Thrown if the sql code is invalid.
     */
    public abstract List<List<String>> execQuery(String sqlCode)
            throws SQLException;

    /**
     * Checks whether the given table of the configured database contains a
     * specific column.
     *
     * @param table The name of the table to search for the column.
     * @param column The column name to search for.
     * @return {@code true} only if the given table contains the given column.
     */
    public boolean checkColumn(String table, String column) {
        //FIXME Try to find other solution not using SQLException.
        try {
            execQuery("SELECT " + column + " FROM " + table);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}

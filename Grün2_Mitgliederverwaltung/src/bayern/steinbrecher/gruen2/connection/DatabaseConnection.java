package bayern.steinbrecher.gruen2.connection;

import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Represents a database connection.
 *
 * @author Stefan Huber
 */
public interface DatabaseConnection extends AutoCloseable {

    /**
     * Closes this connection.
     */
    @Override
    void close();

    /**
     * Executes a query and returns the result.
     *
     * @param sqlCode The sql code.
     * @return Table containing the results.
     * @throws SQLException Thrown if the sql code is invalid.
     */
    LinkedList<String[]> execQuery(String sqlCode) throws SQLException;
}

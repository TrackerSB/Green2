package bayern.steinbrecher.gruen2.connection;

import java.sql.SQLException;
import java.util.List;

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
    List<String[]> execQuery(String sqlCode) throws SQLException;
}

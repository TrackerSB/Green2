/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.connection;

import bayern.steinbrecher.green2.exception.AuthException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
     * @param databaseHost     The address of the database host.
     * @param databaseUsername The username for the database.
     * @param databasePasswd   The password for the database.
     * @param databaseName     The name of the database to connect to.
     * @throws AuthException        Is thrown if some username, password or address is
     *                              wrong.
     * @throws UnknownHostException Is thrown if the host is not reachable.
     */
    public DefaultConnection(String databaseHost, String databaseUsername, String databasePasswd, String databaseName)
            throws AuthException, UnknownHostException {
        try {
            Class.forName(DRIVER);
            if (!databaseHost.endsWith("/")) {
                databaseHost += "/";
            }
            connection = DriverManager.getConnection(DRIVER_PROTOCOL + databaseHost + databaseName
                            + "?verifyServerCertificate=false" + "&useSSL=true&zeroDateTimeBehavior=convertToNull",
                    databaseUsername, databasePasswd);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DefaultConnection.class.getName()).log(Level.SEVERE, null, ex);
            if (ex instanceof CommunicationsException) {
                throw new UnknownHostException(ex.getMessage());
            } else {
                throw new AuthException();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<String>> execQuery(String sqlCode) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCode)) {
            ResultSet resultset = preparedStatement.executeQuery();

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execUpdate(String sqlCode) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCode)) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.WARNING, null, ex);
        }
    }
}

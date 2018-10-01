/*
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.connection;

import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.ProfileSettings;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a connection not using SSH.
 *
 * @author Stefan Huber
 */
public final class SimpleConnection extends DBConnection {

    private static final Logger LOGGER = Logger.getLogger(SimpleConnection.class.getName());
    /**
     * The protocols of the supported databases.
     */
    private static final Map<SupportedDatabases, String> DRIVER_PROTOCOLS
            = Map.of(SupportedDatabases.MY_SQL, "jdbc:mysql://");

    /**
     * The created connection used to execute queries.
     */
    private Connection connection;

    /**
     * Constructs a new database connection.
     *
     * @param databaseHost The address of the database host.
     * @param databasePort The port of the database.
     * @param databaseUsername The username for the database.
     * @param databasePasswd The password for the database.
     * @param databaseName The name of the database to connect to.
     * @throws AuthException Is thrown if some username, password or address is wrong.
     * @throws UnknownHostException Is thrown if the host is not reachable.
     */
    public SimpleConnection(String databaseHost, int databasePort, String databaseUsername, String databasePasswd,
            String databaseName)
            throws AuthException, UnknownHostException {
        super();
        String databaseHostPrefix = databaseHost;
        if (databaseHostPrefix.endsWith("/")) {
            databaseHostPrefix = databaseHostPrefix.substring(0, databaseHostPrefix.length() - 1);
        }
        String databaseAddress = databaseHostPrefix + ":" + databasePort + "/";
        SupportedDatabases dbms = EnvironmentHandler.getProfile().get(ProfileSettings.DBMS);
        try {
            connection = DriverManager.getConnection(DRIVER_PROTOCOLS.get(dbms) + databaseAddress
                    + databaseName + "?verifyServerCertificate=false&useSSL=true&zeroDateTimeBehavior=convertToNull"
                    + "&serverTimezone=UTC",
                    databaseUsername, databasePasswd);
        } catch (CommunicationsException ex) {
            throw new UnknownHostException(ex.getMessage()); //NOPMD - UnknownHostException does not accept a cause.
        } catch (SQLException ex) {
            throw new AuthException("The authentication to the database failed.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<String>> execQuery(String sqlCode) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCode);
                ResultSet resultset = preparedStatement.executeQuery()) {
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
            LOGGER.log(Level.WARNING, null, ex);
        }
    }
}

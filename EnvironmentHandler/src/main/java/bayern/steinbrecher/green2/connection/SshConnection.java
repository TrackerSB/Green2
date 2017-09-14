/* 
 * Copyright (C) 2017 Stefan Huber
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

import bayern.steinbrecher.green2.utility.IOStreamUtility;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents database connections over SSH.
 *
 * @author Stefan Huber
 */
public final class SshConnection extends DBConnection {

    /**
     * The default port for ssh.
     */
    public static final int DEFAULT_SSH_PORT = 22;
    private static final Map<SupportedDatabase, String> COMMANDS = new HashMap<SupportedDatabase, String>() {
        {
            put(SupportedDatabase.MY_SQL, "mysql");
        }
    };
    private final Map<SupportedDatabase, Function<String, String>> sqlCommands = new HashMap<>();
    /**
     * The name of the database.
     */
    private final String databaseName;
    /**
     * The address of the host of the database (without protocol).
     */
    private final String databaseHost;
    /**
     * The username used to login into the database.
     */
    private final String databaseUsername;
    /**
     * The password used to login into the database.
     */
    private final String databasePasswd;
    /**
     * The ssh session used to connect to the database over a secure channel.
     */
    private final Session sshSession;
    /**
     * The charset used by ssh response.
     */
    private final Charset charset;

    static {
        //Configurations which are applieid to all sessions.
        JSch.setConfig("kex", "diffie-hellman-group1-sha1, diffie-hellman-group-exchange-sha1");
        JSch.setConfig("server_host_key", "ssh-rsa,ssh-dss");
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch.setConfig("lang.s2c", "");
        JSch.setConfig("cipher.s2c", "3des-cbc,blowfish-cbc");
        JSch.setConfig("cipher.c2s", "3des-cbc,blowfish-cbc");
        JSch.setConfig("mac.s2c", "hmac-sha1,hmac-sha1-96,hmac-md5,hmac-md5-96");
        JSch.setConfig("mac.c2s", "hmac-sha1,hmac-sha1-96,hmac-md5,hmac-md5-96");
    }

    /**
     * Constructs a new database connection over SSH.
     *
     * @param sshHost The address of the ssh host.
     * @param sshUsername The username for the ssh connection.
     * @param sshPassword The password for the ssh connection.
     * @param databaseHost The address of the database host (without protocol).
     * @param databaseUsername The username for the database.
     * @param databasePasswd The password for the database.
     * @param databaseName The name of the database to connect to.
     * @param charset The charset used by ssh response.
     * @throws AuthException Thrown if some of username, password or host is wrong or not reachable.
     * @throws UnknownHostException Is thrown if the host is not reachable.
     * @throws UnsupportedDatabaseException Thrown only if no supported database was found.
     * @see SupportedDatabase
     */
    public SshConnection(String sshHost, String sshUsername, String sshPassword, String databaseHost,
            String databaseUsername, String databasePasswd, String databaseName, Charset charset)
            throws AuthException, UnknownHostException, UnsupportedDatabaseException {
        this.databaseHost = databaseHost;
        this.databaseUsername = databaseUsername;
        this.databasePasswd = databasePasswd;
        this.databaseName = databaseName;
        this.sshSession = createSshSession(sshHost, sshUsername, sshPassword);
        this.charset = charset;

        sqlCommands.put(SupportedDatabase.MY_SQL, query -> COMMANDS.get(SupportedDatabase.MY_SQL)
                + " -u" + databaseUsername
                + " -p" + databasePasswd
                + " -h" + databaseHost
                + " -e'" + query + "' " + databaseName);

        try {
            this.sshSession.connect();

            String result;
            try {
                result = execCommand("command -v "
                        + COMMANDS.get(DATABASE.getValue()) + " >/dev/null 2>&1 || { echo \"Not installed\"; }");
            } catch (CommandException ex) {
                throw new UnsupportedDatabaseException(
                        "The command to check existence of the correct database failed.", ex);
            }
            if (result.contains("Not installed")) {
                throw new UnsupportedDatabaseException("The configured database is not supported by the SSH host.");
            }

            //Check sql-host connection
            execQuery("SELECT 1");
        } catch (SQLException | JSchException ex) {
            close();
            if (ex instanceof JSchException && !ex.getMessage().contains("Auth")) {
                throw new UnknownHostException(ex.getMessage());
            } else {
                throw new AuthException("Auth fail", ex);
            }
        }
    }

    /**
     * Creates a ssh session.
     *
     * @param sshHost The address of the host of the ssh service to connect to.
     * @param sshUsername The username used to login.
     * @param sshPassword The password used to login.
     * @return A Session representing the ssh connection.
     * @throws AuthException Thrown if some of username, password or host is wrong or unreachable.
     */
    private Session createSshSession(String sshHost, String sshUsername, String sshPassword)
            throws AuthException {
        try {
            Session session = new JSch().getSession(sshUsername, sshHost, DEFAULT_SSH_PORT);
            session.setPassword(sshPassword);
            session.setDaemonThread(true);
            return session;
        } catch (JSchException ex) {
            throw new AuthException();
        }
    }

    private String execCommand(String command) throws JSchException, CommandException {
        try {
            ChannelExec channel = (ChannelExec) sshSession.openChannel("exec");
            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            channel.setErrStream(errStream);
            channel.setInputStream(null);
            channel.setCommand(command);
            InputStream in = channel.getInputStream();

            channel.connect();

            String result = IOStreamUtility.readAll(in, charset);

            String errorMessage = errStream.toString();
            if (errorMessage.toLowerCase().contains("error")) {
                throw new CommandException("The given command returned following error:\n" + errorMessage);
            }

            channel.disconnect();

            return result;
        } catch (IOException ex) {
            Logger.getLogger(SshConnection.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<String>> execQuery(String sqlCode) throws SQLException {
        try {
            String result = execCommand(sqlCommands.get(DATABASE.getValue()).apply(sqlCode));

            String[] rows = result.split("\n");
            List<List<String>> resultTable = Arrays.stream(rows)
                    .map(row -> splitUp(row, '\t'))
                    .map(rowFields -> rowFields.stream()
                    .map(f -> f.equals("0000-00-00") ? null : f) //TODO Remove legacy check 0000-00-00
                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            return resultTable;
        } catch (JSchException | CommandException ex) {
            throw new SQLException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execUpdate(String sqlCode) throws SQLException {
        try {
            Channel channel = sshSession.openChannel("exec");
            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            ((ChannelExec) channel).setErrStream(errStream);
            ((ChannelExec) channel).setCommand("mysql"
                    + " -u" + databaseUsername
                    + " -p" + databasePasswd
                    + " -h" + databaseHost
                    + " -e'" + sqlCode + "' " + databaseName);

            channel.connect();

            if (errStream.size() > 0) {
                throw new SQLException("Invalid SQL-Code");
            }

            channel.disconnect();
        } catch (JSchException ex) {
            Logger.getLogger(SshConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Splits up a string on the given regex. The regex itself won´t show up in any element of the returned list. When
     * two or more regex are right in a row an empty {@link String} will be added. (This is the main difference to
     * {@code String.split(...)})
     */
    private List<String> splitUp(String row, char regex) {
        List<String> columns = new ArrayList<>();
        StringBuilder lastCol = new StringBuilder();
        for (char c : row.toCharArray()) {
            if (c == regex) {
                columns.add(lastCol.toString());
                lastCol.setLength(0);
            } else {
                lastCol.append(c);
            }
        }
        columns.add(lastCol.toString());

        return columns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.sshSession.disconnect();
    }
}

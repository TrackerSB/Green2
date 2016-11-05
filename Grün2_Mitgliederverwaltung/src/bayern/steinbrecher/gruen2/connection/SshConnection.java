/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.connection;

import bayern.steinbrecher.gruen2.exception.AuthException;
import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    static {
        //Configurations which are applieid to all sessions.
        JSch.setConfig("kex", "diffie-hellman-group1-sha1,"
                + "diffie-hellman-group-exchange-sha1");
        JSch.setConfig("server_host_key", "ssh-rsa,ssh-dss");
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch.setConfig("lang.s2c", "");
        JSch.setConfig("cipher.s2c", "3des-cbc,blowfish-cbc");
        JSch.setConfig("cipher.c2s", "3des-cbc,blowfish-cbc");
        JSch.setConfig("mac.s2c",
                "hmac-sha1,hmac-sha1-96,hmac-md5,hmac-md5-96");
        JSch.setConfig("mac.c2s",
                "hmac-sha1,hmac-sha1-96,hmac-md5,hmac-md5-96");
    }

    /**
     * Constructes a new database connection over SSH.
     *
     * @param sshHost The address of the ssh host.
     * @param sshUsername The username for the ssh connection.
     * @param sshPassword The password for the ssh connection.
     * @param databaseHost The address of the database host (without protocol).
     * @param databaseUsername The username for the database.
     * @param databasePasswd The password for the database.
     * @param databaseName The name of the database to connect to.
     * @throws AuthException Thrown if some of username, password or host is
     * wrong or not reachable.
     * @throws UnknownHostException Is thrown if the host is not reachable.
     */
    public SshConnection(String sshHost, String sshUsername,
            String sshPassword, String databaseHost, String databaseUsername,
            String databasePasswd, String databaseName)
            throws AuthException, UnknownHostException {
        this.databaseHost = databaseHost;
        this.databaseUsername = databaseUsername;
        this.databasePasswd = databasePasswd;
        this.databaseName = databaseName;
        this.sshSession = createSshSession(sshHost, sshUsername, sshPassword);

        try {
            this.sshSession.connect();

            //Check authentification
            execQuery("SELECT 1");
        } catch (SQLException | JSchException ex) {
            close();
            if (ex instanceof JSchException
                    && !ex.getMessage().contains("Auth")) {
                throw new UnknownHostException(ex.getMessage());
            } else {
                throw new AuthException();
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
     * @throws JSchException Thrown if some of username, password or host is
     * wrong or unreachable.
     */
    private Session createSshSession(String sshHost, String sshUsername,
            String sshPassword)
            throws AuthException {
        try {
            Session session = new JSch()
                    .getSession(sshUsername, sshHost, DEFAULT_SSH_PORT);
            session.setPassword(sshPassword);
            session.setDaemonThread(true);
            return session;
        } catch (JSchException ex) {
            throw new AuthException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<String>> execQuery(String sqlCode) throws SQLException {
        try {
            Channel channel = sshSession.openChannel("exec");
            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            ((ChannelExec) channel).setErrStream(errStream);
            ((ChannelExec) channel).setCommand("mysql"
                    + " -u" + databaseUsername
                    + " -p" + databasePasswd
                    + " -h" + databaseHost
                    + " -e'" + sqlCode + "' " + databaseName);
            String result = IOStreamUtility.readAll(channel.getInputStream());

            channel.connect();

            String errorMessage = errStream.toString();
            if (result == null
                    || errorMessage.toLowerCase().contains("error")) {
                throw new SQLException(errorMessage);
            }
            String[] rows = result.split("\n");
            List<List<String>> resultTable = Arrays.stream(rows)
                    .parallel()
                    .map(row -> splitUp(row, '\t'))
                    .collect(Collectors.toList());

            channel.disconnect();

            return resultTable;
        } catch (JSchException | IOException ex) {
            Logger.getLogger(SshConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
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
            Logger.getLogger(SshConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Splits up a string on the given regex. The regex itself won´t show up in
     * any element of the returned list. When two or more regex are right in a
     * row an empty {@code String} will be added. (This is the main difference
     * to {@code String.split(...)})
     *
     * @param row
     * @param regex
     * @return
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

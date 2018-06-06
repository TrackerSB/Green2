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
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private static final Map<SupportedDatabases, String> COMMANDS = new HashMap<SupportedDatabases, String>() {
        {
            put(SupportedDatabases.MY_SQL, "mysql");
        }
    };
    private final Map<SupportedDatabases, Function<String, String>> sqlCommands = new HashMap<>();
    /**
     * The name of the database.
     */
    private final String databaseName;
    /**
     * The address of the host of the database (without protocol).
     */
    private final String databaseHost;
    /**
     * The port of the database.
     */
    private final int databasePort;
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
        //Configurations which are applied to all sessions.
        JSch.setConfig("kex", "diffie-hellman-group-exchange-sha1, diffie-hellman-group1-sha1, "
                + "diffie-hellman-group14-sha1, diffie-hellman-group-exchange-sha256, ecdh-sha2-nistp256, "
                + "ecdh-sha2-nistp384, ecdh-sha2-nistp521");
        JSch.setConfig("server_host_key", "ssh-dss, ssh-rsa, ecdsa-sha2-nistp256, ecdsa-sha2-nistp384, "
                + "ecdsa-sha2-nistp521");
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch.setConfig("lang.s2c", "");
        JSch.setConfig("lang.c2s", "");
        JSch.setConfig("cipher.s2c", "blowfish-cbc, 3des-cbc, aes128-cbc, aes192-cbc, aes256-cbc, aes128-ctr, "
                + "aes192-ctr, aes256-ctr, 3des-ctr, arcfour, arcfour128, arcfour256");
        JSch.setConfig("cipher.c2s", "blowfish-cbc, 3des-cbc, aes128-cbc, aes192-cbc, aes256-cbc, aes128-ctr, "
                + "aes192-ctr, aes256-ctr, 3des-ctr, arcfour, arcfour128, arcfour256");
        JSch.setConfig("mac.s2c", "hmac-md5, hmac-sha1, hmac-md5-96, hmac-sha1-96");
        JSch.setConfig("mac.c2s", "hmac-md5, hmac-sha1, hmac-md5-96, hmac-sha1-96");
    }

    /**
     * Constructs a new database connection over SSH.
     *
     * @param sshHost The address of the ssh host.
     * @param sshUsername The username for the ssh connection.
     * @param sshPassword The password for the ssh connection.
     * @param databaseHost The address of the database host (without protocol).
     * @param databasePort The port of the database.
     * @param databaseUsername The username for the database.
     * @param databasePasswd The password for the database.
     * @param databaseName The name of the database to connect to.
     * @param charset The charset used by ssh response.
     * @throws AuthException Thrown if some of username, password or host is wrong or not reachable.
     * @throws UnknownHostException Is thrown if the host is not reachable.
     * @throws UnsupportedDatabaseException Thrown only if no supported database was found.
     * @see SupportedDatabases
     */
    public SshConnection(String sshHost, String sshUsername, String sshPassword, String databaseHost, int databasePort,
            String databaseUsername, String databasePasswd, String databaseName, Charset charset)
            throws AuthException, UnknownHostException, UnsupportedDatabaseException {
        this.databaseHost = databaseHost;
        this.databasePort = databasePort;
        this.databaseUsername = databaseUsername;
        this.databasePasswd = databasePasswd;
        this.databaseName = databaseName;
        this.sshSession = createSshSession(sshHost, sshUsername, sshPassword);
        this.charset = charset;

        //NOTE The echo command is needed for handling UTF8 chars on non UTF8 terminals.
        sqlCommands.put(SupportedDatabases.MY_SQL, query -> "echo -e '" + escapeSingleQuotes(replaceNonAscii(query))
                + "' | "
                + COMMANDS.get(SupportedDatabases.MY_SQL)
                + " --default-character-set=utf8"
                + " -u" + databaseUsername
                + " -p" + databasePasswd
                + " -h" + databaseHost
                + " -P" + databasePort
                + " " + databaseName);

        try {
            this.sshSession.connect();

            String result;
            SupportedDatabases dbms = EnvironmentHandler.getProfile().get(ProfileSettings.DBMS);
            try {
                result = execCommand(
                        "command -v " + COMMANDS.get(dbms) + " >/dev/null 2>&1 || { echo \"Not installed\"; }");
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
     * Escapes every single quote in such way that the resulting {@link String} can be inserted between single quotes.
     *
     * @param nonEscaped The {@link String} whose single quotes to escape.
     * @return The {@link String} who can be quoted in single quotes itself.
     */
    private static String escapeSingleQuotes(String nonEscaped) {
        return nonEscaped.replace("'", "'\"'\"'");
    }

    private String replaceNonAscii(String nonAscii) {
        StringBuilder ascii = new StringBuilder();
        nonAscii.chars()
                .forEach(codePoint -> {
                    Character character = (char) codePoint;
                    if (codePoint > 31 && codePoint < 127) { //32-126 is the printable ascii range
                        ascii.append(character);
                    } else {
                        byte[] bytes = String.valueOf(character).getBytes(StandardCharsets.UTF_8);
                        for (byte utf8byte : bytes) {
                            int toConvert = utf8byte < 0 ? utf8byte + 256 : utf8byte;
                            ascii.append("\\x")
                                    .append(Integer.toHexString(toConvert));
                        }
                    }
                });
        return ascii.toString();
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

            String errorMessage = errStream.toString(charset.name());
            if (errorMessage.toLowerCase(Locale.ROOT).contains("error")) {
                throw new CommandException("The given command returned following error:\n" + errorMessage);
            }

            channel.disconnect();

            return result;
        } catch (IOException ex) {
            Logger.getLogger(SshConnection.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private String generateQueryCommand(String sqlCode) {
        SupportedDatabases dbms = EnvironmentHandler.getProfile().get(ProfileSettings.DBMS);
        return sqlCommands.get(dbms).apply(sqlCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<String>> execQuery(String sqlCode) throws SQLException {
        String result;
        try {
            result = execCommand(generateQueryCommand(sqlCode));
        } catch (JSchException | CommandException ex) {
            throw new SQLException(ex);
        }
        String[] rows = result.split("\n");
        List<List<String>> resultTable = Arrays.stream(rows)
                .map(row -> splitUp(row, '\t'))
                .map(rowFields -> rowFields.stream()
                .map(f -> f.equals("0000-00-00") ? null : f) //TODO Remove legacy check 0000-00-00
                .collect(Collectors.toList()))
                .collect(Collectors.toList());

        return resultTable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execUpdate(String sqlCode) throws SQLException {
        try {
            execCommand(generateQueryCommand(sqlCode));
        } catch (JSchException | CommandException ex) {
            throw new SQLException(ex);
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

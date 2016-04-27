package bayern.steinbrecher.gruen2.connection;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
     * @throws JSchException Thrown if some of username, password or host is
     * wrong or not reachable.
     */
    public SshConnection(String sshHost, String sshUsername,
            String sshPassword, String databaseHost, String databaseUsername,
            String databasePasswd, String databaseName)
            throws JSchException {
        this.databaseHost = databaseHost;
        this.databaseUsername = databaseUsername;
        this.databasePasswd = databasePasswd;
        this.databaseName = databaseName;
        this.sshSession = createSshSession(sshHost, sshUsername, sshPassword);
        this.sshSession.connect();

        try {
            //Check authentification
            execQuery("SELECT 1");
        } catch (SQLException ex) {
            Logger.getLogger(SshConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            close();
            throw new JSchException("Auth fail");
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
            throws JSchException {
        Session session = new JSch()
                .getSession(sshUsername, sshHost, DEFAULT_SSH_PORT);
        session.setPassword(sshPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
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
            BufferedInputStream in
                    = new BufferedInputStream(channel.getInputStream());

            channel.connect();

            String result = readResult(in);
            if (result == null || errStream.size() > 0) {
                throw new SQLException("Invalid SQL-Code");
            }
            String[] rows = result.split("\n");
            List<List<String>> resultTable = Arrays.stream(rows)
                    .limit(rows.length - 1) //Remove EOF
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
     * Reads the result of a SQL-Query bytewise.
     *
     * @param in The inputstream to read from.
     * @return The result as one single String.
     * @throws IOException If the inputstream has been closed or some other
     * I/O-Exception is thrown.
     */
    private String readResult(BufferedInputStream in) throws IOException {
        StringBuilder output = new StringBuilder();
        int nextByte;
        do {
            nextByte = in.read();
            output.append((char) nextByte);
        } while (nextByte != -1);

        return output.toString();
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

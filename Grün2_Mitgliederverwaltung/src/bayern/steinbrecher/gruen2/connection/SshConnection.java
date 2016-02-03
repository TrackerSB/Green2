package bayern.steinbrecher.gruen2.connection;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents database connections over SSH.
 *
 * @author Stefan Huber
 */
public final class SshConnection implements DBConnection {

    private Session sshSession;
    public static final int DEFAULT_SSH_PORT = 22;
    private final String databaseName,
            databaseHost,
            databaseUsername,
            databasePasswd;

    /**
     * Constructes a new database connection over SSH.
     *
     * @param sshHost The address of the ssh host.
     * @param sshUsername The username for the ssh connection.
     * @param sshPassword The password for the ssh connection.
     * @param databaseHost The address of the database host.
     * @param databaseUsername The username for the database.
     * @param databasePasswd The password for the database.
     * @param databaseName The name of the database to connect to.
     * @throws JSchException Is thrown if some of username, password or address
     * is wrong or not reachable.
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
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public List<List<String>> execQuery(String sqlCode)
            throws SQLException {
        try {
            Channel channel = sshSession.openChannel("exec");
            ((ChannelExec) channel).setErrStream(System.err);
            ((ChannelExec) channel).setCommand("mysql"
                    + " -u" + databaseUsername
                    + " -p" + databasePasswd
                    + " -h" + databaseHost
                    + " -e'" + sqlCode + "' " + databaseName);
            BufferedInputStream in
                    = new BufferedInputStream(channel.getInputStream());

            channel.connect();

            String result = readResult(in);
            if (result == null) {
                throw new SQLException("Invalid SQL-Code");
            }
            String[] rows = result.split("\n");
            List<List<String>> resultTable = Arrays.stream(rows)
                    .limit(rows.length - 1) //Remove EOF
                    .parallel()
                    .map(row -> row.split("\t"))
                    .map(Arrays::asList)
                    .collect(Collectors.toList());

            channel.disconnect();

            return resultTable;
        } catch (JSchException | IOException ex) {
            Logger.getLogger(SshConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private String readResult(BufferedInputStream in) throws IOException {
        StringBuilder output = new StringBuilder();
        int nextByte;
        do {
            nextByte = in.read();
            output.append((char) nextByte);
        } while (nextByte != -1);

        return output.toString();
    }

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
    public void close() {
        this.sshSession.disconnect();
    }
}

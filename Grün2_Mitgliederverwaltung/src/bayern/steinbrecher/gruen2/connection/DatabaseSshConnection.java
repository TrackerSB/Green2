package bayern.steinbrecher.gruen2.connection;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import bayern.steinbrecher.gruen2.databaseconnector.DatabaseConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents database connections over SSH.
 *
 * @author Stefan Huber
 */
public final class DatabaseSshConnection implements DatabaseConnection {

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
     * @throws SQLException Is thrown if some username, password or address is
     * wrong.
     */
    public DatabaseSshConnection(String sshHost, String sshUsername,
            String sshPassword, String databaseHost, String databaseUsername,
            String databasePasswd, String databaseName)
            throws SQLException {
        this.databaseHost = databaseHost;
        this.databaseUsername = databaseUsername;
        this.databasePasswd = databasePasswd;
        this.databaseName = databaseName;
        try {
            this.sshSession
                    = createSshSession(sshHost, sshUsername, sshPassword);
            this.sshSession.connect();
            //Check correctness of database-login-data
            execQuery("SELECT 1");
        } catch (JSchException ex) {
            Logger.getLogger(DatabaseSshConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new SQLException("username or password didn´t work or "
                    + "address unreachable");
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public LinkedList<String[]> execQuery(String sqlCode) throws SQLException {
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
            //FIXME Eine bessere Lösung als die Sleep-while-Schleife finden
            while (!channel.isEOF()) {
                Thread.sleep(200);
            }

            String result = readResult(in);
            if (result == null) {
                throw new SQLException("Invalid SQL-Code");
            }
            LinkedList<String[]> formattedResult = new LinkedList<>();
            //Erste Zeile enthält Spaltenüberschriften
            Arrays.stream(result.split("\n")).skip(1).forEach(row -> {
                formattedResult.add(row.split("\t"));
            });
            //Letzte Zeile enth&auml;lt Schlusszeichen
            formattedResult.remove(formattedResult.size() - 1);

            channel.disconnect();
            return formattedResult;
        } catch (JSchException | IOException | InterruptedException ex) {
            Logger.getLogger(DatabaseSshConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private String readResult(BufferedInputStream in) throws IOException {
        if (in.available() <= 0) {
            return null;
        }

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

package bayern.steinbrecher.gruen2.databaseconnector.ssh;

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
 * Diese Klasse erzeugt und verwaltet Verbindungen zur Datenbank des GTEV
 * D´Traunviertler Traunwalchen e.V.
 *
 * @author Stefan Huber
 */
public final class DatabaseSshConnection implements DatabaseConnection {

    private Session sshSession;
    private static final int SSH_PORT = 22;
    private final String databaseName,
            databaseHost,
            sshHost;
    private final String databaseUsername,
            databasePasswd;

    /**
     * Eine neue Verbindung zur GTEV-Datenbank
     *
     * @param sshHost Die Hostadresse der SSH-Verbinding
     * @param sshUsername Der Nutzername f&uuml;r den SSH-Zugang
     * @param sshPassword Das Passwort f&uuml;r den SSH-Zugang
     * @param databaseUsername Der Nutzername f&uuml;r die Datenbank
     * @param databaseHost Die Hostadresse der Datenbank
     * @param databasePasswd Das Passwort f&uuml;r die Datenbank
     * @param databaseName Der Name der Datenbank
     * @throws SQLException Tritt auf, wenn Nutzername oder Passwort falsch
     * sind.
     */
    public DatabaseSshConnection(String sshHost, String sshUsername,
            String sshPassword, String databaseHost, String databaseUsername,
            String databasePasswd, String databaseName)
            throws SQLException {
        this.sshHost = sshHost;
        this.databaseHost = databaseHost;
        this.databaseUsername = databaseUsername;
        this.databasePasswd = databasePasswd;
        this.databaseName = databaseName;
        try {
            this.sshSession = createSshSession(sshUsername, sshPassword);
            this.sshSession.connect();
            //Check correctness of database-login-data
            execQuery("SELECT 1");
        } catch (JSchException ex) {
            Logger.getLogger(DatabaseSshConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new SQLException("username or password didn´t work");
        }
    }

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

    private Session createSshSession(String sshUsername, String sshPassword)
            throws JSchException {
        Session session = new JSch()
                .getSession(sshUsername, sshHost, SSH_PORT);
        session.setPassword(sshPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    @Override
    public void close() {
        this.sshSession.disconnect();
    }
}

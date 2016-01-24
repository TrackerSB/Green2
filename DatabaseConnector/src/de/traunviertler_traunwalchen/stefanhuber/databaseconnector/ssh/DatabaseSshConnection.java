package de.traunviertler_traunwalchen.stefanhuber.databaseconnector.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.DatabaseConnection;
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
    private static final String DATABASE = "traunviertler_t",
            DATABASE_HOST = "traunviertler-traunwalchen.de.mysql",
            SSH_HOST = "ssh.traunviertler-traunwalchen.de";
    private final String databaseUsername,
            databasePasswd;

    /**
     * Eine neue Verbindung zur GTEV-Datenbank
     *
     * @param sshUsername Der Nutzername f&uuml;r den SSH-Zugang
     * @param sshPassword Das Passwort f&uuml;r den SSH-Zugang
     * @param databaseUsername Der Nutzername f&uuml;r die Datenbank
     * @param databasePasswd Das Passwort f&uuml;r die Datenbank
     * @throws SQLException Tritt auf, wenn Nutzername oder Passwort falsch
     * sind.
     */
    public DatabaseSshConnection(String sshUsername, String sshPassword,
            String databaseUsername, String databasePasswd)
            throws SQLException {
        this.databaseUsername = databaseUsername;
        this.databasePasswd = databasePasswd;
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
            ((ChannelExec) channel).setCommand("mysql -u"
                    + this.databaseUsername + " -p" + this.databasePasswd
                    + " -h" + DATABASE_HOST + " -e'" + sqlCode + "' "
                    + DATABASE);
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
                .getSession(sshUsername, SSH_HOST, SSH_PORT);
        session.setPassword(sshPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    @Override
    public void close() {
        this.sshSession.disconnect();
    }
}

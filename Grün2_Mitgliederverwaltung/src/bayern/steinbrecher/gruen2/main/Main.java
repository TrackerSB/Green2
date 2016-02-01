package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.connection.SshConnection;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.login.ssh.SshLogin;
import bayern.steinbrecher.gruen2.serialLetters.DataForSerialLettersGenerator;
import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Stefan Huber
 */
public class Main extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Login l = new SshLogin();
        l.start(new Stage());
        Map<LoginKey, String> loginInfos = l.getLoginInformation();
        DBConnection con = new SshConnection(
                DataProvider.getOrDefault(ConfigKey.SSH_HOST, "localhost"),
                loginInfos.get(LoginKey.SSH_USERNAME),
                loginInfos.get(LoginKey.SSH_PASSWORD),
                DataProvider.getOrDefault(ConfigKey.DATABASE_HOST, "localhost"),
                loginInfos.get(LoginKey.DATABASE_USERNAME),
                loginInfos.get(LoginKey.DATABASE_PASSWORD),
                DataProvider.getOrDefault(ConfigKey.DATABASE_NAME, "dbname"));
        DataForSerialLettersGenerator.generateAddressData(con);
    }
}

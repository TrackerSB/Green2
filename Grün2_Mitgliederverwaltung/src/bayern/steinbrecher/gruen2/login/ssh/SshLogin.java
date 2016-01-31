package bayern.steinbrecher.gruen2.login.ssh;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.login.LoginController;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a login with SSH.
 *
 * @author Stefan Huber
 */
public class SshLogin extends Login {

    private Stage primaryStage;
    private LoginController sshController;
    private boolean wasShown = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("SshLogin.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        sshController = fxmlLoader.getController();
        sshController.setStage(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
    }

    @Override
    public Map<String, String> getLoginInformation() {
        if (!wasShown) {
            primaryStage.showAndWait();
            wasShown = true;
        }
        return sshController.getLoginInformation();
    }
}

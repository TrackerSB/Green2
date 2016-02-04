package bayern.steinbrecher.gruen2.login.ssh;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.data.LoginKey;
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

    /**
     * The stage used to show this login.
     */
    private Stage primaryStage;
    /**
     * The controller used for the view.
     */
    private LoginController sshController;

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LoginKey, String> getLoginInformation() {
        if (primaryStage == null) {
            throw new IllegalStateException(
                    "start(...) has to be called first");
        }
        primaryStage.showAndWait();
        if (!sshController.userConfirmed()) {
            return null;
        }
        return sshController.getLoginInformation();
    }
}

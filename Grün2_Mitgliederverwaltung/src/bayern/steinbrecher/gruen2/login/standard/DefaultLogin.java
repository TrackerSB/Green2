package bayern.steinbrecher.gruen2.login.standard;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.login.LoginController;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a login without SSH.
 *
 * @author Stefan Huber
 */
public class DefaultLogin extends Login {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("DefaultLogin.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        loginContoller = fxmlLoader.getController();
        loginContoller.setStage(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
    }
}

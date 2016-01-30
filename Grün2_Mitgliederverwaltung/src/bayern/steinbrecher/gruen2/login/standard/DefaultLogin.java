package bayern.steinbrecher.gruen2.login.standard;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.Login;
import java.util.Map;
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

    private Stage primaryStage;
    private DefaultLoginController dlController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("DefaultLogin.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        dlController = fxmlLoader.getController();
        dlController.setStage(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
    }

    @Override
    public Map<String, String> getLoginInformation() {
        primaryStage.showAndWait();
        return dlController.getLoginInformation();
    }
}

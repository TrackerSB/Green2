package bayern.steinbrecher.gruen2.login.ssh;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.login.Login;
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
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("SshLogin.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.RESOURCE_BUNDLE.getString("loginTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
    }
}

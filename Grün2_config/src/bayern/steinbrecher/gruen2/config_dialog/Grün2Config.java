package bayern.steinbrecher.gruen2.config_dialog;

import bayern.steinbrecher.gruen2.data.DataProvider;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a dialog for configuring Grün2.
 *
 * @author Stefan Huber
 */
public class Grün2Config extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("Grün2Config.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        Grün2ConfigController controller = fxmlLoader.getController();
        controller.setStage(primaryStage);

        primaryStage.setTitle(
                DataProvider.getResourceValue("configureApplication"));
        primaryStage.getIcons().add(DataProvider.DEFAULT_ICON);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * main-method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

package bayern.steinbrecher.gruen2.config_dialog;

import bayern.steinbrecher.gruen2.data.DataProvider;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Stefan Huber
 */
public class Grün2Config extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("Grün2Config.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        primaryStage.setTitle("Grün2 konfigurieren");
        primaryStage.getIcons().add(DataProvider.DEFAULT_ICON);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}

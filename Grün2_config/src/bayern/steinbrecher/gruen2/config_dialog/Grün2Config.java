package bayern.steinbrecher.gruen2.config_dialog;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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

        primaryStage.setTitle("Grün2 konfigurieren");
        primaryStage.getIcons()
                .add(new Image("file:///" + new File("icon.png").getAbsolutePath().replace("\\", "/")));
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

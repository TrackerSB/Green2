package bayern.steinbrecher.green2.memberManagement.elements;

import bayern.steinbrecher.wizard.StandaloneWizardPageController;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.Locale;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 2u14
 */
public class SplashScreenController extends StandaloneWizardPageController<Optional<Void>> {
    private static final double MAX_WIDTH = 800;
    private static final double MAX_HEIGHT = Double.MAX_VALUE;
    @FXML
    private Pane root;

    @FXML
    private void initialize() {
        ImageView imageView = new ImageView();
        String splashScreenImagePath;
        if (Locale.getDefault().equals(Locale.GERMANY)) {
            splashScreenImagePath = "splashscreen_de.png";
        } else {
            splashScreenImagePath = "splashscreen_en.png";
        }
        Image splashScreenImage = new Image(getClass().getResource(splashScreenImagePath)
                .toExternalForm(), MAX_WIDTH, MAX_HEIGHT, true, true);
        imageView.setImage(splashScreenImage);
        root.getChildren().add(imageView);
    }
}

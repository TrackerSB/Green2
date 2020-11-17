package bayern.steinbrecher.green2.sharedBasis.utility;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class StageUtility {
    private StageUtility(){
        throw new UnsupportedOperationException("The construction of instances is prohibited");
    }

    public static void prepareStage(Stage stage){
        stage.getIcons()
                .add(new Image("/bayern/steinbrecher/green2/sharedBasis/icons/logo.png"));
        stage.getScene()
                .getStylesheets()
                .add("/bayern/steinbrecher/green2/sharedBasis/styles/styles.css");
    }
}

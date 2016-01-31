package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.selection.Selection;
import java.util.Arrays;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Stefan Huber
 */
public class Main extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Selection l = new Selection(Arrays.asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));
        l.start(new Stage());
        System.out.println(l.getSelection() + " : " + l.getContribution());
    }
}

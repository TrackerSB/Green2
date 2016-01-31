package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.sepaform.SepaForm;
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
        SepaForm l = new SepaForm();
        l.start(new Stage());
        System.out.println(l.getOriginator());
    }
}

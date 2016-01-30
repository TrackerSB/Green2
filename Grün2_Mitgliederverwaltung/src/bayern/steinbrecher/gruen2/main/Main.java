package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.login.standard.DefaultLogin;
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
        Login l = new DefaultLogin();
        l.start(new Stage());
        System.out.println(l.getLoginInformation());
    }
}

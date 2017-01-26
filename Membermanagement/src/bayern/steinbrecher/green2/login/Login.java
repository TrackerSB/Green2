/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2.login;

import bayern.steinbrecher.green2.View;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 */
public abstract class Login extends View<LoginController> {

    @Override
    public abstract void start(Stage primaryStage) throws IOException;

    /**
     * Returns the information that was entered in the login. This method blocks
     * until the frame is closed or hidden. It won't show more than once even if
     * multiple threads call it. They will be blocked and notified when the
     * login window closes.
     *
     * @return The Information that was entered in the login.
     */
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        showOnceAndWait();
        return controller.getLoginInformation();
    }
}

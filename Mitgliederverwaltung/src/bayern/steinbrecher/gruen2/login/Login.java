/*
 * Copyright (C) 2016 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.View;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import javafx.stage.Stage;

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
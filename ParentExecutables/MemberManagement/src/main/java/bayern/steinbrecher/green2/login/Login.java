/* 
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2.login;

import bayern.steinbrecher.green2.View;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 */
public abstract class Login extends View<LoginController> {

    /**
     * {@inheritDoc}
     */
    //@Override
    //TODO Is it possible to specify some kind of "an uppper bound" of ViewStartException?
    //public abstract void startImpl(Stage primaryStage) throws IOException;
    /**
     * Returns the information that was entered in the login. This method blocks until the frame is closed or hidden. It
     * won't show more than once even if multiple threads call it. They will be blocked and notified when the login
     * window closes.
     *
     * @return The Information that was entered in the login.
     */
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        showOnceAndWait();
        return getController().getLoginInformation();
    }
}
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

import bayern.steinbrecher.green2.ResultView;
import java.util.Optional;
import bayern.steinbrecher.green2.connection.credentials.DBCredentials;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 * @param <C> The type of the login credentials.
 */
public abstract class Login<C extends DBCredentials> extends ResultView<Optional<C>, LoginController<C>> {

    /**
     * {@inheritDoc}
     */
    //@Override
    //TODO Is it possible to specify some kind of "an uppper bound" of ViewStartException?
    //public abstract void startImpl(Stage primaryStage) throws IOException;
}

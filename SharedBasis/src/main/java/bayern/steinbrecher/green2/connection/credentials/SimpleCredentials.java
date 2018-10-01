/*
 * Copyright (C) 2018 Steinbrecher
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
package bayern.steinbrecher.green2.connection.credentials;

/**
 * Represents simple credentials for a database. That means a single username and password.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class SimpleCredentials implements DBCredentials {

    private final String username;
    private final String password;

    /**
     * Creates a simple representation of credentials.
     *
     * @param username The username.
     * @param password The password.
     */
    public SimpleCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username of these credentials.
     *
     * @return The username of these credentials.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password of these credentials.
     *
     * @return The password of these credentials.
     */
    public String getPassword() {
        return password;
    }
}

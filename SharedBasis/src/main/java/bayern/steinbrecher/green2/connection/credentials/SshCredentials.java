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
 * Represents two pairs of credentials for a database and a SSH connection.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class SshCredentials implements DBCredentials {

    private final String dbUsername;
    private final String dbPassword;
    private final String sshUsername;
    private final String sshPassword;

    /**
     * Creates representation for credentials for a database and a SSH connection.
     *
     * @param dbUsername The database username.
     * @param dbPassword The database password.
     * @param sshUsername The SSH username.
     * @param sshPassword The SSH password.
     */
    public SshCredentials(String dbUsername, String dbPassword, String sshUsername, String sshPassword) {
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.sshUsername = sshUsername;
        this.sshPassword = sshPassword;
    }

    /**
     * Returns the database username.
     *
     * @return The database username.
     */
    public String getDbUsername() {
        return dbUsername;
    }

    /**
     * Returns the database password.
     *
     * @return The database password.
     */
    public String getDbPassword() {
        return dbPassword;
    }

    /**
     * Returns the SSH username.
     *
     * @return The SSH username.
     */
    public String getSshUsername() {
        return sshUsername;
    }

    /**
     * Returns the SSH password.
     *
     * @return The SSH password.
     */
    public String getSshPassword() {
        return sshPassword;
    }
}

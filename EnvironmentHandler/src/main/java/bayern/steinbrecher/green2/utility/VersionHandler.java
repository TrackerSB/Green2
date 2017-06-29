/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a class handling the local and non-local versions.
 *
 * @author Stefan Huber
 */
public class VersionHandler {

    /**
     * The URL of the online repository containing the installation files.
     */
    public static final String PROGRAMFOLDER_PATH_ONLINE
            = URLUtility.resolveURL("https://traunviertler-traunwalchen.de/programme")
                    .orElse("");
    /**
     * The key used in the preferences to store the current version.
     */
    private static final String VERSION_KEY = "version";
    /**
     * The URL of the version file describing the version of the files at {@code PROGRAMFOLDER_PATH_ONLINE}.
     */
    private static final String VERSIONFILE_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/version.txt";

    /**
     * Returns the version of the application on the repository.
     *
     * @return The version of the application on the repository. Returns {@link Optional#empty()} only if the online
     * version could not be read.
     */
    public static Optional<String> readOnlineVersion() {
        try {
            URL onlineVersionUrl = new URL(VERSIONFILE_PATH_ONLINE);
            Scanner sc = new Scanner(onlineVersionUrl.openStream());
            return Optional.of(sc.nextLine());
        } catch (IOException ex) {
            Logger.getLogger(VersionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    /**
     * Returns the currently installed version of this application. NOTE: Since 2u13 the version is not stored in the
     * registry anymore.
     *
     * @return The currently installed version of the application. Returns {@link Optional#empty()} if this application
     * is not installed yet or the version could not be read.
     */
    @Deprecated
    public static Optional<String> readLocalVersion() {
        return Optional.ofNullable(EnvironmentHandler.PREFERENCES_SYSTEM_NODE.get(VERSION_KEY, null));
    }

    /**
     * Placed the version {@code newVersion} into the local version file. NOTE: You need administrator permissions to
     * successfully call this method.
     *
     * @param newVersion The new version to set as new local version.
     */
    public static void updateLocalVersion(String newVersion) {
        EnvironmentHandler.PREFERENCES_USER_NODE.put(VERSION_KEY, newVersion);
    }

    /**
     * Returns a {@link String} containing the current version.
     *
     * @return A {@link String} containing the current version or &bdquo;version not found&ldquo; if it was not found.
     */
    public static String getVersion() {
        return readLocalVersion().orElse(EnvironmentHandler.getResourceValue("versionNotFound"));
    }
}

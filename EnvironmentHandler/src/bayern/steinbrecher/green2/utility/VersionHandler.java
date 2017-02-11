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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a class handling HTTP(S)-connections and handling the version
 * database.
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
     * The path of the local version file.
     */
    private static final String VERSIONFILE_PATH_LOCAL = EnvironmentHandler.APP_DATA_PATH + "/version.txt";
    /**
     * The URL of the version file describing the version of the files at
     * {@code PROGRAMFOLDER_PATH_ONLINE}.
     */
    private static final String VERSIONFILE_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/version.txt";

    /**
     * Returns the version of the application on the repository.
     *
     * @return The version of the application on the repository. Returns
     * {@link Optional#empty()} only if the online version could not be read.
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
     * Returns the currently installed version of this application.
     *
     * @return The currently installed version of the application. Returns
     * {@link Optional#empty()} if this application is not installed yet or the
     * version could not be read.
     */
    public static Optional<String> readLocalVersion() {
        File localVersionfile = new File(VERSIONFILE_PATH_LOCAL);
        if (localVersionfile.exists()) {
            try (Scanner sc = new Scanner(localVersionfile)) {
                return Optional.of(sc.nextLine());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(VersionHandler.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Placed the version {@code newVersion} into the local version file.
     *
     * @param newVersion The new version to set as new local version.
     */
    public static void updateLocalVersion(String newVersion) {
        new File(EnvironmentHandler.APP_DATA_PATH).mkdir();
        IOStreamUtility.printContent(newVersion, new File(VERSIONFILE_PATH_LOCAL), false);
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

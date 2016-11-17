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
package bayern.steinbrecher.gruen2.utility;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.launcher.Launcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
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
     * Returns the version of the application on the repository.
     *
     * @return The version of the application on the repository. Returns
     * {@code Optional.empty()} only if the online version could not be read.
     */
    public static Optional<String> readOnlineVersion() {
        try {
            URL onlineVersionUrl
                    = new URL(DataProvider.VERSIONFILE_PATH_ONLINE);
            Scanner sc = new Scanner(onlineVersionUrl.openStream());
            return Optional.of(sc.nextLine());
        } catch (MalformedURLException ex) {
            Logger.getLogger(Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    /**
     * Returns the currently installed version of this application.
     *
     * @return The currently installed version of the application. Returns
     * {@code Optional.empty()} if this application is not installed yet or the
     * version could not be read.
     */
    public static Optional<String> readLocalVersion() {
        File localVersionfile = new File(DataProvider.VERSIONFILE_PATH_LOCAL);
        if (localVersionfile.exists()) {
            try (Scanner sc = new Scanner(localVersionfile)) {
                return Optional.of(sc.nextLine());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(VersionHandler.class.getName())
                        .log(Level.INFO, null, ex);
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
        new File(DataProvider.APP_DATA_PATH).mkdir();
        IOStreamUtility.printContent(newVersion,
                DataProvider.VERSIONFILE_PATH_LOCAL, false);
    }
}

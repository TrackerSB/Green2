/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.launcher.Launcher;

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

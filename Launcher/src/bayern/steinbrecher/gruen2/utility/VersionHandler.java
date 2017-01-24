/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.utility;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.launcher.Launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
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

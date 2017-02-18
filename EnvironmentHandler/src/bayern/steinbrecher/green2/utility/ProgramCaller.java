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
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * Contains methods for calling programs of Green2 like configuration dialog or the main application.
 *
 * @author Stefan Huber
 */
public final class ProgramCaller {

    /**
     * The path of the local folder where to save the application itself.
     */
    public static final String PROGRAMFOLDER_PATH_LOCAL
            = (EnvironmentHandler.CURRENT_OS == EnvironmentHandler.OS.WINDOWS
                    ? System.getenv("ProgramFiles").replaceAll("\\\\", "/") + "/" : "/opt/")
            + EnvironmentHandler.APPLICATION_FOLDER_NAME;

    private ProgramCaller() {
        throw new UnsupportedOperationException("Construction of an object not allowed");
    }

    private static void startJar(String jarname) {
        try {
            new ProcessBuilder("java", "-jar", Paths.get((EnvironmentHandler.IS_USED_AS_LIBRARY
                    ? EnvironmentHandler.APPLICATION_ROOT : PROGRAMFOLDER_PATH_LOCAL).toString(), jarname).toString())
                    .start();
            Platform.exit();
        } catch (IOException ex) {
            Logger.getLogger(ProgramCaller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Starts Green2 and closes this application.
     */
    public static void startGreen2() {
        startJar("Membermanagement.jar");
    }

    /**
     * Starts Green2 launcher and closes this application.
     */
    public static void startGreen2Launcher() {
        startJar("Launcher.jar");
    }

    /**
     * Starts Green2 configuration dialog and closes this application.
     */
    public static void startGreen2ConfigDialog() {
        startJar("ConfigurationDialog.jar");
    }
}

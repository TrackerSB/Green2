/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.DataProvider;
import javafx.application.Platform;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains methods for calling programs of Green2 like configuration dialog or
 * the main application.
 *
 * @author Stefan Huber
 */
public final class ProgramCaller {

    private ProgramCaller() {
        throw new UnsupportedOperationException("Construction of an object not allowed");
    }

    private static void startJar(String jarname) {
        try {
            new ProcessBuilder("java", "-jar", DataProvider.PROGRAMFOLDER_PATH_LOCAL + "/" + jarname).start();
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

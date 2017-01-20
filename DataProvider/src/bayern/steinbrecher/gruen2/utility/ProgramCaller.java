/*
 * Copyright (C) 2017 Stefan Huber
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * Contains methods for calling programs of Grün2 like configuration dialog or
 * the main application.
 *
 * @author Stefan Huber
 */
public final class ProgramCaller {

    private ProgramCaller() {
        throw new UnsupportedOperationException(
                "Construction of an object not allowed");
    }

    private static void startJar(String jarname) {
        try {
            new ProcessBuilder("java", "-jar",
                    DataProvider.PROGRAMFOLDER_PATH_LOCAL + "/" + jarname)
                    .start();
            Platform.exit();
        } catch (IOException ex) {
            Logger.getLogger(ProgramCaller.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Starts Grün2 and closes this application.
     */
    public static void startGrün2() {
        startJar("Grün2_Mitgliederverwaltung.jar");
    }

    /**
     * Starts Grün2 launcher and closes this application.
     */
    public static void startGrün2Launcher() {
        startJar("Grün2_Launcher.jar");
    }

    /**
     * Starts Grün2 configuration dialog and closes this application.
     */
    public static void startGrün2ConfigDialog() {
        startJar("Grün2_config.jar");
    }
}

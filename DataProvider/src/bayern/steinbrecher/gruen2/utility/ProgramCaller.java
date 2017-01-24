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
import javafx.application.Platform;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

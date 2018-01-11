/* 
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * Contains methods for calling programs of Green2 like configuration dialog or the main application.
 *
 * @author Stefan Huber
 */
public enum Programs {
    /**
     * The main program containing the member management.
     */
    MEMBER_MANAGEMENT("MemberManagement.jar"),
    /**
     * The launcher which installs the application, looks for updates, etc.
     */
    LAUNCHER("Launcher.jar"),
    /**
     * The program for managing the profiles and configurations.
     */
    CONFIGURATION_DIALOG("ConfigurationDialog.jar");

    /**
     * The path of the local folder where to save the application itself.
     */
    public static final String PROGRAMFOLDER_PATH_LOCAL
            = (EnvironmentHandler.CURRENT_OS == EnvironmentHandler.OS.WINDOWS
                    ? System.getenv("ProgramFiles").replaceAll("\\\\", "/") + "/" : "/opt/")
            + EnvironmentHandler.APPLICATION_FOLDER_NAME;
    private final String jarname;
    private final String[] options;

    private Programs(String jarname, String... options) {
        this.jarname = jarname;
        this.options = options;
    }

    /**
     * Calls the given program and closes the program calling this method.
     */
    public void call() {
        String[] args = new String[options.length + 3];
        args[0] = "java";
        args[1] = "-jar";
        args[2] = Paths.get((EnvironmentHandler.IS_USED_AS_LIBRARY
                ? EnvironmentHandler.APPLICATION_ROOT : PROGRAMFOLDER_PATH_LOCAL).toString(), jarname).toString();
        System.arraycopy(options, 0, args, 3, options.length);
        try {
            Process callProcess = new ProcessBuilder(args).start();
            callProcess.waitFor();
            String errorMessage = IOStreamUtility.readAll(callProcess.getErrorStream(), Charset.defaultCharset());
            if (!errorMessage.isEmpty()) {
                Logger.getLogger(Programs.class.getName()).log(Level.WARNING, errorMessage);
            }
            Platform.exit();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Programs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

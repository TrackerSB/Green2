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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
     * Calls this program and closes the program calling this method.
     */
    public void call() {
        String[] args = new String[options.length + 3];
        args[0] = "java";
        args[1] = "-jar";
        args[2] = Paths.get((EnvironmentHandler.IS_USED_AS_LIBRARY
                ? EnvironmentHandler.APPLICATION_ROOT : PROGRAMFOLDER_PATH_LOCAL).toString(), jarname).toString();
        System.arraycopy(options, 0, args, 3, options.length);
        Platform.setImplicitExit(false);
        CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return new ProcessBuilder(args).start();
                    } catch (IOException ex) {
                        throw new CompletionException("Could not start calling the program.", ex);
                    }
                })
                .thenApply(process -> {
                    try {
                        process.waitFor();
                        return process;
                    } catch (InterruptedException ex) {
                        throw new CompletionException("Waiting for end of called program was interrupted.", ex);
                    }
                })
                .thenApply(process -> {
                    try {
                        return IOStreamUtility.readAll(process.getErrorStream(), Charset.defaultCharset());
                    } catch (IOException ex) {
                        throw new CompletionException("Could not read the error stream of the called program.", ex);
                    }
                })
                .whenComplete((errorMessage, ex) -> {
                    if (ex != null) {
                        Logger.getLogger(Programs.class.getName())
                                .log(Level.SEVERE, null, ex);
                    } else if (!errorMessage.isEmpty()) {
                        Logger.getLogger(Programs.class.getName())
                                .log(Level.WARNING, "The called program reported errors:\n{0}", errorMessage);
                    }
                    Platform.exit();
                });
    }
}

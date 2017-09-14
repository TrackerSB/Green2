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
package bayern.steinbrecher.green2.helper;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.File;
import java.io.FilePermission;
import java.security.AccessController;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * This class is only needed for the installer to determine where to put the version of Green2.
 *
 * @author Stefan Huber
 */
public final class Helper {

    private Helper() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }

    /**
     * The main method. Prints the absolute path of the system node of Green2.
     *
     * @param args The command line arguments. If "delete" is passed as first argument, this program deletes the Green2
     * registry keys of the current user.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            String actions = Arrays.stream(HelperAction.values())
                    .map(HelperAction::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("You have to specify an action you want to execute. (" + actions + ")");
        }
        try {
            HelperAction.valueOf(args[0].toUpperCase()).doHelperAction();
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("Action " + args[0] + " not supported.", ex);
        }
    }

    public enum HelperAction {
        /**
         * Prints exactly two lines to stdout. First line: The path to the java preferences Second line: The path of
         * keys within the java preferences
         */
        PRINT_PREFERENCES {
            /**
             * {@inheritDoc}
             */
            @Override
            public void doHelperAction() {
                //This method is implemented according to http://stackoverflow.com/questions/1320709/preference-api-storage
                String path;
                String subkeyPath = EnvironmentHandler.PREFERENCES_SYSTEM_NODE.absolutePath();
                switch (EnvironmentHandler.CURRENT_OS) {
                    case WINDOWS:
                        if ("32".equals(System.getProperty("sun.arch.data.model"))
                                && "64".equals(System.getProperty("os.arch"))) {
                            path = "HKEY_LOCAL_MACHINE\\Software\\Wow6432Node\\JavaSoft\\Prefs";
                        } else {
                            path = "HKEY_LOCAL_MACHINE\\Software\\JavaSoft\\Prefs";
                        }
                        subkeyPath = subkeyPath.replaceAll("/", "\\\\");
                        break;
                    case LINUX:
                        path = System.getProperty("java.util.prefs.systemRoot",
                                System.getProperty("java.home") + "/.systemPrefs");
                        break;
                    default:
                        throw new IllegalArgumentException("OS not supported by PreferencesHelper.");
                }

                //NOTE Never print any unneeded character like a newline at the end to System.out
                System.out.print(path + '\n' + subkeyPath);
            }
        },
        /**
         * Deletes the saved preferences and prints preferences paths.
         *
         * @deprecated Will be replaced.
         * @see #UNINSTALL
         * @see #PRINT_PREFERENCES
         */
        @Deprecated
        DELETE_AND_PRINT_PREFERENCES {
            /**
             * {@inheritDoc}
             */
            @Override
            public void doHelperAction() {
                try {
                    Preferences currentNode = EnvironmentHandler.PREFERENCES_USER_NODE;
                    do {
                        Preferences parent = currentNode.parent();
                        if (currentNode.nodeExists("")) {
                            currentNode.removeNode();
                        }
                        currentNode = parent;
                    } while (currentNode != null && currentNode.childrenNames().length == 0);
                } catch (BackingStoreException ex) {
                    Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
                }

                PRINT_PREFERENCES.doHelperAction();
            }
        },
        /**
         * Installs the program.
         */
        INSTALL {
            /**
             * {@inheritDoc}
             */
            @Override
            public void doHelperAction() {
                String path = EnvironmentHandler.APPLICATION_ROOT.getParent().toString();
                AccessController.checkPermission(new FilePermission(path, "write"));
                throw new UnsupportedOperationException("Not supported yet.");
            }
        },
        /**
         * Removes the saved preferences and deletes all program files.
         */
        UNINSTALL {
            /**
             * {@inheritDoc}
             */
            @Override
            public void doHelperAction() {
                String path = EnvironmentHandler.APPLICATION_ROOT.toString() + File.pathSeparator + "-";
                AccessController.checkPermission(new FilePermission(path, "delete"));
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        /**
         * Executes the enums helper action.
         */
        public abstract void doHelperAction();
    }
}

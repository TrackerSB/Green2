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
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
import cz.adamh.utils.NativeUtils;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This class is only needed for the installer to determine where to put the version of Green2.
 *
 * @author Stefan Huber
 */
public final class Helper extends Application {

    static {
        String osname = EnvironmentHandler.CURRENT_OS.name();
        String jvmarch = System.getProperty("os.arch").endsWith("64") ? "64" : "32";
        Optional<String> fileformat = Optional.empty();
        switch (EnvironmentHandler.CURRENT_OS) {
            case LINUX:
                fileformat = Optional.of("so");
                break;
            case WINDOWS:
                fileformat = Optional.of("dll");
                break;
            default:
                Logger.getLogger(Helper.class.getName())
                        .log(Level.WARNING, "Loading a library for {} is not supported", EnvironmentHandler.CURRENT_OS);
        }

        fileformat.ifPresent(ff -> {
            try {
                NativeUtils.loadLibraryFromJar(
                        "/bayern/steinbrecher/green2/helper/externalLibs/libHelper" + osname + jvmarch + "." + ff);
            } catch (IOException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private Helper() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }

    /**
     * Just needed to make the generated jar be directly executable. (Init of JavaFX)
     *
     * @param primaryStage Unused
     * @deprecated Will be removed when direct testing of this file is not needed anymore.
     */
    @Override
    @Deprecated
    public void start(Stage primaryStage) {
        //No-op
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
         * Prints exactly two lines to stdout.<br />
         * First line: The path to the java system wide preferences<br />
         * Second line: The path of keys within the java preferences
         *
         * @deprecated Until now the version was the only thing to save in the system wide preferences which is now
         * saved directly within {@code EnvironmentHandler}.
         * @see EnvironmentHandler#VERSION
         */
        @Deprecated
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
                install(EnvironmentHandler.APPLICATION_ROOT, EnvironmentHandler.getResourceValue("installMessage"));
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
                CheckBox deletePreferences = new CheckBox(EnvironmentHandler.getResourceValue("deletePreferences"));
                CheckBox deleteAppData = new CheckBox(EnvironmentHandler.getResourceValue("deleteAppData"));
                Pane deleteUserSettings = new VBox(deletePreferences, deleteAppData);
                WizardPage<Boolean[]> deletePreferencesPage
                        = new WizardPage<>(deleteUserSettings, () -> "confirmUninstall", false,
                                () -> new Boolean[]{deletePreferences.isSelected(), deleteAppData.isSelected()});

                CheckBox deleteProgram = new CheckBox(EnvironmentHandler.getResourceValue("deleteProgram"));
                Pane confirmUninstall = new VBox(deleteProgram);
                WizardPage<Boolean> confirmUninstallPage
                        = new WizardPage<>(confirmUninstall, null, true,
                                () -> deleteProgram.isSelected(), deleteProgram.selectedProperty());

                Map<String, WizardPage<?>> pages = new HashMap<>();
                pages.put(WizardPage.FIRST_PAGE_KEY, deletePreferencesPage);
                pages.put("confirmUninstall", confirmUninstallPage);

                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    stage.setTitle(EnvironmentHandler.getResourceValue("uninstall"));
                    stage.setResizable(false);
                    stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
                    Wizard uninstallWizard = new Wizard(pages, stage);
                    try {
                        uninstallWizard.init();
                        stage.getScene().getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
                    } catch (IOException ex) {
                        Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    uninstallWizard.finishedProperty()
                            .addListener((obs, oldVal, newVal) -> {
                                if (newVal) {
                                    Map<String, ?> results = uninstallWizard.getResults().get();
                                    uninstall(EnvironmentHandler.APPLICATION_ROOT,
                                            EnvironmentHandler.getResourceValue("uninstallMessage"));
                                }
                            });
                });
            }

            private void askDeletePreferences() {
                String deletePreferencesMessage = EnvironmentHandler.getResourceValue("deletePreferences");
                Alert deletePreferencesAlert = DialogUtility.createAlert(
                        Alert.AlertType.CONFIRMATION, deletePreferencesMessage, ButtonType.YES, ButtonType.NO);
                boolean deletePreferences = deletePreferencesAlert.getResult() == ButtonType.YES;
                if (deletePreferences) {
                    Preferences currentNode = EnvironmentHandler.PREFERENCES_USER_NODE;
                    try {
                        do {
                            try {
                                currentNode.removeNode();
                                currentNode = currentNode.parent();
                            } catch (BackingStoreException ex) {
                                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
                                break;
                            }
                        } while (currentNode.childrenNames().length < 1);
                    } catch (BackingStoreException ex) {
                        Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            private void askDeleteAppData() {
                String deleteAppDataMessage = EnvironmentHandler.getResourceValue("deleteAppData");
                Alert deleteAppDataAlert = DialogUtility.createAlert(
                        Alert.AlertType.CONFIRMATION, deleteAppDataMessage, ButtonType.YES, ButtonType.NO);
                boolean deleteAppData = deleteAppDataAlert.getResult() == ButtonType.YES;
                if (deleteAppData) {
                    Path appDataRootPath = Paths.get(EnvironmentHandler.APP_DATA_PATH);
                    try {
                        Files.walkFileTree(appDataRootPath, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        /**
         * Executes the enums helper action.
         */
        public abstract void doHelperAction();

        private static native Optional<String> install(Path applicationRootDir, String description);

        private static native Optional<String> uninstall(Path applicationRootDir, String description);
    }
}

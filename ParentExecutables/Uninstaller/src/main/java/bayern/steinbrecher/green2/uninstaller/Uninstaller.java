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
package bayern.steinbrecher.green2.uninstaller;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This class is only needed for uninstalling the application.
 *
 * @author Stefan Huber
 */
public final class Uninstaller extends Application {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        Label deleteConfigsHint = new Label(EnvironmentHandler.getResourceValue("deleteConfigsHint"));
        deleteConfigsHint.setWrapText(true);
        CheckBox deleteConfigsCheckbox = new CheckBox(EnvironmentHandler.getResourceValue("deleteConfigs"));
        Pane deleteConfigsPane = new VBox(deleteConfigsHint, deleteConfigsCheckbox);
        WizardPage<Boolean> deleteConfigsPage = new WizardPage<>(
                deleteConfigsPane, () -> "confirmUninstall", false, () -> deleteConfigsCheckbox.isSelected());

        Label confirmUninstallHint = new Label(EnvironmentHandler.getResourceValue("confirmUninstallHint"));
        confirmUninstallHint.setWrapText(true);
        CheckBox confirmUninstallCheckBox = new CheckBox(EnvironmentHandler.getResourceValue("confirmUninstall"));
        Pane confirmUninstallPane = new VBox(confirmUninstallHint, confirmUninstallCheckBox);
        WizardPage<?> confirmUninstallPage = new WizardPage<>(
                confirmUninstallPane, null, true, () -> Void.TYPE, confirmUninstallCheckBox.selectedProperty());

        Map<String, WizardPage<?>> pages = new HashMap<>();
        pages.put(WizardPage.FIRST_PAGE_KEY, deleteConfigsPage);
        pages.put("confirmUninstall", confirmUninstallPage);

        Wizard wizard = new Wizard(pages);
        wizard.start(primaryStage);
        wizard.finishedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                uninstall((boolean) wizard.getResults().get().get(WizardPage.FIRST_PAGE_KEY));
            }
        });

        primaryStage.setResizable(false);
        primaryStage.setTitle(EnvironmentHandler.getResourceValue("uninstall"));
        primaryStage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
        primaryStage.getScene().getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
        primaryStage.show();
    }

    private String getPreferencesBasePath() {
        //This method is implemented according to http://stackoverflow.com/questions/1320709/preference-api-storage
        String subkey = EnvironmentHandler.PREFERENCES_USER_NODE.absolutePath();
        String javaUserNodePath;
        switch (EnvironmentHandler.CURRENT_OS) {
            case WINDOWS:
                if ("32".equals(System.getProperty("sun.arch.data.model"))
                        && "64".equals(System.getProperty("os.arch"))) {
                    javaUserNodePath = "HKEY_CURRENT_USER\\Software\\Wow6432Node\\JavaSoft\\Prefs";
                } else {
                    javaUserNodePath = "HKEY_CURRENT_USER\\Software\\JavaSoft\\Prefs";
                }
                subkey = subkey.replace('/', '\\');
                break;
            case LINUX:
                javaUserNodePath = System.getProperty("java.util.prefs.userRoot",
                        System.getProperty("user.home") + "/.systemPrefs");
                break;
            default:
                throw new IllegalArgumentException("OS not supported by Uninstaller.");
        }

        return javaUserNodePath + subkey;
    }

    private void uninstall(boolean deleteConfigs) {
        ProcessBuilder builder;
        String deleteConfigsString = Boolean.toString(deleteConfigs);
        switch (EnvironmentHandler.CURRENT_OS) {
            case LINUX:
                builder = new ProcessBuilder(
                        "/bin/bash", EnvironmentHandler.APPLICATION_ROOT.resolve("uninstall.sh").toString(),
                        getPreferencesBasePath(), deleteConfigsString);
                break;
            case WINDOWS:
                builder = new ProcessBuilder(
                        "wscript", EnvironmentHandler.APPLICATION_ROOT.resolve("uninstall.vbs").toString(),
                        getPreferencesBasePath(), deleteConfigsString);
                break;
            default:
                throw new UnsupportedOperationException("The unstaller does not support the current os.");
        }
        try {
            Process process = builder.start();
            process.waitFor();
            try (InputStream errorStream = process.getErrorStream()) {
                String errorMessage = IOStreamUtility.readAll(errorStream, Charset.defaultCharset());
                if (!errorMessage.isEmpty()) {
                    Logger.getLogger(Uninstaller.class.getName()).log(Level.SEVERE,
                            "The uninstaller got the following error:\n{0}", errorMessage);
                }
            } catch (IOException ex) {
                Logger.getLogger(Uninstaller.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Uninstaller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The main method.
     *
     * @param args The commandline arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

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
package bayern.steinbrecher.green2.launcher;

import bayern.steinbrecher.green2.data.Collector;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.ChoiceDialog;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.Programs;
import bayern.steinbrecher.green2.utility.ServiceFactory;
import bayern.steinbrecher.green2.utility.URLUtility;
import bayern.steinbrecher.green2.utility.ZipUtility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Installs Green2 and checks for updates.
 *
 * @author Stefan Huber
 */
public final class Launcher extends Application {

    /**
     * The URL of the online repository containing the installation files.
     */
    public static final String PROGRAMFOLDER_PATH_ONLINE
            = URLUtility.resolveURL("https://traunviertler-traunwalchen.de/programme")
                    .orElse("");
    /**
     * The URL of the version file describing the version of the files at {@code PROGRAMFOLDER_PATH_ONLINE}.
     */
    private static final String VERSIONFILE_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/version.txt";
    /**
     * The URL of the zip containing the installation files of the application.
     */
    private static final String GREEN2_ZIP_URL = PROGRAMFOLDER_PATH_ONLINE + "/Green2.zip";
    /**
     * The URL of the file containing the used charset of the zip and its files.
     */
    private static final String CHARSET_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/charset.txt";
    /**
     * Zipfile is currently delivered ISO-8859-1 (Latin-1) encoded.
     */
    private static Charset ZIP_CHARSET = StandardCharsets.UTF_8;
    private static final int DOWNLOAD_STEPS = 1000;
    private Stage stage;
    private LauncherController controller;

    static {
        try (Scanner sc = new Scanner(new URL(CHARSET_PATH_ONLINE).openStream(), StandardCharsets.UTF_8.name())) {
            ZIP_CHARSET = Charset.forName(sc.nextLine());
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        Optional<String> optOnlineVersion = readOnlineVersion();

        Service<Void> serv = null;
        boolean isInstalled = new File(Programs.PROGRAMFOLDER_PATH_LOCAL).exists();
        if (optOnlineVersion.isPresent()) {
            String onlineVersion = optOnlineVersion.get();
            if (isInstalled) {
                if (!EnvironmentHandler.VERSION.equalsIgnoreCase(onlineVersion)) {
                    Optional<Boolean> installUpdates = ChoiceDialog.askForUpdate();
                    if (installUpdates.isPresent()) {
                        if (installUpdates.get()) {
                            serv = createDownloadAndInstallService(onlineVersion);
                            serv.setOnSucceeded(evt -> Programs.MEMBER_MANAGEMENT.call());
                        } else {
                            Programs.MEMBER_MANAGEMENT.call();
                        }
                    }
                } else {
                    Programs.MEMBER_MANAGEMENT.call();
                }
            } else {
                serv = createDownloadAndInstallService(onlineVersion);
                serv.setOnSucceeded(evt -> Programs.CONFIGURATION_DIALOG.call());
            }
        } else if (isInstalled) {
            Programs.MEMBER_MANAGEMENT.call();
        } else {
            String installError = EnvironmentHandler.getResourceValue("installError");
            DialogUtility.showAndWait(DialogUtility.createErrorAlert(
                    null, EnvironmentHandler.getResourceValue("installErrorMessage"), installError, installError));
            throw new IllegalStateException(
                    "Green2 is currently not installed and there´s no connection to install it.");
        }

        if (serv != null) {
            serv.setOnFailed(evt -> {
                Throwable thrown = evt.getSource().getException();
                Logger.getLogger(Launcher.class.getName())
                        .log(Level.SEVERE, "The downloadAndInstall service failed.", thrown);
                Platform.exit();
            });
            showProgressWindow();
            serv.start();
        }
    }

    /**
     * Returns the version of the application on the repository.
     *
     * @return The version of the application on the repository. Returns {@link Optional#empty()} only if the online
     * version could not be read.
     */
    public static Optional<String> readOnlineVersion() {
        try {
            URL onlineVersionUrl = new URL(VERSIONFILE_PATH_ONLINE);
            Scanner sc = new Scanner(onlineVersionUrl.openStream(), StandardCharsets.UTF_8.name());
            return Optional.of(sc.nextLine());
        } catch (IOException ex) {
            Logger.getLogger(EnvironmentHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    private void showProgressWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Launcher.fxml"));
            fxmlLoader.setResources(EnvironmentHandler.RESOURCE_BUNDLE);
            Parent root = fxmlLoader.load();
            root.getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
            controller = fxmlLoader.getController();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle(EnvironmentHandler.getResourceValue("downloadNewVersion"));
            stage.initStyle(StageStyle.UTILITY);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    private File download() throws IOException {
        File tempFile = Files.createTempFile(null, ".zip").toFile();
        URLConnection downloadConnection = new URL(GREEN2_ZIP_URL).openConnection();
        long fileSize = Long.parseLong(downloadConnection.getHeaderField("Content-Length"));
        long bytesPerLoop = fileSize / DOWNLOAD_STEPS;

        IOStreamUtility.transfer(downloadConnection.getInputStream(),
                new FileOutputStream(tempFile), fileSize, bytesPerLoop, () -> {
                    if (controller != null) {
                        Platform.runLater(() -> controller.incPercentage(DOWNLOAD_STEPS));
                    }
                });
        return tempFile;
    }

    private Process install(File downloadedDir, String newVersion) throws IOException, InterruptedException {
        String dirPath = downloadedDir.getAbsolutePath();
        String[] command;
        switch (EnvironmentHandler.CURRENT_OS) {
            case WINDOWS:
                command = new String[]{"powershell", "Start-Process",
                    "\"wscript '" + dirPath + "/install.vbs " + newVersion + "'\"", "-Verb runAs", "-Wait"};
                break;
            case LINUX:
            default:
                command = new String[]{"chmod", "a+x", dirPath + "/install.sh", dirPath + "/uninstall.sh"};
                new ProcessBuilder(command).start().waitFor();

                command = new String[]{"sh", dirPath + "/install.sh", newVersion};
        }

        return new ProcessBuilder(command).start();
    }

    /**
     * Returns a service which downloads and installs Green2.
     */
    private Service<Void> createDownloadAndInstallService(String newVersion) {
        //TODO May replace this Service by a CompletableFuture
        return ServiceFactory.createService(() -> {
            try {
                File tempFile = download();
                File tempDir = Files.createTempDirectory(null).toFile();

                ZipUtility.unzip(tempFile, tempDir, ZIP_CHARSET);
                tempFile.delete();

                Process installer = install(tempDir, newVersion);
                installer.waitFor();

                int installerExitValue = installer.exitValue();
                tempDir.delete();
                if (installerExitValue != 0) {
                    String errorMessage;
                    try (InputStream errorStream = installer.getErrorStream()) {
                        errorMessage = IOStreamUtility.readAll(errorStream, Charset.defaultCharset());
                    }
                    if (!errorMessage.isEmpty()) {
                        Logger.getLogger(Launcher.class.getName())
                                .log(Level.SEVERE, "The installer got follwing error:\n{0}", errorMessage);
                    }
                } else {
                    Collector.sendData();
                }
            } catch (InterruptedException | IOException ex) {
                throw new CompletionException(ex);
            }
            return null;
        });
    }

    /**
     * The starting point of the hole application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

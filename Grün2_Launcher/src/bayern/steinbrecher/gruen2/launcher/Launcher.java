/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.launcher;

import bayern.steinbrecher.gruen2.data.Collector;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.utility.VersionHandler;
import bayern.steinbrecher.gruen2.elements.ChoiceDialog;
import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
import bayern.steinbrecher.gruen2.utility.ServiceFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
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
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Installs Grün2 and checks for updates. (This application needs to be
 * independent, so it contains DataProvider project.).
 *
 * @author Stefan Huber
 */
public final class Launcher extends Application {

    private static final int DOWNLOAD_STEPS = 1000;
    private Stage stage;
    private LauncherController controller;

    /**
     * Default constructor.
     */
    public Launcher() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        Optional<String> optOnlineVersion = VersionHandler.readOnlineVersion();
        Optional<String> optLocalVersion = VersionHandler.readLocalVersion();

        Service<Void> serv = null;
        if (optOnlineVersion.isPresent()) {
            String onlineVersion = optOnlineVersion.get();
            if (optLocalVersion.isPresent()) {
                String localVersion = optLocalVersion.get();
                if (!localVersion.equalsIgnoreCase(onlineVersion)
                        && ChoiceDialog.askForUpdate()) {
                    serv = downloadAndInstall(onlineVersion);
                    serv.setOnSucceeded(evt -> executeGruen2());
                } else {
                    executeGruen2();
                }
            } else {
                serv = downloadAndInstall(onlineVersion);
                serv.setOnSucceeded(evt -> executeGruen2Config());
            }
        } else if (optLocalVersion.isPresent()) {
            executeGruen2();
        } else {
            throw new IllegalStateException("Grün2 is currently not installed "
                    + "and there´s no connection to install it.");
        }

        if (serv != null) {
            serv.setOnFailed(evt -> {
                Platform.exit();
            });
            showWindow();
            serv.start();
        }
    }

    private void showWindow() {
        try {
            FXMLLoader fxmlLoader
                    = new FXMLLoader(getClass().getResource("Launcher.fxml"));
            fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
            Parent root = fxmlLoader.load();
            root.getStylesheets().add(DataProvider.STYLESHEET_PATH);
            controller = fxmlLoader.getController();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle(
                    DataProvider.getResourceValue("downloadNewVersion"));
            stage.initStyle(StageStyle.UTILITY);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName())
                    .log(Level.WARNING, null, ex);
        }
    }

    private File download() throws IOException {
        File tempFile = Files.createTempFile(
                null, ".zip", new FileAttribute[0])
                .toFile();
        URLConnection downloadConnection
                = new URL(DataProvider.GRUEN2_ZIP_URL)
                .openConnection();
        long fileSize = Long.parseLong(
                downloadConnection.getHeaderField("Content-Length"));
        long bytesPerLoop = fileSize / DOWNLOAD_STEPS;
        ReadableByteChannel rbc = Channels.newChannel(
                downloadConnection.getInputStream());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            for (long offset = 0; offset < fileSize;
                    offset += bytesPerLoop) {
                fos.getChannel()
                        .transferFrom(rbc, offset, bytesPerLoop);
                if (controller != null) {
                    Platform.runLater(() -> {
                        controller.incPercentage(DOWNLOAD_STEPS);
                    });
                }
            }
        }

        return tempFile;
    }

    private Path unzip(File zippedFile) throws IOException {
        Path tempDir = Files.createTempDirectory(
                null, new FileAttribute[0]);
        try {
            ZipFile zipFile = new ZipFile(zippedFile.getAbsolutePath());
            zipFile.extractAll(tempDir.toString());
        } catch (ZipException ex) {
            Logger.getLogger(Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return tempDir;
    }

    private Process install(Path downloadedDir)
            throws IOException, InterruptedException {
        String dirPath = downloadedDir.toString();
        String[] command;
        switch (DataProvider.CURRENT_OS) {
        case WINDOWS:
            command = new String[]{"cscript", dirPath + "/install.vbs"};
            break;
        case LINUX:
        default:
            command = new String[]{"chmod", "a+x", dirPath + "/install.sh",
                dirPath + "/uninstall.sh"};
            new ProcessBuilder(command).start().waitFor();

            command = new String[]{"sh", dirPath + "/install.sh"};
        }

        return new ProcessBuilder(command).start();
    }

    /**
     * Returns a service which downloads and installs Grün2.
     */
    private Service<Void> downloadAndInstall(String newVersion)
            throws IOException {
        Service<Void> service = ServiceFactory.createService(() -> {
            try {
                File tempFile = download();

                Path tempDir = unzip(tempFile);
                tempFile.delete();

                //FIXME install.vbs shows no success message.
                /*
                WScript.StdOut.Write "Grün2 installed."
                 */
                Process installer = install(tempDir);

                //FIXME Doesn´t really wait for everything is completed.
                //FIXME Make sure move commands finished
                installer.waitFor();

                String successMessage;
                //Check whether success message was printed on console
                try (InputStream inputStream = installer.getInputStream()) {
                    successMessage = IOStreamUtility.readAll(inputStream);
                }
                //FIXME Search for success message
                boolean gotInstalled = !successMessage.isEmpty();

                String errorMessage;
                try (InputStream errorStream = installer.getErrorStream()) {
                    errorMessage = IOStreamUtility.readAll(errorStream);
                }
                if (!errorMessage.isEmpty()) {
                    Logger.getLogger(Launcher.class.getName())
                            .log(Level.WARNING,
                                    "The installer got follwing error: {0}",
                                    errorMessage);
                    gotInstalled = false;
                }

                if (gotInstalled) {
                    VersionHandler.updateLocalVersion(newVersion);
                    Collector.sendData();
                }
            } catch (MalformedURLException | FileNotFoundException |
                    InterruptedException ex) {
                Logger.getLogger(Launcher.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            return null;
        });

        return service;
    }

    private void executeGruen2() {
        try {
            new ProcessBuilder("java", "-jar",
                    DataProvider.PROGRAMFOLDER_PATH_LOCAL
                    + "/Grün2_Mitgliederverwaltung.jar")
                    .start();
            Platform.exit();
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private void executeGruen2Config() {
        try {
            new ProcessBuilder("java", "-jar",
                    DataProvider.PROGRAMFOLDER_PATH_LOCAL + "/Grün2_config.jar")
                    .start();
            Platform.exit();
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
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

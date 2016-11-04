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

    private Stage stage;

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

        Service<Boolean> serv = null;
        if (optOnlineVersion.isPresent()) {
            String onlineVersion = optOnlineVersion.get();
            if (optLocalVersion.isPresent()) {
                String localVersion = optLocalVersion.get();
                if (!localVersion.equalsIgnoreCase(onlineVersion)
                        && ChoiceDialog.askForUpdate()) {
                    serv = downloadAndInstallGruen2(onlineVersion);
                    serv.setOnSucceeded(evt -> executeGruen2());
                } else {
                    executeGruen2();
                }
            } else {
                serv = downloadAndInstallGruen2(onlineVersion);
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
            serv.start();
        }
    }

    /**
     * Returns a service which downloads and installs Grün2.
     */
    private Service<Boolean> downloadAndInstallGruen2(String newVersion)
            throws IOException {

        FXMLLoader fxmlLoader
                = new FXMLLoader(getClass().getResource("Launcher.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);
        LauncherController controller = fxmlLoader.getController();

        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setTitle(DataProvider.getResourceValue("downloadNewVersion"));
        stage.initStyle(StageStyle.UTILITY);
        stage.show();

        Service<Boolean> service = ServiceFactory.createService(() -> {
            try {
                File tempFile = Files.createTempFile(
                        null, ".zip", new FileAttribute[0])
                        .toFile();
                tempFile.deleteOnExit();
                Path tempDir = Files.createTempDirectory(
                        null, new FileAttribute[0]);
                tempDir.toFile().deleteOnExit();

                //Download
                URLConnection downloadConnection
                        = new URL(DataProvider.GRUEN2_ZIP_URL)
                        .openConnection();
                long fileSize = Long.parseLong(
                        downloadConnection.getHeaderField("Content-Length"));
                long bytesPerLoop = fileSize / 1000;
                ReadableByteChannel rbc
                        = Channels.newChannel(downloadConnection.getInputStream());
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    for (long offset = 0; offset < fileSize;
                            offset += bytesPerLoop) {
                        fos.getChannel()
                                .transferFrom(rbc, offset, bytesPerLoop);
                        Platform.runLater(() -> controller.incPercentage());
                    }
                }

                //Unzip
                try {
                    ZipFile zipFile = new ZipFile(tempFile.getAbsolutePath());
                    zipFile.extractAll(tempDir.toString());
                } catch (ZipException ex) {
                    Logger.getLogger(Launcher.class.getName())
                            .log(Level.SEVERE, null, ex);
                }

                //Install
                String[] command;
                switch (DataProvider.CURRENT_OS) {
                case WINDOWS:
                    command = new String[]{"cmd", "/C",
                        "\"script " + tempDir.toString() + "/install.vbs\""};
                    break;
                case LINUX:
                default:
                    command = new String[]{"chmod", "a+x", tempDir.toString()
                        + "/install.sh", tempDir.toString() + "/uninstall.sh"};
                    new ProcessBuilder(command).start().waitFor();

                    command = new String[]{"sh",
                        tempDir.toString() + "/install.sh"};
                }

                //Include in install.vbs something like:
                /*
                WScript.StdOut.Write "Grün2 installed."
                 */
                Process installer = new ProcessBuilder(command).start();

                //Check whether success message was printed on console
                InputStream outputStream = installer.getInputStream();
                int b = outputStream.read();
                StringBuilder successMessage = new StringBuilder();
                while (b > -1) {
                    successMessage.append((char) b);
                    b = outputStream.read();
                }
                boolean gotInstalled = successMessage.length() > 0;

                InputStream errorStream = installer.getErrorStream();
                b = errorStream.read();
                StringBuilder errorMessage = new StringBuilder();
                while (b > -1) {
                    errorMessage.append((char) b);
                    b = errorStream.read();
                }
                if (errorMessage.length() > 0) {
                    Logger.getLogger(Launcher.class.getName())
                            .log(Level.WARNING,
                                    "The installer got follwing error: {0}",
                                    errorMessage);
                    gotInstalled = false;
                }

                //FIXME Doesn´t really wait for everything is completed.
                installer.waitFor();

                //FIXME Make sure move commands finished
                Thread.sleep(1000);

                if (gotInstalled) {
                    VersionHandler.updateLocalVersion(tempDir, newVersion);
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

/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.launcher;

import bayern.steinbrecher.green2.data.Collector;
import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.elements.ChoiceDialog;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.ProgramCaller;
import bayern.steinbrecher.green2.utility.ServiceFactory;
import bayern.steinbrecher.green2.utility.VersionHandler;
import bayern.steinbrecher.green2.utility.ZipUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs Green2 and checks for updates. (This application needs to be
 * independent, so it contains DataProvider project.).
 *
 * @author Stefan Huber
 */
public final class Launcher extends Application {

    /**
     * Zipfile is currently delivered ISO-8859-1 (Latin-1) encoded.
     */
    private static Charset ZIP_CHARSET = StandardCharsets.UTF_8;
    private static final int DOWNLOAD_STEPS = 1000;
    private Stage stage;
    private LauncherController controller;

    static {
        try (Scanner sc = new Scanner(new URL(DataProvider.CHARSET_PATH_ONLINE).openStream())) {
            ZIP_CHARSET = Charset.forName(sc.nextLine());
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Default constructor.
     */
    public Launcher() {
    }

    /**
     * {@inheritDoc}
     */
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
                if (!localVersion.equalsIgnoreCase(onlineVersion) && ChoiceDialog.askForUpdate()) {
                    serv = downloadAndInstall(onlineVersion);
                    serv.setOnSucceeded(evt -> ProgramCaller.startGreen2());
                } else {
                    ProgramCaller.startGreen2();
                }
            } else {
                serv = downloadAndInstall(onlineVersion);
                serv.setOnSucceeded(evt -> ProgramCaller.startGreen2ConfigDialog());
            }
        } else if (optLocalVersion.isPresent()) {
            ProgramCaller.startGreen2();
        } else {
            throw new IllegalStateException(
                    "Green2 is currently not installed and there´s no connection to install it.");
        }

        if (serv != null) {
            serv.setOnFailed(evt -> Platform.exit());
            showProgressWindow();
            serv.start();
        }
    }

    private void showProgressWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Launcher.fxml"));
            fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
            Parent root = fxmlLoader.load();
            root.getStylesheets().add(DataProvider.STYLESHEET_PATH);
            controller = fxmlLoader.getController();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle(DataProvider.getResourceValue("downloadNewVersion"));
            stage.initStyle(StageStyle.UTILITY);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    private File download() throws IOException {
        File tempFile = Files.createTempFile(null, ".zip").toFile();
        URLConnection downloadConnection = new URL(DataProvider.GREEN2_ZIP_URL).openConnection();
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

    private Process install(File downloadedDir) throws IOException, InterruptedException {
        String dirPath = downloadedDir.getAbsolutePath();
        String[] command;
        switch (DataProvider.CURRENT_OS) {
            case WINDOWS:
                command = new String[]{"cscript", dirPath + "/install.vbs"};
                break;
            case LINUX:
            default:
                command = new String[]{"chmod", "a+x", dirPath + "/install.sh", dirPath + "/uninstall.sh"};
                new ProcessBuilder(command).start().waitFor();

                command = new String[]{"sh", dirPath + "/install.sh"};
        }

        return new ProcessBuilder(command).start();
    }

    /**
     * Returns a service which downloads and installs Green2.
     */
    private Service<Void> downloadAndInstall(String newVersion) {
        return ServiceFactory.createService(() -> {
            try {
                File tempFile = download();

                File tempDir = Files.createTempDirectory(null).toFile();

                ZipUtility.unzip(tempFile, tempDir, ZIP_CHARSET);
                tempFile.delete();

                Process installer = install(tempDir);

                /* FIXME Doesn´t really wait for everything is completed on
                 * windows.
                 */
                installer.waitFor();
                Thread.sleep(1000);

                //Check whether file "installed" was created.
                boolean gotInstalled = Arrays.asList(tempDir.list())
                        .contains("installed");
                tempDir.delete();

                String errorMessage;
                try (InputStream errorStream = installer.getErrorStream()) {
                    errorMessage = IOStreamUtility.readAll(errorStream, Charset.defaultCharset());
                }
                if (!errorMessage.isEmpty()) {
                    Logger.getLogger(Launcher.class.getName())
                            .log(Level.WARNING, "The installer got follwing error:\n{0}", errorMessage);
                }

                if (gotInstalled) {
                    VersionHandler.updateLocalVersion(newVersion);
                    Collector.sendData();
                }
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
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

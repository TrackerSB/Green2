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

import bayern.steinbrecher.green2.data.CollectorUtility;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.ChoiceDialog;
import bayern.steinbrecher.green2.elements.report.ConditionReport;
import bayern.steinbrecher.green2.progress.ProgressDialog;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.PathUtility;
import bayern.steinbrecher.green2.utility.Programs;
import bayern.steinbrecher.green2.utility.UpdateUtility;
import bayern.steinbrecher.green2.utility.ZipUtility;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Installs Green2 and checks for updates.
 *
 * @author Stefan Huber
 */
public final class Launcher extends Application {

    /**
     * The URL of the version file describing the version of the files at {@code PROGRAMFOLDER_PATH_ONLINE}.
     */
    private static final String VERSIONFILE_PATH_ONLINE = PathUtility.PROGRAMFOLDER_PATH_ONLINE + "/version.txt";
    /**
     * The URL of the zip containing the installation files of the application.
     */
    private static final String GREEN2_ZIP_URL = PathUtility.PROGRAMFOLDER_PATH_ONLINE + "/Green2.zip";
    /**
     * The URL of the file containing the used charset of the zip and its files.
     */
    private static final String CHARSET_PATH_ONLINE = PathUtility.PROGRAMFOLDER_PATH_ONLINE + "/charset.txt";
    /**
     * The charset the zip file containing the application is encoded. Contains {@link Optional#empty()} only if the
     * charset could not be determined.
     */
    private static final Optional<Charset> ZIP_CHARSET = retrieveZipCharset();
    private static final int DOWNLOAD_STEPS = 1000;

    private static Optional<Charset> retrieveZipCharset() {
        Charset zipCharset;
        try (Scanner scanner = new Scanner(new URL(CHARSET_PATH_ONLINE).openStream(), StandardCharsets.UTF_8.name())) {
            zipCharset = Charset.forName(scanner.nextLine());
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            zipCharset = null;
        }
        return Optional.ofNullable(zipCharset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);

        Optional<String> optOnlineVersion = readOnlineVersion();

        CompletableFuture<Void> installUpdateProcess = null;
        boolean isInstalled = new File(Programs.PROGRAMFOLDER_PATH_LOCAL).exists();
        if (optOnlineVersion.isPresent()) {
            String onlineVersion = optOnlineVersion.get();
            if (isInstalled) {
                if (EnvironmentHandler.VERSION.equalsIgnoreCase(onlineVersion)) {
                    Programs.MEMBER_MANAGEMENT.call();
                } else {
                    Optional<Boolean> installUpdates = ChoiceDialog.askForUpdate();
                    if (installUpdates.isPresent()) {
                        if (installUpdates.get()) {
                            installUpdateProcess = createInstallTask()
                                    .thenRun(Programs.MEMBER_MANAGEMENT::call);
                        } else {
                            Programs.MEMBER_MANAGEMENT.call();
                        }
                    }
                }
            } else {
                installUpdateProcess = createInstallTask()
                        .thenRun(Programs.CONFIGURATION_DIALOG::call);
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

        if (installUpdateProcess != null) {
            installUpdateProcess.handle((voidResult, ex) -> {
                if (ex != null) {
                    Logger.getLogger(Launcher.class.getName())
                            .log(Level.SEVERE, "The download/update/install process failed.", ex);
                }
                Platform.exit();
                return null;
            });
        }
    }

    /**
     * Returns the version of the application on the repository.
     *
     * @return The version of the application on the repository. Returns {@link Optional#empty()} only if the online
     * version could not be read.
     */
    public static Optional<String> readOnlineVersion() {
        Optional<String> onlineVersion;
        try {
            URL onlineVersionUrl = new URL(VERSIONFILE_PATH_ONLINE);
            try (Scanner scanner = new Scanner(onlineVersionUrl.openStream(), StandardCharsets.UTF_8.name())) {
                onlineVersion = Optional.of(scanner.nextLine());
            }
        } catch (IOException ex) {
            Logger.getLogger(EnvironmentHandler.class.getName()).log(Level.SEVERE, null, ex);
            onlineVersion = Optional.empty();
        }
        return onlineVersion;
    }

    private File download(ProgressDialog progress) throws IOException {
        File tempFile = Files.createTempFile(null, ".zip")
                .toFile();
        tempFile.deleteOnExit();
        URLConnection downloadConnection = new URL(GREEN2_ZIP_URL).openConnection();
        int fileSize = Integer.parseInt(downloadConnection.getHeaderField("Content-Length"));
        int bytesPerLoop = fileSize / DOWNLOAD_STEPS;

        IOStreamUtility.transfer(downloadConnection.getInputStream(),
                Files.newOutputStream(tempFile.toPath()), fileSize, bytesPerLoop, () -> {
            if (progress != null) {
                Platform.runLater(() -> progress.incPercentage(DOWNLOAD_STEPS));
            }
        });
        return tempFile;
    }

    private void install(File extractDir) throws IOException, InterruptedException {
        String dirPath = extractDir.getAbsolutePath();
        String[] command;
        switch (EnvironmentHandler.CURRENT_OS) {
            case WINDOWS:
                command = new String[]{"powershell", "Start-Process",
                    "\"wscript '" + dirPath + "/install.vbs'\"", "-Verb runAs", "-Wait"};
                break;
            case LINUX: //Linux serves as the default behaviour
            default:
                command = new String[]{"chmod", "a+x", dirPath + "/install.sh", dirPath + "/uninstall.sh"};
                new ProcessBuilder(command).start().waitFor();

                command = new String[]{"sh", dirPath + "/install.sh"};
                break;
        }

        Process installProcess = new ProcessBuilder(command).start();
        installProcess.waitFor();

        int installExitValue = installProcess.exitValue();
        if (installExitValue != 0) {
            try (InputStream errorStream = installProcess.getErrorStream()) {
                String errorMessage = IOStreamUtility.readAll(errorStream, Charset.defaultCharset());
                throw new CompletionException("Installation failed:" + errorMessage, null);
            }
        }
    }

    private CompletableFuture<Void> createInstallTask() {
        return CompletableFuture.supplyAsync(UpdateUtility::getUpdateConditions)
                .thenApply(Optional::orElseThrow)
                .thenApply(conditions -> {
                    boolean allConditionsSuccessful = conditions.entrySet()
                            .stream()
                            .allMatch(entry -> {
                                try {
                                    return entry.getValue()
                                            .call();
                                } catch (Exception ignored) { //NOPMD - Make sure evaluation of conditions continues.
                                    return false;
                                }
                            });
                    return allConditionsSuccessful ? null : new ConditionReport(conditions);
                })
                .thenAccept(conditionReport -> {
                    if (conditionReport != null) {
                        Platform.runLater(() -> conditionReport.start(new Stage()));
                        boolean noConditionFailed = conditionReport.getResult()
                                .orElseThrow(
                                        () -> new CancellationException(
                                                "The user cancelled the install process at the conditions report."));
                        if (!noConditionFailed) {
                            throw new IllegalStateException("There are unfulfilled conditions needed for installing.");
                        }
                    }
                })
                .thenApply(voidResult -> new ProgressDialog())
                .thenApply(progressDialog -> {
                    CompletableFuture<File> downloadProcess = CompletableFuture.supplyAsync(() -> {
                        try {
                            return download(progressDialog);
                        } catch (IOException ex) {
                            throw new CompletionException("The download of the application failed.", ex);
                        }
                    });
                    Platform.runLater(() -> {
                        Stage processDialogStage = new Stage();
                        progressDialog.start(processDialogStage);
                        processDialogStage.show();
                        downloadProcess.thenRun(() -> {
                            Platform.runLater(processDialogStage::hide);
                        });
                    });
                    return downloadProcess.join();
                })
                .thenApply(downloadedFile -> {
                    try {
                        File extractDir = Files.createTempDirectory(null).toFile();
                        ZipUtility.unzip(downloadedFile, extractDir, ZIP_CHARSET.orElseThrow());
                        extractDir.deleteOnExit();
                        return extractDir;
                    } catch (IOException ex) {
                        throw new CompletionException("The extraction of the downloaded application failed,", ex);
                    }
                })
                .thenAccept(extractDir -> {
                    try {
                        install(extractDir);
                    } catch (IOException | InterruptedException ex) {
                        throw new CompletionException("The installation failed.", ex);
                    }
                })
                .thenRun(() -> CollectorUtility.sendData());
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

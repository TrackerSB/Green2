package bayern.steinbrecher.green2.launcher;

import bayern.steinbrecher.green2.launcher.elements.ChoiceDialog;
import bayern.steinbrecher.green2.launcher.progress.ProgressDialog;
import bayern.steinbrecher.green2.launcher.utility.ProgressWrapper;
import bayern.steinbrecher.green2.launcher.utility.ZipUtility;
import bayern.steinbrecher.green2.sharedBasis.data.AppInfo;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.SupportedOS;
import bayern.steinbrecher.green2.sharedBasis.utility.IOStreamUtility;
import bayern.steinbrecher.green2.sharedBasis.utility.PathUtility;
import bayern.steinbrecher.green2.sharedBasis.utility.Programs;
import bayern.steinbrecher.green2.sharedBasis.utility.StagePreparer;
import bayern.steinbrecher.green2.sharedBasis.utility.ThreadUtility;
import bayern.steinbrecher.green2.sharedBasis.utility.URLUtility;
import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs Green2 and checks for updates.
 *
 * @author Stefan Huber
 */
public final class Launcher extends Application {

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());
    public static final String PROGRAMFOLDER_PATH_ONLINE
            = URLUtility.resolveURL("https://steinbrecher.bayern/green2")
            .orElse("");
    /**
     * The URL of the version file describing the version of the files at {@link #PROGRAMFOLDER_PATH_ONLINE}.
     */
    private static final String VERSIONFILE_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/version.txt";
    /**
     * The URL of the zip containing the installation files of the application.
     */
    private static final String GREEN2_ZIP_URL = PROGRAMFOLDER_PATH_ONLINE + "/Green2.zip";
    /**
     * The URL of the file containing the used charset of the online zip and its files.
     */
    private static final String CHARSET_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/charset.txt";
    /**
     * The charset the online zip file containing the application is encoded. Contains {@code null} only if the
     * charset could not be determined.
     */
    private static final Charset ZIP_CHARSET = retrieveZipCharset();

    private static Charset retrieveZipCharset() {
        Charset zipCharset;
        try (Scanner scanner = new Scanner(new URL(CHARSET_PATH_ONLINE).openStream(), StandardCharsets.UTF_8.name())) {
            zipCharset = Charset.forName(scanner.nextLine());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not retrieve charset of application archive online", ex);
            zipCharset = null;
        }
        return zipCharset;
    }

    private static File downloadApplicationArchive() throws IOException {
        URL downloadSource = new URL(GREEN2_ZIP_URL);
        URLConnection downloadConnection = downloadSource.openConnection();
        long fileSize = Long.parseLong(downloadConnection.getHeaderField("Content-Length"));
        ProgressWrapper<ReadableByteChannel> downloadSourceChannel
                = ProgressWrapper.wrap(Channels.newChannel(downloadSource.openStream()), fileSize);

        File downloadTarget = Files.createTempFile("green2_", ".zip")
                .toFile();
        downloadTarget.deleteOnExit();
        FileOutputStream downloadTargetStream = new FileOutputStream(downloadTarget);

        ProgressDialog progress = new ProgressDialog();
        Stage progressStage = StagePreparer.getDefaultPreparedStage();
        progress.embedStandaloneWizardPage(progressStage, EnvironmentHandler.getResourceValue("hide"));
        downloadSourceChannel.progressProperty()
                .addListener((obs, previousProgress, currentProgress)
                        -> progress.setProgress(currentProgress.doubleValue()));
        Platform.runLater(progressStage::show);
        downloadTargetStream.getChannel()
                .transferFrom(downloadSourceChannel.getWrapped(), 0, Long.MAX_VALUE);
        Platform.runLater(progressStage::close);
        return downloadTarget;
    }

    private static File unpackApplicationArchive(File downloadedFile) throws IOException {
        assert ZIP_CHARSET != null : "Unpack application archive since its encoding is unknown";
        File extractDir = Files.createTempDirectory("green2_").toFile();
        ZipUtility.unzip(downloadedFile, extractDir, ZIP_CHARSET);
        extractDir.deleteOnExit();
        return extractDir;
    }

    private static void installApplication(File extractDir) throws IOException, InterruptedException {
        String dirPath = extractDir.getAbsolutePath();
        Iterable<List<String>> installCommands = switch (SupportedOS.CURRENT) {
            case WINDOWS -> List.of(
                    List.of("powershell", "Start-Process",
                            "\"wscript '" + dirPath + "/install.vbs'\"", "-Verb runAs", "-Wait")
            );
            case LINUX -> List.of(
                    List.of("chmod", "a+x", dirPath + "/install.sh", dirPath + "/uninstall.sh"),
                    List.of("sh", dirPath + "/install.sh")
            );
        };

        for (List<String> command : installCommands) {
            LOGGER.log(Level.INFO, String.format("Execute %s", command));
            Process execution = new ProcessBuilder(command)
                    .start();
            execution.waitFor();
            int exitValue = execution.exitValue();
            if (exitValue == 0) {
                LOGGER.log(Level.INFO, "Command succeeded");
            } else {
                try (InputStream errorStream = execution.getErrorStream()) {
                    String errorMessage = IOStreamUtility.readAll(errorStream, Charset.defaultCharset());
                    throw new CompletionException("Command failed: " + errorMessage, null);
                }
            }
        }
    }

    private static void startUpdateProcess() {
        throw new UnsupportedOperationException("Update is not implemented yet");
    }

    private static void startInstallationProcess() {
        try {
            File applicationArchive = downloadApplicationArchive();
            File applicationSourceFolder = unpackApplicationArchive(applicationArchive);
            installApplication(applicationSourceFolder);
        } catch (InterruptedException | IOException ex) {
            LOGGER.log(Level.SEVERE, "The installation process failed", ex);
            try {
                Alert stacktraceAlert = StagePreparer.prepare(
                        DialogUtility.createStacktraceAlert(ex,
                                EnvironmentHandler.getResourceValue("installErrorMessage"),
                                EnvironmentHandler.getResourceValue("installError")
                        )
                );
                DialogUtility.showAndWait(stacktraceAlert);
            } catch (DialogCreationException exx) {
                LOGGER.log(Level.WARNING, "Could not show exception to user", exx);
            }
        }
    }

    private static void startMemberManagement() {
        Programs.MEMBER_MANAGEMENT.call();
    }

    private static boolean isApplicationInstalled() {
        return PathUtility.INSTALL_ROOT
                .toFile()
                .exists();
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
            LOGGER.log(Level.INFO, "Could not determine most recent available application version", ex);
            onlineVersion = Optional.empty();
        }
        return onlineVersion;
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);

        Thread startup = new Thread(() -> {
            if (isApplicationInstalled()) {
                Optional<String> optOnlineVersion = readOnlineVersion();
                if (optOnlineVersion.isPresent()) {
                    boolean isInstallationOutdated = !AppInfo.VERSION.equalsIgnoreCase(optOnlineVersion.get());
                    if (isInstallationOutdated) {
                        Optional<Boolean> userConfirmedUpdate = ChoiceDialog.askForUpdate(getHostServices());
                        if (userConfirmedUpdate.orElse(false)) {
                            try {
                                startUpdateProcess();
                            } catch (Exception ex) { // In any case: If update fails, start installed version
                                LOGGER.log(Level.SEVERE, "Update failed. Try starting installed version.", ex);
                            }
                        }
                    }
                }
            } else {
                startInstallationProcess();
            }

            if (isApplicationInstalled()) {
                startMemberManagement();
            }
            Platform.exit();
        }, "Startup thread");
        startup.setUncaughtExceptionHandler((thread, exception) -> {
            ThreadUtility.DEFAULT_THREAD_EXCEPTION_HANDLER.uncaughtException(thread, exception);
            Platform.exit();
        });
        startup.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

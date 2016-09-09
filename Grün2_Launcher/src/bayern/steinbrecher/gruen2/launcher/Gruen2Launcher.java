package bayern.steinbrecher.gruen2.launcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Installs Grün2 and checks for updates. Contains parts of DataProvider, Output
 * and ServiceFactory (This application needs to be independent).
 *
 * @author Stefan Huber
 */
public final class Gruen2Launcher extends Application {

    /**
     * Containing translations for the system default language.
     */
    public static final ResourceBundle RESOURCE_BUNDLE
            = ResourceBundle.getBundle(
                    "bayern.steinbrecher.gruen2.launcher.language");
    /**
     * The path to the home directory of the user.
     */
    private static final String HOME_DIR
            = System.getProperty("user.home").replaceAll("\\\\", "/");
    /**
     * The path of the folder where to put user specific data of the
     * application.
     */
    public static final String APP_DATA_PATH = HOME_DIR
            + (System.getProperty("os.name").toLowerCase().contains("win")
            ? "/AppData/Roaming/Grün2_Mitgliederverwaltung"
            : "/.Grün2_Mitgliederverwaltung");
    private static final String GRUEN2_HOST_URL
            = "http://www.traunviertler-traunwalchen.de/programme";
    private static final String VERSIONFILE_PATH
            = APP_DATA_PATH + "/version.txt";
    private static final String PROGRAMFOLDER_PATH
            = System.getProperty("os.name").toLowerCase().contains("win")
            ? "C:/Program Files/Grün2_Mitgliederverwaltung"
            : "/opt/Grün2_Mitgliederverwaltung";
    private Stage stage;

    /**
     * Default constructor.
     */
    public Gruen2Launcher() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        File localVersionfile = new File(VERSIONFILE_PATH);
        Optional<String> optOnlineVersion = readOnlineVersion();

        Service<Boolean> serv = null;
        if (optOnlineVersion.isPresent()) {
            String onlineVersion = optOnlineVersion.get();
            if (localVersionfile.exists()) {
                try (Scanner sc = new Scanner(localVersionfile)) {
                    String localVersion = sc.nextLine();
                    if (!localVersion.equalsIgnoreCase(onlineVersion)) {
                        serv = downloadAndInstallGruen2(onlineVersion);
                        serv.setOnSucceeded(evt -> {
                            executeGruen2();
                            Platform.exit();
                        });
                    } else {
                        executeGruen2();
                    }
                }
            } else {
                serv = downloadAndInstallGruen2(onlineVersion);
                serv.setOnSucceeded(evt -> executeGruen2Config());
            }
        } else if (localVersionfile.exists()) {
            executeGruen2();
        } else {
            throw new IllegalStateException("Grün2 is currently not installed "
                    + "and there´s no connection to install it.");
        }

        if (serv == null) {
            Platform.exit();
        } else {
            serv.setOnFailed(evt -> {
                Platform.exit();
            });
            serv.start();
        }
    }

    /**
     * Returns the value behind {@code key} of the resource bundle inserted
     * params.
     *
     * @param key The key to serach for.
     * @param params The params to insert.
     * @return The value with inserted params.
     */
    public static String getResourceValue(String key, Object... params) {
        return MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
    }

    /**
     * Creates a service which executes {@code task} and returns a value of type
     * {@code V}.
     *
     * @param <V> The type of the value to return.
     * @param task The task to execute.
     * @return The service which executes {@code task} and returns a value of
     * type {@code V}.
     */
    public static <V> Service<V> createService(Callable<V> task) {
        return new Service<V>() {
            @Override
            protected Task<V> createTask() {
                return new Task<V>() {
                    @Override
                    protected V call() throws Exception {
                        return task.call();
                    }
                };
            }
        };
    }

    /**
     * Overrides the hole content of {@code pathToFile} with {@code content}. If
     * {@code pathToFile} doesn´t exist it creates one.
     *
     * @param content The content to be written into the file.
     * @param pathToFile The file to write in.
     * @param withBom Only if {@code true} it adds '\uFEFF' to the beginning of
     * the file.
     */
    public static void printContent(String content, String pathToFile,
            boolean withBom) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(pathToFile), "UTF-8"))) {
            //To make no UTF-8 without BOM but with BOM.
            if (withBom) {
                bw.append('\uFEFF');
            }
            bw.append(content);
        } catch (IOException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a service which downloads and installs Grün2.
     */
    private Service<Boolean> downloadAndInstallGruen2(String newVersion)
            throws IOException {

        FXMLLoader fxmlLoader
                = new FXMLLoader(getClass().getResource("Grün2Launcher.fxml"));
        fxmlLoader.setResources(RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add("styles.css");
        Grün2LauncherController controller = fxmlLoader.getController();

        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setTitle(getResourceValue("downloadNewVersion"));
        stage.initStyle(StageStyle.UTILITY);
        stage.show();

        Service<Boolean> service = createService(() -> {
            try {
                File tempFile = Files.createTempFile(null, ".zip", new FileAttribute[0])
                        .toFile();
                Path tempDir = Files.createTempDirectory(null, new FileAttribute[0]);

                //Download
                URLConnection downloadConnection
                        = new URL(GRUEN2_HOST_URL + "/Gruen2.zip").openConnection();
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

                try {
                    //Unzip
                    ZipFile zipFile = new ZipFile(tempFile.getAbsolutePath());
                    zipFile.extractAll(tempDir.toString());
                } catch (ZipException ex) {
                    Logger.getLogger(Gruen2Launcher.class.getName())
                            .log(Level.SEVERE, null, ex);
                }

                //Install
                String[] command;
                if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
                    command = new String[]{"cmd", "/C",
                        tempDir.toString() + "/install.vbs"};
                } else {
                    command = new String[]{"chmod", "a+x", tempDir.toString()
                        + "/install.sh", tempDir.toString() + "/uninstall.sh"};
                    new ProcessBuilder(command).start().waitFor();

                    command = new String[]{"sh",
                        tempDir.toString() + "/install.sh"};
                }
                Process installer = new ProcessBuilder(command).start();
                InputStream outputStream = installer.getErrorStream();
                int b = outputStream.read();
                while (b > -1) {
                    System.err.print((char) b);
                    b = outputStream.read();
                }
                //FIXME Doesn´t really wait for everything is completed.
                installer.waitFor();

                //Make sure move commands finished
                Thread.sleep(1000);

                //Update version.txt
                if (tempDir.toFile().list().length > 1) { //If dir is not empty
                    System.out.println(tempDir.toFile().list().length);
                    throw new CompletionException(
                            "Installer got no admin rights", null);
                } else {
                    printContent(newVersion, VERSIONFILE_PATH, false);
                }
            } catch (MalformedURLException | FileNotFoundException |
                    InterruptedException ex) {
                Logger.getLogger(Gruen2Launcher.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Gruen2Launcher.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            return null;
        });

        return service;
    }

    private static Optional<String> readOnlineVersion() {
        try {
            URL onlineVersionUrl = new URL(GRUEN2_HOST_URL + "/version.txt");
            Scanner sc = new Scanner(onlineVersionUrl.openStream());
            return Optional.of(sc.nextLine());
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    private void executeGruen2() {
        try {
            new ProcessBuilder("java", "-jar", PROGRAMFOLDER_PATH
                    + "/Grün2_Mitgliederverwaltung.jar")
                    .start();
        } catch (IOException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private void executeGruen2Config() {
        try {
            new ProcessBuilder("java", "-jar",
                    PROGRAMFOLDER_PATH + "/Grün2_config.jar")
                    .start();
            Platform.exit();
        } catch (IOException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
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

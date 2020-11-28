package bayern.steinbrecher.green2.uninstaller;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.utility.StyleUtility;
import bayern.steinbrecher.green2.uninstaller.confirmUninstall.ConfirmUninstall;
import bayern.steinbrecher.green2.uninstaller.deleteConfigs.DeleteConfigs;
import bayern.steinbrecher.wizard.EmbeddedWizardPage;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardState;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is only needed for uninstalling the application.
 *
 * @author Stefan Huber
 */
public final class Uninstaller extends Application {

    private static final Logger LOGGER = Logger.getLogger(Uninstaller.class.getName());

    @Override
    public void start(Stage primaryStage) throws IOException {
        EmbeddedWizardPage<Optional<Boolean>> deleteConfigsPage = new DeleteConfigs()
                .generateEmbeddableWizardPage();
        deleteConfigsPage.setFinishAndNext(false, () -> "confirmUninstall");

        EmbeddedWizardPage<Optional<Void>> confirmUninstallPage = new ConfirmUninstall()
                .generateEmbeddableWizardPage();

        Map<String, EmbeddedWizardPage<?>> pages = new HashMap<>();
        pages.put(EmbeddedWizardPage.FIRST_PAGE_KEY, deleteConfigsPage);
        pages.put("confirmUninstall", confirmUninstallPage);

        Wizard wizard = Wizard.create(pages);
        wizard.stateProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == WizardState.FINISHED) {
                deleteConfigsPage.getResult()
                        .ifPresentOrElse(this::uninstall, () -> {
                            LOGGER.log(Level.SEVERE, "The user made no decision about whether to keep or delete "
                                    + "config files. Uninstall aborted.");
                        });
            }
        });

        primaryStage.setScene(new Scene(wizard.getRoot()));
        StyleUtility.prepare(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.setTitle(EnvironmentHandler.getResourceValue("uninstall"));
        primaryStage.show();
    }

    private String getPreferencesBasePath() {
        //This method is implemented according to http://stackoverflow.com/questions/1320709/preference-api-storage
        String subkey = EnvironmentHandler.PREFERENCES_USER_NODE.absolutePath();
        String javaUserNodePath;
        switch (EnvironmentHandler.CURRENT_OS) {
        case WINDOWS:
            if ("32".equals(System.getProperty("sun.arch.data.model")) //NOPMD
                    && "64".equals(System.getProperty("os.arch"))) { //NOPMD
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
            /*Process process = */
            builder.start();
            //FIXME Can´t wait for otherwise this jar can not be deleted
            /*process.waitFor();
            try (InputStream errorStream = process.getErrorStream()) {
                String errorMessage = IOStreamUtility.readAll(errorStream, Charset.defaultCharset());
                if (!errorMessage.isEmpty()) {
                    LOGGER.log(Level.SEVERE, "The uninstaller got the following error:\n{0}", errorMessage);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }*/
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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

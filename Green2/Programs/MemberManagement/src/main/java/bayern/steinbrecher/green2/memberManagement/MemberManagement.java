package bayern.steinbrecher.green2.memberManagement;

import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.ConnectionFailedException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.DatabaseNotFoundException;
import bayern.steinbrecher.dbConnector.SimpleConnection;
import bayern.steinbrecher.dbConnector.SshConnection;
import bayern.steinbrecher.dbConnector.credentials.DBCredentials;
import bayern.steinbrecher.dbConnector.credentials.SimpleCredentials;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.SupportedDBMS;
import bayern.steinbrecher.dbConnector.scheme.SimpleColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.green2.memberManagement.elements.SplashScreen;
import bayern.steinbrecher.green2.memberManagement.elements.WaitScreen;
import bayern.steinbrecher.green2.memberManagement.login.Login;
import bayern.steinbrecher.green2.memberManagement.login.simple.SimpleLogin;
import bayern.steinbrecher.green2.memberManagement.login.ssh.SshLogin;
import bayern.steinbrecher.green2.memberManagement.menu.MainMenu;
import bayern.steinbrecher.green2.sharedBasis.data.AppInfo;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Profile;
import bayern.steinbrecher.green2.sharedBasis.data.ProfileSettings;
import bayern.steinbrecher.green2.sharedBasis.data.Tables;
import bayern.steinbrecher.green2.sharedBasis.elements.ProfileChoice;
import bayern.steinbrecher.green2.sharedBasis.utility.Programs;
import bayern.steinbrecher.green2.sharedBasis.utility.ThreadUtility;
import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogFactory;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.LoadException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 */
public class MemberManagement extends Application {

    private static final Logger LOGGER = Logger.getLogger(MemberManagement.class.getName());
    private static final long SPLASHSCREEN_DISPLAY_DURATION = 2500; // [ms]
    private Profile loadedProfile;
    private DBConnection dbConnection;

    public MemberManagement() {
        super();
    }

    private static void dumpFeatureSupport() {
        String state = Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW) ? "supported" : "not supported";
        LOGGER.log(Level.INFO, "Transparent windows: " + state);
    }

    private static Optional<Profile> askUserForProfile() {
        Optional<Profile> profile;
        List<String> availableProfiles = Profile.getAvailableProfiles();
        if (availableProfiles.isEmpty()) {
            Programs.CONFIGURATION_DIALOG.call();
            LOGGER.log(Level.INFO, "The are no profiles which can be loaded. Requested config dialog.");
            profile = Optional.empty();
        } else if (availableProfiles.size() == 1) { //NOPMD - Do not ask user when there is no choice for selection.
            profile = Optional.of(EnvironmentHandler.loadProfile(availableProfiles.get(0), false));
        } else {
            profile = ProfileChoice.askForProfile(false)
                    .map(EnvironmentHandler::loadProfile);
        }
        return profile;
    }

    private static void showSplashScreen() {
        SplashScreen splashScreen = new SplashScreen();
        Stage splashScreenStage = EnvironmentHandler.STAGE_FACTORY.create();
        try {
            splashScreen.embedStandaloneWizardPage(splashScreenStage, EnvironmentHandler.getResourceValue("skip"));
        } catch (LoadException ex) {
            LOGGER.log(Level.WARNING, "Could not show splash screen to user. It is skipped.", ex);
            return;
        }
        splashScreenStage.initModality(Modality.APPLICATION_MODAL);
        splashScreenStage.initStyle(StageStyle.TRANSPARENT);
        splashScreenStage.showingProperty()
                .addListener((obs, wasShowing, isShowing) -> {
                    if (isShowing) {
                        Thread closeTask = new Thread(() -> {
                            try {
                                Thread.sleep(SPLASHSCREEN_DISPLAY_DURATION);
                            } catch (InterruptedException ex) {
                                LOGGER.log(Level.WARNING,
                                        "The visualization of the splash screen has been interrupted", ex);
                            }
                            Platform.runLater(splashScreenStage::close);
                        });
                        closeTask.setUncaughtExceptionHandler(ThreadUtility.DEFAULT_THREAD_EXCEPTION_HANDLER);
                        closeTask.start();
                    }
                });
        splashScreenStage.showAndWait();
    }

    private Optional<DBConnection> establishDBConnection(DBCredentials credentials) {
        assert loadedProfile != null : "Establishing a connection requires a profile to be loaded first";

        SupportedDBMS dbms = loadedProfile.get(ProfileSettings.DBMS);
        String databaseHost = loadedProfile.getOrDefault(ProfileSettings.DATABASE_HOST, "localhost");
        int databasePort = loadedProfile.getOrDefault(ProfileSettings.DATABASE_PORT, dbms.getDefaultPort());
        String databaseName = loadedProfile.get(ProfileSettings.DATABASE_NAME);
        boolean enableSSL = loadedProfile.getOrDefault(ProfileSettings.USE_SSL_IF_NO_SSH, true);
        DBConnection dbConnection = null;

        Alert failureReport = null;
        try {
            try {
                if (credentials instanceof SimpleCredentials) {
                    dbConnection = new SimpleConnection(
                            dbms, databaseHost, databasePort, databaseName, (SimpleCredentials) credentials, enableSSL);
                } else if (credentials instanceof SshCredentials) {
                    String sshHost = loadedProfile.getOrDefault(ProfileSettings.SSH_HOST, "localhost");
                    int sshPort = loadedProfile.getOrDefault(ProfileSettings.SSH_PORT, 22);
                    Charset sshCharset = loadedProfile
                            .getOrDefault(ProfileSettings.SSH_CHARSET, StandardCharsets.UTF_8);
                    dbConnection = new SshConnection(dbms, databaseHost, databasePort, databaseName, sshHost, sshPort,
                            sshCharset, (SshCredentials) credentials);
                } else {
                    throw new UnsupportedOperationException(
                            "Credentials of type " + credentials.getClass().getCanonicalName() + " are not supported.");
                }
            } catch (AuthException ex) {
                LOGGER.log(Level.INFO, null, ex);
                String checkInput = EnvironmentHandler.getResourceValue("checkInput");
                failureReport = EnvironmentHandler.DIALOG_FACTORY.createInfoAlert(checkInput, checkInput);
            } catch (ConnectionFailedException ex) {
                LOGGER.log(Level.INFO, null, ex);
                String checkConnection = EnvironmentHandler.getResourceValue("checkConnection");
                failureReport = EnvironmentHandler.DIALOG_FACTORY.createInfoAlert(checkConnection, checkConnection);
            } catch (DatabaseNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Could not find database on host.", ex);
                String databaseNotFound = EnvironmentHandler.getResourceValue("databaseNotFound");
                failureReport = EnvironmentHandler.DIALOG_FACTORY.createErrorAlert(databaseNotFound, databaseNotFound);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not connect due to an unhandled exception.", ex);
                String unexpectedAbort = EnvironmentHandler.getResourceValue("unexpectedAbort");
                failureReport = EnvironmentHandler.DIALOG_FACTORY.createStacktraceAlert(ex, unexpectedAbort,
                        unexpectedAbort);
            }
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not show error to user", ex);
        }
        if (failureReport != null) {
            DialogFactory.showAndWait(failureReport);
        }
        return Optional.ofNullable(dbConnection);
    }

    private <T> boolean validateDBConnection() {
        assert dbConnection != null : "Cannot validate non existing database connection";

        Alert failureReport = null;
        try {
            try {
                if (!dbConnection.databaseExists()) {
                    LOGGER.log(Level.WARNING, "The database to connect to does not exist");
                    String databaseNotExistent = EnvironmentHandler.getResourceValue(
                            "couldntFindDatabase", dbConnection.getDatabaseName());
                    failureReport = EnvironmentHandler.DIALOG_FACTORY.createErrorAlert(databaseNotExistent,
                            databaseNotExistent);
                } else {
                    Map<TableScheme<?, ?>, Set<SimpleColumnPattern<?, ?>>> missingColumns = new HashMap<>();
                    for (TableScheme<?, ?> scheme : Tables.SCHEMES) {
                        dbConnection.createTableIfNotExists(scheme);
                        Set<? extends SimpleColumnPattern<?, ?>> currentMissingColumns = dbConnection.getMissingColumns(scheme);
                        if (!currentMissingColumns.isEmpty()) {
                            missingColumns.put(scheme, (Set<SimpleColumnPattern<?, ?>>) currentMissingColumns);
                        }
                    }
                    if (!missingColumns.isEmpty()) {
                        String invalidScheme = EnvironmentHandler.getResourceValue("invalidScheme");
                        String missingColumnsListingMessage = missingColumns.entrySet()
                                .stream()
                                .map(entry -> {
                                    String missingColumnsListing = entry.getValue()
                                            .stream()
                                            .map(SimpleColumnPattern::getRealColumnName)
                                            .collect(Collectors.joining(", "));
                                    return entry.getKey() + ": " + missingColumnsListing;
                                })
                                .collect(Collectors.joining("\n"));
                        failureReport = EnvironmentHandler.DIALOG_FACTORY.createErrorAlert(invalidScheme,
                                missingColumnsListingMessage);
                    }
                }
            } catch (QueryFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not validate database connection", ex);
                String validationFailed = EnvironmentHandler.getResourceValue("validationFailed");
                failureReport = EnvironmentHandler.DIALOG_FACTORY.createErrorAlert(validationFailed, validationFailed);
            }
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not show error to user", ex);
        }
        if (failureReport == null) {
            return true;
        } else {
            DialogFactory.showAndWait(failureReport);
            return false;
        }
    }

    private boolean validateCredentials(DBCredentials credentials) {
        Optional<DBConnection> optDbConnection = establishDBConnection(credentials);
        if (optDbConnection.isPresent()) {
            dbConnection = optDbConnection.get();
            return validateDBConnection();
        } else {
            return false;
        }
    }

    private void showMainMenu() {
        assert dbConnection != null : "Cannot open main menu without established database connection";

        MainMenu mainMenu = new MainMenu(dbConnection);
        Stage menuStage = EnvironmentHandler.STAGE_FACTORY.create();
        try {
            mainMenu.embedStandaloneWizardPage(menuStage, null);
        } catch (LoadException ex) {
            throw new RuntimeException("Could not create main menu", ex);
        }
        menuStage.setTitle(String.format("%s (%s \"%s\")", AppInfo.APP_NAME, AppInfo.VERSION, AppInfo.UPDATE_NAME));
        menuStage.show();
    }

    @Override
    public void start(Stage primaryStage) throws LoadException {
        dumpFeatureSupport();

        Optional<Profile> optLoadedProfile = askUserForProfile();
        if (optLoadedProfile.isPresent()) {
            loadedProfile = optLoadedProfile.get();
            if (loadedProfile.isAllConfigurationsSet()) {
                showSplashScreen();

                WaitScreen waitScreen = new WaitScreen();
                Stage waitScreenStage = EnvironmentHandler.STAGE_FACTORY.create();
                try {
                    waitScreen.embedStandaloneWizardPage(
                            waitScreenStage, EnvironmentHandler.getResourceValue("cancel"));
                } catch (LoadException ex) {
                    LOGGER.log(Level.WARNING, "Could not show wait screen. It is skipped.", ex);
                    return;
                }
                waitScreenStage.initModality(Modality.APPLICATION_MODAL);
                waitScreenStage.initStyle(StageStyle.TRANSPARENT);

                Login<? extends DBCredentials> login;
                if (loadedProfile.get(ProfileSettings.USE_SSH)) {
                    login = new SshLogin();
                } else {
                    login = new SimpleLogin();
                }
                Stage loginStage = EnvironmentHandler.STAGE_FACTORY.create();
                try {
                    login.embedStandaloneWizardPage(loginStage, EnvironmentHandler.getResourceValue("login"));
                } catch (LoadException ex) {
                    throw new LoadException("Could not generate login dialog", ex);
                }
                AtomicBoolean loginCanceled = new AtomicBoolean(false);
                loginStage.setOnCloseRequest(wevt -> loginCanceled.set(true));
                loginStage.show();

                loginStage.showingProperty().addListener((obs, wasShowing, isShowing) -> {
                    if (isShowing) {
                        waitScreenStage.hide();
                    } else {
                        if (!loginCanceled.get()) {
                            waitScreenStage.show();
                            if (login.isValid()) {
                                Optional<? extends DBCredentials> credentials = login.getResult();
                                if (credentials.isPresent()) {
                                    // FIXME Can the following thread be split in a clearer name?
                                    Thread validateCredentialsAndShowMenu = new Thread(() -> {
                                        boolean credentialsAreValid = validateCredentials(credentials.get());
                                        if (credentialsAreValid) {
                                            Platform.runLater(() -> {
                                                showMainMenu();
                                                waitScreenStage.close();
                                            });
                                        } else {
                                            Platform.runLater(loginStage::show);
                                        }
                                    });
                                    validateCredentialsAndShowMenu.setUncaughtExceptionHandler(
                                            ThreadUtility.DEFAULT_THREAD_EXCEPTION_HANDLER);
                                    validateCredentialsAndShowMenu.start();
                                } else {
                                    LOGGER.log(Level.WARNING, "The login did not provide credentials");
                                    Platform.exit();
                                }
                            } else {
                                LOGGER.log(Level.INFO, "The login dialog classifies the input as invalid. "
                                        + "Show login dialog again.");
                                Platform.runLater(loginStage::show);
                            }
                        }
                    }
                });
            } else {
                String badConfigs = EnvironmentHandler.getResourceValue("badConfigs", loadedProfile.getProfileName());
                try {
                    Alert failureReport = EnvironmentHandler.DIALOG_FACTORY.createErrorAlert(badConfigs, badConfigs);
                    DialogFactory.showAndWait(failureReport)
                            .ifPresent(buttontype -> {
                                if (buttontype == ButtonType.OK) {
                                    Programs.CONFIGURATION_DIALOG.call();
                                }
                            });
                } catch (DialogCreationException ex) {
                    LOGGER.log(Level.SEVERE, "Could not create error dialog for user", ex);
                }
            }
        }
    }

    /**
     * This method is called when the application should stop, destroys resources and prepares for application exit.
     */
    @Override
    public void stop() {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

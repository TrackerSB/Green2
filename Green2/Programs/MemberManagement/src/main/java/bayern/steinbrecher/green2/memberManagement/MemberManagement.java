package bayern.steinbrecher.green2.memberManagement;

import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.DatabaseNotFoundException;
import bayern.steinbrecher.dbConnector.SimpleConnection;
import bayern.steinbrecher.dbConnector.SshConnection;
import bayern.steinbrecher.dbConnector.UnsupportedDatabaseException;
import bayern.steinbrecher.dbConnector.credentials.DBCredentials;
import bayern.steinbrecher.dbConnector.credentials.SimpleCredentials;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.SupportedDatabases;
import bayern.steinbrecher.dbConnector.scheme.SimpleColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.green2.memberManagement.elements.SplashScreen;
import bayern.steinbrecher.green2.memberManagement.elements.WaitScreen;
import bayern.steinbrecher.green2.memberManagement.login.Login;
import bayern.steinbrecher.green2.memberManagement.login.simple.SimpleLogin;
import bayern.steinbrecher.green2.memberManagement.login.ssh.SshLogin;
import bayern.steinbrecher.green2.memberManagement.menu.MainMenu;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Profile;
import bayern.steinbrecher.green2.sharedBasis.data.ProfileSettings;
import bayern.steinbrecher.green2.sharedBasis.data.Tables;
import bayern.steinbrecher.green2.sharedBasis.elements.ProfileChoice;
import bayern.steinbrecher.green2.sharedBasis.utility.Programs;
import bayern.steinbrecher.green2.sharedBasis.utility.StyleUtility;
import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.LoadException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 */
public class MemberManagement extends Application {

    private static final Logger LOGGER = Logger.getLogger(MemberManagement.class.getName());
    private static final long SPLASHSCREEN_DISPLAY_DURATION = 2500; // [ms]
    private Profile loadedProfile = null;
    private DBConnection dbConnection = null;

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
        Stage splashScreenStage = new Stage();
        try {
            new SplashScreen()
                    .embedStandaloneWizardPage(splashScreenStage, null);
        } catch (LoadException ex) {
            LOGGER.log(Level.WARNING, "Could not show splash screen to user. It is skipped.", ex);
            return;
        }
        StyleUtility.prepare(splashScreenStage);
        splashScreenStage.initModality(Modality.APPLICATION_MODAL);
        splashScreenStage.initStyle(StageStyle.TRANSPARENT);
        splashScreenStage.showingProperty()
                .addListener((obs, wasShowing, isShowing) -> {
                    if (isShowing) {
                        Runnable closeTask = () -> {
                            try {
                                Thread.sleep(SPLASHSCREEN_DISPLAY_DURATION);
                            } catch (InterruptedException ex) {
                                LOGGER.log(Level.WARNING,
                                        "The visualization of the splash screen has been interrupted", ex);
                            }
                            Platform.runLater(splashScreenStage::close);
                        };
                        new Thread(closeTask)
                                .start();
                    }
                });
        splashScreenStage.showAndWait();
    }

    private static void showWaitScreenWhile(ObservableBooleanValue showWhile) {
        var waitScreenStage = new Stage();
        try {
            new WaitScreen()
                    .embedStandaloneWizardPage(waitScreenStage, null);
        } catch (LoadException ex) {
            LOGGER.log(Level.WARNING, "Could not show wait screen. It is skipped.", ex);
            return;
        }
        StyleUtility.prepare(waitScreenStage);
        waitScreenStage.initModality(Modality.APPLICATION_MODAL);
        waitScreenStage.initStyle(StageStyle.TRANSPARENT);
        ChangeListener<Boolean> showWhileListener = (obs, hadToBeShown, hasToBeShown) -> {
            if (hasToBeShown) {
                waitScreenStage.show();
            } else {
                waitScreenStage.hide();
            }
        };
        showWhile.addListener(showWhileListener);
    }

    private Login<? extends DBCredentials> prepareLogin(Stage loginStage) throws LoadException {
        assert loadedProfile != null : "The preparation of the login requires a profile to be loaded first";

        Login<? extends DBCredentials> login;
        if (loadedProfile.get(ProfileSettings.USE_SSH)) {
            login = new SshLogin();
        } else {
            login = new SimpleLogin();
        }
        try {
            login.embedStandaloneWizardPage(loginStage, null);
        } catch (LoadException ex) {
            throw new LoadException("Could not generate login dialog", ex);
        }
        StyleUtility.prepare(loginStage);
        return login;
    }

    private Optional<DBConnection> establishDBConnection(DBCredentials credentials) {
        assert loadedProfile != null : "Establishing a connection requires a profile to be loaded first";

        SupportedDatabases dbms = loadedProfile.get(ProfileSettings.DBMS);
        String databaseHost = loadedProfile.getOrDefault(ProfileSettings.DATABASE_HOST, "localhost");
        int databasePort = loadedProfile.getOrDefault(ProfileSettings.DATABASE_PORT, dbms.getDefaultPort());
        String databaseName = loadedProfile.get(ProfileSettings.DATABASE_NAME);
        DBConnection dbConnection = null;

        Alert failureReport = null;
        try {
            try {
                if (credentials instanceof SimpleCredentials) {
                    dbConnection = new SimpleConnection(
                            dbms, databaseHost, databasePort, databaseName, (SimpleCredentials) credentials);
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
                failureReport = DialogUtility.createInfoAlert(checkInput, checkInput);
            } catch (UnknownHostException ex) {
                LOGGER.log(Level.INFO, null, ex);
                String checkConnection = EnvironmentHandler.getResourceValue("checkConnection");
                failureReport = DialogUtility.createInfoAlert(checkConnection, checkConnection);
            } catch (UnsupportedDatabaseException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                String noSupportedDatabase = EnvironmentHandler.getResourceValue("noSupportedDatabase");
                failureReport = DialogUtility.createErrorAlert(noSupportedDatabase, noSupportedDatabase);
            } catch (DatabaseNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Could not find database on host.", ex);
                String databaseNotFound = EnvironmentHandler.getResourceValue("databaseNotFound");
                failureReport = DialogUtility.createErrorAlert(databaseNotFound, databaseNotFound);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not connect due to an unhandled exception.", ex);
                String unexpectedAbort = EnvironmentHandler.getResourceValue("unexpectedAbort");
                failureReport = DialogUtility.createStacktraceAlert(ex, unexpectedAbort, unexpectedAbort);
            }
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not show error to user", ex);
        }
        if (failureReport != null) {
            DialogUtility.showAndWait(failureReport);
        }
        return Optional.ofNullable(dbConnection);
    }

    private boolean validateDBConnection() {
        assert dbConnection != null : "Cannot validate non existing database connection";

        Alert failureReport = null;
        try {
            try {
                if (!dbConnection.databaseExists()) {
                    LOGGER.log(Level.WARNING, "The database to connect to does not exist");
                    String databaseNotExistent = EnvironmentHandler.getResourceValue(
                            "couldntFindDatabase", dbConnection.getDatabaseName());
                    failureReport = DialogUtility.createErrorAlert(databaseNotExistent, databaseNotExistent);
                } else {
                    Map<TableScheme<?, ?>, Set<SimpleColumnPattern<?, ?>>> missingColumns = new HashMap<>();
                    for (TableScheme<?, ?> scheme : Tables.SCHEMES) {
                        dbConnection.createTableIfNotExists(scheme);
                        Set<SimpleColumnPattern<?, ?>> currentMissingColumns = dbConnection.getMissingColumns(scheme);
                        if (!currentMissingColumns.isEmpty()) {
                            missingColumns.put(scheme, currentMissingColumns);
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
                        failureReport = DialogUtility.createErrorAlert(invalidScheme, missingColumnsListingMessage);
                    }
                }
            } catch (QueryFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not validate database connection", ex);
                String validationFailed = EnvironmentHandler.getResourceValue("validationFailed");
                failureReport = DialogUtility.createErrorAlert(validationFailed, validationFailed);
            }
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not show error to user", ex);
        }
        if (failureReport == null) {
            return true;
        } else {
            DialogUtility.showAndWait(failureReport);
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

    private void showMainMenu(Stage menuStage) {
        assert dbConnection != null : "Cannot open main menu without established database connection";

        try {
            new MainMenu(dbConnection)
                    .embedStandaloneWizardPage(menuStage, null);
        } catch (LoadException ex) {
            throw new RuntimeException("Could not create main menu", ex);
        }
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

                Stage loginStage = new Stage();
                Login<? extends DBCredentials> login = prepareLogin(loginStage);
                BooleanBinding noStageIsVisible = loginStage.showingProperty().not()
                        .and(primaryStage.showingProperty().not());
                showWaitScreenWhile(noStageIsVisible);
                loginStage.show();

                loginStage.showingProperty().addListener((obs, wasShowing, isShowing) -> {
                    if (!isShowing) {
                        if (login.isValid()) {
                            Optional<? extends DBCredentials> credentials = login.getResult();
                            if (credentials.isPresent()) {
                                new Thread(() -> {
                                    boolean credentialsAreValid = validateCredentials(credentials.get());
                                    if (credentialsAreValid) {
                                        Platform.runLater(() -> showMainMenu(primaryStage));
                                    } else {
                                        Platform.runLater(loginStage::show);
                                    }
                                }).start();
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
                });
            } else {
                String badConfigs = EnvironmentHandler.getResourceValue("badConfigs", loadedProfile.getProfileName());
                try {
                    DialogUtility.showAndWait(DialogUtility.createErrorAlert(badConfigs, badConfigs))
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

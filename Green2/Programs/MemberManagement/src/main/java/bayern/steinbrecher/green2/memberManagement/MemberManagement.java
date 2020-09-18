package bayern.steinbrecher.green2.memberManagement;

import bayern.steinbrecher.dbConnector.SupportedDatabases;
import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.DatabaseNotFoundException;
import bayern.steinbrecher.dbConnector.SchemeCreationException;
import bayern.steinbrecher.dbConnector.SimpleConnection;
import bayern.steinbrecher.dbConnector.SshConnection;
import bayern.steinbrecher.dbConnector.UnsupportedDatabaseException;
import bayern.steinbrecher.dbConnector.credentials.SimpleCredentials;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.green2.memberManagement.elements.Splashscreen;
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
import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.LoadException;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the entry of the hole application.
 *
 * @author Stefan Huber
 */
public class MemberManagement extends Application {

    private static final Logger LOGGER = Logger.getLogger(MemberManagement.class.getName());
    private static final long SPLASHSCREEN_MILLIS = 2500;
    private Profile profile;
    private Stage menuStage;
    private DBConnection dbConnection;

    /**
     * Default constructor.
     */
    public MemberManagement() {
        super();
        List<String> availableProfiles = Profile.getAvailableProfiles();
        if (availableProfiles.isEmpty()) {
            Programs.CONFIGURATION_DIALOG.call();
            throw new IllegalStateException("The are no profiles which can be loaded.");
        } else if (availableProfiles.size() == 1) { //NOPMD - Do not ask user when there is no choice for selection.
            profile = EnvironmentHandler.loadProfile(availableProfiles.get(0), false);
        } else {
            Optional<Profile> requestedProfile = ProfileChoice.askForProfile(false);
            if (requestedProfile.isPresent()) {
                profile = EnvironmentHandler.loadProfile(requestedProfile.get());
            } else {
                Platform.exit();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws LoadException {
        menuStage = primaryStage;

        if (profile.isAllConfigurationsSet()) {
            Platform.setImplicitExit(false);

            //Show splashscreen
            Splashscreen.showSplashscreen(SPLASHSCREEN_MILLIS, new Stage());

            //Show waitscreen
            WaitScreen waitScreen = new WaitScreen();
            waitScreen.start(new Stage());

            //Show login
            Login<?> login;
            if (profile.getOrDefault(ProfileSettings.USE_SSH, true)) {
                login = new SshLogin();
            } else {
                login = new SimpleLogin();
            }
            Stage loginStage = new Stage();
            AtomicBoolean userAborted = new AtomicBoolean(false);
            loginStage.setOnCloseRequest(event -> userAborted.set(true));
            loginStage.showingProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    waitScreen.close();
                } else if (userAborted.get()) {
                    Platform.exit();
                } else {
                    waitScreen.show();
                }
            });
            loginStage.setScene(new Scene(login.generateEmbeddableWizardPage().getRoot()));
            loginStage.show();

            CompletableFuture.supplyAsync(
                    () -> {
                        dbConnection = getConnection(login, waitScreen)
                                .orElseThrow(() -> new IllegalStateException("Could not create a connection"));
                        return dbConnection;
                    })
                    //Check existence of database
                    .thenRunAsync(() -> {
                        if (!dbConnection.databaseExists()) {
                            String databaseNotExistent = EnvironmentHandler.getResourceValue(
                                    "couldntFindDatabase", dbConnection.getDatabaseName());
                            Platform.runLater(() -> {
                                try {
                                    DialogUtility.createErrorAlert(databaseNotExistent, databaseNotExistent)
                                            .show();
                                } catch (DialogCreationException ex) {
                                    LOGGER.log(Level.SEVERE, "Could not create error dialog for user", ex);
                                }
                            });
                            throw new IllegalStateException(databaseNotExistent);
                        }
                    })
                    //Check existence of tables and create them if missing
                    .thenRunAsync(() -> {
                        Tables.SCHEMES.forEach(scheme -> {
                            try {
                                dbConnection.createTableIfNotExists(scheme);
                            } catch (SchemeCreationException ex) {
                                String couldntCreateScheme = EnvironmentHandler.getResourceValue("couldntCreateScheme");
                                Platform.runLater(() -> {
                                    try {
                                        DialogUtility.createErrorAlert(couldntCreateScheme, couldntCreateScheme)
                                                .show();
                                    } catch (DialogCreationException exx) {
                                        LOGGER.log(Level.SEVERE, "Could not create error dialog for user", exx);
                                    }
                                });
                                throw new CompletionException("Some tables are missing and could not be created", ex);
                            }
                        });
                    })
                    //Check whether every table has all its required columns
                    .thenRunAsync(() -> {
                        Tables.SCHEMES.forEach(tableScheme -> {
                            Optional<String> missingColumnsString = dbConnection.getMissingColumnsString(tableScheme);
                            if (missingColumnsString.isPresent()) {
                                String invalidScheme = EnvironmentHandler.getResourceValue("invalidScheme");
                                String message = invalidScheme + "\n" + missingColumnsString;
                                Platform.runLater(() -> {
                                    try {
                                        DialogUtility.createErrorAlert(message, invalidScheme)
                                                .show();
                                    } catch (DialogCreationException ex) {
                                        LOGGER.log(Level.SEVERE, "Could not create error dialog for user", ex);
                                    }
                                });
                                throw new CompletionException(new IllegalStateException(message));
                            }
                        });
                    })
                    //Handle error occured at any stage
                    .whenCompleteAsync((voidResult, throwable) -> {
                        if (throwable == null) {
                            menuStage.showingProperty().addListener((obs, oldVal, newVal) -> {
                                if (newVal) {
                                    waitScreen.close();
                                } else {
                                    Platform.exit();
                                }
                            });

                            try {
                                Pane mainMenu = new MainMenu(dbConnection)
                                        .generateEmbeddableWizardPage()
                                        .getRoot();
                                menuStage.setScene(new Scene(mainMenu));
                                menuStage.setTitle(EnvironmentHandler.getResourceValue("chooseProgram"));
                                menuStage.setResizable(false);
                            } catch (LoadException ex) {
                                LOGGER.log(Level.SEVERE, "Could not create main menu", ex);
                            }
                            Platform.runLater(() -> {
                                menuStage.show();
                            });
                        } else {
                            LOGGER.log(Level.SEVERE, null, throwable);
                            try {
                                DialogUtility.createStacktraceAlert(throwable, "Application could not start.")
                                        .showAndWait();
                            } catch (DialogCreationException ex) {
                                LOGGER.log(Level.SEVERE, "Could not create error dialog for user", ex);
                            }
                            Platform.exit();
                        }
                    });
        } else {
            String badConfigs = MessageFormat.format(
                    EnvironmentHandler.getResourceValue("badConfigs"), profile.getProfileName());
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

    /**
     * This method is called when the application should stop, destroys resources and prepares for application exit.
     */
    @Override
    public void stop() {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }

    /**
     * Asks the user for the needed logindata as long as the inserted data is not correct or the user aborts. This
     * method should NOT be called by JavaFX Application Thread..
     *
     * @param login      The loginframe used to ask the user.
     * @param waitScreen The waitscreen to show when trying to connect to the server.
     * @return {@link Optional#empty()} only if the connection could not be established. E.g. the user closed the window
     * or the configured connection is not reachable.
     */
    private Optional<? extends DBConnection> getConnection(Login<?> login, WaitScreen waitScreen) {
        String databaseHost = profile.getOrDefault(ProfileSettings.DATABASE_HOST, "localhost");
        int databasePort = profile.getOrDefault(
                ProfileSettings.DATABASE_PORT, profile.get(ProfileSettings.DBMS).getDefaultPort());
        String databaseName = profile.get(ProfileSettings.DATABASE_NAME);
        SupportedDatabases dbms = profile.get(ProfileSettings.DBMS);

        return login.getResult()
                .map(dbCredentials -> {
                    DBConnection connection = null;
                    Callable<DBConnection> createConnection;
                    if (profile.getOrDefault(ProfileSettings.USE_SSH, false)) {
                        assert dbCredentials.getClass() == SshCredentials.class :
                                "Profile is configured to use SSH, but there were no SshCredentials requested.";
                        SshCredentials credentials = (SshCredentials) dbCredentials;

                        String sshHost = profile.getOrDefault(ProfileSettings.SSH_HOST, "localhost");
                        int sshPort = profile.getOrDefault(ProfileSettings.SSH_PORT, 22);
                        Charset sshCharset = profile.getOrDefault(ProfileSettings.SSH_CHARSET, StandardCharsets.UTF_8);

                        createConnection = () -> new SshConnection(dbms, databaseHost, databasePort, databaseName,
                                sshHost, sshPort, sshCharset, credentials);

                    } else {
                        assert dbCredentials.getClass() == SimpleCredentials.class :
                                "Profile is configured to directly connect to a database, "
                                        + "but it got no SimpleCredentials.";
                        SimpleCredentials credentials = (SimpleCredentials) dbCredentials;
                        createConnection
                                = () -> new SimpleConnection(
                                dbms, databaseHost, databasePort, databaseName, credentials);
                    }

                    Optional<Alert> userReport = Optional.empty();
                    boolean retryConnection = false;
                    try {
                        try {
                            connection = createConnection.call();
                        } catch (AuthException ex) {
                            LOGGER.log(Level.FINE, null, ex);
                            String checkInput = EnvironmentHandler.getResourceValue("checkInput");
                            userReport = Optional.of(DialogUtility.createInfoAlert(checkInput, checkInput));
                            retryConnection = true;
                        } catch (UnknownHostException | ConnectException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                            String checkConnection = EnvironmentHandler.getResourceValue("checkConnection");
                            userReport = Optional.of(
                                    DialogUtility.createStacktraceAlert(ex, checkConnection, checkConnection));
                        } catch (UnsupportedDatabaseException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            String noSupportedDatabase = EnvironmentHandler.getResourceValue("noSupportedDatabase");
                            userReport = Optional.of(DialogUtility.createErrorAlert(noSupportedDatabase));
                        } catch (DatabaseNotFoundException ex) {
                            LOGGER.log(Level.SEVERE, "Could not find database \""
                                    + databaseName + "\" on host \"" + databaseHost + "\".", ex);
                            String databaseNotFound = EnvironmentHandler.getResourceValue("databaseNotFound");
                            userReport = Optional.of(DialogUtility.createErrorAlert(databaseNotFound));
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Could not connect due to an unhandled exception.", ex);
                            String unexpectedAbort = EnvironmentHandler.getResourceValue("unexpectedAbort");
                            userReport = Optional.of(
                                    DialogUtility.createStacktraceAlert(ex, unexpectedAbort, unexpectedAbort));
                        }
                    } catch (DialogCreationException ex) {
                        LOGGER.log(Level.WARNING, "Could not show error to user", ex);
                    }
                    Platform.runLater(waitScreen::close);
                    userReport.ifPresent(DialogUtility::showAndWait);
                    if (retryConnection) {
                        // login.reset(); // FIXME Required?
                        connection = getConnection(login, waitScreen)
                                .orElse(null);
                    }
                    return connection;
                });
    }

    /**
     * The main method.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
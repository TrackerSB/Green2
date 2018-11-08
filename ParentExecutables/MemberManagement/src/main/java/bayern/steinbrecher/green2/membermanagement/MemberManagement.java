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
package bayern.steinbrecher.green2.membermanagement;

import bayern.steinbrecher.green2.connection.AuthException;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.connection.DatabaseNotFoundException;
import bayern.steinbrecher.green2.connection.SimpleConnection;
import bayern.steinbrecher.green2.connection.InvalidSchemeException;
import bayern.steinbrecher.green2.connection.SchemeCreationException;
import bayern.steinbrecher.green2.connection.SshConnection;
import bayern.steinbrecher.green2.connection.UnsupportedDatabaseException;
import bayern.steinbrecher.green2.connection.credentials.SimpleCredentials;
import bayern.steinbrecher.green2.connection.credentials.SshCredentials;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.ProfileChoice;
import bayern.steinbrecher.green2.elements.Splashscreen;
import bayern.steinbrecher.green2.elements.WaitScreen;
import bayern.steinbrecher.green2.login.Login;
import bayern.steinbrecher.green2.login.ssh.SshLogin;
import bayern.steinbrecher.green2.login.simple.SimpleLogin;
import bayern.steinbrecher.green2.menu.MainMenu;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.Programs;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

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
    public void start(Stage primaryStage) {
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
            loginStage.showingProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    waitScreen.close();
                } else if (login.userAborted()) {
                    Platform.exit();
                } else {
                    waitScreen.show();
                }
            });
            login.start(loginStage);

            CompletableFuture.supplyAsync(
                    () -> {
                        dbConnection = getConnection(login, waitScreen)
                                .orElseThrow(() -> new IllegalStateException("Could not create a connection"));
                        return dbConnection;
                    })
                    //Check existence of tables
                    .thenRunAsync(() -> {
                        try {
                            if (!dbConnection.createTablesIfNeeded()) {
                                throw new IllegalStateException(
                                        "Some tables are missing and the user did not confirm the creation of them.");
                            }
                        } catch (SchemeCreationException ex) {
                            String couldntCreateScheme = EnvironmentHandler.getResourceValue("couldntCreateScheme");
                            Platform.runLater(
                                    () -> DialogUtility.createErrorAlert(null, couldntCreateScheme, couldntCreateScheme)
                                            .show());
                            throw new CompletionException(ex);
                        }
                    })
                    //Check whether every table has all its required columns
                    .thenRunAsync(() -> {
                        String missingColumnsString = dbConnection.getMissingColumnsString();
                        if (!missingColumnsString.isEmpty()) {
                            String invalidScheme = EnvironmentHandler.getResourceValue("invalidScheme");
                            String message = invalidScheme + "\n" + missingColumnsString;
                            Platform.runLater(
                                    () -> DialogUtility.createErrorAlert(null, message, invalidScheme).show());
                            throw new CompletionException(new InvalidSchemeException(message));
                        }
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

                            Platform.runLater(() -> {
                                new MainMenu(dbConnection).start(menuStage);
                                menuStage.show();
                            });
                        } else {
                            LOGGER.log(Level.SEVERE, null, throwable);
                            Platform.exit();
                        }
                    });
        } else {
            String badConfigs = MessageFormat.format(
                    EnvironmentHandler.getResourceValue("badConfigs"), profile.getProfileName());
            DialogUtility.showAndWait(DialogUtility.createErrorAlert(null, badConfigs, badConfigs))
                    .ifPresent(buttontype -> {
                        if (buttontype == ButtonType.OK) {
                            Programs.CONFIGURATION_DIALOG.call();
                        }
                    });
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
     * @param login The loginframe used to ask the user.
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
                        connection = createConnection.call();
                    } catch (AuthException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                        String checkInput = EnvironmentHandler.getResourceValue("checkInput");
                        userReport = Optional.of(DialogUtility.createInfoAlert(null, checkInput, checkInput));
                        retryConnection = true;
                    } catch (UnknownHostException | ConnectException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                        String checkConnection = EnvironmentHandler.getResourceValue("checkConnection");
                        userReport = Optional.of(
                                DialogUtility.createStacktraceAlert(null, ex, checkConnection, checkConnection));
                    } catch (UnsupportedDatabaseException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                        String noSupportedDatabase = EnvironmentHandler.getResourceValue("noSupportedDatabase");
                        userReport = Optional.of(DialogUtility.createErrorAlert(null, noSupportedDatabase));
                    } catch (DatabaseNotFoundException ex) {
                        LOGGER.log(Level.SEVERE, "Could not find database \""
                                + databaseName + "\" on host \"" + databaseHost + "\".", ex);
                        String databaseNotFound = EnvironmentHandler.getResourceValue("databaseNotFound");
                        userReport = Optional.of(DialogUtility.createErrorAlert(null, databaseNotFound));
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Could not connect due to an unhandled exception.", ex);
                        String unexpectedAbort = EnvironmentHandler.getResourceValue("unexpectedAbort");
                        userReport = Optional.of(
                                DialogUtility.createStacktraceAlert(null, ex, unexpectedAbort, unexpectedAbort));
                    }
                    Platform.runLater(waitScreen::close);
                    userReport.ifPresent(DialogUtility::showAndWait);
                    if (retryConnection) {
                        login.reset();
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

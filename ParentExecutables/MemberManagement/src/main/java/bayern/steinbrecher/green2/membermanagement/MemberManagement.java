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
import bayern.steinbrecher.green2.connection.DefaultConnection;
import bayern.steinbrecher.green2.connection.InvalidSchemeException;
import bayern.steinbrecher.green2.connection.SchemeCreationException;
import bayern.steinbrecher.green2.connection.SshConnection;
import bayern.steinbrecher.green2.connection.UnsupportedDatabaseException;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.ProfileChoice;
import bayern.steinbrecher.green2.elements.Splashscreen;
import bayern.steinbrecher.green2.elements.WaitScreen;
import bayern.steinbrecher.green2.login.Login;
import bayern.steinbrecher.green2.login.LoginKey;
import bayern.steinbrecher.green2.login.ssh.SshLogin;
import bayern.steinbrecher.green2.login.standard.DefaultLogin;
import bayern.steinbrecher.green2.menu.MainMenu;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.Programs;
import bayern.steinbrecher.green2.utility.ThreadUtility;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private transient Profile profile;
    private transient Stage menuStage;
    private transient DBConnection dbConnection;

    /**
     * Default constructor.
     */
    public MemberManagement() {
        super();
        List<String> availableProfiles = Profile.getAvailableProfiles();
        if (availableProfiles.isEmpty()) {
            Programs.CONFIGURATION_DIALOG.call();
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
            //Show splashscreen
            Splashscreen.showSplashscreen(SPLASHSCREEN_MILLIS, new Stage());

            //Show waitscreen
            WaitScreen waitScreen = new WaitScreen();
            waitScreen.start(new Stage());

            //Show login
            Login login = createLogin();
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

            CompletableFuture.supplyAsync(() -> getConnection(login, waitScreen))
                    //Get connection
                    .thenAcceptAsync(connection -> {
                        dbConnection = connection.orElseThrow(
                                () -> new IllegalStateException("Could not create a connection"));
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
                            Platform.runLater(() -> waitScreen.close());
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

    private Login createLogin() {
        Login login;
        if (profile.getOrDefault(ProfileSettings.USE_SSH, true)) {
            login = new SshLogin();
        } else {
            login = new DefaultLogin();
        }
        return login;
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

    private void handleAuthException(Login login, WaitScreen waitScreen, Exception cause) {
        Alert dialog;
        if (cause instanceof UnknownHostException || cause instanceof ConnectException) {
            String checkConnection = EnvironmentHandler.getResourceValue("checkConnection");
            dialog = DialogUtility.createStacktraceAlert(null, cause, checkConnection, checkConnection);
        } else if (cause instanceof AuthException) {
            String checkInput = EnvironmentHandler.getResourceValue("checkInput");
            dialog = DialogUtility.createInfoAlert(null, checkInput, checkInput);
        } else {
            LOGGER.log(Level.SEVERE, "Not action specified for: {0}", cause);
            String unexpectedAbort = EnvironmentHandler.getResourceValue("unexpectedAbort");
            dialog = DialogUtility.createErrorAlert(null, unexpectedAbort, unexpectedAbort);
        }

        dialog.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (dialog.getResult() == ButtonType.OK) {
                    login.reset();
                    synchronized (this) {
                        notifyAll();
                    }
                } else {
                    Platform.exit();
                }
            }
        });

        Platform.runLater(() -> {
            waitScreen.close();
            dialog.show();
        });
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
    private Optional<DBConnection> getConnection(Login login, WaitScreen waitScreen) {
        Optional<DBConnection> con;

        Optional<Map<LoginKey, String>> loginInfos = login.getResult();
        if (loginInfos.isPresent()) {
            Map<LoginKey, String> loginValues = loginInfos.get();
            try {
                String databaseHost = profile.getOrDefault(ProfileSettings.DATABASE_HOST, "localhost");
                int databasePort = profile.getOrDefault(ProfileSettings.DATABASE_PORT,
                        profile.get(ProfileSettings.DBMS).getDefaultPort());
                String databaseUsername = loginValues.get(LoginKey.DATABASE_USERNAME);
                String databasePassword = loginValues.get(LoginKey.DATABASE_PASSWORD);
                String databaseName = profile.getOrDefault(ProfileSettings.DATABASE_NAME, "dbname");
                if (profile.getOrDefault(ProfileSettings.USE_SSH, true)) {
                    String sshHost = profile.getOrDefault(ProfileSettings.SSH_HOST, "localhost");
                    String sshUsername = loginValues.get(LoginKey.SSH_USERNAME);
                    String sshPassword = loginValues.get(LoginKey.SSH_PASSWORD);
                    Charset sshCharset = profile.getOrDefault(ProfileSettings.SSH_CHARSET, StandardCharsets.UTF_8);
                    con = Optional.of(new SshConnection(sshHost, sshUsername, sshPassword, databaseHost, databasePort,
                            databaseUsername, databasePassword, databaseName, sshCharset));
                } else {
                    con = Optional.of(new DefaultConnection(
                            databaseHost, databasePort, databaseUsername, databasePassword, databaseName));
                }
            } catch (UnknownHostException | AuthException ex) {
                handleAuthException(login, waitScreen, ex);

                ThreadUtility.waitWhile(this, login.wouldShowBinding().not());

                con = getConnection(login, waitScreen);
            } catch (UnsupportedDatabaseException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                DialogUtility.showAndWait(DialogUtility.createErrorAlert(
                        null, EnvironmentHandler.getResourceValue("noSupportedDatabase")));
                con = Optional.empty();
            }
        } else {
            con = Optional.empty();
        }

        return con;
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

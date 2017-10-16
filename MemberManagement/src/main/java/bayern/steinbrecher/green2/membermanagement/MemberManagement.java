/* 
 * Copyright (C) 2017 Stefan Huber
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
import bayern.steinbrecher.green2.connection.SchemeCreationException;
import bayern.steinbrecher.green2.connection.SshConnection;
import bayern.steinbrecher.green2.connection.UnsupportedDatabaseException;
import bayern.steinbrecher.green2.data.ConfigKey;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.ProfileChoice;
import bayern.steinbrecher.green2.elements.Splashscreen;
import bayern.steinbrecher.green2.elements.WaitScreen;
import bayern.steinbrecher.green2.login.Login;
import bayern.steinbrecher.green2.login.LoginKey;
import bayern.steinbrecher.green2.login.ssh.SshLogin;
import bayern.steinbrecher.green2.login.standard.DefaultLogin;
import bayern.steinbrecher.green2.menu.Menu;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.ProgramCaller;
import bayern.steinbrecher.green2.utility.ServiceFactory;
import bayern.steinbrecher.green2.utility.ThreadUtility;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Represents the entry of the hole application.
 *
 * @author Stefan Huber
 */
public class MemberManagement extends Application {

    private static final long SPLASHSCREEN_MILLIS = 2500;
    private Profile profile;
    private Stage menuStage;
    private DBConnection dbConnection = null;

    /**
     * Default constructor.
     */
    public MemberManagement() {
        List<String> availableProfiles = Profile.getAvailableProfiles();
        if (availableProfiles.size() < 1) {
            ProgramCaller.startGreen2ConfigDialog();
        } else if (availableProfiles.size() == 1) {
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
    public void start(Stage primaryStage) throws IOException {
        menuStage = primaryStage;
        Platform.setImplicitExit(false);

        if (profile.isAllConfigurationsSet()) {
            Splashscreen.showSplashscreen(SPLASHSCREEN_MILLIS, new Stage());
            Login login = createLogin();
            WaitScreen waitScreen = createWaitScreen(login);
            createConnectionService(login, waitScreen).start();
        } else {
            String badConfigs = MessageFormat.format(
                    EnvironmentHandler.getResourceValue("badConfigs"), profile.getProfileName());
            DialogUtility.createErrorAlert(null, badConfigs, badConfigs)
                    .showAndWait()
                    .ifPresent(buttontype -> {
                        if (buttontype == ButtonType.OK) {
                            ProgramCaller.startGreen2ConfigDialog();
                        }
                    });
        }
    }

    private Login createLogin() {
        Login login;
        if (profile.getOrDefault(ConfigKey.USE_SSH, true)) {
            login = new SshLogin();
        } else {
            login = new DefaultLogin();
        }
        return login;
    }

    /*
     * This method creates a {@link WaitScreen}, connects {@code login} to it
     * AND calls {@link Application#start(Stage)} of login.
     */
    private WaitScreen createWaitScreen(Login login) throws IOException {
        WaitScreen waitScreen = new WaitScreen();
        waitScreen.start(new Stage());
        Stage loginStage = new Stage();
        loginStage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                waitScreen.close();
            } else if (!login.userAborted()) {
                waitScreen.show();
            } else {
                Platform.exit();
            }
        });
        login.start(loginStage);
        return waitScreen;
    }

    private Service<Optional<DBConnection>> createConnectionService(Login login, WaitScreen waitScreen) {
        Service<Optional<DBConnection>> connectionService
                = ServiceFactory.createService(() -> getConnection(login, waitScreen));

        connectionService.setOnSucceeded(wse -> {
            Optional<DBConnection> optDBConnection = connectionService.getValue();
            if (optDBConnection.isPresent()) {
                dbConnection = optDBConnection.get();
                try {
                    dbConnection.createTablesIfNeeded();
                } catch (SchemeCreationException ex) {
                    String couldntCreateScheme = EnvironmentHandler.getResourceValue("couldntCreateScheme");
                    DialogUtility.createErrorAlert(null, couldntCreateScheme, couldntCreateScheme).showAndWait();
                    Logger.getLogger(MemberManagement.class.getName()).log(Level.SEVERE, null, ex);
                    Platform.exit();
                }
                if (!dbConnection.hasValidSchemes()) {
                    String invalidScheme = EnvironmentHandler.getResourceValue("invalidScheme");
                    DialogUtility.createErrorAlert(null, invalidScheme, invalidScheme).showAndWait();
                    Logger.getLogger(MemberManagement.class.getName()).log(Level.SEVERE, invalidScheme);
                    Platform.exit();
                }

                menuStage.showingProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        waitScreen.close();
                    } else {
                        Platform.exit();
                    }
                });

                try {
                    new Menu(dbConnection).start(menuStage);
                } catch (Exception ex) {
                    Logger.getLogger(MemberManagement.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return connectionService;
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
        Platform.runLater(() -> {
            Alert dialog;
            if (cause instanceof UnknownHostException || cause instanceof ConnectException) {
                String checkConnection = EnvironmentHandler.getResourceValue("checkConnection");
                dialog = DialogUtility.createStacktraceAlert(null, cause, checkConnection, checkConnection);
            } else if (cause instanceof AuthException) {
                String checkInput = EnvironmentHandler.getResourceValue("checkInput");
                dialog = DialogUtility.createInfoAlert(null, checkInput, checkInput);
            } else {
                Logger.getLogger(MemberManagement.class.getName())
                        .log(Level.SEVERE, "Not action specified for: {0}", cause);
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

            waitScreen.close();
            dialog.show();
        });
    }

    /**
     * Asks the user for the needed logindata as long as the inserted data is not correct or the user aborts. This
     * method should NOT be called by JavaFX Application Thread. E.g. use
     * {@link ServiceFactory#createService(Callable)}.
     *
     * @param login The loginframe used to ask the user.
     * @param waitScreen The waitscreen to show when trying to connect to the server.
     * @return {@link Optional#empty()} only if the connection could not be established. E.g. the user closed the window
     * or the configured connection is not reachable.
     */
    private Optional<DBConnection> getConnection(Login login, WaitScreen waitScreen) {
        DBConnection con = null;

        Optional<Map<LoginKey, String>> loginInfos = login.getLoginInformation();
        if (loginInfos.isPresent()) {
            Map<LoginKey, String> loginValues = loginInfos.get();
            try {
                if (profile.getOrDefault(ConfigKey.USE_SSH, true)) {
                    con = new SshConnection(
                            profile.getOrDefault(ConfigKey.SSH_HOST, "localhost"),
                            loginValues.get(LoginKey.SSH_USERNAME),
                            loginValues.get(LoginKey.SSH_PASSWORD),
                            profile.getOrDefault(ConfigKey.DATABASE_HOST, "localhost"),
                            loginValues.get(LoginKey.DATABASE_USERNAME),
                            loginValues.get(LoginKey.DATABASE_PASSWORD),
                            profile.getOrDefault(ConfigKey.DATABASE_NAME, "dbname"),
                            profile.getOrDefault(ConfigKey.SSH_CHARSET, StandardCharsets.ISO_8859_1));
                } else {
                    con = new DefaultConnection(
                            profile.getOrDefault(ConfigKey.DATABASE_HOST, "localhost"),
                            loginValues.get(LoginKey.DATABASE_USERNAME),
                            loginValues.get(LoginKey.DATABASE_PASSWORD),
                            profile.getOrDefault(ConfigKey.DATABASE_NAME, "dbname"));
                }
            } catch (UnknownHostException | AuthException ex) {
                handleAuthException(login, waitScreen, ex);

                ThreadUtility.waitWhile(this, login.wouldShowBinding().not());

                return getConnection(login, waitScreen);
            } catch (UnsupportedDatabaseException ex) {
                Logger.getLogger(MemberManagement.class.getName()).log(Level.SEVERE, null, ex);
                DialogUtility.createErrorAlert(null, EnvironmentHandler.getResourceValue("noSupportedDatabase"));
            }
        }

        return Optional.ofNullable(con);
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

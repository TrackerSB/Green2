package bayern.steinbrecher.green2.sharedBasis.elements;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Profile;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Represents a dialog for choosing the profile to use.
 *
 * @author Stefan Huber
 */
public class ProfileChoice extends Application {

    private static final Logger LOGGER = Logger.getLogger(ProfileChoice.class.getName());
    private static final String LAST_PROFILE_KEY = "defaultProfile";
    private Stage stage;
    private Profile profile;
    private boolean created;
    private final GridPane profilePane = new GridPane();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        Label choiceLabel = new Label(EnvironmentHandler.getResourceValue("chooseProfile"));

        int currentRowIndex = 0;
        List<String> profiles = Profile.getAvailableProfiles();
        profiles.sort(String::compareTo);
        String editLabel = EnvironmentHandler.getResourceValue("edit");
        String deleteLabel = EnvironmentHandler.getResourceValue("delete");
        //PMD: For each profile separate elements have to be created.
        for (String profileName : profiles) {
            Label name = new Label(profileName); //NOPMD
            Button edit = new Button(editLabel, EnvironmentHandler.ImageSet.EDIT.getAsImageView()); //NOPMD
            edit.setOnAction(evt -> editProfile(profileName));
            Button delete = new Button(deleteLabel, EnvironmentHandler.ImageSet.TRASH.getAsImageView()); //NOPMD
            delete.setOnAction(evt -> askForDeleteProfile(profileName));
            profilePane.addRow(currentRowIndex, name, edit, delete);
            currentRowIndex++;
        }

        Button create = new Button(
                EnvironmentHandler.getResourceValue("create"), EnvironmentHandler.ImageSet.ADD.getAsImageView());
        create.setOnAction(evt -> {
            created = true;
            primaryStage.close();
        });

        Scene scene = new Scene(new VBox(choiceLabel, profilePane, create));
        scene.getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle(EnvironmentHandler.getResourceValue("chooseProfile"));
        primaryStage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());

        primaryStage.showAndWait();

        if (created) {
            String newConfigBaseName = EnvironmentHandler.getResourceValue("newConfigname");
            List<String> availableProfiles = Profile.getAvailableProfiles();
            Random random = new Random();
            String newConfigName = newConfigBaseName;
            while (availableProfiles.contains(newConfigName)) {
                //CHECKSTYLE.OFF: MagicNumber - Choosing 1000 is quite random and has no special matter.
                newConfigName = newConfigBaseName + " (" + random.nextInt(1000) + ")";
                //CHECKSTYLE.ON: MagicNumber
            }
            profile = new Profile(newConfigName, true);
        }
    }

    private void editProfile(String profileName) {
        profile = new Profile(profileName, false);
        stage.close();
    }

    private void askForDeleteProfile(String profileName) {
        String message = MessageFormat.format(EnvironmentHandler.getResourceValue("reallyDelete"), profileName);
        try {
            Alert confirmation
                    = DialogUtility.createConfirmationAlert(Alert.AlertType.CONFIRMATION, message);
            DialogPane dialogPane = confirmation.getDialogPane();
            ((Button) dialogPane.lookupButton(ButtonType.YES)).setDefaultButton(false);
            ((Button) dialogPane.lookupButton(ButtonType.NO)).setDefaultButton(true);
            DialogUtility.showAndWait(confirmation)
                    .ifPresent(buttonType -> {
                        if (buttonType == ButtonType.YES) {
                            int rowIndexOfDeletedRow = -1;
                            for (Node cell : profilePane.getChildren()) {
                                if (cell instanceof Label && ((Label) cell).getText().equals(profileName)) {
                                    rowIndexOfDeletedRow = GridPane.getRowIndex(cell);
                                    break;
                                }
                            }

                            if (rowIndexOfDeletedRow == -1) {
                                throw new IllegalArgumentException("Profile " + profileName + " not found");
                            } else {
                                List<Node> nodesToDelete = new ArrayList<>();
                                for (Node cell : profilePane.getChildren()) {
                                    int currentRowIndex = GridPane.getRowIndex(cell);
                                    if (currentRowIndex > rowIndexOfDeletedRow) {
                                        GridPane.setRowIndex(cell, currentRowIndex - 1);
                                    } else if (currentRowIndex == rowIndexOfDeletedRow) {
                                        nodesToDelete.add(cell);
                                    }
                                }

                                profilePane.getChildren().removeAll(nodesToDelete);
                                try {
                                    new Profile(profileName, false).deleteProfile();
                                } catch (IOException ex) {
                                    LOGGER.log(Level.SEVERE, "The profile could not be deleted.", ex);
                                }
                                stage.sizeToScene();
                            }
                        }
                    });
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.SEVERE, "Could not ask user for delete confirmation", ex);
        }
    }

    /**
     * Returns an {@link Optional} containing the selected profile if any. If the window is not closed it always returns
     * {@link Optional#empty()}.
     *
     * @return An {@link Optional} containing the selected profile if any. If the window is not closed it always returns
     * {@link Optional#empty()}.
     */
    public Optional<Profile> getProfile() {
        return Optional.ofNullable(profile);
    }

    /**
     * Creates a dialog for selecting a profile. This method blocks until the dialog is closed.
     *
     * @param editable {@code true} only if profiles should be able to edit and/or to delete.
     * @return An {@link Optional} which contains the selected profile if any.
     */
    public static Optional<Profile> askForProfile(boolean editable) {
        Optional<Profile> profile;
        if (editable) {
            ProfileChoice choice = new ProfileChoice();
            choice.start(new Stage());
            profile = Optional.ofNullable(choice.profile);
        } else {
            List<String> availableProfiles = Profile.getAvailableProfiles();
            String initialProfile;
            String lastProfile = EnvironmentHandler.PREFERENCES_USER_NODE.get(LAST_PROFILE_KEY, null);
            if (availableProfiles.contains(lastProfile)) {
                initialProfile = lastProfile;
            } else {
                initialProfile = null;
            }

            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(initialProfile, availableProfiles);
            DialogPane dialogPane = choiceDialog.dialogPaneProperty().get();
            dialogPane.getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
            ((Stage) dialogPane.getScene().getWindow()).getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
            Platform.runLater(() -> dialogPane.lookupButton(ButtonType.OK).requestFocus());

            Optional<String> profileName = choiceDialog.showAndWait();
            if (profileName.isPresent()) {
                EnvironmentHandler.PREFERENCES_USER_NODE.put(LAST_PROFILE_KEY, profileName.get());
                profile = Optional.of(new Profile(profileName.get(), false));
            } else {
                profile = Optional.empty();
            }
        }
        return profile;
    }
}

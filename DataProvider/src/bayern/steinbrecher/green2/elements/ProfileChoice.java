/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.utility.DialogUtility;
import javafx.application.Application;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Represents a dialog for choosing the profile to use.
 *
 * @author Stefan Huber
 */
public class ProfileChoice extends Application {

    private Stage stage;
    private Profile profile;
    private boolean created = false;
    private GridPane profilePane = new GridPane();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        Label choiceLabel = new Label(DataProvider.getResourceValue("chooseProfile"));

        profilePane.setHgap(10);
        profilePane.setVgap(10);

        int currentRowIndex = 0;
        List<String> profiles = Profile.getAvailableProfiles();
        profiles.sort(String::compareTo);
        String editLabel = DataProvider.getResourceValue("edit");
        String deleteLabel = DataProvider.getResourceValue("delete");
        for (String profileName : profiles) {
            Label name = new Label(profileName);
            Button edit = new Button(editLabel);
            edit.setOnAction(evt -> editProfile(profileName));
            Button delete = new Button(deleteLabel);
            delete.setOnAction(evt -> askForDeleteProfile(profileName));
            profilePane.addRow(currentRowIndex, name, edit, delete);
            currentRowIndex++;
        }

        Button create = new Button(DataProvider.getResourceValue("create"));
        create.setOnAction(evt -> {
            created = true;
            stage.close();
        });

        Scene scene = new Scene(new VBox(10, choiceLabel, profilePane, create));
        scene.getStylesheets().add(DataProvider.STYLESHEET_PATH);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(DataProvider.getResourceValue("chooseProfile"));
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());

        stage.showAndWait();

        if (created) {
            String newConfigName = DataProvider.getResourceValue("newConfigname");
            List<String> availableProfiles = Profile.getAvailableProfiles();
            Random random = new Random();
            while (availableProfiles.contains(newConfigName)) {
                newConfigName = newConfigName + " (" + random.nextInt(1000) + ")";
            }
            profile = new Profile(newConfigName, true);
        }
    }

    private void editProfile(String profileName) {
        profile = new Profile(profileName, false);
        stage.close();
    }

    private void askForDeleteProfile(String profileName) {
        String message = MessageFormat.format(DataProvider.getResourceValue("reallyDelete"), profileName);
        Alert confirmation
                = DialogUtility.createAlert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        DialogPane dialogPane = confirmation.getDialogPane();
        ((Button) dialogPane.lookupButton(ButtonType.YES)).setDefaultButton(false);
        ((Button) dialogPane.lookupButton(ButtonType.NO)).setDefaultButton(true);
        confirmation.showAndWait()
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
                            new Profile(profileName, false).deleteProfile();
                        }
                    }
                });

    }

    /**
     * Returns an {@code Optional} containing the selected profile if any. If the window is not closed it always returns {@code Optional.empty()}.
     *
     * @return An {@code Optional} containing the selected profile if any. If the window is not closed it always returns {@code Optional.empty()}.
     */
    public Optional<Profile> getProfile() {
        return Optional.ofNullable(profile);
    }

    /**
     * Creates a dialog for selecting a profile. This method blocks until the
     * dialog is closed.
     *
     * @param editable {@code true} only if profiles should be able to edit and/or to delete.
     * @return An {@code Optional} which contains the selected profile if any.
     */
    public static Optional<Profile> askForProfile(boolean editable) {
        if (editable) {
            ProfileChoice choice = new ProfileChoice();
            choice.start(new Stage());
            return Optional.ofNullable(choice.profile);
        } else {
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(null, Profile.getAvailableProfiles());
            DialogPane dialogPane = choiceDialog.dialogPaneProperty().get();
            dialogPane.getStylesheets().add(DataProvider.STYLESHEET_PATH);
            ((Stage) dialogPane.getScene().getWindow()).getIcons().add(DataProvider.ImageSet.LOGO.get());

            Optional<String> profileName = choiceDialog.showAndWait();
            if (profileName.isPresent()) {
                return Optional.of(new Profile(profileName.get(), false));
            } else {
                return Optional.empty();
            }
        }
    }
}
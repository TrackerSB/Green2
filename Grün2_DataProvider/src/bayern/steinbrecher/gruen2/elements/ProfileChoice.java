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
package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.data.Profile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Represents a dialog for choosing the profile to use.
 *
 * @author Stefan Huber
 */
public class ProfileChoice extends Application {

    private static final String NEW_CONFIG_NAME
            = DataProvider.getResourceValue("newConfigname");
    private Optional<Profile> profile = Optional.empty();
    private boolean selected = false;
    private boolean created = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        List<Node> nodes = new ArrayList<>();

        Label choiceLabel
                = new Label(DataProvider.getResourceValue("chooseProfile"));
        nodes.add(choiceLabel);

        ListView<String> profileList = new ListView<>(FXCollections
                .observableArrayList(Profile.getAvailableProfiles()));
        nodes.add(profileList);

        Button select = new Button(DataProvider.getResourceValue("select"));
        select.setOnAction(evt -> {
            selected = true;
            stage.close();
        });
        nodes.add(select);

        MultipleSelectionModel<String> selectionModel
                = profileList.selectionModelProperty().get();
        selectionModel.getSelectedItems().addListener(
                (Change<? extends String> c) -> {
                    select.setDisable(c.getList().isEmpty());
                });
        select.setDisable(selectionModel.isEmpty());

        Button create = new Button(DataProvider.getResourceValue("create"));
        create.setOnAction(evt -> {
            created = true;
            stage.close();
        });
        nodes.add(create);

        Scene scene = new Scene(
                new VBox(10, nodes.toArray(new Node[nodes.size()])));
        scene.getStylesheets().add(DataProvider.STYLESHEET_PATH);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(DataProvider.getResourceValue("chooseProfile"));
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
        stage.showAndWait();

        if (selected) {
            profile = Optional.of(
                    new Profile(selectionModel.getSelectedItem(), false));
        } else if (created) {
            String newConfigName = NEW_CONFIG_NAME;
            List<String> availableProfiles = Profile.getAvailableProfiles();
            Random random = new Random();
            while (availableProfiles.contains(newConfigName)) {
                newConfigName
                        = NEW_CONFIG_NAME + " (" + random.nextInt(1000) + ")";
            }
            profile = Optional.of(new Profile(newConfigName, true));
        }
    }

    /**
     * Creates a dialog for selecting a profile. This method blocks untilt the
     * dialog is closed.
     *
     * @param showNewButton {@code true} only if to show a "new" button to the
     * user. When {@code false} a simple {@code ChoiceDialog} is used; a
     * {@code ListView} otherwise.
     * @return An {@code Optional} which contains the selected profile if any.
     */
    public static Optional<Profile> askForProfile(boolean showNewButton) {
        if (showNewButton) {
            ProfileChoice choice = new ProfileChoice();
            choice.start(new Stage());
            return choice.profile;
        } else {
            Optional<String> profileName
                    = new ChoiceDialog<>(null, Profile.getAvailableProfiles())
                            .showAndWait();
            if (profileName.isPresent()) {
                return Optional.of(new Profile(profileName.get(), false));
            } else {
                return Optional.empty();
            }
        }
    }
}

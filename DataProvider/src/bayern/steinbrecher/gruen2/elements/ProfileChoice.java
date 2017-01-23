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
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    private static final String NEW_CONFIG_NAME
            = DataProvider.getResourceValue("newConfigname");
    private Profile profile;
    private boolean selected = false;
    private boolean created = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        List<Node> nodes = new ArrayList<>();

        Label choiceLabel = new Label(DataProvider.getResourceValue("chooseProfile"));
        nodes.add(choiceLabel);

        List<String> profiles = Profile.getAvailableProfiles();
        ObservableList<HBox> rows = FXCollections.observableArrayList();
        for (String profileName : profiles) {
            HBox row = new HBox(5, new Label(profileName), new Button("Edit"), new Button("delete"));
            rows.add(row);
        }
        ListView<HBox> profileList = new ListView<>(rows);
        nodes.add(profileList);

        MultipleSelectionModel<HBox> selectionModel = profileList.selectionModelProperty().get();

        Button select = new Button(DataProvider.getResourceValue("select"));
        select.setOnAction(evt -> {
            selected = true;
            stage.close();
        });
        select.setDisable(selectionModel.isEmpty());
        nodes.add(select);

        Button delete = new Button(DataProvider.getResourceValue("delete"));
        delete.setOnAction(evt -> {
            HBox row = selectionModel.getSelectedItem();
            String selectedProfile = ((Label) row.getChildren().get(0)).getText(); //TODO Guaranteed that the label is first?
            new Profile(selectedProfile, false).deleteProfile();
            profileList.getItems().remove(row);
            if (selectionModel.getSelectedIndex() < profileList.getItems().size() - 1) {
                selectionModel.selectNext();
            }
        });
        delete.setDisable(selectionModel.isEmpty());

        selectionModel.getSelectedItems().addListener((Change<? extends HBox> c) -> {
            boolean nothingSelected = c.getList().isEmpty();
            select.setDisable(nothingSelected);
            delete.setDisable(nothingSelected);
        });

        Button create = new Button(DataProvider.getResourceValue("create"));
        create.setOnAction(evt -> {
            created = true;
            stage.close();
        });
        nodes.add(new HBox(10, create, delete));

        Scene scene = new Scene(new VBox(10, nodes.toArray(new Node[nodes.size()])));
        scene.getStylesheets().add(DataProvider.STYLESHEET_PATH);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(DataProvider.getResourceValue("chooseProfile"));
        stage.getIcons().add(DataProvider.DEFAULT_ICON);

        //FIXME Find workaround for calculating sizes without showing stage
        stage.show();
        stage.close();

        double maxRowWidth = rows.stream().mapToDouble(HBox::getWidth).max().getAsDouble();
        profileList.setMinWidth(maxRowWidth);

        stage.showAndWait();

        if (selected) {
            profile = new Profile(((Label) selectionModel.getSelectedItem().getChildren().get(0)).getText(), false); //TODO Guaranteed that the label is first?
        } else if (created) {
            String newConfigName = NEW_CONFIG_NAME;
            List<String> availableProfiles = Profile.getAvailableProfiles();
            Random random = new Random();
            while (availableProfiles.contains(newConfigName)) {
                newConfigName = NEW_CONFIG_NAME + " (" + random.nextInt(1000) + ")";
            }
            profile = new Profile(newConfigName, true);
        }
    }

    /**
     * Creates a dialog for selecting a profile. This method blocks untilt the
     * dialog is closed.
     *
     * @param showNewButton {@code true} only if to show a "new" button to the
     *                      user. When {@code false} a simple {@code ChoiceDialog} is used; a
     *                      {@code ListView} otherwise.
     * @return An {@code Optional} which contains the selected profile if any.
     */
    public static Optional<Profile> askForProfile(boolean showNewButton) {
        if (showNewButton) {
            ProfileChoice choice = new ProfileChoice();
            choice.start(new Stage());
            return Optional.ofNullable(choice.profile);
        } else {
            Optional<String> profileName = new ChoiceDialog<>(null, Profile.getAvailableProfiles())
                    .showAndWait();
            if (profileName.isPresent()) {
                return Optional.of(new Profile(profileName.get(), false));
            } else {
                return Optional.empty();
            }
        }
    }
}

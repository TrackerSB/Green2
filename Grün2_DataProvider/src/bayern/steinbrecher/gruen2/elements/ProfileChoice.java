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
import java.util.Optional;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    private Optional<String> profileName = Optional.empty();
    private boolean selected = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        Label choiceLabel
                = new Label(DataProvider.getResourceValue("chooseProfile"));

        ListView<String> profileList = new ListView<>(FXCollections
                .observableArrayList(DataProvider.getAvailableProfiles()));

        Button select = new Button(DataProvider.getResourceValue("select"));
        select.setOnAction(evt -> {
            selected = true;
            stage.close();
        });

        MultipleSelectionModel<String> selectionModel
                = profileList.selectionModelProperty().get();
        selectionModel.getSelectedItems().addListener(
                (Change<? extends String> c) -> {
                    select.setDisable(c.getList().isEmpty());
                });
        select.setDisable(selectionModel.isEmpty());

        Scene scene = new Scene(new VBox(10, choiceLabel, profileList, select));
        scene.getStylesheets().add(DataProvider.STYLESHEET_PATH);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(DataProvider.getResourceValue("chooseProfile"));
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
        stage.showAndWait();

        if (selected) {
            profileName = Optional.of(selectionModel.getSelectedItem());
        }
    }

    public static Optional<String> askForProfile() {
        ProfileChoice choice = new ProfileChoice();
        choice.start(new Stage());
        return choice.profileName;
    }
}

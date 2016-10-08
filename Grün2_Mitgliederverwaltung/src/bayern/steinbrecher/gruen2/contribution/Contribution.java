/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.contribution;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the contribution inserted.
 *
 * @author Stefan Huber
 */
public class Contribution extends View<ContributionController> {

    private Stage owner;

    /**
     * Default constructor. Owner set to {@code null}.
     */
    public Contribution() {
        this(null);
    }

    /**
     * Creates a contribution dialog.
     *
     * @param owner The owner which causes this window to close if the owner is
     * closed. The owner is not blocked.
     */
    public Contribution(Stage owner) {
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("Contribution.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.initOwner(owner);
        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("contributionTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
    }

    /**
     * Opens the contribution window if no other process yet opened one, blocks
     * until the window is closed and returns the value entered in the
     * {@code TextField}. Returns {@code Optional.empty} if the user did not
     * confirm the contribution. The window will only be opened ONCE; even if
     * multiple threads are calling this function. They will be blocked until
     * the window is closed.
     *
     * @return The value entered in the the {@code TextField} if any.
     * @see ContributionController#getContribution()
     */
    public Optional<Double> getContribution() {
        showOnceAndWait();
        return controller.getContribution();
    }
}

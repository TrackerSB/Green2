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
package bayern.steinbrecher.wizard;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Represents a wizard for showing a sequence of {@code Pane}s. You can step back and forward on these {@code Panes} and
 * only can close is on the last page.<br>
 * You also can style it using CSS. Following CSS classes are available:<br>
 * <ul>
 * <li>wizard</li>
 * <li>wizard-content</li>
 * <li>wizard-controls</li>
 * </ul>
 *
 * @author Stefan Huber
 */
public class Wizard extends Application {

    private WizardController controller;
    private final Map<String, WizardPage<?>> pages;
    private Stage stage;

    /**
     * Constructs a wizard with showing {@code pages} and using default stylesheet.
     *
     * @param pages The pages to show.
     *
     */
    public Wizard(Map<String, WizardPage<?>> pages) {
        checkPages(pages);
        this.pages = pages;
    }

    static void checkPages(Map<String, WizardPage<?>> pages) {
        if (!pages.containsKey(WizardPage.FIRST_PAGE_KEY)) {
            throw new IllegalArgumentException("Map of pages must have a key WizardPage.FIRST_PAGE_KEY");
        }
    }

    /**
     * Initializes the wizard. The wizard is not shown yet. NOTE: Add your own stylesheets only after calling this
     * method.
     *
     * @param stage The stage to be used by the wizard.
     * @throws java.io.IOException May be thrown when loading the wizard.
     * @see FXMLLoader#load()
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.sceneProperty().addListener((obs, oldVal, newVal) -> {
            newVal.getStylesheets().addListener((Change<? extends String> c) -> updateContentSize());
        });

        FXMLLoader fxmlLoader = new FXMLLoader(Wizard.class.getResource("Wizard.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("bayern.steinbrecher.wizard.bundles.Wizard"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setPages(pages);

        updateContentSize(); //TODO Think about whether an explicit call is needed.
    }

    private void updateContentSize() {
        double maxWidth = -1;
        double maxHeight = -1;

        //Save settings
        boolean wasShowing = stage.isShowing();
        boolean wasImplicitExit = Platform.isImplicitExit();

        //FIXME Avoid showing/hiding pages just for determining their size.
        Platform.setImplicitExit(false);
        for (Entry<String, WizardPage<?>> entry : controller.getPages().entrySet()) {
            Pane pane = entry.getValue().getRoot();
            pane.getChildren().forEach(child -> child.getStyleClass().add("wizard-inner-content"));
            pane.applyCss();
            pane.layout();
            pane.autosize();

            double paneWidth = pane.getBoundsInLocal().getWidth();
            double paneHeight = pane.getBoundsInLocal().getHeight();
            if (paneWidth > maxWidth) {
                maxWidth = paneWidth;
            }
            if (paneHeight > maxHeight) {
                maxHeight = paneHeight;
            }
        }

        //Restore settings
        if (wasShowing) {
            stage.show();
        }
        Platform.setImplicitExit(wasImplicitExit);

        controller.setContentSize(maxWidth, maxHeight);
    }

    /**
     * Adds the given page to the wizard and replaces pages with the same key but only if the page was not already
     * visited. This method can be used if a page of the wizard is depending on the result of a previous one. NOTE: The
     * size of {@code page} is not considered anymore after {@code start(...)} was called.
     *
     * @param key The key the page is associated with.
     * @param page The page to add to the wizard.
     */
    public void put(String key, WizardPage<?> page) {
        controller.put(key, page);
    }

    /**
     * Returns the property representing whether the wizard is finished. NOTE: It is not finished when it was closed
     * without using the "finish" button.
     *
     * @return The property representing whether the wizard is finished.
     */
    public ReadOnlyBooleanProperty finishedProperty() {
        return controller.finishedProperty();
    }

    /**
     * Checks whether the wizard is finished. NOTE: It is not finished when it was closed without using the "finish"
     * button.
     *
     * @return {@code true} only if the wizard is finished.
     */
    public boolean isFinished() {
        return controller.isFinished();
    }

    /**
     * Returns the results of all pages visited in a sequence to an end.
     *
     * @return {@code Optional.empty()} only if the wizard is not finished yet, otherwise the results of the visited
     * pages.
     * @throws IllegalCallableException Only thrown if thrown by one of the result functions of the visited pages.
     */
    public Optional<Map<String, ?>> getResults() {
        return controller.getResults();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return controller.atBeginningProperty();
    }

    /**
     * Returns a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public boolean isAtBeginning() {
        return atBeginningProperty().get();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public ReadOnlyBooleanProperty atFinishProperty() {
        return controller.atFinishProperty();
    }

    /**
     * Returns a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public boolean isAtFinish() {
        return atFinishProperty().get();
    }

    /**
     * Returns the property holding the currently shown page.
     *
     * @return The property holding the currently shown page.
     */
    public ReadOnlyProperty<WizardPage<?>> currentPageProperty() {
        return controller.currentPageProperty();
    }

    /**
     * Returns the currently shown page.
     *
     * @return The currently shown page.
     */
    public WizardPage<?> getCurrentPage() {
        return currentPageProperty().getValue();
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

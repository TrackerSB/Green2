/*
 * Copyright (c) 2017. Stefan Huber
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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * Represents a wizard for showing a sequence of {@code Pane}s. You can step
 * back and forward on these {@code Panes} and only can close is on the last
 * page.<br>
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

    private StringProperty currentIndex
            = new SimpleStringProperty(this, "currentIndex");
    private Property<WizardPage<?>> currentPage = new SimpleObjectProperty<>(
            this, "currentPage", new WizardPage<>());
    private MapProperty<String, WizardPage<?>> pages
            = new SimpleMapProperty<>();
    private BooleanProperty atBeginnng
            = new SimpleBooleanProperty(this, "atBeginning", true);
    private BooleanProperty atFinish = new SimpleBooleanProperty(this, "atEnd");
    private BooleanProperty finished
            = new SimpleBooleanProperty(this, "finished", false);
    private WizardController controller;
    private Stage stage;
    private Stack<String> history = new Stack<>();

    private void checkPages(Map<String, WizardPage<?>> pages) {
        if (!pages.containsKey(WizardPage.FIRST_PAGE_KEY)) {
            throw new IllegalArgumentException(
                    "Map of pages must have a key WizardPage.FIRST_PAGE_KEY");
        }
    }

    Wizard() {
        /*
         * Dummy constructor needed for WizardController in order making caller
         * non-null when FXMLLoader#load() is called.
         */
    }

    /**
     * Constructs a wizard with showing {@code pages} and using default
     * stylesheet.
     *
     * @param pages The pages to show.
     */
    public Wizard(Map<String, WizardPage<?>> pages) {
        checkPages(pages);
        this.pages.set(FXCollections.observableMap(pages));

        currentIndex.addListener((obs, oldVal, newVal) -> {
            WizardPage<?> newPage = pages.get(newVal);
            atFinish.set(newPage.isFinish());
            currentPage.setValue(newPage);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Wizard.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("bayern.steinbrecher.wizard.bundles.Wizard"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        controller = fxmlLoader.getController();
        controller.setCaller(this);

        double maxWidth = -1;
        double maxHeight = -1;

        //TODO Think about whether this is really needed.
        boolean previousImplicitExit = Platform.isImplicitExit();
        Platform.setImplicitExit(false);

        for (Entry<String, WizardPage<?>> entry : pages.get().entrySet()) {
            Pane pane = entry.getValue().getRoot();
            pane.getChildren().forEach(n -> n.getStyleClass().add("wizard-inner-content"));
            controller.setContent(pane);
            stage.setScene(scene);
            stage.show();
            stage.close();

            double paneWidth = pane.getWidth();
            double paneHeight = pane.getHeight();
            if (paneWidth > maxWidth) {
                maxWidth = paneWidth;
            }
            if (paneHeight > maxHeight) {
                maxHeight = paneHeight;
            }
        }

        Platform.setImplicitExit(previousImplicitExit);

        controller.setContentSize(maxWidth, maxHeight);

        currentIndex.set(WizardPage.FIRST_PAGE_KEY);
        currentPage.setValue(pages.get(WizardPage.FIRST_PAGE_KEY));
        history.push(WizardPage.FIRST_PAGE_KEY);
        updatePage();
        stage.show();
        stage.sizeToScene();
    }

    private void updatePage() {
        controller.setContent(pages.get(currentIndex.get()).getRoot());
    }

    /**
     * Moves to the previous page if possible.
     */
    void showPrevious() {
        if (!atBeginnng.get()) {
            history.pop(); //Pop current index
            atBeginnng.set(history.size() < 2);
            currentIndex.set(history.peek());
            updatePage();
        }
    }

    /**
     * Moves to the next page if possible.
     */
    void showNext() {
        WizardPage<?> page = currentPage.getValue();
        Callable<String> nextFunction = page.getNextFunction();
        if (page.isHasNextFunction() && page.isValid()) {
            try {
                String nextIndex = nextFunction.call();
                if (!pages.containsKey(nextIndex)) {
                    throw new IllegalPageException(
                            "Wizard contains no page with key \""
                                    + nextIndex + "\".");
                }
                currentIndex.set(nextIndex);
                history.push(currentIndex.get());
                atBeginnng.set(false);
                updatePage();
            } catch (Exception ex) {
                throw new IllegalCallableException(
                        "A valid function or a next function of page \""
                                + currentIndex.get() + "\" has thrown an exception",
                        ex);
            }
        }
    }

    /**
     * Closes the stage only if the current page is the last one.
     */
    void finish() {
        if (atFinish.get()) {
            finished.set(true);
            stage.close();
        }
    }

    /**
     * Returns the property representing whether the wizard is finished.
     *
     * @return The property representing whether the wizard is finished.
     */
    public ReadOnlyBooleanProperty finishedProperty() {
        return finished;
    }

    /**
     * Checks whether the wizard is finished.
     *
     * @return Only {@code true} if the wizard is finished.
     */
    public boolean isFinished() {
        return finished.get();
    }

    /**
     * Returns the results of all pages visited in a sequence to an end.
     *
     * @return {@code Optional.empty()} only if the wizard is not finished yet,
     * otherwise the results of the visited pages.
     * @throws IllegalCallableException Only thrown if thrown by one of the
     *                                  result functions of the visited pages.
     */
    public Optional<Map<String, ?>> getResults() {
        if (isFinished()) {
            //FIXME Cannot use ? as type
            Map<String, Object> results = new HashMap<>();
            history.forEach(key -> {
                try {
                    results.put(key, pages.get(key).getResultFunction().call());
                } catch (Exception ex) {
                    throw new IllegalCallableException(
                            "The result function of wizard page \""
                                    + key
                                    + "\" has thrown an exception", ex);
                }
            });
            return Optional.of(results);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Property containing a boolean value representing whether the current page
     * shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return atBeginnng;
    }

    /**
     * Returns a boolean value representing whether the current page shown is
     * the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public boolean isAtBeginning() {
        return atBeginnng.get();
    }

    /**
     * Property containing a boolean value representing whether the current page
     * shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public ReadOnlyBooleanProperty atFinishProperty() {
        return atFinish;
    }

    /**
     * Returns a boolean value representing whether the current page shown is a
     * last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public boolean isAtFinish() {
        return atFinish.get();
    }

    public ReadOnlyProperty<WizardPage<?>> currentPageProperty() {
        return currentPage;
    }

    public WizardPage<?> getCurrentPage() {
        return currentPage.getValue();
    }
}

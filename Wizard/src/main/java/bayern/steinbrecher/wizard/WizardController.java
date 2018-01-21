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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.concurrent.Callable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

/**
 * Contains the controller of the wizard.
 *
 * @author Stefan Huber
 */
public class WizardController implements Initializable {

    private final StringProperty currentIndex = new SimpleStringProperty(this, "currentIndex");
    private final Property<WizardPage<?>> currentPage = new SimpleObjectProperty<>(this, "currentPage", new WizardPage<>());
    private final MapProperty<String, WizardPage<?>> pages = new SimpleMapProperty<>();
    private final BooleanProperty atBeginning = new SimpleBooleanProperty(this, "atBeginning", true);
    private final BooleanProperty atFinish = new SimpleBooleanProperty(this, "atEnd");
    private final BooleanProperty finished = new SimpleBooleanProperty(this, "finished", false);
    private final Stack<String> history = new Stack<>();
    @FXML
    private StackPane contents;
    private Stage stage;
    private static final String WIZARD_CONTENT_STYLECLASS = "wizard-content";

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //no-op
    }

    /**
     * Replaces the current content of the wizard with {@code pane}.
     *
     * @param pane The new content of the wizard.
     */
    void setContent(Pane pane) {
        contents.getChildren().forEach(n -> n.getStyleClass().remove(WIZARD_CONTENT_STYLECLASS));
        contents.getChildren().clear();
        pane.getStyleClass().add(WIZARD_CONTENT_STYLECLASS);
        contents.getChildren().add(pane);
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void showPrevious() {
        if (!atBeginning.get()) {
            history.pop(); //Pop current index
            atBeginning.set(history.size() < 2);
            currentIndex.set(history.peek());
            updatePage();
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void showNext() {
        if (currentPage.getValue().isValid()) {
            WizardPage<?> page = currentPage.getValue();
            Callable<String> nextFunction = page.getNextFunction();
            if (page.isHasNextFunction() && page.isValid()) {
                try {
                    String nextIndex = nextFunction.call();
                    if (!pages.containsKey(nextIndex)) {
                        throw new PageNotFoundException(
                                "Wizard contains no page with key \""
                                + nextIndex + "\".");
                    }
                    currentIndex.set(nextIndex);
                    history.push(currentIndex.get());
                    atBeginning.set(false);
                    updatePage();
                } catch (Exception ex) {
                    throw new IllegalCallableException(
                            "A valid function or a next function of page \""
                            + currentIndex.get() + "\" has thrown an exception",
                            ex);
                }
            }
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void finish() {
        if (currentPage.getValue().isValid() && atFinish.get()) {
            finished.set(true);
            stage.close();
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Sets the size of the {@code Pane} containing the content of the page. This function can be used to make all
     * wizard pages the same size.
     *
     * @param width The width of the content of the page.
     * @param height The height of the content of the page.
     */
    public void setContentSize(double width, double height) {
        contents.setMinWidth(width);
        contents.setPrefWidth(width);
        contents.setMaxWidth(width);
        contents.setMinHeight(height);
        contents.setPrefHeight(height);
        contents.setMaxHeight(height);
    }

    /**
     * Returns the property holding all pages visitable.
     *
     * @return The property holding all pages visitable.
     */
    public MapProperty<String, WizardPage<?>> pagesProperty() {
        return pages;
    }

    /**
     * Returns all visitable pages.
     *
     * @return All visitable pages.
     */
    public Map<String, WizardPage<?>> getPages() {
        return pages.get();
    }

    private void updatePage() {
        setContent(pages.get(currentIndex.get()).getRoot());
    }

    /**
     * Sets a new map of visitable pages. NOTE: Calling this method causes the wizard to reset to the first page and
     * clear the history.
     *
     * @param pages The map of pages to set.
     */
    public void setPages(Map<String, WizardPage<?>> pages) {
        Wizard.checkPages(pages);
        this.pages.set(FXCollections.observableMap(pages));

        currentIndex.addListener((obs, oldVal, newVal) -> {
            WizardPage<?> newPage = pages.get(newVal);
            atFinish.set(newPage.isFinish());
            currentPage.setValue(newPage);
        });

        currentIndex.set(WizardPage.FIRST_PAGE_KEY);
        currentPage.setValue(pages.get(WizardPage.FIRST_PAGE_KEY));
        history.clear();
        history.push(WizardPage.FIRST_PAGE_KEY);

        updatePage();
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
        if (history.contains(key)) {
            throw new IllegalStateException("A page already visited can not be replaced");
        }
        pages.put(key, page);
    }

    /**
     * Returns the property representing whether the wizard is finished. NOTE: It is not finished when it was closed
     * without using the "finish" button.
     *
     * @return The property representing whether the wizard is finished.
     */
    public ReadOnlyBooleanProperty finishedProperty() {
        return finished;
    }

    /**
     * Checks whether the wizard is finished. NOTE: It is not finished when it was closed without using the "finish"
     * button.
     *
     * @return {@code true} only if the wizard is finished.
     */
    public boolean isFinished() {
        return finished.get();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public ReadOnlyBooleanProperty atBeginningProperty() {
        return atBeginning;
    }

    /**
     * Returns a boolean value representing whether the current page shown is the first one.
     *
     * @return {@code true} only if the current page is the first one.
     */
    public boolean isAtBeginning() {
        return atBeginning.get();
    }

    /**
     * Property containing a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public ReadOnlyBooleanProperty atFinishProperty() {
        return atFinish;
    }

    /**
     * Returns a boolean value representing whether the current page shown is a last one.
     *
     * @return {@code true} only if the current page is a last one.
     */
    public boolean isAtFinish() {
        return atFinish.get();
    }

    /**
     * Returns the property holding the currently shown page.
     *
     * @return The property holding the currently shown page.
     */
    public ReadOnlyProperty<WizardPage<?>> currentPageProperty() {
        return currentPage;
    }

    /**
     * Returns the currently shown page.
     *
     * @return The currently shown page.
     */
    public WizardPage<?> getCurrentPage() {
        return currentPage.getValue();
    }

    /**
     * Returns the results of all pages visited in a sequence to an end.
     *
     * @return {@code Optional.empty()} only if the wizard is not finished yet, otherwise the results of the visited
     * pages.
     * @throws IllegalCallableException Only thrown if thrown by one of the result functions of the visited pages.
     */
    public Optional<Map<String, ?>> getResults() {
        if (isFinished()) {
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
}

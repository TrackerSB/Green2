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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contains the controller of the wizard.
 *
 * @author Stefan Huber
 */
public class WizardController implements Initializable {

    @FXML
    private StackPane contents;
    private Property<Wizard> caller = new SimpleObjectProperty<>(this, "caller", new Wizard());
    private static final String WIZARD_CONTENT_STYLECLASS = "wizard-content";

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //No-op
    }

    private void checkCaller() {
        if (caller.getValue() == null) {
            throw new UnsupportedOperationException("You have to set caller first.");
        }
    }

    /**
     * Replaces the current content of the wizard with {@code pane}.
     *
     * @param pane The new content of the wizard.
     */
    public void setContent(Pane pane) {
        contents.getChildren().forEach(n -> n.getStyleClass().remove(WIZARD_CONTENT_STYLECLASS));
        contents.getChildren().clear();
        pane.getStyleClass().add(WIZARD_CONTENT_STYLECLASS);
        contents.getChildren().add(pane);
    }

    @FXML
    private void showPrevious() {
        checkCaller();
        caller.getValue().showPrevious();
    }

    @FXML
    private void showNext() {
        checkCaller();
        caller.getValue().showNext();
    }

    @FXML
    private void finish() {
        checkCaller();
        caller.getValue().finish();
    }

    /**
     * Sets the size of the {@code Pane} containing the content of the page.
     * This function can be used to make all wizard pages the same size.
     *
     * @param width  The width of the content of the page.
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
     * Property containing the wizard this controller has to use as callback.
     *
     * @return The property containing the wizard this controller has to use as
     * callback.
     */
    public Property<Wizard> callerProperty() {
        return caller;
    }

    /**
     * Returning the wizard this controller has to use as callback.
     *
     * @return The wizard this controller has to use as callback.
     */
    public Wizard getCaller() {
        return caller.getValue();
    }

    /**
     * Sets the wizard this controller has to use as callback.
     *
     * @param caller The wizard this controller has to use as callback.
     */
    public void setCaller(Wizard caller) {
        this.caller.setValue(caller);
    }
}

/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bayern.steinbrecher.green2.elements.buttons;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.DialogUtility;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;

/**
 * Represents a button which shows a help message on click. This button uses
 * {@link ButtonBase#setOnAction(EventHandler)}. NOTE: Because this method is final it cannot be overridden. That
 * means: Don't call it on this button otherwise it may not work as expected.
 *
 * @author Stefan Huber
 */
public class HelpButton extends Button {
    /**
     * The CSS class representing a {@link HelpButton}.
     */
    public static final String CSS_CLASS_HELP_BUTTON = "help-button";
    private StringProperty helpMessage = new SimpleStringProperty(this, "helpMessage", "");

    /**
     * Creates a new {@link HelpButton} without help message and labeled with &bdquo;?&ldquo;.
     */
    public HelpButton() {
        this("");
    }

    /**
     * Creates a new {@link HelpButton} showing given {@code helpMessage} and labeled with &bdquo;?&ldquo;.
     *
     * @param helpMessage The helpMessage message to show.
     */
    public HelpButton(String helpMessage) {
        this("?", helpMessage);
    }

    /**
     * Creates a new {@link HelpButton} showing given {@code helpMessage} and labeled with given {@code text}.
     *
     * @param text        The label of the button.
     * @param helpMessage The helpMessage message to show.
     */
    public HelpButton(String text, String helpMessage) {
        this(text, helpMessage, null);
    }

    /**
     * Creates a new {@link HelpButton} showing given {@code helpMessage}, labeled with given {@code text} and with
     * given graphic shown on it.
     *
     * @param text        The label of the button.
     * @param helpMessage The helpMessage message to show.
     * @param graphic     The graphic to show.
     */
    public HelpButton(String text, String helpMessage, Node graphic) {
        super(text, graphic);
        this.helpMessage.addListener((obs, oldVal, newVal) -> {
            setOnAction(aevt -> {
                Alert alert = DialogUtility.createMessageAlert(newVal, getScene().getWindow());
                String help = EnvironmentHandler.getResourceValue("help");
                alert.setTitle(help);
                alert.setHeaderText(help);
                alert.show();
            });
        });
        //FIXME Try not to call explicitly.
        setOnAction(aevt -> {
            Alert alert = DialogUtility.createMessageAlert(helpMessage, getScene().getWindow());
            String help = EnvironmentHandler.getResourceValue("help");
            alert.setTitle(help);
            alert.setHeaderText(help);
            alert.show();
        });
        this.helpMessage.set(helpMessage);
        setFocusTraversable(false);
        getStyleClass().add(CSS_CLASS_HELP_BUTTON);
    }

    /**
     * Returns the property holding the current help message.
     *
     * @return The property holding the current help message.
     */
    public StringProperty helpMessageProperty() {
        return helpMessage;
    }

    /**
     * Sets a new help message.
     *
     * @param helpMessage The new help message.
     */
    public void setHelpMessage(String helpMessage) {
        this.helpMessage.set(helpMessage);
    }

    /**
     * Returns the current help message.
     *
     * @return The current help message.
     */
    public String getHelpMessage() {
        return helpMessage.get();
    }
}

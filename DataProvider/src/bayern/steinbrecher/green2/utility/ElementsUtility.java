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

package bayern.steinbrecher.green2.utility;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains methods useful for many custom controls of {@code bayern.steinbrecher.green2.elements}.
 */
public final class ElementsUtility {
    private ElementsUtility() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    /**
     * Adds {@code cssClass} to the style classes of {@code parent} if {@code observable} holds {@code true}. Otherwise it removes the style class.
     *
     * @param parent     The parent to add/remove the given style class.
     * @param observable The value to observe.
     * @param cssClass   The style class to add/remove.
     */
    public static void addCssClassIf(Parent parent, ObservableBooleanValue observable, String cssClass) {
        ChangeListener<Boolean> changeListener = (obs, oldVal, newVal) -> {
            ObservableList<String> styleClasses = parent.getStyleClass();
            if (newVal) {
                if (!styleClasses.contains(cssClass)) {
                    styleClasses.add(cssClass);
                }
            } else {
                styleClasses.remove(cssClass);
            }
        };
        observable.addListener(changeListener);
        Logger.getLogger(ElementsUtility.class.getName()).log(Level.INFO, "Listener registered for styleClasses: " + parent.getStyleClass().stream().collect(Collectors.joining(" ")));

        //FIXME Don't call listener explicitly
        changeListener.changed(observable, observable.get(), observable.get());
    }
}

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
package bayern.steinbrecher.green2.utility;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

/**
 * Contains methods useful for many custom controls of {@code bayern.steinbrecher.green2.elements}.
 */
public final class ElementsUtility {

    public static final String CSS_CLASS_INVALID_CONTENT = "invalidContent";

    private ElementsUtility() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    /**
     * Adds {@code cssClass} to the style classes of {@code parent} if {@code observable} holds {@code true}. Otherwise
     * it removes the style class. NOTE: When passing bindings directly make sure they are not garbage collected. It is
     * recommended to pass a {@link BooleanProperty} which may be bound.
     *
     * @param parent The parent to add/remove the given style class.
     * @param observable The value to observe.
     * @param cssClass The style class to add/remove.
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

        //FIXME Don't call listener explicitly
        changeListener.changed(observable, observable.get(), observable.get());
    }
}

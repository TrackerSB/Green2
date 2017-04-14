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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;

import java.util.concurrent.Callable;

/**
 * Represents a page of the wizard.
 *
 * @author Stefan Huber
 * @param <T> The return type of the result represented by the page.
 */
public final class WizardPage<T> {

    /**
     * The key of the page to be used as first one.
     */
    public static final String FIRST_PAGE_KEY = "first";
    private BooleanProperty valid = new SimpleBooleanProperty(this, "valid");
    private BooleanProperty hasNextFunction
            = new SimpleBooleanProperty(this, "hasNextFunction");
    private Pane root;
    private Property<Callable<String>> nextFunction
            = new SimpleObjectProperty<>(this, "nextFunction");
    private boolean finish;
    private Callable<T> resultFunction;

    WizardPage() {
        /*
         * Dummy constructor needed for Wizard in order making currentPage
         * non-null when FXMLLoader#load() is called.
         */
    }

    /**
     * Creates a new page with given params.
     *
     * @param root The root pane containing all controls.
     * @param nextFunction The function calculating the name of th enext page.
     * @param finish {@code true} only if this page is a last one.
     * @param resultFunction The function calculating the result this page
     * represents.
     * @param valid A binding to bind this pages {@code valid} property to.
     */
    public WizardPage(Pane root, Callable<String> nextFunction, boolean finish,
            Callable<T> resultFunction,
            ObservableValue<? extends Boolean> valid) {
        this(root, nextFunction, finish, resultFunction);
        this.valid.bind(valid);
    }

    /**
     * Creates a new page with given params. The {@code valid} property always
     * contains {@code true}.
     *
     * @param root The root pane containing all controls.
     * @param nextFunction The function calculating the name of th enext page.
     * @param finish {@code true} only if this page is a last one.
     * @param resultFunction The function calculating the result this page
     * represents.
     */
    public WizardPage(Pane root, Callable<String> nextFunction, boolean finish,
            Callable<T> resultFunction) {
        this.root = root;
        this.nextFunction.addListener((obs, oldVal, newVal) -> {
            hasNextFunction.set(newVal != null);
        });
        this.nextFunction.setValue(nextFunction);
        this.finish = finish;
        setResultFunction(resultFunction);
        valid.unbind();
        valid.set(true);
    }

    /**
     * Returns the pane containing all controls.
     *
     * @return The pane containing all controls.
     */
    public Pane getRoot() {
        return root;
    }

    /**
     * Sets a new pane containing all controls.
     *
     * @param root The pane containing all controls.
     */
    public void setRoot(Pane root) {
        this.root = root;
    }

    /**
     * The property containing the function calculating which page to show next.
     *
     * @return The property containing the function calculating which page to
     * show next.
     */
    public Property<Callable<String>> nextFunctionProperty() {
        return nextFunction;
    }

    /**
     * Returns the function calculating the key of the next page.
     *
     * @return The function calculating the key of the next page. Returns
     * {@code null} if this page has no next one.
     */
    public Callable<String> getNextFunction() {
        return nextFunction.getValue();
    }

    /**
     * Sets a new function calculating the key of the next page.
     *
     * @param nextFunction The function calculating the key of the next page.
     */
    public void setNextFunction(Callable<String> nextFunction) {
        this.nextFunction.setValue(nextFunction);
    }

    /**
     * Returns whether this page is a last one.
     *
     * @return {@code true} only if this page is a last one.
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * Changes whether this page is a last one.
     *
     * @param finish {@code true} if this page has to be a last one.
     */
    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    /**
     * Returns the function calculating the result this page represents.
     *
     * @return The function calculating the result this page represents.
     */
    public Callable<T> getResultFunction() {
        return resultFunction;
    }

    /**
     * Sets a new function calculating the result this page represents.
     *
     * @param resultFunction The function calculating the result this page
     * represents.
     */
    public void setResultFunction(Callable<T> resultFunction) {
        if (resultFunction == null) {
            throw new IllegalArgumentException(
                    "resultFunction must not be null");
        }
        this.resultFunction = resultFunction;
    }

    /**
     * Sets a new binding to bind this pages {@code valid} property to.
     *
     * @param valid A new binding to bind this pages {@code valid} property to.
     */
    public void setValidBinding(ObservableValue<? extends Boolean> valid) {
        this.valid.bind(valid);
    }

    /**
     * Returns the property representing whether this page has valid input.
     *
     * @return The property representing whether this page has valid input.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Returns whether the current input of this page is valid.
     *
     * @return {@code true} only if the current input of this page is valid.
     */
    public boolean isValid() {
        return valid.get();
    }

    public ReadOnlyBooleanProperty hasNextFunctionProperty() {
        return hasNextFunction;
    }

    public boolean isHasNextFunction() {
        return hasNextFunction.get();
    }
}
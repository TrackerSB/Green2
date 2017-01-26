/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2;

import bayern.steinbrecher.wizard.WizardPage;

/**
 * Represents a class which can be in a {@code Wizard}.
 *
 * @param <T> The type of the result of the {@code WizardPage}.
 * @param <C> The type of the controller used by the {@code View}.
 * @author Stefan Huber
 */
public abstract class WizardableView<T, C extends WizardableController> extends View<C> {

    /**
     * Creates a {@code WizardPage}. The nextFunction is set to {@code null} and
     * isFinish is set to {@code false}.
     *
     * @return The newly created {@code WizardPage}. Returns {@code null} if the
     * {@code WizardPage} could not be created.
     */
    public abstract WizardPage<T> getWizardPage();
}

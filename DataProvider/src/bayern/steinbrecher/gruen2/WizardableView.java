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
package bayern.steinbrecher.gruen2;

import bayern.steinbrecher.wizard.WizardPage;

/**
 * Represents a class which can be in a {@code Wizard}.
 *
 * @author Stefan Huber
 * @param <T> The type of the result of the {@code WizardPage}.
 * @param <C> The type of the controller used by the {@code View}.
 */
public abstract class WizardableView<T, C extends WizardableController>
        extends View<C> {

    /**
     * Creates a {@code WizardPage}. The nextFunction is set to {@code null} and
     * isFinish is set to {@code false}.
     *
     * @return The newly created {@code WizardPage}. Returns {@code null} if the
     * {@code WizardPage} could not be created.
     */
    public abstract WizardPage<T> getWizardPage();
}
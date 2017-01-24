/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

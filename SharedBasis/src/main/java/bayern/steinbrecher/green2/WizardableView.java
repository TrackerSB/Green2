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
package bayern.steinbrecher.green2;

import bayern.steinbrecher.wizard.WizardPage;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.Pane;

/**
 * Represents a class which can be in a {@link bayern.steinbrecher.wizard.Wizard}.
 *
 * @param <T> The type of the result of the {@link WizardPage}.
 * @param <C> The type of the controller used by the {@link View}.
 * @author Stefan Huber
 */
public abstract class WizardableView<T extends Optional<?>, C extends WizardableController<T>>
        extends ResultView<T, C> {

    /**
     * Returns the path of the FXML file to load to be used by a wizard.
     *
     * @return The path of the FXML file to load to be used by a wizard.
     * @see #getWizardPage()
     */
    protected abstract String getWizardFxmlPath();

    /**
     * Creates a {@link WizardPage}. The nextFunction is set to {@code null} and isFinish is set to {@code false}.
     *
     * @return The newly created {@link WizardPage}. Returns {@code null} if the {@link WizardPage} could not be
     * created.
     */
    public WizardPage<T> getWizardPage() {
        try {
            Pane root = loadFXML(getWizardFxmlPath());
            return new WizardPage<>(
                    root, null, false, () -> getController().getResult(), getController().validProperty());
        } catch (IOException ex) {
            Logger.getLogger(WizardableView.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

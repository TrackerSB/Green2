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
package bayern.steinbrecher.green2.contribution;

import bayern.steinbrecher.green2.elements.spinner.ContributionField;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;

/**
 * Contains functionality needed to extend {@link ContributionController} to standalone dialog.
 *
 * @author Stefan Huber
 */
final class ContributionControllerParent extends ContributionController {

    public ContributionControllerParent() {
        addListenerToContributionFields((ListChangeListener.Change<? extends ContributionField> change) -> {
            while(change.next()){
                
            }
        });
    }

    @FXML
    private void submitContributions() {
        checkStage();
        if (valid.get()) {
            stage.close();
        }
    }
}

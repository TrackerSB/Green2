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
package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.textfields.NameField;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;

/**
 * Represents a {@link NameField} specialized for profile names which also checks whether the currently inserted
 * profilename already exists.
 *
 * @author Stefan Huber
 */
public class ProfileNameField extends NameField {

    private final BooleanProperty profileAlreadyExists = new SimpleBooleanProperty(this, "profileAlreadyExists", false);

    /**
     * Creates a new {@link ProfileNameField}.
     */
    public ProfileNameField() {
        super();
        textProperty().addListener((obs, oldVal, newVal) -> {
            profileAlreadyExists.set(!EnvironmentHandler.getProfile().getProfileName().equals(newVal)
                    && Profile.getAvailableProfiles().contains(newVal));
        });
        addValidCondition(profileAlreadyExists.not());
        getStyleClass().add("profile-name-field");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Pair<ReportType, BooleanExpression>> getReports() {
        Map<String, Pair<ReportType, BooleanExpression>> reports = new HashMap<>();
        reports.putAll(super.getReports());
        reports.put(EnvironmentHandler.getResourceValue("profileAlreadyExists"),
                new Pair<>(ReportType.ERROR, profileAlreadyExists));
        return reports;
    }

    /**
     * Returns the property containing a value indicating whether the current profile could be renamed to given profile.
     *
     * @return The property containing a value indicating whether the current profile could be renamed to given profile.
     */
    public ReadOnlyBooleanProperty profileAlreadyExistsProperty() {
        return profileAlreadyExists;
    }

    /**
     * Checks whether the current profile could be renamed to given profile.
     *
     * @return {@code true} only if the current profile could be renamed to given profile.
     */
    public boolean isProfileAlreadyExists() {
        return profileAlreadyExists.get();
    }
}

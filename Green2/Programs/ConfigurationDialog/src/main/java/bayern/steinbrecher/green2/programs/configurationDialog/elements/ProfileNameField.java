package bayern.steinbrecher.green2.programs.configurationDialog.elements;

import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.ReportType;
import bayern.steinbrecher.checkedElements.textfields.NameField;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Profile;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

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
        getStyleClass().add("profile-name-field");
        initProperties();
    }

    private void initProperties() {
        textProperty().addListener((obs, oldVal, newVal) -> {
            profileAlreadyExists.set(!EnvironmentHandler.getProfile().getProfileName().equals(newVal)
                    && Profile.getAvailableProfiles().contains(newVal));
        });
        addReport(new ReportEntry("profileAlreadyExists", ReportType.ERROR, profileAlreadyExists));
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

package bayern.steinbrecher.gruen2.elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.DatePicker;

/**
 * Represents a DatePicker which sets a css class attribute when it is empty or
 * an invalid date is inserted.
 *
 * @author Stefan Huber
 */
public class CheckedDatePicker extends DatePicker {

    /**
     * The css class added when the inserted date is no valid date.
     */
    public static final String CSS_CLASS_INVALID_DATE = "invalidDate";
    /**
     * BooleanProperty indicating whether the currently inserted date is valid.
     */
    private final BooleanProperty valid
            = new SimpleBooleanProperty(this, "valid");
    private final BooleanProperty empty
            = new SimpleBooleanProperty(this, "empty");
    private final BooleanProperty forceFuture
            = new SimpleBooleanProperty(this, "forceFuture", false);

    /**
     * Constructes a {@code CheckedDatePicker} with no initial date inserted.
     */
    public CheckedDatePicker(boolean forceFuture) {
        this(null, forceFuture);
    }

    /**
     * Constructes a {@code CheckedDatePicker} with {@code locale} as initial
     * date.
     *
     * @param locale The initial date.
     */
    public CheckedDatePicker(LocalDate locale, boolean forceFuture) {
        super(locale);
        this.forceFuture.set(forceFuture);

        empty.bind(getEditor().textProperty().isEmpty());

        getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            DateTimeFormatter dtf
                    = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.GERMANY);
            try {
                LocalDate newDate = LocalDate.parse(newVal, dtf);
                getStyleClass().remove(CSS_CLASS_INVALID_DATE);
                valid.set(!this.forceFuture.get() || (this.forceFuture.get()
                        && newDate.isAfter(LocalDate.now())));
            } catch (DateTimeParseException ex) {
                if (!getStyleClass().contains(CSS_CLASS_INVALID_DATE)) {
                    getStyleClass().add(CSS_CLASS_INVALID_DATE);
                }
                valid.set(false);
            }
        });

        //Initiate ChangeListener
        String oldText = getEditor().getText();
        getEditor().setText(oldText + "extended");
        getEditor().setText(oldText);
    }

    /**
     * Returns the {@code BooleanProperty} representing whether the current
     * value is valid or not.
     *
     * @return The {@code BooleanProperty} representing whether the current
     * value is valid or not.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether the current value is a valid date.
     *
     * @return {@code true} only if the current value is a valid date.
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Represents a boolean value indicating whether the textfield is empty or
     * not.
     *
     * @return The property represents a boolean value indicating whether the
     * textfield is empty or not.
     */
    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    /**
     * Checks whether the textfield is empty.
     *
     * @return {@code true} only if the textfield is empty.
     */
    public boolean isEmpty() {
        return empty.get();
    }
}

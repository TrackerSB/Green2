package bayern.steinbrecher.gruen2.elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
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

    /**
     * Constructes a {@code CheckedDatePicker} with no initial date inserted.
     */
    public CheckedDatePicker() {
        this(null);
    }

    /**
     * Constructes a {@code CheckedDatePicker} with {@code locale} as initial
     * date.
     *
     * @param locale The initial date.
     */
    public CheckedDatePicker(LocalDate locale) {
        super(locale);

        empty.bind(getEditor().textProperty().isEmpty());

        getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            DateTimeFormatter dtf
                    = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.GERMANY);
            try {
                LocalDate.parse(newVal, dtf);
                getStyleClass().remove(CSS_CLASS_INVALID_DATE);
                valid.set(true);
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

    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    public boolean isEmpty() {
        return empty.get();
    }
}

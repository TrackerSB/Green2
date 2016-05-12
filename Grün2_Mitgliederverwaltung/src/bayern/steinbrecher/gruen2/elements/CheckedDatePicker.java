package bayern.steinbrecher.gruen2.elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

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
    private final BooleanProperty validProperty = new BooleanPropertyBase() {
        /**
         * {@inheritDoc}
         */
        @Override
        public Object getBean() {
            return CheckedDatePicker.this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return "valid";
        }
    };

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
        getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            DateTimeFormatter dtf
                    = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.GERMANY);
            try {
                LocalDate.parse(newVal, dtf);
                getStyleClass().remove(CSS_CLASS_INVALID_DATE);
                validProperty.set(true);
            } catch (DateTimeParseException ex) {
                if (!getStyleClass().contains(CSS_CLASS_INVALID_DATE)) {
                    getStyleClass().add(CSS_CLASS_INVALID_DATE);
                }
                validProperty.set(false);
            }
        });

        //Initiate ChangeListener
        String oldText = getEditor().getText();
        getEditor().setText(oldText + "extended");
        getEditor().setText(oldText);
    }

    /**
     * Checks whether the current value is a valid date.
     *
     * @return {@code true} only if the current value is a valid date.
     */
    public boolean isValid() {
        return validProperty.get();
    }

    /**
     * Returns the {@code BooleanProperty} representing whether the current
     * value is valid or not.
     *
     * @return The {@code BooleanProperty} representing whether the current
     * value is valid or not.
     */
    public BooleanProperty validProperty() {
        return validProperty;
    }
}

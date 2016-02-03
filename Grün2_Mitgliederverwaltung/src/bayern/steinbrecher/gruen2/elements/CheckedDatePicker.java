package bayern.steinbrecher.gruen2.elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.scene.control.DatePicker;

/**
 * Represents a DatePicker which sets a css class attribute when it is empty or
 * an invalid date is inserted.
 *
 * @author Stefan Huber
 */
public class CheckedDatePicker extends DatePicker {

    public static final String CSS_CLASS_INVALID_DATE = "invalidDate";
    private final BooleanProperty validProperty = new BooleanPropertyBase() {
        @Override
        public Object getBean() {
            return CheckedDatePicker.this;
        }

        @Override
        public String getName() {
            return "valid";
        }
    };

    public CheckedDatePicker() {
        this(null);
    }

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

    public boolean isValid() {
        return validProperty.get();
    }

    public BooleanProperty validProperty() {
        return validProperty;
    }
}

package bayern.steinbrecher.gruen2.elements;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Represents a spinner for double values which sets a css attribute when the
 * inserted value is not valid.
 *
 * @author Stefan Huber
 */
public class CheckedDoubleSpinner extends Spinner<Double> {

    public static final String CSS_CLASS_INVALID = "invalidSpinnerValue";
    private BooleanProperty validProperty = new BooleanPropertyBase(true) {
        @Override
        public Object getBean() {
            return CheckedDoubleSpinner.this;
        }

        @Override
        public String getName() {
            return "valid";
        }
    };

    public CheckedDoubleSpinner(@NamedArg("min") int min,
            @NamedArg("max") int max,
            @NamedArg("initialValue") int initialValue,
            @NamedArg("amountToStepBy") double amountToStepBy) {
        super(min, max, initialValue, amountToStepBy);

        SpinnerValueFactory.DoubleSpinnerValueFactory factory
                = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        min, max, initialValue, amountToStepBy);
        setValueFactory(factory);
        getEditor().textProperty()
                .addListener((obs, oldVal, newVal) -> {
                    try {
                        double parsed = Double.parseDouble(
                                newVal.replace(',', '.'));
                        factory.setValue(parsed);
                        validProperty.set(true);
                    } catch (NumberFormatException ex) {
                        validProperty.set(false);
                    }
                    
                    if(!validProperty.get()){
                        if(!getStyleClass().contains(CSS_CLASS_INVALID)){
                            getStyleClass().add(CSS_CLASS_INVALID);
                        }
                    } else{
                        getStyleClass().remove(CSS_CLASS_INVALID);
                    }
                });
    }

    public BooleanProperty validProperty() {
        return validProperty;
    }
}

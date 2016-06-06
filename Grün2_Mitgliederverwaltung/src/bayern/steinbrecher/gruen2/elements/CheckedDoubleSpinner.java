package bayern.steinbrecher.gruen2.elements;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Represents a spinner for double values which sets a css attribute when the
 * inserted value is not valid.
 *
 * @author Stefan Huber
 */
public class CheckedDoubleSpinner extends Spinner<Double> {

    /**
     * Css class attribute indicating that the current value of a
     * {@code CheckedDoubleSpinner} is not valid.
     */
    public static final String CSS_CLASS_INVALID = "invalidSpinnerValue";
    /**
     * {@code BooleanProperty} indicating whether the current value is valid.
     */
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

    /**
     * Constructes a new {@code CheckedDoubleSpinner}.
     *
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @param initialValue The value of the Spinner when first instantiated,
     * must be within the bounds of the min and max arguments, or else the min
     * value will be used.
     * @param amountToStepBy The amount to increment or decrement by, per step.
     */
    public CheckedDoubleSpinner(@NamedArg("min") double min,
            @NamedArg("max") double max,
            @NamedArg("initialValue") double initialValue,
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

                    if (!validProperty.get()) {
                        if (!getStyleClass().contains(CSS_CLASS_INVALID)) {
                            getStyleClass().add(CSS_CLASS_INVALID);
                        }
                    } else {
                        getStyleClass().remove(CSS_CLASS_INVALID);
                    }
                });
    }

    /**
     * Returns the {@code BooleanProperty} representing whether the current
     * value is valid or not.
     *
     * @return The {@code BooleanProperty} representing whether the current
     * value is valid or not.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return validProperty;
    }
    
    public boolean isValid(){
        return validProperty.get();
    }
}

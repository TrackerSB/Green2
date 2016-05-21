package bayern.steinbrecher.gruen2.utility;

import java.util.concurrent.Callable;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Represents methods for creating services.
 *
 * @author Stefan Huber
 */
public final class ServiceFactory {

    /**
     * Prohibit construction of an object.
     */
    private ServiceFactory() {
        throw new UnsupportedOperationException("Construction not supported.");
    }

    /**
     * Creates a service which executes {@code task} and returns a value of type
     * {@code V}.
     *
     * @param <V> The type of the value to return.
     * @param task The task to execute.
     * @return The service which executes {@code task} and returns a value of
     * type {@code V}.
     */
    public static <V> Service<V> createService(Callable<V> task) {
        return new Service<V>() {
            @Override
            protected Task<V> createTask() {
                return new Task<V>() {
                    @Override
                    protected V call() throws Exception {
                        return task.call();
                    }
                };
            }
        };
    }
}

package bayern.steinbrecher.gruen2;

import java.util.concurrent.Callable;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Represents methods for creating services.
 *
 * @author Stefan Huber
 */
public final class ServiceFactory {

    private ServiceFactory() {
        throw new UnsupportedOperationException("Construction not supported.");
    }

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

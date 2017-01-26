/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2.utility;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;

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
     * @param <V>  The type of the value to return.
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

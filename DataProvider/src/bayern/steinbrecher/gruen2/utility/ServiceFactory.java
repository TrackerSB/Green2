/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.utility;

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

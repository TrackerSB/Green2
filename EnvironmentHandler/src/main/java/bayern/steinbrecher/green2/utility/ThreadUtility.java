/* 
 * Copyright (C) 2017 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.utility;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanExpression;

/**
 * Providing often used functions in a comfortable way.
 *
 * @author Stefan Huber
 */
public final class ThreadUtility {

    /**
     * Prohibit construction.
     */
    private ThreadUtility() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    /**
     * Calls {@code wait()} and when it´s notified it checks {@code exp}. If {@code exp} returns {@code true} it calls
     * {@code wait()} again.
     *
     * @param monitor The monitor whose {@code wait()} to call.
     * @param exp The expression to check whether to wait again or not.
     * @see Object#wait()
     */
    public static void waitWhile(Object monitor, BooleanExpression exp) {
        while (exp.get()) {
            try {
                synchronized (monitor) {
                    monitor.wait();
                }
            } catch (InterruptedException ex1) {
                Logger.getLogger(ThreadUtility.class.getName()).log(Level.WARNING, null, ex1);
            }
        }
    }

    /**
     * Creates an {@link ExecutorService}, passes {@code task} to it and returns the resulting {@link Future}. NOTE:
     * Handling the {@link ExecutorService} manually should be the preffered way due it may be expensive to create and
     * shutdown new {@link ExecutorService}s many times.
     *
     * @param <T> The type of the result.
     * @param task The task to execute asynchroniously.
     * @return The {@link Future} returned from the {@link ExecutorService}.
     * @see Executors#newSingleThreadExecutor()
     */
    public static <T> Future<T> passSingleTaskAsync(Callable<T> task) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> result = executor.submit(task);
        executor.shutdown();
        return result;
    }

    /**
     * Creates an {@link ExecutorService}, passes {@code task} to it and returns the resulting {@link Future}. NOTE:
     * Handling the {@link ExecutorService} manually should be the preffered way due it may be expensive to create and
     * shutdown new {@link ExecutorService}s many times.
     *
     * @param task The task to execute asynchroniously.
     * @return The {@link Future} returned from the {@link ExecutorService}.
     * @see Executors#newSingleThreadExecutor()
     */
    public static Future<?> passSingleTaskAsync(Runnable task) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> result = executor.submit(task);
        executor.shutdown();
        return result;
    }
}

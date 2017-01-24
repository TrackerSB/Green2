/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.utility;

import javafx.beans.binding.BooleanExpression;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        throw new UnsupportedOperationException(
                "Construction of an object not allowed.");
    }

    /**
     * Calls {@code wait()} and when it´s notified it checks {@code exp}. If
     * {@code exp} returns {@code true} it calls {@code wait()} again.
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
                Logger.getLogger(ThreadUtility.class.getName())
                        .log(Level.WARNING, null, ex1);
            }
        }
    }
}

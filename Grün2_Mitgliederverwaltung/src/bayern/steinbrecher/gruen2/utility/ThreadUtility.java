package bayern.steinbrecher.gruen2.utility;

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
                        .log(Level.SEVERE, null, ex1);
            }
        }
    }
}

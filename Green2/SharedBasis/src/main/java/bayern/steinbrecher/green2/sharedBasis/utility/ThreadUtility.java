package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import javafx.scene.control.Alert;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ThreadUtility {
    private static final Logger LOGGER = Logger.getLogger(ThreadUtility.class.getName());
    public static final UncaughtExceptionHandler DEFAULT_THREAD_EXCEPTION_HANDLER = (thread, exception) -> {
        LOGGER.log(
                Level.SEVERE, String.format("Thread %s threw an uncaught exception", thread.getName()), exception);
        Alert stacktraceAlert;
        try {
            stacktraceAlert = DialogUtility.createStacktraceAlert(exception, "unexpectedAbort");
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not show stacktrace to user", ex);
            stacktraceAlert = null;
        }
        if (stacktraceAlert != null) {
            DialogUtility.showAndWait(stacktraceAlert);
        }
    };

    private ThreadUtility() {
        throw new UnsupportedOperationException("Construction of instances is prohibited");
    }
}

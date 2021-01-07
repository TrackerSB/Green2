package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ThreadUtility {
    private static final Logger LOGGER = Logger.getLogger(ThreadUtility.class.getName());
    public static final UncaughtExceptionHandler DEFAULT_THREAD_EXCEPTION_HANDLER = (thread, exception) -> {
        LOGGER.log(
                Level.SEVERE, String.format("Thread %s threw an uncaught exception", thread.getName()), exception);
        Alert stacktraceAlert;
        try {
            stacktraceAlert = StagePreparer.prepare(
                    DialogUtility.createStacktraceAlert(exception, "unexpectedAbort"));
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

    public static <T> T runLaterBlocking(Callable<T> actions) {
        AtomicReference<T> resultRef = new AtomicReference<>();
        Runnable exceptionSafeExecution = () -> {
            try {
                resultRef.set(actions.call());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Actions on FXAppThread failed", ex);
                resultRef.set(null);
            }
        };
        if (Platform.isFxApplicationThread()) {
            exceptionSafeExecution.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                exceptionSafeExecution.run();
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO,
                        "Waiting for FX main thread to execute the given actions was interrupted", ex);
            }
        }
        return resultRef.get();
    }
}

package bayern.steinbrecher.green2.sharedBasis.data;

/**
 * @author Stefan Huber
 */
public class ProfileRenamingException extends RuntimeException {

    /**
     * Creates an exception without detail message and no cause.
     */
    public ProfileRenamingException() {
        super();
    }

    /**
     * Creates an exception with detail message but without cause.
     *
     * @param message The detail message.
     */
    public ProfileRenamingException(String message) {
        super(message);
    }

    /**
     * Creates an exception with detail message and cause.
     *
     * @param message The detail message.
     * @param cause The cause of this exception
     */
    public ProfileRenamingException(String message, Throwable cause) {
        super(message, cause);
    }
}

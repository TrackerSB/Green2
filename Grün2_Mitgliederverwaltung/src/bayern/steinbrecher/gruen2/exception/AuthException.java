package bayern.steinbrecher.gruen2.exception;

/**
 * Indicating that a try to authenticate somewhere failed.
 *
 * @author Stefan Huber
 */
public class AuthException extends Exception {

    /**
     * Default constructor.
     */
    public AuthException() {
        super("Auth fail");
    }
}

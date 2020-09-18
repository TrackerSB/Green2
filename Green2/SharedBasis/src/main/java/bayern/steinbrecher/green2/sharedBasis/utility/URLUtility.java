package bayern.steinbrecher.green2.sharedBasis.utility;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class providing utility functions according to URLs.
 *
 * @author Stefan Huber
 */
public final class URLUtility {

    private static final Logger LOGGER = Logger.getLogger(URLUtility.class.getName());

    /**
     * Prohibit construction of an object.
     */
    private URLUtility() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }

    /**
     * Resolves the real HTTP(S)-URL of the given HTTP(S)-URL. (E.g. it follows redirects)
     *
     * @param url The HTTP(S)-URL to resolve.
     * @return The resolved HTTP(S)-URL. Returns {@link Optional#empty()} only if the given URL is invalid, not
     * reachable or an unrecognized status code is thrown like 401 or 500.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static Optional<String> resolveURL(String url) {
        String resolvedUrl = url;
        try {
            boolean redirected;
            do {
                redirected = false;
                HttpURLConnection connection = (HttpURLConnection) new URL(resolvedUrl).openConnection();
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                            || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                        redirected = true;
                        resolvedUrl = connection.getHeaderField("Location");
                    } else {
                        LOGGER.log(Level.WARNING, "\"{0}\" returned code {1}. No action defined for handling.",
                                new Object[]{resolvedUrl, responseCode});
                        resolvedUrl = null;
                    }
                }
            } while (redirected);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            resolvedUrl = null;
        }
        return Optional.ofNullable(resolvedUrl);
    }
}
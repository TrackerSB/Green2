/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2.utility;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    /**
     * Prohibit construction of an object.
     */
    private URLUtility() {
        throw new UnsupportedOperationException(
                "Construction of an object is not allowed.");
    }

    /**
     * Resolves the real HTTP(S)-URL of the given HTTP(S)-URL. (E.g. it follows
     * redirects)
     *
     * @param url The HTTP(S)-URL to resolve.
     * @return The resolved HTTP(S)-URL. Returns {@code Optional.empty()} only
     * if the given URL is invalid, not reachable or an unrecognized status code
     * is thrown like 401 or 500.
     */
    public static Optional<String> resolveURL(String url) {
        try {
            boolean redirected;
            do {
                redirected = false;
                HttpURLConnection connection
                        = (HttpURLConnection) new URL(url).openConnection();
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                            || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || responseCode
                            == HttpURLConnection.HTTP_SEE_OTHER) {
                        redirected = true;
                        url = connection.getHeaderField("Location");
                    } else {
                        Logger.getLogger(URLUtility.class.getName())
                                .log(Level.WARNING, "\"{0}\" returned code {1}."
                                                + " No action defined for handling.",
                                        new Object[]{url, responseCode});
                        url = null;
                    }
                }
            } while (redirected);
        } catch (MalformedURLException ex) {
            Logger.getLogger(URLUtility.class.getName())
                    .log(Level.SEVERE, null, ex);
            url = null;
        } catch (IOException ex) {
            Logger.getLogger(URLUtility.class.getName())
                    .log(Level.SEVERE, null, ex);
            url = null;
        }
        return Optional.ofNullable(url);
    }
}

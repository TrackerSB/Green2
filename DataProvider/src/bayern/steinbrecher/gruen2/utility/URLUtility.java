/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.utility;

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

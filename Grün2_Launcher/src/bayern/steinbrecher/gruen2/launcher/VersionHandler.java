/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a class handling HTTP-connections and handling the version
 * database.
 *
 * @author Stefan Huber
 */
public class VersionHandler {

    private static final String GRUEN2_HOST_URL
            = resolveURL("http://www.traunviertler-traunwalchen.de/programme");
    static final String GRUEN2_ZIP_LOCATION
            = resolveURL(GRUEN2_HOST_URL + "/Gruen2.zip");
    static final String VERSIONFILE_PATH_ONLINE
            = resolveURL(GRUEN2_HOST_URL + "/version.txt");
    static final String VERSIONFILE_PATH_LOCAL
            = Gruen2Launcher.APP_DATA_PATH + "/version.txt";

    /**
     * Resolves the real HTTP(S)-URL of the given HTTP(S)-URL. (E.g. it follows
     * redirects)
     *
     * @param url The HTTP(S)-URL to resolve.
     * @return The resolved HTTP(S)-URL. Returns {@code null} only if the given
     * URL is invalid, not reachable or an unrecognized status code is thrown
     * like 401 or 500.
     */
    private static String resolveURL(String url) {
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
                        System.err.println("\"" + url + "\" returned code "
                                + responseCode
                                + ". No action defined for handling.");
                        url = null;
                    }
                }
            } while (redirected);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
            url = null;
        } catch (IOException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
            url = null;
        }
        return url;
    }

    public static Optional<String> readOnlineVersion() {
        try {
            URL onlineVersionUrl = new URL(VERSIONFILE_PATH_ONLINE);
            Scanner sc = new Scanner(onlineVersionUrl.openStream());
            return Optional.of(sc.nextLine());
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    public static Optional<String> readLocalVersion() {
        File localVersionfile = new File(VERSIONFILE_PATH_LOCAL);
        if (localVersionfile.exists()) {
            try (Scanner sc = new Scanner(localVersionfile)) {
                return Optional.of(sc.nextLine());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(VersionHandler.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        return Optional.empty();
    }
}

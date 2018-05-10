/* 
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2.data;

import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.URLUtility;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;

/**
 * A class collecting and sending data relating to the main application.
 *
 * @author Stefan Huber
 */
public final class Collector {

    private static boolean preparedToSend = false;
    private static final URL POST_URL = resolvePostURL();
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private Collector() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }

    private static URL resolvePostURL() {
        URL url = null;
        Optional<String> resolvedURL = URLUtility.resolveURL("https://traunviertler-traunwalchen.de/php/collector.php");
        if (resolvedURL.isPresent()) {
            try {
                url = new URL(resolvedURL.get());
                preparedToSend = true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(Collector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return url;
    }

    private static String generateDataString() {
        List<String> parameters = new ArrayList<>(DataParams.values().length);
        for (DataParams dp : DataParams.values()) {
            dp.getValue()
                    .ifPresentOrElse(
                            value -> parameters.add(URLEncoder.encode(dp.toString(), StandardCharsets.UTF_8)
                                    + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)),
                            () -> Logger.getLogger(Collector.class.getName())
                                    .log(Level.WARNING, "{0} not transmitted since it could not be computed.",
                                            dp.toString()));
        }
        return parameters.stream().collect(Collectors.joining("&"));
    }

    /**
     * Collects data and send it to the central database.
     *
     * @return {@code true} only if the was successfully collected and sent.
     */
    public static boolean sendData() {
        boolean wasSent = false;
        if (preparedToSend) {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) POST_URL.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                String values = generateDataString();

                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", String.valueOf(values.length()));

                try (OutputStreamWriter writer = new OutputStreamWriter(
                        connection.getOutputStream(), CHARSET)) {
                    writer.write(values);
                    wasSent = true;
                }

                //FIXME POST only successful when getInputStream() is called
                String response;
                try (InputStream inputStream = connection.getInputStream()) {
                    response = IOStreamUtility.readAll(inputStream, CHARSET);
                }
                if (!response.isEmpty()) {
                    Logger.getLogger(Collector.class.getName()).log(Level.INFO, response);
                }

                connection.disconnect();
            } catch (IOException ex) {
                Logger.getLogger(Collector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return wasSent;
    }

    private enum DataParams {
        MAC_ADDRESS {
            /**
             * {@inheritDoc}
             */
            @Override
            public Optional<String> getValue() {
                String macAddress = null;
                try {
                    NetworkInterface localhostInetAddress
                            = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                    if (localhostInetAddress == null) {
                        Logger.getLogger(DataParams.class.getName())
                                .log(Level.SEVERE, "Could not resolve InetAddress of localhost.");
                    } else {
                        byte[] mac = localhostInetAddress.getHardwareAddress();
                        StringJoiner macJoiner = new StringJoiner("-");
                        for (int i = 0; i < mac.length; i++) {
                            macJoiner.add(String.format("%02X", mac[i]));
                        }
                        macAddress = macJoiner.toString();
                    }
                } catch (SocketException | UnknownHostException ex) {
                    Logger.getLogger(DataParams.class.getName()).log(Level.SEVERE, null, ex);
                }
                return Optional.ofNullable(macAddress);
            }
        },
        MESSAGE_CREATION_DATE {
            /**
             * {@inheritDoc}
             */
            @Override
            public Optional<String> getValue() {
                Calendar today = GregorianCalendar.getInstance();
                return Optional.of(today.get(Calendar.YEAR) + "-" + (today.get(Calendar.MONTH) + 1) + "-"
                        + today.get(Calendar.DAY_OF_MONTH));
            }
        },
        VERSION {
            /**
             * {@inheritDoc}
             */
            @Override
            public Optional<String> getValue() {
                return Optional.of(EnvironmentHandler.VERSION);
            }
        };

        /**
         * Returns the value represented by the enum.
         *
         * @return The value represented by the enum. Returns {@link Optional#empty()} only if the value could not be
         * determined.
         */
        public abstract Optional<String> getValue();

        /**
         * Calls {@code toString()} of superclass and makes it lowercase.
         *
         * @return {@code toString()} of superclass in lowercase.
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT);
        }
    }
}

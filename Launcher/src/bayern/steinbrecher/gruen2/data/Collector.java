/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.data;

import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
import bayern.steinbrecher.gruen2.utility.URLUtility;
import bayern.steinbrecher.gruen2.utility.VersionHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A class collecting and sending data relating to the main application.
 *
 * @author Stefan Huber
 */
public final class Collector {

    private static URL POST_URL; //FIXME Should be final
    private static boolean preparedToSend = false;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    static {
        Optional<String> resolvedURL = URLUtility.resolveURL(
                "https://traunviertler-traunwalchen.de/php/collector.php");
        resolvedURL.ifPresent(url -> {
            try {
                POST_URL = new URL(url);
                preparedToSend = true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(Collector.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
    }

    private Collector() {
        throw new UnsupportedOperationException(
                "Construction of an object is not allowed.");
    }

    private static String generateDataString() {
        List<String> parameters = new ArrayList<>(DataParams.values().length);
        for (DataParams dp : DataParams.values()) {
            try {
                parameters.add(URLEncoder.encode(dp.toString(), "UTF-8") + "="
                        + URLEncoder.encode(dp.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Collector.class.getName())
                        .log(Level.INFO, dp + " skipped on sending.", ex);
            }
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
                HttpsURLConnection connection
                        = (HttpsURLConnection) POST_URL.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                String values = generateDataString();

                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length",
                        String.valueOf(values.length()));

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
                    Logger.getLogger(Collector.class.getName())
                            .log(Level.INFO, response);
                }

                connection.disconnect();
            } catch (IOException ex) {
                Logger.getLogger(Collector.class.getName())
                        .log(Level.SEVERE, null, ex);
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
            public String getValue() {
                try {
                    byte[] mac = NetworkInterface.getByInetAddress(
                            InetAddress.getLocalHost())
                            .getHardwareAddress();
                    StringBuilder macString = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        macString.append(String.format("%02X%s", mac[i],
                                (i < mac.length - 1) ? "-" : ""));
                    }
                    return macString.toString();
                } catch (SocketException | UnknownHostException ex) {
                    Logger.getLogger(Collector.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                return null;
            }
        },
        MESSAGE_CREATION_DATE {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getValue() {
                Calendar today = GregorianCalendar.getInstance();
                return today.get(Calendar.YEAR) + "-"
                        + (today.get(Calendar.MONTH) + 1) + "-"
                        + today.get(Calendar.DAY_OF_MONTH);
            }
        },
        VERSION {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getValue() {
                return VersionHandler.readLocalVersion().orElse("");
            }
        };

        /**
         * Returns the value represented by the enum.
         *
         * @return The value represented by the enum.
         */
        public abstract String getValue();

        /**
         * Calls {@code toString()} of superclass and makes it lowercase.
         *
         * @return {@code toString()} of superclass in lowercase.
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}

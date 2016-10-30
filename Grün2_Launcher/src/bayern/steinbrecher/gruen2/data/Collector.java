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
package bayern.steinbrecher.gruen2.data;

import bayern.steinbrecher.gruen2.launcher.VersionHandler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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

    private static URL POST_URL; //FIXME Should be final

    static {
        try {
            POST_URL = new URL(
                    "https://traunviertler-traunwalchen.de/php/collector.php");
        } catch (MalformedURLException ex) {
            Logger.getLogger(Collector.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private Collector() {
        throw new UnsupportedOperationException(
                "Construction of an object is not allowed.");
    }

    private static String generateDataString() {
        List<String> parameters = new ArrayList<>(DataParams.values().length);
        for (DataParams dp : DataParams.values()) {
            try {
                parameters.add(dp.toString()
                        + URLEncoder.encode(dp.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Collector.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        return parameters.stream().collect(Collectors.joining("&"));
    }

    public static void sendData() {
        try {
            HttpsURLConnection connection
                    = (HttpsURLConnection) POST_URL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(false);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            String values = generateDataString();

            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length",
                    String.valueOf(values.length()));

            try (OutputStreamWriter writer
                    = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(values);
            }

        } catch (IOException ex) {
            Logger.getLogger(Collector.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private enum DataParams {
        MAC_ADDRESS {
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
        MESSAGE_CREATION_TIMESTAMP {
            @Override
            public String getValue() {
                return new Timestamp(System.currentTimeMillis()).toString();
            }
        },
        VERSION {
            @Override
            public String getValue() {
                return VersionHandler.readLocalVersion().orElse("");
            }
        };

        public abstract String getValue();
    }
}

package bayern.steinbrecher.green2.launcher.data;

import bayern.steinbrecher.green2.sharedBasis.data.AppInfo;
import bayern.steinbrecher.green2.sharedBasis.utility.IOStreamUtility;
import bayern.steinbrecher.green2.sharedBasis.utility.URLUtility;

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
public final class CollectorUtility {

    private static final Logger LOGGER = Logger.getLogger(CollectorUtility.class.getName());
    private static final URL POST_URL = resolvePostURL();
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static boolean preparedToSend;

    private CollectorUtility() {
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
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return url;
    }

    private static String generateDataString() {
        List<String> parameters = new ArrayList<>(DataParams.values().length);
        for (DataParams dp : DataParams.values()) {
            dp.getValue()
                    .ifPresentOrElse(value -> parameters.add(URLEncoder.encode(dp.toString(), StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)),
                            () -> LOGGER.log(Level.WARNING, "{0} not transmitted since it could not be computed.",
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
                    LOGGER.log(Level.INFO, response);
                }

                connection.disconnect();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return wasSent;
    }

    /**
     * Represents information that has to be collected when this application got installed.
     */
    private enum DataParams {
        /**
         * The MAC address of the computer to register.
         */
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
                        for (byte b : mac) {
                            macJoiner.add(String.format("%02X", b));
                        }
                        macAddress = macJoiner.toString();
                    }
                } catch (SocketException | UnknownHostException ex) {
                    Logger.getLogger(DataParams.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                return Optional.ofNullable(macAddress);
            }
        },
        /**
         * The date when the installation of this application took place.
         */
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
        /**
         * The version of this application which got installed.
         */
        VERSION {
            /**
             * {@inheritDoc}
             */
            @Override
            public Optional<String> getValue() {
                return Optional.of(AppInfo.VERSION);
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

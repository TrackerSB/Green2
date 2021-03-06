package bayern.steinbrecher.green2.sharedBasis.data;

import bayern.steinbrecher.dbConnector.query.SupportedDBMS;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @param <T> The type of the setting hold.
 * @author Stefan Huber
 */
public abstract class /*enum*/ ProfileSettings<T> {
    //FIXME Add generic to enum when available (JDK9?)

    private static final Logger LOGGER = Logger.getLogger(ProfileSettings.class.getName());
    /**
     * Indicating whether to use SSH or not. Write "1" or "true" to use SSH.
     */
    public static final ProfileSettings<Boolean> USE_SSH = new BooleanSetting();
    /**
     * The host for connecting over SSH.
     */
    public static final ProfileSettings<String> SSH_HOST = new StringSetting();
    /**
     * The port to use for the ssh connection.
     */
    public static final ProfileSettings<Integer> SSH_PORT = new IntegerSetting();
    /**
     * The host for connecting to the database.
     */
    public static final ProfileSettings<String> DATABASE_HOST = new StringSetting();
    /**
     * The port to use for the database connection.
     */
    public static final ProfileSettings<Integer> DATABASE_PORT = new IntegerSetting();
    /**
     * The name of the database to connect to.
     */
    public static final ProfileSettings<String> DATABASE_NAME = new StringSetting();
    /**
     * Indicates whether the generated SEPA is UTF-8 or "UTF-8 with BOM".
     */
    public static final ProfileSettings<Boolean> SEPA_USE_BOM = new BooleanSetting();
    /**
     * The regex to check against the value containing the birthday expression.
     */
    public static final Pattern BIRTHDAY_FUNCTION_PATTERN
            = Pattern.compile(" *((>=?)|(<=?)|=)[1-9]\\d*(, *((>=?)|(<=?)|=)[1-9]\\d*)* *");
    /**
     * The expression to indicate which people get birthday notifications. Like =50,=60,=70,=75,&gt;=80
     */
    public static final ProfileSettings<String> BIRTHDAY_EXPRESSION = new BirthdayFunctionSetting();
    /**
     * The charset used by the response of the ssh connection.
     */
    public static final ProfileSettings<Charset> SSH_CHARSET = new CharsetSetting();
    /**
     * The type of the SQL database. (e.g. MySQL).
     */
    public static final ProfileSettings<SupportedDBMS> DBMS = new SupportedDatabaseSetting();
    /**
     * Indicates whether to activate the features relating to the birthday of member.
     */
    public static final ProfileSettings<Boolean> ACTIVATE_BIRTHDAY_FEATURES = new BooleanSetting();
    public static final ProfileSettings<Boolean> USE_SSL_IF_NO_SSH = new BooleanSetting();

    /**
     * Contains all values like an enum. NOTE: It will be removed when generic enums are added to Java.
     */
    @Deprecated(forRemoval = true, since = "2u13")
    private static final ProfileSettings<?>[] VALUES = Arrays.stream(ProfileSettings.class.getFields())
            .filter(field -> {
                int mod = field.getModifiers();
                return Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod);
            })
            .filter(field -> field.getType().isAssignableFrom(ProfileSettings.class))
            .map(field -> {
                try {
                    return ProfileSettings.class.cast(field.get(null));
                } catch (IllegalAccessException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toArray(ProfileSettings<?>[]::new);

    /**
     * Returns the list of all values like an enum. NOTE: It will be removed when generic enums are added to Java.
     *
     * @return The list of available "enums".
     */
    @Deprecated(forRemoval = true, since = "2u13")
    public static ProfileSettings<?>[] values() {
        return Arrays.copyOf(VALUES, VALUES.length);
    }

    /**
     * Returns the name of the given "enum".
     *
     * @param setting The "enum" to retrieve the name for.
     * @return The name of the given "enum".
     * @see Enum#name()
     */
    @Deprecated(forRemoval = true, since = "2u13")
    public static String name(ProfileSettings<?> setting) {
        return Arrays.stream(ProfileSettings.class.getFields())
                .filter(field -> {
                    try {
                        return field.get(null) == setting;
                    } catch (IllegalAccessException ex) {
                        return false;
                    }
                })
                .findAny()
                .map(Field::getName)
                .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Returns the {@link ProfileSettings} object which has the name {@code name}. NOTE: It will be removed when Java
     * supports generic enums.
     *
     * @param name The name of the {@link ProfileSettings} object to return.
     * @return The appropriate {@link ProfileSettings} object.
     */
    @Deprecated(forRemoval = true, since = "2u13")
    public static final ProfileSettings<?> valueOf(String name) {
        Optional<Field> possibleEnum = Arrays.stream(ProfileSettings.class.getFields())
                .filter(field -> {
                    return ProfileSettings.class.isAssignableFrom(field.getType());
                })
                .filter(field -> field.getName().equalsIgnoreCase(name))
                .findAny();
        try {
            return (ProfileSettings<?>) possibleEnum.orElseThrow(IllegalArgumentException::new).get(null);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("The reimplementation based on Enum#valueOf(...) failed.", ex);
        }
    }

    /**
     * Checks whether the given value is valid for this ProfileSettings.
     *
     * @param value The value to check.
     * @return {@code true} only if this value is valid for this ProfileSettings.
     */
    public boolean isValid(T value) {
        return value != null;
    }

    /**
     * Parses the given value to an object of the type represented by this {@link ProfileSettings}. It is not checked
     * whether the result is a valid setting. NOTE: It can be assumed that the argument is never {@code null} since
     * {@code null} is handled by {@link #parse(java.lang.String)}.
     *
     * @param value The value to parse.
     * @return The object representing the value of the setting hold by {@code value} or {@link Optional#empty()} if the
     * value could not be parsed.
     * @see #toString(java.lang.Object)
     * @see #isValid(java.lang.Object)
     */
    protected abstract Optional<T> parseImpl(String value);

    /**
     * Parses the given value to an object of the type represented by this {@link ProfileSettings}. It is not checked
     * whether the result is a valid setting.
     *
     * @param value The value to parse.
     * @return The object representing the value of the setting hold by {@code value} or {@link Optional#empty()} if the
     * value could not be parsed or is {@code null}.
     * @see #isValid(java.lang.Object)
     */
    public final Optional<T> parse(String value) {
        Optional<T> parsed;
        if (value == null) {
            parsed = Optional.empty();
        } else {
            parsed = parseImpl(value);
        }
        return parsed;
    }

    /**
     * Returns a {@link String} representation of the given object which can be parsed to reproduce the given object.
     *
     * @param value The value to create a {@link String} for.
     * @return The {@link String} representation for which {@link #parse(java.lang.String)} returns {@code value}.
     */
    public String toString(T value) {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass() + ":" + name(this);
    }

    /**
     * A setting holding a {@link String}.
     */
    private static class StringSetting extends ProfileSettings<String> {

        @Override
        protected Optional<String> parseImpl(String value) {
            return Optional.of(value);
        }
    }

    /**
     * A setting holding an {@link Integer}.
     */
    private static class IntegerSetting extends ProfileSettings<Integer> {

        @Override
        protected Optional<Integer> parseImpl(String value) {
            Optional<Integer> parsed;
            try {
                parsed = Optional.of(Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                parsed = Optional.empty();
            }
            return parsed;
        }
    }

    /**
     * A setting holding a {@link Boolean}.
     */
    private static class BooleanSetting extends ProfileSettings<Boolean> {

        @Override
        protected Optional<Boolean> parseImpl(String value) {
            return Optional.of("1".equalsIgnoreCase(value) || Boolean.parseBoolean(value));
        }
    }

    /**
     * A setting holding a {@link String} representing a birthday function. Such a function is used to determine whether
     * a person of certain age gets congratulations.
     */
    private static class BirthdayFunctionSetting extends ProfileSettings<String> {

        @Override
        public boolean isValid(String value) {
            return BIRTHDAY_FUNCTION_PATTERN.matcher(value).matches() || value.isEmpty();
        }

        @Override
        protected Optional<String> parseImpl(String value) {
            return Optional.of(value);
        }
    }

    /**
     * A setting holding a {@link Charset}.
     */
    private static class CharsetSetting extends ProfileSettings<Charset> {

        @Override
        public boolean isValid(Charset value) {
            return true; //NOTE A Charset can only be constructed if and only if it is supported and therefore is valid.
        }

        @Override
        protected Optional<Charset> parseImpl(String value) {
            Charset charset = null;
            if (Charset.isSupported(value)) {
                charset = Charset.forName(value);
            }
            return Optional.ofNullable(charset);
        }
    }

    /**
     * A setting holding a dbms of {@link SupportedDBMS}.
     */
    private static class SupportedDatabaseSetting extends ProfileSettings<SupportedDBMS> {

        @Override
        public String toString(SupportedDBMS value) {
            return value.getShellCommand();
        }

        @Override
        public boolean isValid(SupportedDBMS value) {
            return true; //NOTE: An enum of a class can only be constructed if and only if it already exists.
        }

        @Override
        protected Optional<SupportedDBMS> parseImpl(String value) {
            SupportedDBMS supportedDbms = null;
            for (SupportedDBMS dbms : SupportedDBMS.DBMSs) {
                if (dbms.getShellCommand().equalsIgnoreCase(value)) {
                    supportedDbms = dbms;
                    break;
                }
            }
            return Optional.ofNullable(supportedDbms);
        }
    }
}

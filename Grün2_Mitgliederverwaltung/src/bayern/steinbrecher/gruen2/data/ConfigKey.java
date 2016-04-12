package bayern.steinbrecher.gruen2.data;

/**
 * Represents all options allowed to configure in gruen2.conf.
 *
 * @author Stefan Huber
 */
public enum ConfigKey {

    /**
     * Indicating whether to use SSH or not. Write "Ja" to use SSH.
     */
    USE_SSH,
    /**
     * The host for connecting over SSH.
     */
    SSH_HOST,
    /**
     * The host for connecting to the database.
     */
    DATABASE_HOST,
    /**
     * The name of the database to connect to.
     */
    DATABASE_NAME,
    /**
     * The expression to indicate which people get birthday notifications. Like
     * =50,=60,=70,=75,&gt;=80
     */
    BIRTHDAY_EXPRESSION;
}

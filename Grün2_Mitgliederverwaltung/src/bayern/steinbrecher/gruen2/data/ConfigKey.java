package bayern.steinbrecher.gruen2.data;

/**
 * Represents all options allowed to configure in gruen2.conf.
 *
 * @author Stefan Huber
 */
public enum ConfigKey {

    /**
     * Keys indicating configurations like addresses of database- and ssh-host,
     * the name of the database or whether to use ssh to connect to a database.
     */
    USE_SSH, SSH_HOST, DATABASE_HOST, DATABASE_NAME, BIRTHDAY_EXPRESSION;
}

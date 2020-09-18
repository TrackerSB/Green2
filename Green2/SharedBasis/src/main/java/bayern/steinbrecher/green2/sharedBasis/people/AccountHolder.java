package bayern.steinbrecher.green2.sharedBasis.people;

import java.time.LocalDate;

/**
 * Represents an account holder. Since it has a variety of fields it is designed to be constructed by chaining calls to
 * setter instead of a constructor.
 *
 * @author Stefan Huber
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public final class AccountHolder {

    private String prename, lastname, title;
    private String iban, bic;
    private LocalDate mandateSigned;
    private boolean hasMandateChanged;

    private AccountHolder() {
        //A completely uninitialized person (Only to be used by the builder).
    }

    /**
     * Creates a fully initialized {@link AccountHolder}.
     *
     * @param iban The IBAN of the bank account.
     * @param bic The BIC of the bank account.
     * @param mandateSigned The date when the SEPA mandate was signed.
     * @param hasMandateChanged {@code true} only if the SEPA mandate changed since last usage.
     * @param prename The prename of the {@link AccountHolder}.
     * @param lastname The lastname of the {@link AccountHolder}.
     * @param title The title of the {@link AccountHolder} like "Dr." or "Ph.D." if any. It may be a good idea to use
     * empty {@link String} for no title.
     */
    public AccountHolder(String iban, String bic, LocalDate mandateSigned, boolean hasMandateChanged, String prename,
            String lastname, String title) {
        this.prename = prename;
        this.lastname = lastname;
        this.title = title;
        this.iban = iban;
        this.bic = bic;
        this.mandateSigned = mandateSigned;
        this.hasMandateChanged = hasMandateChanged;
    }

    /**
     * Returns the first name.
     *
     * @return The first name.
     */
    public String getPrename() {
        return prename;
    }

    /**
     * Returns the last name.
     *
     * @return The last name.
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Returns last name and first name space separated.
     *
     * @return last name and first name space separated.
     */
    public String getName() {
        return getLastname() + " " + getPrename();
    }

    /**
     * Returns the title if any.
     *
     * @return The title if any.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the IBAN of the account holder.
     *
     * @return The IBAN of the account holder.
     */
    public String getIban() {
        return iban;
    }

    /**
     * Checks whether the account holder has a IBAN.
     *
     * @return {@code true} only if this account holder has a IBAN.
     */
    public boolean hasIban() {
        return !iban.isEmpty();
    }

    /**
     * Returns the BIC of this account holder.
     *
     * @return The BIC of this account holder.
     */
    public String getBic() {
        return bic;
    }

    /**
     * Checks whether this account holder a a BIC.
     *
     * @return {@code true} only if this account holder has a BIC.
     */
    public boolean hasBic() {
        return !bic.isEmpty();
    }

    /**
     * Returns the date when this account holder signed his mandate.
     *
     * @return The date when this account holder signed his mandate.
     */
    public LocalDate getMandateSigned() {
        return mandateSigned;
    }

    /**
     * Returns whether the mandate changed since the last transfer.
     *
     * @return {@code true} only if the mandate changed since the last transfer.
     */
    public boolean hasMandateChanged() {
        return hasMandateChanged;
    }

    /**
     * Allows to build an {@link AccountHolder} stepwise.
     */
    public static final class Builder extends PeopleBuilder<AccountHolder> {

        /**
         * Creates a {@link Builder} with no values of {@link AccountHolder} are set.
         */
        public Builder() {
            this(new AccountHolder());
        }

        /**
         * Creates a {@link Builder} whose initial values are taken from the given {@link AccountHolder}.
         *
         * @param accountHolder The {@link AccountHolder} to take the initial values from.
         */
        public Builder(AccountHolder accountHolder) {
            super(accountHolder);
        }

        /**
         * Sets a new first name.
         *
         * @param prename The new first name.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setPrename(String prename) {
            getToBuild().prename = prename;
            return this;
        }

        /**
         * Sets a new last name.
         *
         * @param lastname The new last name.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setLastname(String lastname) {
            getToBuild().lastname = lastname;
            return this;
        }

        /**
         * Sets a new title.
         *
         * @param title The new title.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setTitle(String title) {
            getToBuild().title = title;
            return this;
        }

        /**
         * Sets the IBAN to assocate with a {@link AccountHolder}.
         *
         * @param iban The IBAN to assocate with a {@link AccountHolder}.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setIban(String iban) {
            getToBuild().iban = iban;
            return this;
        }

        /**
         * Sets the BIC to assocate with a {@link AccountHolder}.
         *
         * @param bic The BIC to assocate with a {@link AccountHolder}.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setBic(String bic) {
            getToBuild().bic = bic;
            return this;
        }

        /**
         * Sets the date when a {@link AccountHolder} signed his/her mandat.
         *
         * @param mandateSigned The date when a {@link AccountHolder} signed his/her mandat.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setMandateSigned(LocalDate mandateSigned) {
            getToBuild().mandateSigned = mandateSigned;
            return this;
        }

        /**
         * Sets whether the mandat changed since its last use.
         *
         * @param hasMandateChanged {@code true} only if the mandat changed since its last use.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setHasMandateChanged(Boolean hasMandateChanged) {
            getToBuild().hasMandateChanged = hasMandateChanged;
            return this;
        }
    }
}

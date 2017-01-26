/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.people;

import java.time.LocalDate;

/**
 * Represents an account holder.
 *
 * @author Stefan Huber
 */
public class AccountHolder extends Person {

    private final String iban, bic;
    private final LocalDate mandatSigned;
    private final boolean hasMandatChanged;

    /**
     * Constructes a account holder.
     *
     * @param iban             The IBAN of the account holder.
     * @param bic              The BIC of the accound holder.
     * @param mandatSigned     The date when he signed his mandat.
     * @param hasMandatChanged {@code true}, only if his mandat changed since
     *                         last transfer.
     * @param prename          The prename of the account holder.
     * @param lastname         The lastname of the account holder.
     * @param title            The title of the account holder if any.
     * @param birthday         The birthday date of the account holder.
     * @param isMale           {@code true} only if the account holder is male.
     */
    public AccountHolder(String iban, String bic, LocalDate mandatSigned, boolean hasMandatChanged, String prename,
                         String lastname, String title, LocalDate birthday, boolean isMale) {
        super(prename, lastname, title, birthday, isMale);
        this.iban = iban;
        this.bic = bic;
        this.mandatSigned = mandatSigned;
        this.hasMandatChanged = hasMandatChanged;
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
     * Returns the date when this account holder signed his mandat.
     *
     * @return The date when this account holder signed his mandat.
     */
    public LocalDate getMandatSigned() {
        return mandatSigned;
    }

    /**
     * Returns whether the mandat changed since the last transfer.
     *
     * @return {@code true} only if the mandat changed since the last transfer.
     */
    public boolean hasMandatChanged() {
        return hasMandatChanged;
    }
}

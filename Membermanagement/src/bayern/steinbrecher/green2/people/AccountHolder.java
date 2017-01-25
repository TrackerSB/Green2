/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    public AccountHolder(String iban, String bic, LocalDate mandatSigned,
                         boolean hasMandatChanged, String prename, String lastname,
                         String title, LocalDate birthday, boolean isMale) {
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

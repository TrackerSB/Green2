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
package bayern.steinbrecher.green2.people;

import java.time.LocalDate;

/**
 * Represents an account holder.
 *
 * @author Stefan Huber
 */
public class AccountHolder extends Person {

    private String iban, bic;
    private LocalDate mandateSigned;
    private boolean hasMandateChanged;

    /**
     * Constructs a account holder.
     *
     * @param iban The IBAN of the account holder.
     * @param bic The BIC of the account holder.
     * @param mandateSigned The date when he signed his mandate.
     * @param hasMandateChanged {@code true}, only if his mandate changed since last transfer.
     * @param prename The firstName of the account holder.
     * @param lastname The last name of the account holder.
     * @param title The title of the account holder if any.
     * @param birthday The birthday date of the account holder.
     * @param male {@code true} only if the account holder is male.
     */
    public AccountHolder(String iban, String bic, LocalDate mandateSigned, boolean hasMandateChanged, String prename,
            String lastname, String title, LocalDate birthday, boolean male) {
        super(prename, lastname, title, birthday, male);
        this.iban = iban;
        this.bic = bic;
        this.mandateSigned = mandateSigned;
        this.hasMandateChanged = hasMandateChanged;
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
     * Changes the IBAN.
     *
     * @param iban The new IBAN.
     */
    public void setIban(String iban) {
        this.iban = iban;
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
     * Changes the BIC.
     *
     * @param bic The new BIC.
     */
    public void setBic(String bic) {
        this.bic = bic;
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
     * Changes the date when the mandat was signed.
     *
     * @param mandateSigned The date when the mandat was signed.
     */
    public void setMandateSigned(LocalDate mandateSigned) {
        this.mandateSigned = mandateSigned;
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
     * Changes whether the mandat has changed.
     *
     * @param hasMandateChanged {@code true} if the mandat has changed.
     */
    public void setHasMandateChanged(boolean hasMandateChanged) {
        this.hasMandateChanged = hasMandateChanged;
    }
}

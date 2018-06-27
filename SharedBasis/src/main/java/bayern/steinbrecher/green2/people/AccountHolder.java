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
 * Represents an account holder. Since it has a variety of fields it is designed to be constructed by chaining calls to
 * setter instead of a constructor.
 *
 * @author Stefan Huber
 */
public class AccountHolder extends Person {

    private String iban, bic;
    private LocalDate mandateSigned;
    private boolean hasMandateChanged;

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountHolder setBirthday(LocalDate birthday) {
        super.setBirthday(birthday);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountHolder setLastname(String lastname) {
        super.setLastname(lastname);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountHolder setMale(boolean male) {
        super.setMale(male);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountHolder setPrename(String prename) {
        super.setPrename(prename);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountHolder setTitle(String title) {
        super.setTitle(title);
        return this;
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
     * @return This {@link AccountHolder} which can be used for chaining calls to setter.
     */
    public AccountHolder setIban(String iban) {
        this.iban = iban;
        return this;
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
     * @return This {@link AccountHolder} which can be used for chaining calls to setter.
     */
    public AccountHolder setBic(String bic) {
        this.bic = bic;
        return this;
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
     * @return This {@link AccountHolder} which can be used for chaining calls to setter.
     */
    public AccountHolder setMandateSigned(LocalDate mandateSigned) {
        this.mandateSigned = mandateSigned;
        return this;
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
     * @return This {@link AccountHolder} which can be used for chaining calls to setter.
     */
    public AccountHolder setHasMandateChanged(boolean hasMandateChanged) {
        this.hasMandateChanged = hasMandateChanged;
        return this;
    }
}

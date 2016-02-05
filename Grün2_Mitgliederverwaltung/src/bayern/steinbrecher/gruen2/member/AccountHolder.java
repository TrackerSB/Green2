package bayern.steinbrecher.gruen2.member;

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

    public AccountHolder(String iban, String bic, LocalDate mandatSigned,
            boolean hasMandatChanged, String prename, String lastname,
            String title, LocalDate birthday, boolean isMale) {
        super(prename, lastname, title, birthday, isMale);
        this.iban = iban;
        this.bic = bic;
        this.mandatSigned = mandatSigned;
        this.hasMandatChanged = hasMandatChanged;
    }

    public String getIban() {
        return iban;
    }

    public boolean hasIban() {
        return !iban.isEmpty();
    }

    public String getBic() {
        return bic;
    }

    public boolean hasBic() {
        return !bic.isEmpty();
    }

    public LocalDate getMandatSigned() {
        return mandatSigned;
    }

    public boolean hasMandatChanged() {
        return hasMandatChanged;
    }
}

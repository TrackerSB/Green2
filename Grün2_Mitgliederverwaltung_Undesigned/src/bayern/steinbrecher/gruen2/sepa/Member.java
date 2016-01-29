package bayern.steinbrecher.gruen2.sepa;

/**
 * Jedes Objekt dieser Klasse stellt ein Mitglied des Trachtenvereins samt
 * seinen Kontoinformationen dar.
 *
 * @author Stefan Huber
 */
public class Member implements Comparable<Member> {

    private final int mitgliedsnummer;
    private final String mandatErstellt,
            bic,
            nachname,
            vorname,
            iban;
    private final boolean hasMandatChanged;

    /**
     * Konstruktor.
     * @param mitgliedsnummer Die Mitgliedsnummer
     * @param mandatErstellt 
     * @param bic
     * @param nachname
     * @param vorname
     * @param iban 
     * @param hasMandatChanged 
     */
    public Member(int mitgliedsnummer, String mandatErstellt, String bic, String nachname, String vorname, String iban, boolean hasMandatChanged) {
        this.mitgliedsnummer = mitgliedsnummer;
        this.mandatErstellt = mandatErstellt;
        this.bic = bic;
        this.nachname = nachname;
        this.vorname = vorname;
        this.iban = iban;
        this.hasMandatChanged = hasMandatChanged;
    }

    public int getMitgliedsnummer() {
        return mitgliedsnummer;
    }

    public boolean hasMandatChanged() {
        return hasMandatChanged;
    }

    public String getMandatErstellt() {
        return mandatErstellt;
    }

    public String getBic() {
        return bic;
    }

    public String getNachname() {
        return nachname;
    }

    public String getVorname() {
        return vorname;
    }

    public String getIban() {
        return iban;
    }

    public boolean hasIban() {
        return !iban.isEmpty();
    }

    public boolean hasBic() {
        return !bic.isEmpty();
    }

    @Override
    public String toString() {
        return mitgliedsnummer + "\t" + nachname + " " + vorname;
    }

    @Override
    public int compareTo(Member other) {
        return (nachname + vorname).compareTo(other.nachname + other.vorname);
        //FIXME Sortierung bzgl. Umlauten korrigieren
    }
}

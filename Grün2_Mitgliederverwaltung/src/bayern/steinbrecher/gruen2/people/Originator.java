package bayern.steinbrecher.gruen2.people;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an originator of a "Sepa-Sammellastschrift".
 *
 * @author Stefan Huber
 */
public class Originator {

    private String creator,
            msgId,
            creditor,
            iban,
            bic,
            trusterId,
            pmtInfId,
            purpose,
            filename;
    private static final Properties DEFAULT_PROPERTIES = new Properties() {
        {
            put("creator", "");
            put("creditor", "");
            put("iban", "");
            put("bic", "");
            put("trusterId", "");
            put("pmtInfId", "");
            put("purpose", "");
            put("executionDate", "");
        }
    };
    private LocalDate executionDate;

    /**
     * Erstellt ein Objekt, das die Sepa-Eigeninfos der Datei
     * <code>filename</code> repr&auml;sentiert.
     *
     * @param filename Der Name der Eigeninfos-Datei
     */
    public Originator(String filename) {
        this.filename = filename;
    }

    /**
     * Erstellt ein Originator-Objekt und liest die Datei <code>filename</code>
     * ein, die Informationen &uuml;ber den Ersteller des Einzuges enth&auml;lt,
     * falls die Datei existiert.
     *
     * @param filename Die einzulesende Datei
     * @return Neues Originator-Objekt, das <code>filename</code> bereits
     * eingelesen hat.
     * @throws java.io.FileNotFoundException Tritt auf, wenn die Datei nicht
     * gefunden werden konnte.
     */
    public static Originator readOriginatorInfo(String filename)
            throws FileNotFoundException {
        Originator e = new Originator(filename);
        e.readOriginatorInfo();
        return e;
    }

    /**
     * Liest die Datei, die den Originator-Objekt bei der Erzeugung
     * &uuml;bergeben wurde ein, falls die Datei existiert.
     *
     * @throws FileNotFoundException Tritt auf, wenn die Datei nicht gefunden
     * werden konnte.
     */
    public void readOriginatorInfo() throws FileNotFoundException {
        try {
            Properties originatorProps = new Properties(DEFAULT_PROPERTIES);
            originatorProps.load(new FileInputStream(new File(filename)));
            Arrays.stream(getClass().getDeclaredFields())
                    .parallel()
                    .filter(f -> !f.getName().equalsIgnoreCase("filename"))
                    .forEach(f -> {
                        try {
                            String property
                                    = originatorProps.getProperty(f.getName());

                            if (f.getType() == LocalDate.class) {
                                f.set(this, LocalDate.parse(property));
                            } else if (f.getType() == String.class) {
                                f.set(this, property);
                            } else {
                                System.err.println(Originator.class
                                        + ": No action for reading in "
                                        + f.getType()
                                        + " defined. Gets skipped.");
                            }
                        } catch (IllegalArgumentException |
                                IllegalAccessException ex) {
                            Logger.getLogger(Originator.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    });
        } catch (IOException ex) {
            Logger.getLogger(Originator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Schreibt die eingegebenen Daten in {@code filename}. Falls die Datei noch
     * nicht existiert, wird sie erstellt.
     */
    public void saveOriginator() {
        Properties originatorProps = new Properties(DEFAULT_PROPERTIES);
        Arrays.stream(getClass().getDeclaredFields())
                .parallel()
                .filter(f ->
                        !f.getName().equalsIgnoreCase("default_properties"))
                .filter(f -> !f.getName().equalsIgnoreCase("filename"))
                .forEach(f -> {
                    try {
                        originatorProps.put(
                                f.getName(), f.get(this).toString());
                    } catch (IllegalArgumentException |
                            IllegalAccessException ex) {
                        Logger.getLogger(Originator.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                });
        try {
            originatorProps.store(
                    new BufferedWriter(new FileWriter(filename)), null);
        } catch (Exception ex) {
            Logger.getLogger(Originator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getCreditor() {
        return creditor;
    }

    public void setCreditor(String creditor) {
        this.creditor = creditor;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getTrusterId() {
        return trusterId;
    }

    public void setTrusterId(String trusterId) {
        this.trusterId = trusterId;
    }

    public String getPmtInfId() {
        return pmtInfId;
    }

    public void setPmtInfId(String pmtInfId) {
        this.pmtInfId = pmtInfId;
    }

    public LocalDate getExecutiondate() {
        return executionDate;
    }

    public void setExecutiondate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}

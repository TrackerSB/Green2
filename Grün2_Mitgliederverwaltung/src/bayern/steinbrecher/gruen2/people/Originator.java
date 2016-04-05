package bayern.steinbrecher.gruen2.people;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an originator of a Sepa direct debit.
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
     * Constructes a new originator which has owns the attributes specified in
     * {@code filename}. HINT: Attributes are only set after calling
     * {@code readOriginatorInfo()}.
     *
     * @param filename The file containing all attributes.
     */
    public Originator(String filename) {
        this.filename = filename;
    }

    /**
     * Constructes a new originator out of the attributes specified in
     * {@code filename}.
     *
     * @param filename The file to read from.
     * @return The new originator
     * @throws java.io.FileNotFoundException Thrown if the file could not be
     * found.
     */
    public static Originator readOriginatorInfo(String filename)
            throws FileNotFoundException {
        Originator e = new Originator(filename);
        e.readOriginatorInfo();
        return e;
    }

    /**
     * Reads in the specified file and sets the apropriate attributes.
     *
     * @throws FileNotFoundException Thrown if the file could not be found.
     */
    public void readOriginatorInfo() throws FileNotFoundException {
        try {
            Properties originatorProps = new Properties(DEFAULT_PROPERTIES);
            originatorProps.load(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
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
     * Saves the currently set attributes into file given at construction.
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
                    new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "UTF-8")), null);
        } catch (Exception ex) {
            Logger.getLogger(Originator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the name of the creator.
     *
     * @return The name of the creator.
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets a new name for the creator.
     *
     * @param creator The new name for the creator.
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Returns the message id.
     *
     * @return The message id.
     */
    public String getMsgId() {
        return msgId;
    }

    /**
     * Sets a new message id.
     *
     * @param msgId The new message id.
     */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /**
     * Returns the name of the creditor.
     *
     * @return The name of the creditor.
     */
    public String getCreditor() {
        return creditor;
    }

    /**
     * Sets a new name for the creditor.
     *
     * @param creditor The new name of the creditor.
     */
    public void setCreditor(String creditor) {
        this.creditor = creditor;
    }

    /**
     * Returns the IBAN.
     *
     * @return The IBAN.
     */
    public String getIban() {
        return iban;
    }

    /**
     * Sets a new IBAN.
     *
     * @param iban The new IBAN.
     */
    public void setIban(String iban) {
        this.iban = iban;
    }

    /**
     * Returns the BIC.
     *
     * @return The BIC.
     */
    public String getBic() {
        return bic;
    }

    /**
     * Sets a new BIC.
     *
     * @param bic The new BIC.
     */
    public void setBic(String bic) {
        this.bic = bic;
    }

    /**
     * Returns the current truster id.
     *
     * @return The truster id.
     */
    public String getTrusterId() {
        return trusterId;
    }

    /**
     * Sets a new truster id.
     *
     * @param trusterId The new truster id.
     */
    public void setTrusterId(String trusterId) {
        this.trusterId = trusterId;
    }

    /**
     * Returns the currently set payment information id.
     *
     * @return The payment information id.
     */
    public String getPmtInfId() {
        return pmtInfId;
    }

    /**
     * Sets a new payment information id.
     *
     * @param pmtInfId The new payment information id.
     */
    public void setPmtInfId(String pmtInfId) {
        this.pmtInfId = pmtInfId;
    }

    /**
     * Returns the execution date.
     *
     * @return The execution date.
     */
    public LocalDate getExecutiondate() {
        return executionDate;
    }

    /**
     * Sets a new execution date.
     *
     * @param executionDate The new execution date.
     */
    public void setExecutiondate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    /**
     * Returns the purpose of direct debits.
     *
     * @return The purpose of direct debits.
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * Sets a new purpose for direct debits.
     *
     * @param purpose The new purpose for direct debits.
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}

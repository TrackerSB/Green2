package bayern.steinbrecher.green2.memberManagement.people;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Profile;
import bayern.steinbrecher.sepaxmlgenerator.CreditorId;
import bayern.steinbrecher.sepaxmlgenerator.IBAN;
import bayern.steinbrecher.sepaxmlgenerator.MessageId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an originator of a Sepa direct debit.
 *
 * @author Stefan Huber
 */
public class Originator {

    private static final Logger LOGGER = Logger.getLogger(Originator.class.getName());
    private String creator,
            msgId,
            creditor,
            iban,
            bic,
            creditorId,
            pmtInfId,
            purpose;
    private final File originatorFile;
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    static {
        DEFAULT_PROPERTIES.put("creator", "");
        DEFAULT_PROPERTIES.put("creditor", "");
        DEFAULT_PROPERTIES.put("iban", "");
        DEFAULT_PROPERTIES.put("bic", "");
        DEFAULT_PROPERTIES.put("trusterId", "");
        DEFAULT_PROPERTIES.put("pmtInfId", "");
        DEFAULT_PROPERTIES.put("purpose", "");
        DEFAULT_PROPERTIES.put("executionDate", "");
    }
    private LocalDate executionDate;

    /**
     * Constructs a new originator whose attributes are specified by the currently loaded {@link Profile}.
     *
     * @see EnvironmentHandler#getProfile()
     */
    public Originator() {
        this(EnvironmentHandler.getProfile());
    }

    /**
     * Constructs a new originator which has the attributes specified in {@code originatorFile}. NOTE: Attributes are
     * only set after calling {@link #readOriginatorInfo()}.
     *
     * @param profile The profile this originator belongs to.
     */
    public Originator(Profile profile) {
        this.originatorFile = profile.getOriginatorInfoFile();
    }

    /**
     * Constructs a new {@link Originator} out of the attributes of the currently loaded {@link Profile}.
     *
     * @return The new originator or {@link Optional#empty()} if the file was not found or could not be read.
     * @see EnvironmentHandler#getProfile()
     */
    public static Optional<Originator> readCurrentOriginatorInfo() {
        return readOriginatorInfo(EnvironmentHandler.getProfile());
    }

    /**
     * Constructs a new originator out of the attributes specified in {@code originatorFile} of {@link Profile}.
     *
     * @param profile The profile the originator has to belong to.
     * @return The new originator or {@link Optional#empty()} if the file was not found or could not be read.
     * @see Profile#getOriginatorInfoFile()
     */
    public static Optional<Originator> readOriginatorInfo(Profile profile) {
        Originator originator = new Originator(profile);
        Optional<Originator> originatorInfo;
        if (originator.readOriginatorInfo()) {
            originatorInfo = Optional.of(originator);
        } else {
            originatorInfo = Optional.empty();
        }
        return originatorInfo;
    }

    /**
     * Reads the originator of the specified {@link Profile}.
     *
     * @return {@code true} only if the file associated with this originator was found and could be read.
     * @see Profile#getOriginatorInfoFile()
     */
    public boolean readOriginatorInfo() {
        boolean readSuccessfull;
        if (originatorFile.exists()) {
            try (Reader reader = new InputStreamReader(Files.newInputStream(originatorFile.toPath()), "UTF-8")) {
                Properties originatorProps = new Properties(DEFAULT_PROPERTIES);
                originatorProps.load(reader);
                Arrays.stream(getClass().getDeclaredFields())
                        .parallel()
                        .filter(f -> !"originatorFile".equalsIgnoreCase(f.getName()))
                        .forEach(f -> {
                            try {
                                String property = originatorProps.getProperty(f.getName());

                                if (f.getType() == LocalDate.class) {
                                    f.set(this, LocalDate.parse(property));
                                } else if (f.getType() == String.class) {
                                    f.set(this, property);
                                } else {
                                    LOGGER.log(Level.INFO, "No action for reading in {0} defined. Gets skipped.",
                                            f.getType());
                                }
                            } catch (IllegalArgumentException | IllegalAccessException ex) {
                                LOGGER.log(Level.WARNING, null, ex);
                            }
                        });
                readSuccessfull = true;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
                readSuccessfull = false;
            }
        } else {
            readSuccessfull = false;
        }
        return readSuccessfull;
    }

    /**
     * Saves the currently set attributes into file given at construction.
     */
    public void saveOriginator() {
        Properties originatorProps = new Properties(DEFAULT_PROPERTIES);
        Arrays.stream(getClass().getDeclaredFields())
                .parallel()
                .filter(f -> !f.getName().equalsIgnoreCase("default_properties"))
                .filter(f -> !f.getName().equalsIgnoreCase("originatorFile"))
                .forEach(f -> {
                    try {
                        originatorProps.put(f.getName(), f.get(this).toString());
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                });
        try (Writer writer
                = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(originatorFile.toPath()), "UTF-8"))) {
            originatorProps.store(writer, null);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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
        if (!new MessageId(msgId).isValid()) {
            throw new IllegalArgumentException("\"" + msgId + "\" no valid message id.");
        }
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
        if (!new IBAN(iban).isValid()) {
            throw new IllegalArgumentException("\"" + iban + "\" is no valid iban.");
        }
        this.iban = iban.replaceAll(" ", "");
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
    public String getCreditorId() {
        return creditorId;
    }

    /**
     * Sets a new truster id.
     *
     * @param creditorId The new truster id.
     */
    public void setCreditorId(String creditorId) {
        if (!new CreditorId(creditorId).isValid()) {
            throw new IllegalArgumentException("\"" + creditorId + "\" is no valid creditor id.");
        }
        this.creditorId = creditorId.replaceAll(" ", "");
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
    public LocalDate getExecutionDate() {
        return executionDate;
    }

    /**
     * Sets a new execution date.
     *
     * @param executionDate The new execution date.
     */
    public void setExecutionDate(LocalDate executionDate) {
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

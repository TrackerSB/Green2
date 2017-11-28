/* 
 * Copyright (C) 2017 Stefan Huber
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

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.utility.SepaUtility;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

    private String creator,
            msgId,
            creditor,
            iban,
            bic,
            creditorId,
            pmtInfId,
            purpose;
    private final File originatorFile;
    private static final Properties DEFAULT_PROPERTIES = new Properties() {
        private static final long serialVersionUID = 1L;

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
        return Optional.ofNullable(originator.readOriginatorInfo() ? originator : null);
    }

    /**
     * Reads the originator of the specified {@link Profile}.
     *
     * @return {@code true} only if the file associated with this originator was found and could be read.
     * @see Profile#getOriginatorInfoFile()
     */
    public boolean readOriginatorInfo() {
        if (originatorFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(originatorFile), "UTF-8")) {
                Properties originatorProps = new Properties(DEFAULT_PROPERTIES);
                originatorProps.load(reader);
                Arrays.stream(getClass().getDeclaredFields())
                        .parallel()
                        .filter(f -> !f.getName().equalsIgnoreCase("originatorFile"))
                        .forEach(f -> {
                            try {
                                String property = originatorProps.getProperty(f.getName());

                                if (f.getType() == LocalDate.class) {
                                    f.set(this, LocalDate.parse(property));
                                } else if (f.getType() == String.class) {
                                    f.set(this, property);
                                } else {
                                    Logger.getLogger(Originator.class.getName())
                                            .log(Level.INFO, "No action for reading in {0} defined. Gets skipped.",
                                                    f.getType());
                                }
                            } catch (IllegalArgumentException | IllegalAccessException ex) {
                                Logger.getLogger(Originator.class.getName()).log(Level.WARNING, null, ex);
                            }
                        });
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Originator.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        return false;
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
                        Logger.getLogger(Originator.class.getName()).log(Level.WARNING, null, ex);
                    }
                });
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(originatorFile), "UTF-8"))) {
            originatorProps.store(writer, null);
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            Logger.getLogger(Originator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Originator.class.getName()).log(Level.SEVERE, null, ex);
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
        if (!SepaUtility.isValidMessageId(msgId)) {
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
        if (!SepaUtility.isValidIban(iban)) {
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
        if (!SepaUtility.isValidCreditorId(creditorId)) {
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

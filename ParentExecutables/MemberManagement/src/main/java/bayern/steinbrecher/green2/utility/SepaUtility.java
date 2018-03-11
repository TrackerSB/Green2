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
package bayern.steinbrecher.green2.utility;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Contains methods for checking some Sepa Direct Debit attributes which are especially needed by {@code Originator} and
 * {@code SepaPain00800302XMLGenerator}.
 *
 * @author Stefan Huber
 */
public final class SepaUtility {

    /**
     * Holds the count of days the MessageId has to be unique.
     */
    public static final int UNIQUE_DAYS_MESSAGEID = 15;
    /**
     * The maximum length of the message id.
     */
    public static final int MAX_CHAR_MESSAGE_ID = 35;
    /**
     * Holds the count of month the PmtInfId has to be unique.
     */
    public static final int UNIQUE_MONTH_PMTINFID = 3;
    /**
     * The maximum length of payment information id (PmtInfId).
     */
    public static final int MAX_CHAR_PMTINFID = 35;
    /**
     * The maximum length of an IBAN.
     */
    public static final int MAX_CHAR_IBAN = 34;
    /**
     * The maximum length of the name of the party creating the SEPA Direct Debit.
     */
    public static final int MAX_CHAR_NAME_FIELD = 70;

    /**
     * Length of countrycode (CC);
     */
    private static final int SEPA_CC_LENGTH = 2;
    /**
     * Length of countrycode (CC) and checksum together.
     */
    private static final int SEPA_CC_CHECKSUM_LENGTH = SEPA_CC_LENGTH + 2;
    private static final int SEPA_MIN_LENGTH = SEPA_CC_CHECKSUM_LENGTH + 1;
    private static final String SEPA_BUSINESS_CODE = "ZZZ";
    /**
     * A=10, B=11, C=12 etc.
     */
    private static final int SEPA_SHIFT_CC = 10;
    private static final int IBAN_CHECKSUM_MODULO = 97;
    /**
     * Regex describing a possible valid IBAN. (Checksum of the IBAN is not checked by this regex)
     */
    public static final String IBAN_REGEX
            = "[A-Z]{" + SEPA_CC_LENGTH + "}\\d{2," + (MAX_CHAR_IBAN - SEPA_CC_LENGTH) + "}";
    private static final Pattern IBAN_PATTERN = Pattern.compile(IBAN_REGEX);
    /**
     * Regex describing a possible valid BIC. NOTE Currently only length and valid characters are checked.
     */
    public static final String BIC_REGEX = "([A-Z]|\\d){8}(([A-Z]|\\d){3})?";
    private static final Pattern BIC_PATTERN = Pattern.compile(BIC_REGEX);
    /**
     * The regex for checking whether a message id is valid. Which characters are supported by Sepa is taken from
     * http://www.sepaforcorporates.com/sepa-implementation/valid-xml-characters-sepa-payments/
     */
    public static final String MESSAGE_ID_REGEX = "([a-zA-Z0-9]|/| |-|\\?|:|\\(|\\)|\\.|,|'|\\+)*";
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile(MESSAGE_ID_REGEX);
    private static /*final*/ Validator SEPA_VALIDATOR;

    static {
        try {
            SEPA_VALIDATOR = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    //Source of schema:
                    //https://github.com/w2c/sepa-sdd-xml-generator/blob/master/validation_schemes/pain.008.003.02.xsd
                    .newSchema(SepaUtility.class.getResource("pain.008.003.02.xsd"))
                    .newValidator();
        } catch (SAXException ex) {
            Logger.getLogger(SepaUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Prohibit instantiation.
     */
    private SepaUtility() {
        throw new UnsupportedOperationException("Construction of an object no allowed.");
    }

    /**
     * Checks whether the IBAN of the given member has a valid checksum.
     *
     * @param iban The IBAN to check.
     * @return {@code true} only if IBAN has a valid checksum.
     * @see #IBAN_PATTERN
     */
    public static boolean isValidIban(String iban) {
        if (iban == null || iban.isEmpty()) {
            return false;
        }

        String trimmedIban = iban.replace(" ", "");

        //Check whether it CAN be a valid IBAN
        if (!IBAN_PATTERN.matcher(trimmedIban).matches()) {
            return false;
        }

        //Check the checksum
        int posAlphabetFirstChar = ((int) trimmedIban.charAt(0)) - ((int) 'A') + SEPA_SHIFT_CC;
        int posAlphabetSecondChar = ((int) trimmedIban.charAt(1)) - ((int) 'A') + SEPA_SHIFT_CC;
        if (trimmedIban.length() < SEPA_MIN_LENGTH || posAlphabetFirstChar < SEPA_SHIFT_CC
                || posAlphabetSecondChar < SEPA_SHIFT_CC) {
            return false;
        }

        trimmedIban = trimmedIban.substring(SEPA_CC_CHECKSUM_LENGTH) + posAlphabetFirstChar
                + posAlphabetSecondChar + trimmedIban.substring(SEPA_CC_LENGTH, SEPA_CC_CHECKSUM_LENGTH);
        return new BigInteger(trimmedIban).mod(BigInteger.valueOf(IBAN_CHECKSUM_MODULO))
                .equals(BigInteger.ONE);
    }

    /**
     * Checks whether the given BIC is valid. Currently only the length and the allowed characters are checked.
     *
     * @param bic The BIC to check.
     * @return {@code false} only if the BIC is invalid.
     * @see #BIC_PATTERN
     */
    public static boolean isValidBic(String bic) {
        return BIC_PATTERN.matcher(bic)
                .matches();
    }

    /**
     * Checks whether the given creditor id is valid.
     *
     * @param creditorId The creditor id to check.
     * @return {@code true} only if the given creditor id is valid.
     */
    public static boolean isValidCreditorId(String creditorId) {
        String trimmedCreditorId = creditorId.replaceAll(" ", "");
        return trimmedCreditorId.contains(SEPA_BUSINESS_CODE)
                && isValidIban(trimmedCreditorId.replace(SEPA_BUSINESS_CODE, ""));
    }

    /**
     * Checks whether the given message id would be valid for being used in a Sepa Direct Debit.
     *
     * @param messageId The message id to check.
     * @return {@code true} only if the message id is valid.
     */
    public static boolean isValidMessageId(String messageId) {
        return MESSAGE_ID_PATTERN.matcher(messageId).matches() && messageId.length() <= MAX_CHAR_MESSAGE_ID;
    }

    /**
     * Returns a {@link String} representation of the given {@code dateTime} which is valid for SEPA Direct Debits.
     *
     * @param dateTime The date to convert.
     * @return The valid representation of the given {@code dateTime}.
     */
    public static String getSepaDate(LocalDateTime dateTime) {
        /*
         * The DateTimeFormatter could accept any subtype of TemporalAccessor but a Sepa Direct Debit only accepts
         * DateTime values and TemporalAccessor does not support all fields needed.
         */
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
    }

    /**
     * Returns a {@link String} representation of the given {@code date} which is valid for SEPA Direct Debits.
     *
     * @param date The date to convert.
     * @return The valid representation of the given {@code date}.
     */
    public static String getSepaDate(LocalDate date) {
        /*
         * The DateTimeFormatter could accept any subtype of TemporalAccessor but a Sepa Direct Debit only accepts
         * DateTime values and TemporalAccessor does not support all fields needed.
         */
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    /**
     * Checks whether the given {@link String} contains valid SEPA DD content.
     *
     * @param xml The XML content to check.
     * @return An {@link Optional} containing the error output if {@code xml} is erroneous. If {@link Optional#empty()}
     * is returned the XML does not contain errors but it may still contain warnings. If so these are logged.
     * @throws SAXException If any parse error occurs.
     * @throws IOException If any I/O error occurs.
     */
    public static Optional<String> validateSepaXML(String xml) throws SAXException, IOException {
        //Validate against xsd schema
        DocumentBuilderFactory xmlBuilderFactory = DocumentBuilderFactory.newInstance();
        xmlBuilderFactory.setIgnoringComments(true);
        xmlBuilderFactory.setNamespaceAware(true);
        xmlBuilderFactory.setValidating(false);
        DocumentBuilder xmlBuilder;
        try {
            xmlBuilder = xmlBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new Error("The DocumentBuilder used for SEPA xml validation is invalid.", ex);
        }
        Map<String, List<String>> validationProblemsMap = new HashMap<String, List<String>>() {
            @Override
            public List<String> get(Object key) {
                if (key instanceof String) {
                    String keyString = (String) key;
                    if (!containsKey(keyString)) {
                        super.put(keyString, new ArrayList<>());
                    }
                }
                return super.get(key);
            }
        };
        BooleanProperty isValid = new SimpleBooleanProperty(true);
        xmlBuilder.setErrorHandler(new ErrorHandler() {
            private String createLine(SAXParseException ex) {
                return "line: " + ex.getLineNumber() + ": " + ex.getMessage();
            }

            @Override
            public void warning(SAXParseException exception) throws SAXException {
                validationProblemsMap.get("warning").add(createLine(exception));
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                validationProblemsMap.get("error").add(createLine(exception));
                isValid.set(false);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                validationProblemsMap.get("fatalError").add(createLine(exception));
                isValid.set(false);
            }
        });
        Document xmlDocument = xmlBuilder.parse(new InputSource(new StringReader(xml)));
        try {
            SEPA_VALIDATOR.validate(new DOMSource(xmlDocument.getFirstChild()));
        } catch (SAXException ex) {
            /*
             * NOTE: When a fatal error occurs some implementations may or may not continue evaluation.
             * (See {@link ErrorHandler#fatalError(SAXParseException)})
             */
            validationProblemsMap.get("fatalError (discontinue)").add(ex.getMessage());
            isValid.set(false);
        }

        String validationOutput = validationProblemsMap.entrySet()
                .stream()
                .sorted((entryA, entryB) -> entryA.getKey().compareTo(entryB.getKey()))
                .flatMap(entry -> entry.getValue().stream().map(cause -> entry.getKey() + ": " + cause))
                .collect(Collectors.joining("\n"));
        if (isValid.get()) {
            if (!validationOutput.isEmpty()) {
                Logger.getLogger(SepaUtility.class.getName()).log(Level.WARNING, validationOutput);
            }
            return Optional.empty();
        } else {
            return Optional.of(validationOutput);
        }
    }
}

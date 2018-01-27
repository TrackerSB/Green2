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
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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

    public static boolean validateSepaXML(String xml) throws SAXException, IOException {
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
        Map<String, List<String>> validationProblemsMap = new HashMap<>(3);
        xmlBuilder.setErrorHandler(new ErrorHandler() {
            private final BiConsumer<String, SAXParseException> logValidationProblem = (type, exception) -> {
                validationProblemsMap.putIfAbsent(type, new ArrayList<>());
                validationProblemsMap.get(type)
                        .add("line: " + exception.getLineNumber() + ": " + exception.getMessage());
            };

            @Override
            public void warning(SAXParseException exception) throws SAXException {
                logValidationProblem.accept("warning", exception);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                logValidationProblem.accept("error", exception);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                logValidationProblem.accept("fatalError", exception);
            }
        });
        Document xmlDocument = xmlBuilder.parse(new InputSource(new StringReader(xml)));
        SEPA_VALIDATOR.validate(new DOMSource(xmlDocument.getFirstChild()));

        //Check lengths of names
        NodeList nameNodes = xmlDocument.getElementsByTagName("Nm");
        for (int i = 0; i < nameNodes.getLength(); i++) {
            String itemContent = nameNodes.item(i).getTextContent();
            if (itemContent.length() > MAX_CHAR_NAME_FIELD) {
                validationProblemsMap.putIfAbsent("warning", new ArrayList<>());
                validationProblemsMap.get("warning").add(itemContent + " is longer than " + MAX_CHAR_NAME_FIELD);
            }
        }

        boolean isValid = (!validationProblemsMap.containsKey("error") || validationProblemsMap.get("error").isEmpty())
                && (!validationProblemsMap.containsKey("fatalError")
                || validationProblemsMap.get("fatalError").isEmpty());
        if (!validationProblemsMap.isEmpty()) {
            Logger.getLogger(SepaUtility.class.getName())
                    .log(Level.WARNING, validationProblemsMap.entrySet()
                            .stream()
                            .sorted((entryA, entryB) -> entryA.getKey().compareTo(entryB.getKey()))
                            .flatMap(entry -> entry.getValue().stream().map(cause -> entry.getKey() + ": " + cause))
                            .collect(Collectors.joining("\n")));
        }
        return isValid;
    }
}

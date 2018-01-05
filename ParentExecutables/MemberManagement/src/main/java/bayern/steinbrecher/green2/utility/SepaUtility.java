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

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

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
    public static final int MAX_CHAR_NAME_OF_CREATOR = 70;

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
     * Returns the given date as {@link String} which is valid for usage in Sepa Direct Debits.
     *
     * @param date The date to convert.
     * @return The valid representation of the given date.
     */
    public static String getSepaDate(LocalDateTime date) {
        /*
         * The DateTimeFormatter could accept any subtype of TemporalAccessor but a Sepa Direct Debit only accepts
         * DateTime values.
         */
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date);
    }
}

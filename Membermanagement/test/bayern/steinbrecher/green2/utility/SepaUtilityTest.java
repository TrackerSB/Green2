/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bayern.steinbrecher.green2.utility;

import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Stefan Huber
 */
public class SepaUtilityTest {

    private static final String VALID_IBAN = "DE02100500000024290661";
    private static final String INVALID_IBAN = "DE021005000000w24290661";
    private static final String VALID_CREDITORID = "DE98ZZZ09999999999";
    private static final String VALID_MESSAGEID = "2017-02-02 Membercontributions";
    private static final Date SEPA_TEST_DATE = new Date(2017, 2, 20, 12, 0, 0);
    private static final String SEPA_TEST_DATE_STRING = "2017-02-20T12:00:00";

    public SepaUtilityTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isValidIban method, of class SepaUtility.
     */
    @Test
    public void testIsValidIban() {
        assertTrue(SepaUtility.isValidIban(VALID_IBAN));
    }

    /**
     * Test of isValidIban method, of class SepaUtility.
     */
    @Test
    public void testIsValidIban_unallowedCharacter() {
        assertFalse(SepaUtility.isValidIban(INVALID_IBAN));
    }

    /**
     * Test of isValidCreditorId method, of class SepaUtility.
     */
    @Test
    public void testIsValidCreditorId() {
        assertTrue(SepaUtility.isValidCreditorId(VALID_CREDITORID));
    }

    /**
     * Test of isValidCreditorId method, of class SepaUtility.
     */
    @Test
    public void testIsValidCreditorId_fail() {
        assertFalse(SepaUtility.isValidCreditorId(VALID_IBAN));
    }

    /**
     * Test of isValidMessageId method, of class SepaUtility.
     */
    @Test
    public void testIsValidMessageId() {
        assertTrue(SepaUtility.isValidMessageId(VALID_MESSAGEID));
    }

    /**
     * Test of getSepaDate method, of class SepaUtility.
     */
    @Test
    public void testGetSepaDate() {
        assertEquals(SEPA_TEST_DATE_STRING, SepaUtility.getSepaDate(SEPA_TEST_DATE));
    }
}

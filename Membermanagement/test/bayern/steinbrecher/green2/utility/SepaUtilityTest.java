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
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Stefan Huber
 */
public class SepaUtilityTest {
    
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
        System.out.println("isValidIban");
        String iban = "";
        boolean expResult = false;
        boolean result = SepaUtility.isValidIban(iban);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValidCreditorId method, of class SepaUtility.
     */
    @Test
    public void testIsValidCreditorId() {
        System.out.println("isValidCreditorId");
        String creditorId = "";
        boolean expResult = false;
        boolean result = SepaUtility.isValidCreditorId(creditorId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValidMessageId method, of class SepaUtility.
     */
    @Test
    public void testIsValidMessageId() {
        System.out.println("isValidMessageId");
        String messageId = "";
        boolean expResult = false;
        boolean result = SepaUtility.isValidMessageId(messageId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSepaDate method, of class SepaUtility.
     */
    @Test
    public void testGetSepaDate() {
        System.out.println("getSepaDate");
        Date date = null;
        String expResult = "";
        String result = SepaUtility.getSepaDate(date);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

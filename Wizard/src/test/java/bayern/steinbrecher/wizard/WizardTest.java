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
package bayern.steinbrecher.wizard;

import java.util.Map;
import java.util.Optional;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Stefan Huber
 */
@Ignore
public class WizardTest {

    public WizardTest() {
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
     * Test of init method, of class Wizard.
     */
    @Test
    public void testInit() throws Exception {
        System.out.println("init");
        Wizard instance = new Wizard();
        instance.init();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showPrevious method, of class Wizard.
     */
    @Test
    public void testShowPrevious() {
        System.out.println("showPrevious");
        Wizard instance = new Wizard();
        instance.showPrevious();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showNext method, of class Wizard.
     */
    @Test
    public void testShowNext() {
        System.out.println("showNext");
        Wizard instance = new Wizard();
        instance.showNext();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of finish method, of class Wizard.
     */
    @Test
    public void testFinish() {
        System.out.println("finish");
        Wizard instance = new Wizard();
        instance.finish();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of finishedProperty method, of class Wizard.
     */
    @Test
    public void testFinishedProperty() {
        System.out.println("finishedProperty");
        Wizard instance = new Wizard();
        ReadOnlyBooleanProperty expResult = null;
        ReadOnlyBooleanProperty result = instance.finishedProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isFinished method, of class Wizard.
     */
    @Test
    public void testIsFinished() {
        System.out.println("isFinished");
        Wizard instance = new Wizard();
        boolean expResult = false;
        boolean result = instance.isFinished();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getResults method, of class Wizard.
     */
    @Test
    public void testGetResults() {
        System.out.println("getResults");
        Wizard instance = new Wizard();
        Optional<Map<String, ?>> expResult = null;
        Optional<Map<String, ?>> result = instance.getResults();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of atBeginningProperty method, of class Wizard.
     */
    @Test
    public void testAtBeginningProperty() {
        System.out.println("atBeginningProperty");
        Wizard instance = new Wizard();
        ReadOnlyBooleanProperty expResult = null;
        ReadOnlyBooleanProperty result = instance.atBeginningProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAtBeginning method, of class Wizard.
     */
    @Test
    public void testIsAtBeginning() {
        System.out.println("isAtBeginning");
        Wizard instance = new Wizard();
        boolean expResult = false;
        boolean result = instance.isAtBeginning();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of atFinishProperty method, of class Wizard.
     */
    @Test
    public void testAtFinishProperty() {
        System.out.println("atFinishProperty");
        Wizard instance = new Wizard();
        ReadOnlyBooleanProperty expResult = null;
        ReadOnlyBooleanProperty result = instance.atFinishProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAtFinish method, of class Wizard.
     */
    @Test
    public void testIsAtFinish() {
        System.out.println("isAtFinish");
        Wizard instance = new Wizard();
        boolean expResult = false;
        boolean result = instance.isAtFinish();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of currentPageProperty method, of class Wizard.
     */
    @Test
    public void testCurrentPageProperty() {
        System.out.println("currentPageProperty");
        Wizard instance = new Wizard();
        ReadOnlyProperty<WizardPage<?>> expResult = null;
        ReadOnlyProperty<WizardPage<?>> result = instance.currentPageProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentPage method, of class Wizard.
     */
    @Test
    public void testGetCurrentPage() {
        System.out.println("getCurrentPage");
        Wizard instance = new Wizard();
        WizardPage expResult = null;
        WizardPage result = instance.getCurrentPage();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}

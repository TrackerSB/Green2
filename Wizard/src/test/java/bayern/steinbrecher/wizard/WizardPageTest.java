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

import java.util.concurrent.Callable;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
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
public class WizardPageTest {
    
    public WizardPageTest() {
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
     * Test of getRoot method, of class WizardPage.
     */
    @Test
    public void testGetRoot() {
        System.out.println("getRoot");
        WizardPage instance = new WizardPage();
        Pane expResult = null;
        Pane result = instance.getRoot();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRoot method, of class WizardPage.
     */
    @Test
    public void testSetRoot() {
        System.out.println("setRoot");
        Pane root = null;
        WizardPage instance = new WizardPage();
        instance.setRoot(root);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextFunctionProperty method, of class WizardPage.
     */
    @Test
    public void testNextFunctionProperty() {
        System.out.println("nextFunctionProperty");
        WizardPage instance = new WizardPage();
        Property<Callable<String>> expResult = null;
        Property<Callable<String>> result = instance.nextFunctionProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNextFunction method, of class WizardPage.
     */
    @Test
    public void testGetNextFunction() {
        System.out.println("getNextFunction");
        WizardPage instance = new WizardPage();
        Callable<String> expResult = null;
        Callable<String> result = instance.getNextFunction();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setNextFunction method, of class WizardPage.
     */
    @Test
    public void testSetNextFunction() {
        System.out.println("setNextFunction");
        Callable<String> nextFunction = null;
        WizardPage instance = new WizardPage();
        instance.setNextFunction(nextFunction);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isFinish method, of class WizardPage.
     */
    @Test
    public void testIsFinish() {
        System.out.println("isFinish");
        WizardPage instance = new WizardPage();
        boolean expResult = false;
        boolean result = instance.isFinish();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFinish method, of class WizardPage.
     */
    @Test
    public void testSetFinish() {
        System.out.println("setFinish");
        boolean finish = false;
        WizardPage instance = new WizardPage();
        instance.setFinish(finish);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getResultFunction method, of class WizardPage.
     */
    @Test
    public void testGetResultFunction() {
        System.out.println("getResultFunction");
        WizardPage instance = new WizardPage();
        Callable expResult = null;
        Callable result = instance.getResultFunction();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setResultFunction method, of class WizardPage.
     */
    @Test
    public void testSetResultFunction() {
        System.out.println("setResultFunction");
        WizardPage instance = new WizardPage();
        instance.setResultFunction(null);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValidBinding method, of class WizardPage.
     */
    @Test
    public void testSetValidBinding() {
        System.out.println("setValidBinding");
        ObservableValue<? extends Boolean> valid = null;
        WizardPage instance = new WizardPage();
        instance.setValidBinding(valid);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of validProperty method, of class WizardPage.
     */
    @Test
    public void testValidProperty() {
        System.out.println("validProperty");
        WizardPage instance = new WizardPage();
        ReadOnlyBooleanProperty expResult = null;
        ReadOnlyBooleanProperty result = instance.validProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValid method, of class WizardPage.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid");
        WizardPage instance = new WizardPage();
        boolean expResult = false;
        boolean result = instance.isValid();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasNextFunctionProperty method, of class WizardPage.
     */
    @Test
    public void testHasNextFunctionProperty() {
        System.out.println("hasNextFunctionProperty");
        WizardPage instance = new WizardPage();
        ReadOnlyBooleanProperty expResult = null;
        ReadOnlyBooleanProperty result = instance.hasNextFunctionProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isHasNextFunction method, of class WizardPage.
     */
    @Test
    public void testIsHasNextFunction() {
        System.out.println("isHasNextFunction");
        WizardPage instance = new WizardPage();
        boolean expResult = false;
        boolean result = instance.isHasNextFunction();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

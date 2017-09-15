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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.Property;
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
public class WizardControllerTest {

    public WizardControllerTest() {
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
     * Test of initialize method, of class WizardController.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        URL location = null;
        ResourceBundle resources = null;
        WizardController instance = new WizardController();
        instance.initialize(location, resources);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContent method, of class WizardController.
     */
    @Test
    public void testSetContent() {
        System.out.println("setContent");
        Pane pane = null;
        WizardController instance = new WizardController();
        instance.setContent(pane);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContentSize method, of class WizardController.
     */
    @Test
    public void testSetContentSize() {
        System.out.println("setContentSize");
        double width = 0.0;
        double height = 0.0;
        WizardController instance = new WizardController();
        instance.setContentSize(width, height);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of callerProperty method, of class WizardController.
     */
    @Test
    public void testCallerProperty() {
        System.out.println("callerProperty");
        WizardController instance = new WizardController();
        Property<Wizard> expResult = null;
        Property<Wizard> result = instance.callerProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCaller method, of class WizardController.
     */
    @Test
    public void testGetCaller() {
        System.out.println("getCaller");
        WizardController instance = new WizardController();
        Wizard expResult = null;
        Wizard result = instance.getCaller();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCaller method, of class WizardController.
     */
    @Test
    public void testSetCaller() {
        System.out.println("setCaller");
        Wizard caller = null;
        WizardController instance = new WizardController();
        instance.setCaller(caller);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}

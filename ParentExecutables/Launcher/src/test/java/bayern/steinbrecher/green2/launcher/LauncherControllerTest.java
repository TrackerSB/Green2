/*
 * Copyright (C) 2017 Green2
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
package bayern.steinbrecher.green2.launcher;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
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
public class LauncherControllerTest {

    public LauncherControllerTest() {
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
     * Test of initialize method, of class LauncherController.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        URL url = null;
        ResourceBundle rb = null;
        LauncherController instance = new LauncherController();
        instance.initialize(url, rb);
        fail("The test case is a prototype.");
    }

    /**
     * Test of percentageProperty method, of class LauncherController.
     */
    @Test
    public void testPercentageProperty() {
        System.out.println("percentageProperty");
        LauncherController instance = new LauncherController();
        ReadOnlyDoubleProperty expResult = null;
        ReadOnlyDoubleProperty result = instance.percentageProperty();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPercentage method, of class LauncherController.
     */
    @Test
    public void testGetPercentage() {
        System.out.println("getPercentage");
        LauncherController instance = new LauncherController();
        double expResult = 0.0;
        double result = instance.getPercentage();
        assertEquals(expResult, result, 0.0);
        fail("The test case is a prototype.");
    }

    /**
     * Test of incPercentage method, of class LauncherController.
     */
    @Test
    public void testIncPercentage() {
        System.out.println("incPercentage");
        int steps = 0;
        LauncherController instance = new LauncherController();
        instance.incPercentage(steps);
        fail("The test case is a prototype.");
    }

    /**
     * Test of percentageStringProperty method, of class LauncherController.
     */
    @Test
    public void testPercentageStringProperty() {
        System.out.println("percentageStringProperty");
        LauncherController instance = new LauncherController();
        ReadOnlyStringProperty expResult = null;
        ReadOnlyStringProperty result = instance.percentageStringProperty();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPercentageString method, of class LauncherController.
     */
    @Test
    public void testGetPercentageString() {
        System.out.println("getPercentageString");
        LauncherController instance = new LauncherController();
        String expResult = "";
        String result = instance.getPercentageString();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

}

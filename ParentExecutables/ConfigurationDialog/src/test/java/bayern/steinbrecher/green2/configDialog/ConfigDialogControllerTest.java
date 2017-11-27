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
package bayern.steinbrecher.green2.configDialog;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanProperty;
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
public class ConfigDialogControllerTest {

    public ConfigDialogControllerTest() {
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
     * Test of initialize method, of class ConfigDialogController.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        URL url = null;
        ResourceBundle rb = null;
        ConfigDialogController instance = new ConfigDialogController();
        instance.initialize(url, rb);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of profileAlreadyExistsProperty method, of class ConfigDialogController.
     */
    @Test
    public void testProfileAlreadyExistsProperty() {
        System.out.println("profileAlreadyExistsProperty");
        ConfigDialogController instance = new ConfigDialogController();
        ReadOnlyBooleanProperty expResult = null;
        ReadOnlyBooleanProperty result = instance.profileAlreadyExistsProperty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isProfileAlreadyExists method, of class ConfigDialogController.
     */
    @Test
    public void testIsProfileAlreadyExists() {
        System.out.println("isProfileAlreadyExists");
        ConfigDialogController instance = new ConfigDialogController();
        boolean expResult = false;
        boolean result = instance.isProfileAlreadyExists();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}

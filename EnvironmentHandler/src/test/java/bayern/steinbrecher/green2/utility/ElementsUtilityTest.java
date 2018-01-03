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
package bayern.steinbrecher.green2.utility;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Parent;
import org.junit.After;
import org.junit.AfterClass;
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
public class ElementsUtilityTest {

    public ElementsUtilityTest() {
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
     * Test of addCssClassIf method, of class ElementsUtility.
     */
    @Test
    public void testAddCssClassIf() {
        System.out.println("addCssClassIf");
        Parent parent = null;
        ObservableBooleanValue observable = null;
        String cssClass = "";
        ElementsUtility.addCssClassIf(parent, observable, cssClass);
        fail("The test case is a prototype.");
    }

}

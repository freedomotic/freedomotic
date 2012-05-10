/*Copyright 2009 Enrico Nicoletti
 * eMail: enrico.nicoletti84@gmail.com
 *
 * This file is part of Freedom.
 *
 * Freedom is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Freedom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedom; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package it.freedom.X10.gateways;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author enrico
 */
public class PMix35GatewayTest {

    public PMix35GatewayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of composePMix35Message method, of class PMix35Gateway.
     */
    @Test
    public void testComposePMix35Message() {
        System.out.println("composePMix35Message");
        assertEquals("$>9000RQCE#", PMix35Gateway.composeMessage("RQ"));
        assertEquals("$>9000LW A01A01 AONAON0E#", PMix35Gateway.composeMessage("LW A01A01 AONAON"));
        assertEquals("$>9000PXD3#", PMix35Gateway.composeMessage("PX"));

    }

}
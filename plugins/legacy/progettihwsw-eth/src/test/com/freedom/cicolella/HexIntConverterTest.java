/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.cicolella;

import it.cicolella.phwsw.HexIntConverter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Enrico
 */
public class HexIntConverterTest {

    public HexIntConverterTest() {
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
     * Test of HexIntConverter method, of class HexIntConverter.
     */
    @Test
    public void testHexIntConverter_int() {
        String result = HexIntConverter.convert(42);
        assertEquals("2a", result);
        String result1 = HexIntConverter.convert(0);
        assertEquals("0", result1);
        String result2 = HexIntConverter.convert(16);
        assertEquals("10", result2);
        String result3 = HexIntConverter.convert(7);
        assertEquals("7", result3);
        String result4 = HexIntConverter.convert(11);
        assertEquals("b", result4);
    }

    /**
     * Test of HexIntConverter method, of class HexIntConverter.
     */
    @Test
    public void testHexIntConverter_String() {
        int result = HexIntConverter.convert("2A");
        assertEquals(42, result);
        int result2 = HexIntConverter.convert("2a");
        assertEquals(42, result2);
    }
}

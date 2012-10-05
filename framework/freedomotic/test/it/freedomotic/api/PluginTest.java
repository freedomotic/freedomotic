/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.api;

import it.freedomotic.model.ds.Config;
import java.io.File;
import javax.swing.JFrame;
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
public class PluginTest {
    
    public PluginTest() {
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
     * Test of getMostRecentVersion method, of class Plugin.
     */
    @Test
    public void testGetMostRecentVersion() {
        System.out.println("getOldestVersion");
        assertEquals(0, Plugin.getOldestVersion("5.3.0", "5.3.0"));
        assertEquals(-1, Plugin.getOldestVersion("5.3.0", "5.4.0"));
        assertEquals(1, Plugin.getOldestVersion("5.4.0", "5.3.0"));
        assertEquals(1, Plugin.getOldestVersion("5.4.0", "5.3.999"));
        assertEquals(-1, Plugin.getOldestVersion("5.4.0", "5.99.999"));
        assertEquals(1, Plugin.getOldestVersion("6.0.0", "5.3.999"));
        assertEquals(1, Plugin.getOldestVersion("6.1.0", "5.3.999"));
    }
}

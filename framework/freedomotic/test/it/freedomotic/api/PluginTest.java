/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.api;

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
    public void testCompareVersions() {
        System.out.println("compareVersions");
        assertEquals(Plugin.SAME_VERSION, Plugin.getOldestVersion("5.1.0", "5.1.0"));
        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "5.2.0"));
        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "6.1.0"));
        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "5.1.1"));
        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "5.1.x"));
        assertEquals(Plugin.SAME_VERSION, Plugin.getOldestVersion("5.1.x", "5.1.x"));
        assertEquals(Plugin.LAST_IS_OLDER, Plugin.getOldestVersion("5.2.x", "5.1.x"));
        assertEquals(Plugin.LAST_IS_OLDER, Plugin.getOldestVersion("5.2.x", "5.1.0"));
        assertEquals(Plugin.LAST_IS_OLDER, Plugin.getOldestVersion("5.2.1", "5.2.0"));
    }
}

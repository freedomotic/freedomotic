/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import junit.framework.Assert;
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
public class VersionTest {
    
    public VersionTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
        Version version = new Version("it.surname.plugin-5.3.x-1.10.device");
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
     * Test of extractVersion method, of class Version.
     */
    @Test
    public void testExtractVersion() {
        System.out.println("extractVersion");
        String filename = "it.surname.plugin-5.3.x-1.10.device";
        String expResult = "5.3.x-1.10";
        String result = Version.extractVersion(filename);
        assertEquals(expResult, result);
    }

    /**
     * Test of compareVersions method, of class Version.
     */
     /*
    @Test
    public void testCompareVersions() {
        System.out.println("compareVersions");
        String name = "";
        String version = "";
        int expResult = 0;
        int result = Version.compareVersions(name, version);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}

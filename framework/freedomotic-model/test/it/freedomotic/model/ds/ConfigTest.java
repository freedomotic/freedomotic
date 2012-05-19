/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.model.ds;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author Enrico
 */
public class ConfigTest {
    private static Config config;
    
    public ConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        config = new Config();
        config.setProperty("boolean-property-true", "true");
        config.setProperty("boolean-property-false", "false");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetXmlFile() {
    }

    @Test
    public void testSetXmlFile_File() {
    }

    @Test
    public void testSetXmlFile_String() {
    }

    @Test
    public void testSetProperty() {
    }

    @Test
    public void testGetProperty() {
    }

    @Test
    public void testGetTuples() {
    }

    @Test
    public void testEntrySet() {
    }

    @Test
    public void testPut() {
    }

    @Test
    public void testGetStringProperty() {
    }

    @Test
    public void testGetIntProperty() {
    }

    @Test
    public void testGetBooleanProperty() {
        Assert.assertTrue("This property in Config must be true", config.getBooleanProperty("boolean-property-true", false));
        Assert.assertFalse("This property in Config must be false", config.getBooleanProperty("boolean-property-false", true));
    }

    @Test
    public void testGetDoubleProperty() {
    }

    @Test
    public void testGetUrlListProperty() {
    }

    @Test
    public void testGetPathListProperty() {
    }

    @Test
    public void testGetProperties() {
    }

    @Test
    public void testToString() {
    }
}

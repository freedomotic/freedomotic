/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.model.ds;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Enrico Nicoletti
 */
public class ConfigTest {

    private static Config config;

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        config = new Config();
        config.setProperty("boolean-property-true", "true");
        config.setProperty("boolean-property-false", "false");
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetXmlFile() {
    	config.setXmlFile("test.xml");
    	assertTrue(config.getXmlFile().equals("test.xml"));
    }

    @Test
    public void testSetXmlFile_File() throws IOException {
    	Path path = Files.createTempFile("freedomotic", "temp");
    	config.setXmlFile(path.toFile());
    	assertTrue(config.getXmlFile() !=null);
    }

    @Test
    public void testSetXmlFile_String() {
    	config.setXmlFile("test.xml");
    	assertTrue(config.getXmlFile().equals("test.xml"));
    }

    @Test
    public void testGetTuples() {
    	assertTrue(config.getTuples()!=null);
    }

    @Test
    public void testEntrySet() {
    	assertTrue(config.entrySet()!=null);
    }

    @Test
    public void testPut() {
    	config.put("test", "000");
    	assertTrue(config.getStringProperty("test", null)!=null);
    }

    @Test
    public void testGetStringProperty() {
    	assertTrue(config.getStringProperty("boolean-property-true", null)!=null);
    }

    @Test
    public void testGetIntProperty() {
    	config.put("testInt", "1");
    	assertTrue(config.getIntProperty("testInt",0) == 1);
    }

    /**
     *
     */
    @Test
    public void testGetBooleanProperty() {
        assertTrue("This property in Config must be true", config.getBooleanProperty("boolean-property-true", false));
        assertFalse("This property in Config must be false", config.getBooleanProperty("boolean-property-false", true));
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

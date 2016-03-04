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

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico Nicoletti
 */
public class ConfigTest {

    private static Config config;

    /**
     *
     */
    public ConfigTest() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        config = new Config();
        config.setProperty("boolean-property-true", "true");
        config.setProperty("boolean-property-false", "false");
    }

    /**
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     *
     */
    @Test
    public void testGetXmlFile() {
    }

    /**
     *
     */
    @Test
    public void testSetXmlFile_File() {
    }

    /**
     *
     */
    @Test
    public void testSetXmlFile_String() {
    }

    /**
     *
     */
    @Test
    public void testSetProperty() {
    }

    /**
     *
     */
    @Test
    public void testGetProperty() {
    }

    /**
     *
     */
    @Test
    public void testGetTuples() {
    }

    /**
     *
     */
    @Test
    public void testEntrySet() {
    }

    /**
     *
     */
    @Test
    public void testPut() {
    }

    /**
     *
     */
    @Test
    public void testGetStringProperty() {
    }

    /**
     *
     */
    @Test
    public void testGetIntProperty() {
    }

    /**
     *
     */
    @Test
    public void testGetBooleanProperty() {
        Assert.assertTrue("This property in Config must be true", config.getBooleanProperty("boolean-property-true", false));
        Assert.assertFalse("This property in Config must be false", config.getBooleanProperty("boolean-property-false", true));
    }

    /**
     *
     */
    @Test
    public void testGetDoubleProperty() {
    }

    /**
     *
     */
    @Test
    public void testGetUrlListProperty() {
    }

    /**
     *
     */
    @Test
    public void testGetPathListProperty() {
    }

    /**
     *
     */
    @Test
    public void testGetProperties() {
    }

    /**
     *
     */
    @Test
    public void testToString() {
    }
}

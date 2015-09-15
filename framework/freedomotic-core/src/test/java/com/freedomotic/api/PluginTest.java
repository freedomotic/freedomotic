/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author enrico
 */
public class PluginTest {

    /**
     *
     */
    public PluginTest() {
    }

    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of getMostRecentVersion method, of class Plugin.
     */
    @Test
    public void testCompareVersions() {
        System.out.println("compareVersions");

//        assertEquals(Plugin.SAME_VERSION, Plugin.getOldestVersion("5.1.0", "5.1.0"));
//        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "5.2.0"));
//        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "6.1.0"));
//        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "5.1.1"));
//        assertEquals(Plugin.FIRST_IS_OLDER, Plugin.getOldestVersion("5.1.0", "5.1.x"));
//        assertEquals(Plugin.SAME_VERSION, Plugin.getOldestVersion("5.1.x", "5.1.x"));
//        assertEquals(Plugin.LAST_IS_OLDER, Plugin.getOldestVersion("5.2.x", "5.1.x"));
//        assertEquals(Plugin.LAST_IS_OLDER, Plugin.getOldestVersion("5.2.x", "5.1.0"));
//        assertEquals(Plugin.LAST_IS_OLDER, Plugin.getOldestVersion("5.2.1", "5.2.0"));
    }
    private static final Logger LOG = Logger.getLogger(PluginTest.class.getName());
}

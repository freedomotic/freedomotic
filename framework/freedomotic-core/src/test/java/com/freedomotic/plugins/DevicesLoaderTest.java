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
package com.freedomotic.plugins;

import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico
 */
public class DevicesLoaderTest {
//    private static AddonManager loader;
//    private static File folder;

    /**
     *
     */
    
    public DevicesLoaderTest() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
//        loader = new AddonManager();
//        folder = new File(Info.getPluginsPath() + "/devices/");
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
    public void testLoad() {
        //TODO: always fail because Freedomotic instance is not loaded (is null)
//         loader.recursiveSearchIn(folder);
//         Assert.assertTrue("At least one plugin is loaded succesfully", loader.ADDONS.size() > 0);
    }
    private static final Logger LOG = Logger.getLogger(DevicesLoaderTest.class.getName());
}

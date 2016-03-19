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
package com.freedomotic.plugins;

import com.freedomotic.api.Client;
import com.freedomotic.testutils.FreedomoticTestsInjector;
import com.freedomotic.testutils.GuiceJUnitRunner;
import com.freedomotic.testutils.GuiceJUnitRunner.GuiceInjectors;
import com.google.inject.Inject;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Enrico Nicoletti
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceInjectors({FreedomoticTestsInjector.class})
public class PluginLoaderFilesystemITCase {

    //saves the loaded plugins in memory
    @Inject
    ClientStorage storage;
    //loads plugin from local filesystem
    @Inject
    PluginsManager loader;

    private static File boundlePath;

    /**
     *
     */
    public PluginLoaderFilesystemITCase() {
    }

    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
        String boundlePathString = System.getProperty("boundlePath");
        PluginLoaderFilesystemITCase.boundlePath = new File(boundlePathString);
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
     * Test of addSingleBoundle method, of class PluginsManager.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLoadPlugins() throws Exception {
        System.out.println("Load plugins");

        loader.loadSingleBoundle(boundlePath);
        System.out.println("Loaded " + storage.getClients().size() + " plugins");
        for (Client client : storage.getClients()) {
            System.out.println("Starting " + client.getName());
            client.start();
        }
    }
    /**
     * Test of installBoundle method, of class PluginsManager.
     */
//    @Test
//    public void testInstallPlugin() {
//        System.out.println("installBoundle");
//        URL fromURL = null;
//        boolean expResult = false;
//        boolean result = loader.installBoundle(fromURL);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}

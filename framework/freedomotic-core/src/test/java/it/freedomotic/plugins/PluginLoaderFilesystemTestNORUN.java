/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.testutils.TestsInjector;
import com.google.inject.Inject;
import it.freedomotic.bus.AbstractBusConnector;
import it.freedomotic.exceptions.PluginLoadingException;
import it.freedomotic.plugins.filesystem.PluginLoaderFilesystem;
import it.freedomotic.testutils.GuiceJUnitRunner;
import it.freedomotic.testutils.GuiceJUnitRunner.GuiceInjectors;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author enrico
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceInjectors({TestsInjector.class})
public class PluginLoaderFilesystemTestNORUN {

    //saves the loaded plugins in memory
    @Inject ClientStorage storage;
    //loads plugin from local filesystem
    @Inject PluginLoaderFilesystem loader;
    //creates a messaging bus on which publish the events generated while loading plugins
    @Inject AbstractBusConnector bus;

    public PluginLoaderFilesystemTestNORUN() {
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
     * Test of loadPlugin method, of class PluginLoaderFilesystem.
     */
    @Test
    public void testLoadPlugins() throws Exception {
        System.out.println("Load Devices Plugins");

        int DEVICES = 1;

        try {
            loader.loadPlugins(DEVICES);
            System.out.println("Loaded " + storage.getClients().size() + " plugins");
        } catch (PluginLoadingException ex) {
            fail("Cannot load plugin " + ex.getPluginName() + " due to " + ex.getCause());
        }
    }
    /**
     * Test of installPlugin method, of class PluginLoaderFilesystem.
     */
//    @Test
//    public void testInstallPlugin() {
//        System.out.println("installPlugin");
//        URL fromURL = null;
//        boolean expResult = false;
//        boolean result = loader.installPlugin(fromURL);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}

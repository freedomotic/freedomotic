/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.testutils.FreedomoticTestsInjector;
import com.google.inject.Inject;
import it.freedomotic.api.Client;
import it.freedomotic.bus.AbstractBusConnector;
import it.freedomotic.plugins.filesystem.PluginsManager;
import it.freedomotic.testutils.GuiceJUnitRunner;
import it.freedomotic.testutils.GuiceJUnitRunner.GuiceInjectors;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author enrico
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
    //creates a messaging bus on which publish the events generated while loading plugins
    @Inject
    AbstractBusConnector bus;
    private static File boundlePath;

    public PluginLoaderFilesystemITCase() {
    }

    @BeforeClass
    public static void setUpClass() {
        String boundlePathString = System.getProperty("boundlePath");
        PluginLoaderFilesystemITCase.boundlePath = new File(boundlePathString);
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
     * Test of loadSingleBoundle method, of class PluginsManager.
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

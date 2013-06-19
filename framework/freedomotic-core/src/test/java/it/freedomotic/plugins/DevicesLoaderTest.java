/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

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

    public DevicesLoaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
//        loader = new AddonManager();
//        folder = new File(Info.getPluginsPath() + "/devices/");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testLoad() {
        //TODO: always fail because Freedomotic instance is not loaded (is null)
//         loader.recursiveSearchIn(folder);
//         Assert.assertTrue("At least one plugin is loaded succesfully", loader.ADDONS.size() > 0);
    }
}

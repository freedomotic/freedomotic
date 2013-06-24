/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Client;
import it.freedomotic.api.Plugin;

import java.util.Properties;

/**
 *
 * @author enrico
 */
public class Version {

    private int requiredMajor;
    private int requiredMior;
    private int requiredBuild;
    private int versionMajor;
    private int versionMinor;
    private int versionBuild;

    public Version(int major, int minor, int build) {
        versionMajor= major;
        versionMinor=minor;
        versionBuild=build;
    }

    public Version(String version) {
        String[] components = version.split("-");
        if (components.length == 2) { //required + version
//             String[] required = components[0].split(".");
//            requiredMajor = Integer.parseInt(required[0], "999999"));
//            requiredMinor = required[1];
//            requiredBUild = required[2];
        } else {
            if (components.length == 1) {//only version
            }
        }
    }

    public static String extractVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        filename = filename.substring(0, filename.lastIndexOf("."));
        String[] tokens = filename.split("-");
        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1] + "-" + tokens[2];
        } else {
            return filename;
        }
    }

    private static int getVersionProperty(Properties properties, String property) {
        //if property is not specified returns 99999
        //if is a string returns 0 to match any value with "x"
        try {
            int value = Integer.parseInt(properties.getProperty(property, "999999"));
            return value;
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    /*
     * Checks if a plugin is already installed, if is an obsolete or newer
     * version
     */
    public static int compareVersions(String name, String version) {
        Client client = ClientStorage.get(name);
        if (client != null && client instanceof Plugin) {
            //already installed
            //now check for version
            Plugin plugin = (Plugin) client;
            return Plugin.compareVersions(plugin.getVersion(), version);
        } else {
            //not installed
            return -1;
        }
    }
}

/*Copyright 2009 Enrico Nicoletti
 eMail: enrico.nicoletti84@gmail.com

 This file is part of Freedomotic.

 Freedomotic is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.

 Freedomotic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with EventEngine; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.api;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;
import it.freedomotic.model.ds.Config;
import it.freedomotic.persistence.ConfigPersistence;
import it.freedomotic.util.EqualsUtil;
import it.freedomotic.util.Info;
import java.io.*;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public abstract class Plugin implements Client {

//    private boolean isConnected = false;
    protected boolean isRunning = false;
    private String pluginName;
    protected String type = "Plugin";
    protected Config configuration;
    protected JFrame gui;
    private static final String SEPARATOR = "-";
    //config file parameters
    protected String description;
    protected String version;
    protected String requiredVersion;
    protected String category;
    protected String shortName;
    protected String listenOn;
    protected String sendOn;

    public Plugin(String pluginName, String manifest) {
        setName(pluginName);
        init(new File(Info.getDevicesPath() + manifest));
    }

    protected void onStart() {
    }

    protected void onStop() {
    }

    @Override
    public final void setDescription(String description) {
        if (!getDescription().equalsIgnoreCase(description)) {
            this.description = description;
            PluginHasChanged event = new PluginHasChanged(this, this.getName(), PluginActions.DESCRIPTION);
            Freedomotic.sendEvent(event);
        }
    }

    @Override
    public String getDescription() {
        if (description == null) {
            return getName();
        } else {
            return description;
        }
    }

    public Config getConfiguration() {
        return configuration;
    }

    public String getReadQueue() {
        return listenOn;
    }
//
//    public String getWriteQueue() {
//        return sendOn;
//    }

    public String getCategory() {
        return category;
    }

    public String getRequiredVersion() {
        return requiredVersion;
    }

    public String getVersion() {
        return version;
    }

    public void bindGuiToPlugin(JFrame window) {
        gui = window;
        gui.setVisible(false);
        gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void showGui() {
        if (!isRunning()) {
            start();
        }
        onShowGui();
        if (gui != null) {
            gui.setVisible(true);
        } else {
            Freedomotic.logger.warning("ERROR: plugin gui is null");
        }
    }

    @Override
    public void hideGui() {
        onHideGui();
        if (gui != null) {
            gui.setVisible(false);
        }
    }

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    public String getClassName() {
        return (this.getClass().getSimpleName());
    }

    @Override
    public final void setName(String name) {
        pluginName = name;
    }

//    public boolean isConnected() {
//        return isConnected;
//    }
//
//    public void setConnected() {
//        isConnected = true;
//    }
    @Override
    public boolean equals(Object aThat) {
        //check for self-comparison
        if (this == aThat) {
            return true;
        }

        //use instanceof instead of getClass here for two reasons
        //1. if need be, it can match any supertype, and not just one class;
        //2. it renders an explict check for "that == null" redundant, since
        //it does the check for null already - "null instanceof [type]" always
        //returns false. (See Effective Java by Joshua Bloch.)
        if (!(aThat instanceof Plugin)) {
            return false;
        }
        //Alternative to the above line :
        //if ( aThat == null || aThat.getClass() != this.getClass() ) return false;

        //cast to native object is now safe
        Plugin that = (Plugin) aThat;

        //now a proper field-by-field evaluation can be made
        return EqualsUtil.areEqual(this.getName().toLowerCase(), that.getName().toLowerCase());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.pluginName != null ? this.pluginName.hashCode() : 0);
        return hash;
    }

    private void init(File manifest) {
        setDescription("No description");
        try {
            configuration = ConfigPersistence.deserialize(manifest);
        } catch (IOException ex) {
            Freedomotic.logger.severe("Missing manifest " + manifest.toString() + " for plugin " + getName());
            setDescription("Missing manifest file " + manifest.toString());
        }
        description = configuration.getStringProperty("description", "Missing plugin manifest");
        setDescription(description);
        version = configuration.getStringProperty("version", "1.0.0");
        requiredVersion = configuration.getStringProperty("required", "1.0.0");
//        if (!isCompatible("requiredVersion")) {
//            setDescription("This plugin cannot work with this framework version. Framework version " + requiredVersion + " is needed.");
//        }
        category = configuration.getStringProperty("category", "undefined");
        shortName = configuration.getStringProperty("short-name", "undefined");
        listenOn = configuration.getStringProperty("listen-on", "undefined");
        sendOn = configuration.getStringProperty("send-on", "undefined");

    }

    public static boolean isCompatible(final File pluginFolder) {
        if (!pluginFolder.isDirectory()) {
            throw new IllegalArgumentException();
        }
        //seach for a file called PACKAGE
        Properties plugin = new Properties();
        try {
            plugin.load(new FileInputStream(new File(pluginFolder + "/PACKAGE")));

            int requiredMajor = getIntProperty(plugin, "framework.required.major");
            int requiredMinor = requiredMajor = getIntProperty(plugin, "framework.required.minor");
            int requiredBuild = requiredMajor = getIntProperty(plugin, "framework.required.build");
            //checking framework version compatibility
            if ((getLaterVersion(
                    Info.getVersion(),
                    requiredMajor + "." + requiredMinor + "." + requiredBuild) >= 0)) {
                return true;
            }

        } catch (IOException ex) {
            Freedomotic.logger.severe("Folder " + pluginFolder + " don't contains a PACKAGE file. This plugin is not loaded.");
        } catch (NumberFormatException numex) {
            //do nothing
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
        return false;
    }

    private static int getIntProperty(Properties properties, String property) {
        //if property is not specified returns 99999
        //if is a string returns 0 to match any value with "x"
        try {
            int value = Integer.parseInt(properties.getProperty(property, "999999"));
            return value;
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    /**
     *
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int getLaterVersion(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        if (i < vals1.length && i < vals2.length) {
            int diff = new Integer(vals1[i]).compareTo(new Integer(vals2[i]));
            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }

        return vals1.length < vals2.length ? -1 : vals1.length == vals2.length ? 0 : 1;
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

    public static void mergePackageConfiguration(Plugin plugin, File pluginFolder) {
        //seach for a file called PACKAGE
        Properties pack = new Properties();
        try {
            pack.load(new FileInputStream(new File(pluginFolder + "/PACKAGE")));
            //merges data found in file PACKGE to the the configuration of every single plugin in this package
            plugin.getConfiguration().setProperty("package.name", pack.getProperty("package.name"));
            plugin.getConfiguration().setProperty("package.nodeid", pack.getProperty("package.nodeid"));
            plugin.getConfiguration().setProperty("package.version",
                    pack.getProperty("build.major") + "."
                    + pack.getProperty("build.number"));
            plugin.getConfiguration().setProperty("framework.required.version",
                    pack.getProperty("framework.required.major") + "."
                    + pack.getProperty("framework.required.minor") + "."
                    + pack.getProperty("framework.required.build"));
            //TODO: add also the other properties

        } catch (IOException ex) {
            Freedomotic.logger.severe("Folder " + pluginFolder + " doesen't contains a PACKAGE file. This plugin is not loaded.");
        }
    }

    protected void onShowGui() {

    }

    protected void onHideGui() {

    }
}

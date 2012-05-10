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

import it.freedomotic.bus.BusConsumer;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;
import it.freedomotic.model.ds.Config;
import it.freedomotic.persistence.ConfigPersistence;
import it.freedomotic.util.EqualsUtil;
import it.freedomotic.util.Info;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
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
        description = configuration.getStringProperty("description", "Set a description in config file");
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

    public boolean isCompatible(final String requiredFrameworkVersion) {
        try {
            StringTokenizer required = new StringTokenizer(requiredFrameworkVersion, ".");
            if ((required.countTokens() == 3)) {
                int pluginMajor = 0;
                int pluginMinor = 0;
                int pluginRevision = 0;
                String jolly = "x";
                try {
                    pluginMajor = Integer.parseInt(required.nextToken());
                    pluginMinor = Integer.parseInt(required.nextToken());
                    pluginRevision = Integer.parseInt(required.nextToken());
                } catch (NumberFormatException numberFormatException) {
                    Freedomotic.logger.severe("Cannot be loaded as it has a not valid specification of framework required version (eg: 5.2.x)");
                    return false;
                }
                //checking framework version compatibility
                if (Info.getMajor() >= pluginMajor) {
                    if ((Info.getMinor() >= pluginMinor) || (required.nextToken().equalsIgnoreCase(jolly))) {
                        if ((Info.getRevision() >= pluginRevision) || (required.nextToken().equalsIgnoreCase(jolly))) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
        return false;
    }

    protected void onShowGui() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void onHideGui() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

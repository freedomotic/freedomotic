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
package com.freedomotic.api;

import com.freedomotic.exceptions.PluginShutdownException;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.app.ConfigPersistence;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.PluginHasChanged.PluginActions;
import com.freedomotic.model.ds.Config;
import com.freedomotic.util.EqualsUtil;
import com.freedomotic.settings.Info;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jms.ObjectMessage;
import javax.swing.JFrame;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Plugin implements Client, BusConsumer {

    final static int SAME_VERSION = 0;
    final static int FIRST_IS_OLDER = -1;
    final static int LAST_IS_OLDER = 1;
    private static final String ACTUATORS_QUEUE_DOMAIN = "app.actuators.";
    private static final Logger LOG = LoggerFactory.getLogger(Plugin.class.getName());
    @XmlElement
    private String pluginName;
    @XmlElement
    private final String type = "Plugin";
    @XmlElement
    private volatile PluginStatus currentPluginStatus = PluginStatus.STOPPED;
    @XmlElement
    public Config configuration;
    @Deprecated
    protected JFrame gui;
    @XmlElement
    protected String description;
    @XmlElement
    protected String version;
    @XmlElement
    protected String requiredVersion;
    @XmlElement
    protected String category;
    @XmlElement
    protected String shortName;
    @XmlElement
    protected String listenOn;
    @XmlElement
    protected String sendOn;
    @XmlElement
    private File path;

    protected BusMessagesListener listener;

    @Inject
    private API api;
    @Inject
    private BusService busService;

    /**
     *
     * @param pluginName the plugin name
     * @param manifestPath the path of the plugin configuration file
     */
    public Plugin(String pluginName, String manifestPath) {
        Freedomotic.INJECTOR.injectMembers(this);
        setName(pluginName);
        path = new File(Info.PATHS.PATH_DEVICES_FOLDER + manifestPath);
        init();
    }

    /**
     * Used to create a Plugin placeholder for things.
     *
     * @param pluginName the plugin name
     */
    public Plugin(String pluginName) {
        Freedomotic.INJECTOR.injectMembers(this);
        setName(pluginName);
        init();
    }

    /**
     * Used by JoinPlugin to instantiate a new plugin.
     *
     * @param pluginName the plugin name
     * @param manifest the manifest config file
     */
    public Plugin(String pluginName, Config manifest) {
        Freedomotic.INJECTOR.injectMembers(this);
        setName(pluginName);
        init();
    }

    /**
     * Returns the path of the plugin configuration file.
     *
     * @return the path of the plugin configuration file
     */
    public File getFile() {
        return path;
    }

    /**
     *
     * @return
     */
    public API getApi() {
        return api;
    }

    /**
     *
     * @throws com.freedomotic.exceptions.PluginStartupException
     */
    protected void onStart() throws PluginStartupException {
    }

    /**
     *
     * @throws com.freedomotic.exceptions.PluginShutdownException
     */
    protected void onStop() throws PluginShutdownException {
    }

    /**
     * Sets the plugin description.
     *
     * @param description
     */
    @Override
    public final void setDescription(String description) {
        if (!getDescription().equalsIgnoreCase(description)) {
            this.description = description;

            try {
                PluginHasChanged event = new PluginHasChanged(this,
                        this.getName(),
                        PluginActions.DESCRIPTION);
                busService.send(event);
            } catch (Exception e) {
                LOG.warn("Cannot notify new plugin description for " + getName(), e);
            }
        }
    }

    /**
     * Notifies an error on console/logfile and shows a callout on the frontend.
     *
     * @param message the error message
     */
    public void notifyError(String message) {
        //Log the error on console/logfiles
        LOG.warn(message);
        //write something on the GUI
        MessageEvent callout = new MessageEvent(this, message);
        callout.setType("callout"); //display as callout on frontends
        callout.setLevel("warning");
        callout.setExpiration(10 * 1000);//message lasts 10 seconds
        busService.send(callout);
    }

    public void notifyCriticalError(String message) {
        //Log the error on console/logfiles
        LOG.warn(message);
        //write something on the GUI
        MessageEvent callout = new MessageEvent(this, message);
        callout.setType("callout"); //display as callout on frontends
        callout.setLevel("warning");
        callout.setExpiration(10 * 1000);//message lasts 10 seconds
        busService.send(callout);
        //stop this plugin
        stop();
        //override plugin description
        setDescription(message);
        //plugin is now set as STOPPED, but should be marked as FAILED
        currentPluginStatus = PluginStatus.FAILED;
    }

    /**
     * Notifies a critical error on the console/logfile and keeps a stack trace.
     *
     * @param message the error message
     * @param ex the raised exception
     */
    protected void notifyCriticalError(String message, Exception ex) {
        //Log and keep stack trace
        LOG.error(message, ex);
        notifyCriticalError(message);
    }

    /**
     * Returns the plugin description.
     *
     * @return the plugin description
     */
    @Override
    public String getDescription() {
        if (description == null) {
            return getName();
        } else {
            return description;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public final Config getConfiguration() {
        return configuration;
    }

    /**
     *
     * @return
     */
    public final String getReadQueue() {
        return listenOn;
    }

    /**
     * Returns the plugin category.
     *
     * @return the plugin category
     */
    public final String getCategory() {
        return category;
    }

    /**
     * Returns the required version for the plugin.
     *
     * @return the required version
     */
    public String getRequiredVersion() {
        return requiredVersion;
    }

    /**
     * Returns the plugin version.
     *
     * @return the plugin version
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @param window
     */
    public void bindGuiToPlugin(JFrame window) {
        gui = window;
        gui.setVisible(false);
        gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     *
     */
    @Override
    @Deprecated
    public void showGui() {
        if (!isRunning()) {
            start();
        }

        onShowGui();

        if (gui != null) {
            gui.setVisible(true);
        } else {
            LOG.warn("ERROR: plugin gui is null");
        }
    }

    /**
     *
     */
    @Override
    @Deprecated
    public void hideGui() {
        onHideGui();

        if (gui != null) {
            gui.setVisible(false);
        }
    }

    /**
     * Returns the plugin name.
     *
     * @return the plugin name
     */
    @Override
    public String getName() {
        return pluginName;
    }

    /**
     * Returns the current plugin status.
     *
     * @return the current plugin status
     */
    public String getStatus() {
        return currentPluginStatus.name();
    }

    /**
     * Returns the plugin type.
     *
     * @return the plugin type
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Checks if the plugin status is 'RUNNING'.
     *
     * @return true if the plugin status is 'RUNNING', false otherwise
     */
    @Override
    public boolean isRunning() {
        return currentPluginStatus.equals(PluginStatus.RUNNING);
    }

    /**
     * Checks if the plugin status is 'STARTING' or 'RUNNING'.
     *
     * @return true if the plugin status is 'STARTING' or 'RUNNING', false
     * otherwise
     */
    public boolean isAllowedToSend() {
        return currentPluginStatus.equals(PluginStatus.STARTING) || currentPluginStatus.equals(PluginStatus.RUNNING);
    }

    /**
     * Checks if the plugin can start.
     *
     * @return true if the plugin can start, false otherwise
     */
    public boolean isAllowedToStart() {
        return PluginStatus.isAllowedToStart(currentPluginStatus);
    }

    /**
     * Gets the plugin class name.
     *
     * @return the plugin class name
     */
    @XmlElement(name = "uuid")
    public String getClassName() {
        return (this.getClass().getSimpleName().toLowerCase());
    }

    /**
     * Sets the plugin name.
     *
     * @param name the new plugin name
     */
    @Override
    public final void setName(String name) {
        pluginName = name;
    }

    /**
     * Sets the plugin status.
     *
     * @param newStatus the new plugin status
     */
    protected final void setStatus(PluginStatus newStatus) {
        currentPluginStatus = newStatus;

    }

//    public boolean isConnected() {
//        return isConnected;
//    }
//
//    public void setConnected() {
//        isConnected = true;
//    }
    /**
     *
     * @param aThat
     * @return
     */
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
        return EqualsUtil.areEqual(this.getName().toLowerCase(),
                that.getName().toLowerCase());
    }

    /**
     * Calculates an hash code.
     *
     * @return an hash code
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = (53 * hash) + ((this.pluginName != null) ? this.pluginName.hashCode() : 0);

        return hash;
    }

    private void init() {
        if (configuration == null) {
            //try to load it from file
            deserializeManifest(path);
        }
        description = configuration.getStringProperty("description", "Missing plugin manifest");
        setDescription(description);
        category = configuration.getStringProperty("category", "undefined");
        shortName = configuration.getStringProperty("short-name", "undefined");
        listenOn = configuration.getStringProperty("listen-on", "undefined");
        sendOn = configuration.getStringProperty("send-on", "undefined");
        register();
    }

    /**
     * Deserializes the xml configuration file.
     *
     * @param manifest the manifest configuration file
     */
    private void deserializeManifest(File manifest) {
        try {
            configuration = ConfigPersistence.deserialize(manifest);
        } catch (IOException ex) {
            LOG.error("Missing manifest {} for plugin {}", new Object[]{manifest.toString(), getName()});
            setDescription("Missing manifest file " + manifest.toString());
        }

    }

    private void register() {
        listener = new BusMessagesListener(this, getBusService());
        listener.consumeCommandFrom(getCommandsChannelToListen());
    }

    private String getCommandsChannelToListen() {
        String defaultQueue = ACTUATORS_QUEUE_DOMAIN + category + "." + shortName;
        String customizedQueue = ACTUATORS_QUEUE_DOMAIN + listenOn;

        if (getReadQueue().equalsIgnoreCase("undefined")) {
            listenOn = defaultQueue + ".in";

            return listenOn;
        } else {
            return customizedQueue;
        }
    }

    /**
     *
     */
    @Deprecated
    protected void onShowGui() {
    }

    /**
     *
     */
    @Deprecated
    protected void onHideGui() {
    }

    /**
     *
     */
    @Override
    public void start() {
        //do not add code here
    }

    /**
     *
     */
    @Override
    public void stop() {
        //do not add code here
    }

    /**
     *
     * @return the plugin name
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     *
     */
    public void loadPermissionsFromManifest() {
        getApi().getAuth().setPluginPrivileges(this, configuration.getStringProperty("permissions", getApi().getAuth().getPluginDefaultPermission()));
    }

    public BusService getBusService() {
        return busService;
    }

    @Override
    public void onMessage(ObjectMessage message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Destroyies the messaging channel.
     *
     */
    @Override
    public void destroy() {
        // Destroy the messaging channel
        listener.destroy();
        stop();
    }

}

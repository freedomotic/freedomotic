/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
import com.freedomotic.bus.BusService;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.PluginHasChanged.PluginActions;
import com.freedomotic.model.ds.Config;
import com.freedomotic.util.EqualsUtil;
import com.freedomotic.util.Info;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author nicoletti
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Plugin implements Client {

    final static int SAME_VERSION = 0;
    final static int FIRST_IS_OLDER = -1;
    final static int LAST_IS_OLDER = 1;
    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());
    @XmlElement
    private String pluginName;
    @XmlElement
    private final String type = "Plugin";
    @XmlElement
    protected volatile PluginStatus currentPluginStatus = PluginStatus.STOPPED;
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

    @Inject
    private API api;
    @Inject
    private BusService busService;

    /**
     *
     * @param pluginName
     * @param manifestPath
     */
    public Plugin(String pluginName, String manifestPath) {
        this(pluginName);
        path = new File(Info.PATHS.PATH_DEVICES_FOLDER + manifestPath);
        init(path);
    }

    public Plugin() {
        Freedomotic.INJECTOR.injectMembers(this);
    }

    /**
     *
     * @param pluginName
     * @param manifest
     */
    public Plugin(String pluginName, Config manifest) {
        this(pluginName);
        init(manifest);
    }

    /**
     *
     * @param pluginName
     */
    public Plugin(String pluginName) {
        //probably this class is not instantiated using DI, we have to force the injection
        Freedomotic.INJECTOR.injectMembers(this);
        setName(pluginName);
    }

    /**
     *
     * @return
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
     * @throws com.freedomotic.api.PluginStartupException
     */
    protected void onStart() throws PluginStartupException {
    }

    /**
     *
     * @throws com.freedomotic.api.PluginShutdownException
     */
    protected void onStop() throws PluginShutdownException {
    }

    /**
     *
     * @param description
     */
    @Override
    public final void setDescription(String description) {
        if (!getDescription().equalsIgnoreCase(description)) {
            this.description = description;

            PluginHasChanged event = new PluginHasChanged(this,
                    this.getName(),
                    PluginActions.DESCRIPTION);
            busService.send(event);
        }
    }

    public void notifyError(String message) {
        //Log the error on console/logfiles
        LOG.warning(message);
        //write something on the GUI
        MessageEvent callout = new MessageEvent(this, message);
        callout.setType("callout"); //display as callout on frontends
        callout.setLevel("warning");
        callout.setExpiration(10 * 1000);//message lasts 10 seconds
        busService.send(callout);
    }

    public void notifyCriticalError(String message) {
        //Log the error on console/logfiles
        LOG.warning(message);
        //change plugin description
        setDescription(message);
        //write something on the GUI
        MessageEvent callout = new MessageEvent(this, message);
        callout.setType("callout"); //display as callout on frontends
        callout.setLevel("warning");
        callout.setExpiration(10 * 1000);//message lasts 10 seconds
        busService.send(callout);
        //stop this plugin
        stop();
        //plugin is now set as STOPPED, but should be marked as FAILED
        currentPluginStatus = PluginStatus.FAILED;
    }

    /**
     *
     * @return
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
     *
     * @return
     */
    public final String getCategory() {
        return category;
    }

    /**
     *
     * @return
     */
    public String getRequiredVersion() {
        return requiredVersion;
    }

    /**
     *
     * @return
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
            LOG.warning("ERROR: plugin gui is null");
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
     *
     * @return
     */
    @Override
    public String getName() {
        return pluginName;
    }

    /**
     *
     * @return
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isRunning() {
        return currentPluginStatus.equals(PluginStatus.RUNNING);
    }
    
    public boolean isAllowedToStart() {
        return PluginStatus.isAllowedToStart(currentPluginStatus);
    }

    /**
     *
     * @return
     */
    @XmlElement(name="uuid")
    public String getClassName() {
        return (this.getClass().getSimpleName().toLowerCase());
    }

    /**
     *
     * @param name
     */
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
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = (53 * hash) + ((this.pluginName != null) ? this.pluginName.hashCode() : 0);

        return hash;
    }

    private void init(File manifest) {
        try {
            configuration = ConfigPersistence.deserialize(manifest);
        } catch (IOException ex) {
            LOG.severe("Missing manifest " + manifest.toString() + " for plugin " + getName());
            setDescription("Missing manifest file " + manifest.toString());
        }

        init(configuration);

    }

    private void init(Config configuration) {
        setDescription("No description");
        description = configuration.getStringProperty("description", "Missing plugin manifest");
        setDescription(description);
        category = configuration.getStringProperty("category", "undefined");
        shortName = configuration.getStringProperty("short-name", "undefined");
        listenOn = configuration.getStringProperty("listen-on", "undefined");
        sendOn = configuration.getStringProperty("send-on", "undefined");
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
        LOG.log(Level.INFO, "Starting plugin {0}", getName());
        //do not add code here
    }

    /**
     *
     */
    @Override
    public void stop() {
        LOG.log(Level.INFO, "Stopping plugin {0}", getName());
        //do not add code here
    }

    /**
     *
     * @return
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

}

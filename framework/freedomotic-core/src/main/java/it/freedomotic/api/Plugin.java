/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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
package it.freedomotic.api;

import com.google.inject.Inject;

import it.freedomotic.app.ConfigPersistence;
import it.freedomotic.app.Freedomotic;

import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;

import it.freedomotic.model.ds.Config;

import it.freedomotic.util.EqualsUtil;
import it.freedomotic.util.Info;

import java.io.*;

import javax.swing.JFrame;
import it.freedomotic.security.Auth;
import java.util.logging.Logger;

public class Plugin
        implements Client {
//    private boolean isConnected = false;

    protected volatile boolean isRunning;
    private String pluginName;
    private String type = "Plugin";
    public Config configuration;
    protected JFrame gui;
    // private static final String SEPARATOR = "-";
    
    //config file parameters
    protected String description;
    protected String version;
    protected String requiredVersion;
    protected String category;
    protected String shortName;
    protected String listenOn;
    protected String sendOn;
    private File path;
    final static int SAME_VERSION = 0;
    final static int FIRST_IS_OLDER = -1;
    final static int LAST_IS_OLDER = 1;
    @Inject
    private API api;

    public Plugin(String pluginName, String manifestPath) {
        setName(pluginName);
        path = new File(Info.getDevicesPath() + manifestPath);
        init(path);
    }

    public Plugin(String pluginName, Config manifest) {
        setName(pluginName);
        init(manifest);
    }

    public Plugin(String pluginName) {
        setName(pluginName);
    }

    public File getFile() {
        return path;
    }

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    protected void onStart() {
    }

    protected void onStop() {
    }

    @Override
    public final void setDescription(String description) {
        if (!getDescription().equalsIgnoreCase(description)) {
            this.description = description;

            PluginHasChanged event = new PluginHasChanged(this,
                    this.getName(),
                    PluginActions.DESCRIPTION);
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

    @Override
    public final Config getConfiguration() {
        return configuration;
    }

    public final String getReadQueue() {
        return listenOn;
    }

    public final String getCategory() {
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
            LOG.warning("ERROR: plugin gui is null");
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
        return EqualsUtil.areEqual(this.getName().toLowerCase(),
                that.getName().toLowerCase());
    }

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
        loadPermissionsFromManifest();
    }

    protected void onShowGui() {
    }

    protected void onHideGui() {
    }

    @Override
    public void start() {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        };
        Auth.pluginExecutePrivileged(this, action);
    }

    @Override
    public void stop() {
    }

    @Override
    public String toString() {
        return getName();
    }

    protected void loadPermissionsFromManifest() {
        Auth.setPluginPrivileges(this, configuration.getStringProperty("permissions", Auth.getPluginDefaultPermission()));
    }
    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());
}

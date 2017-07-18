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
package com.freedomotic.plugins.impl;

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.bus.BusService;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.PluginHasChanged.PluginActions;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import com.freedomotic.settings.Info;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage of added plugins and connected clients
 */
class ClientStorageInMemory implements ClientStorage {

    private static final Logger LOG = LoggerFactory.getLogger(ClientStorageInMemory.class.getName());
    private static final List<Client> clients = new ArrayList<>();
    //It works because this class is created by guice and the injector is automatically available in this case
    @Inject
    private Injector injector;
    @Inject
    private ThingRepository thingsRepository;
    @Inject
    private BusService busService;

    ClientStorageInMemory() {
        //just injected
    }

    /**
     *
     * @param c
     */
    @Override
    public void add(Client c) {
        if (!clients.contains(c)) {
            if (isCompatible(c)) {
                clients.add(c);
            } else {
                Client client
                        = createPluginPlaceholder(c.getName(),
                                "Plugin",
                                "Not compatible with this framework version v" + Info.getVersion());
                clients.add(client);
                LOG.warn("Plugin \"{}\" is not compatible with this framework version v{}", c.getName(), Info.getVersion());
            }

            PluginHasChanged event
                    = new PluginHasChanged(ClientStorageInMemory.class,
                            c.getName(), PluginActions.ENQUEUE);
            busService.send(event);
            LOG.info("Extension \"{}\" added to plugins list.", c.getName());
        }
    }

    /**
     * Unloads a plugin from memory and destroys all bus listeners associated to
     * it.
     *
     * @param c
     */
    @Override
    public void remove(Client c) {
        if (clients.contains(c)) {
            // Stops and unsubscribes channels
            c.destroy();
            clients.remove(c);

            PluginHasChanged event
                    = new PluginHasChanged(ClientStorageInMemory.class,
                            c.getName(), PluginActions.DEQUEUE);
            busService.send(event);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<Client> getClients() {
        Collections.sort(clients,
                new ClientNameComparator());

        return Collections.unmodifiableList(clients);
    }

    /**
     *
     * @param name
     * @return
     */
    @Override
    public Client get(String name) {
        for (Client client : clients) {
            if (client.getName().equalsIgnoreCase(name)) {
                return client;
            }
        }

        return null;
    }

    /**
     *
     * @param filterType
     * @return
     */
    @Override
    public List<Client> getClients(String filterType) {
        List<Client> tmp = new ArrayList<>();
        for (Client client : clients) {
            if (client.getType().equalsIgnoreCase(filterType)) {
                tmp.add(client);
            }
        }

        Collections.sort(tmp,
                new ClientNameComparator());

        return Collections.unmodifiableList(tmp);
    }

    /**
     *
     * @param protocol
     * @return
     */
    @Override
    public Client getClientByProtocol(String protocol) {
        for (Client client : clients) {
            if (client.getConfiguration().getStringProperty("protocol.name", "").equals(protocol)) {
                return client;
            }
        }

        return null;
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public boolean isLoaded(Client input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }

        return clients.contains(input);
    }

    /*
     * Checks if a plugin is already installed, if is an obsolete or newer
     * version
     */
    /**
     *
     * @param name
     * @param version
     * @return
     */
    @Override
    public int compareVersions(String name, String version) {
        Client client = get(name);

        if ((client != null) && client instanceof Plugin) {
            //already installed
            //now check for version
            Plugin plugin = (Plugin) client;

            if (plugin.getVersion() == null) {
                return -1;
            }

            return getOldestVersion(plugin.getVersion(), version);
        } else {
            //not installed
            return -1;
        }
    }

    /**
     *
     * @param client
     * @return
     */
    protected boolean isCompatible(Client client) {
        //seach for a file called PACKAGE
        Properties config = client.getConfiguration().getProperties();
        int requiredMajor = getVersionProperty(config, "framework.required.major");
        int requiredMinor = getVersionProperty(config, "framework.required.minor");
        int requiredBuild = getVersionProperty(config, "framework.required.build");

        //checking framework version compatibility
        //required version must be older (or equal) then current version
        return requiredMajor == Info.getMajor() && getOldestVersion(requiredMajor + "." + requiredMinor + "." + requiredBuild, Info.getVersion()) <= 0;
    }

    private int getVersionProperty(Properties properties, String key) {
        //if property is not specified returns Integer.MAX_VALUE so it never matches
        //if is a string returns 0 to match any value with "x"
        try {
            int value;
            if (properties.getProperty(key).equalsIgnoreCase("x")) {
                value = 0;
            } else {
                value = Integer.parseInt(properties.getProperty(key, Integer.toString(Integer.MAX_VALUE)));
            }

            return value;
        } catch (NumberFormatException numberFormatException) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Calculates the oldest version between two version string
     * MAJOR.MINOR.BUILD (eg: 5.3.1)
     *
     * @param clientVersion first version string 5.3.0
     * @param infoVersion second version string 5.3.1
     * @return -1 if str1 is older then str2, 0 if str1 equals str2, 1 if str2
     * is older then str1
     */
    private int getOldestVersion(String clientVersion, String infoVersion) {
        String maxInteger = Integer.toString(Integer.MAX_VALUE);
        String val1 = clientVersion.replaceAll("x", maxInteger);
        String val2 = infoVersion.replaceAll("x", maxInteger);

        String[] vals1 = val1.split("\\.");
        String[] vals2 = val2.split("\\.");
        int i = 0;

        while ((i < vals1.length) && (i < vals2.length) && vals1[i].equals(vals2[i])) {
            i++;
        }

        if ((i < vals1.length) && (i < vals2.length)) {
            return Integer.compare(new Integer(vals1[i]), new Integer(vals2[i]));
        }
        
        return Integer.compare(vals1.length, vals2.length);
    }

    /**
     * Creates a placeholder plugin and adds it to the list of added plugins.
     * This plugin is just a mock object to inform the user that an object with
     * complete features is expected here. It can be used for example to list a
     * fake plugin that informs the user the real plugin cannot be added.
     *
     * @param simpleName
     * @param type
     * @param description
     * @return
     */
    @Override
    public Plugin createPluginPlaceholder(final String simpleName, final String type, final String description) {
        final Plugin placeholder
                = new Plugin(simpleName) {
                    @Override
                    public String getDescription() {
                        if (description == null) {
                            return "Plugin Unavailable. Error on loading";
                        } else {
                            return description;
                        }
                    }

                    @Override
                    public String getName() {
                        return "Cannot start \"" + simpleName + "\"";
                    }

                    @Override
                    public String getType() {
                        return type;
                    }

                    @Override
                    public void start() {
                    	LOG.debug("start placeholder");
                    }

                    @Override
                    public void stop() {
                    	LOG.debug("stop placeholder");
                    }

                    @Override
                    public boolean isRunning() {
                        return false;
                    }

                    @Override
                    public void showGui() {
                    	LOG.debug("Show GUI placeholder");
                    }

                    @Override
                    public void hideGui() {
                    	LOG.debug("Hide GUI placeholder");
                    }
                };

        placeholder.setDescription(description);
        placeholder.configuration = new Config();

        return placeholder;
    }

    /**
     *
     * @param template
     * @return
     * @throws RepositoryException
     */
    @Override
    public Client createObjectPlaceholder(final File template) throws RepositoryException {
        return new ObjectPluginPlaceholder(thingsRepository, template);
    }

    class ClientNameComparator implements Comparator<Client> {

        @Override
        public int compare(Client m1, Client m2) {
            //possibly check for nulls to avoid NullPointerException
            return m1.getName().compareTo(m2.getName());
        }
    }
}

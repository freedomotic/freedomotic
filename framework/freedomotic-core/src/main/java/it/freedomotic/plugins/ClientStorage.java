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
package it.freedomotic.plugins;

import it.freedomotic.api.Client;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A storage of loaded plugins and connected clients
 */
public final class ClientStorage {

    private static final List<Client> clients = new ArrayList<Client>();
    private static final ClientComparator compare = new ClientComparator();
    //instantiated into Freedomotic.java
    //an instance is needed
    public ClientStorage() {
    }

    public void enqueue(Client c) {
        if (!clients.contains(c)) {
            clients.add(c);
            PluginHasChanged event = new PluginHasChanged(this, c.getName(), PluginActions.ENQUEUE);
            Freedomotic.sendEvent(event);
        }
    }
    
    public void dequeue(Client c){
        if (clients.contains(c)) {
            clients.remove(c);
            PluginHasChanged event = new PluginHasChanged(this, c.getName(), PluginActions.DEQUEUE);
            Freedomotic.sendEvent(event);
        }
    }

    public static List<Client> getClients() {
        Collections.sort(clients, compare);
        return Collections.unmodifiableList(clients);
    }

    public static Client get(String name) {
        for (Client client : clients) {
            if (client.getName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }

    public List<Client> getClients(String filterType) {
        List<Client> tmp = new ArrayList<Client>();
        for (Client client : clients) {
            if (client.getType().equalsIgnoreCase(filterType)) {
                tmp.add(client);
            }
        }
        return Collections.unmodifiableList(tmp);
    }

    public Client getClientByProtocol(String protocol) {
        for (Client client : clients) {
            if (client.getConfiguration().getStringProperty("protocol.name", "").equals(protocol)) {
                return client;
            }
        }
        return null;
    }

    public static boolean isLoaded(Client input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        return clients.contains(input);
    }

    protected Plugin createPlaceholder(final String simpleName, final String type, final String description) {
        final Plugin placeholder = new Plugin(simpleName) {
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
                return "Cannot start " + simpleName;
            }

            @Override
            public String getType() {
                return type;
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }

            @Override
            public boolean isRunning() {
                return false;
            }

            @Override
            public void showGui() {
            }

            @Override
            public void hideGui() {
            }
        };
        placeholder.setDescription(description);
        enqueue(placeholder);
        return placeholder;
    }

    protected void createObjectTemplate(final File template) {
        ObjectPlugin placeholder = new ObjectPlugin(template);
        enqueue(placeholder);
    }
    
    static class ClientComparator implements Comparator<Client> {
        @Override
    public int compare(Client m1, Client m2) {
       //possibly check for nulls to avoid NullPointerException
       return m1.getName().compareTo(m2.getName());
    }
}
}

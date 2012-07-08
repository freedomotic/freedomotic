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
package it.freedomotic.plugins;

import it.freedomotic.api.Client;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A storage of loaded plugins and connected clients
 */
public class ClientStorage {

    private static final ArrayList<it.freedomotic.api.Client> clients = new ArrayList<it.freedomotic.api.Client>();

    public ClientStorage() {
    }

    public void enqueue(it.freedomotic.api.Client c) {
        clients.add((it.freedomotic.api.Client) c);
        PluginHasChanged event = new PluginHasChanged(this, c.getName(), PluginActions.ENQUEUE);
        Freedomotic.sendEvent(event);
    }

    public ArrayList<it.freedomotic.api.Client> getClients() {
        return clients;
    }

    public static Client get(String name) {
        for (Client client : clients) {
            if (client.getName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }

    public ArrayList<Client> getClients(String filterType) {
        ArrayList<Client> tmp = new ArrayList<Client>();
        for (Client client : clients) {
            if (client.getType().equalsIgnoreCase(filterType)) {
                tmp.add(client);
            }
        }
        return tmp;
    }
    
    public static boolean alreadyLoaded(Client input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        Iterator it = clients.iterator();
        while (it.hasNext()) {
            Client client = (Client) it.next();
            if (input.getName().equalsIgnoreCase(client.getName())) {
                return true;
            }
        }
        return false;
    }

    public Plugin createPlaceholder(final String simpleName, final String type, final String description) {
        final Plugin placeholder = new Plugin(simpleName, null) {

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

    void createObjectPlaceholder(final Class objClazz, final File folder) {
        ObjectPlugin placeholder = new ObjectPlugin(objClazz, folder);
        enqueue(placeholder);
    }
}

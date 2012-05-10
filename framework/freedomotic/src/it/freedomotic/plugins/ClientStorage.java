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
import it.freedomotic.api.Configuration;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;
import it.freedomotic.model.ds.Config;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.persistence.ConfigPersistence;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.util.Info;
import java.io.File;
import java.util.ArrayList;

/**
 * Classe che tiene traccia del nome del client che si è connesso tramite socket
 * e del suo outputStrem
 */
public class ClientStorage {

    private static ArrayList<it.freedomotic.api.Client> clients = new ArrayList<it.freedomotic.api.Client>();

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

    public Client get(String name) {
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

    public void createPlaceholder(final String simpleName, final String type) {
        final Client placeholder = new Client() {

            @Override
            public void setName(String name) {
            }

            @Override
            public String getDescription() {
                return "Plugin Unavailable. Error on loading";
            }

            @Override
            public void setDescription(String description) {
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
        enqueue(placeholder);
    }

    void createObjectPlaceholder(final Class objClazz, final File folder) {
        final Client placeholder = new Client() {

            private Class clazz = objClazz;

            @Override
            public void setName(String name) {
                //no name change allowed. do nothing
            }

            @Override
            public String getDescription() {
                return "This object is a " + clazz.getSimpleName();
            }

            @Override
            public void setDescription(String description) {
            }

            @Override
            public String getName() {
                return clazz.getSimpleName();
            }

            @Override
            public String getType() {
                return "Object";
            }

            @Override
            public void start() {
                //if (!isRunning()){
                //add this object to the environment
                //Config configuration = ConfigPersistence.deserialize(new File(folder.getAbsolutePath() + "/manifest.xml"));
                EnvObjectPersistence.loadObjects(new File(folder.getAbsolutePath() + "/data/examples/" + getName().toLowerCase()), true);
                //}
            }

            @Override
            public void stop() {
                EnvObjectPersistence.loadObjects(new File(folder.getAbsolutePath() + "/data/examples/" + getName().toLowerCase()), true);
            }

            @Override
            public boolean isRunning() {
                //is running if there is already an object of this kind in the map
                boolean found = false;
                for (EnvObjectLogic obj : EnvObjectPersistence.getObjectList()) {
                    if (obj.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
                        found = true;
                    }
                }
                return found;
            }

            @Override
            public void showGui() {
            }

            @Override
            public void hideGui() {
            }
        };
        enqueue(placeholder);
    }
}

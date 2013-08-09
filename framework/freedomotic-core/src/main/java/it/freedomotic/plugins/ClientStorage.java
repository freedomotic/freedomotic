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

import it.freedomotic.exceptions.DaoLayerException;

import java.io.File;
import java.util.List;

/**
 *
 * @author enrico
 */
public interface ClientStorage {
    /*
     * Checks if a plugin is already installed, if is an obsolete or newer
     * version
     */
    int compareVersions(String name, String version);

    Client createObjectPlaceholder(final File template)
            throws DaoLayerException;

    /**
     * Creates a placeholder plugin and adds it to the list of loaded plugins.
     * This plugin is just a mock object to inform the user that an object with
     * complete features is expected here. It can be used for example to list a
     * fake plugin that informs the user the real plugin cannot be loaded.
     *
     * @param simpleName
     * @param type
     * @param description
     * @return
     */
    Plugin createPluginPlaceholder(final String simpleName, final String type, final String description);

    void remove(Client c);

    void add(Client c);

    Client get(String name);

    Client getClientByProtocol(String protocol);

    List<Client> getClients();

    List<Client> getClients(String filterType);

    boolean isLoaded(Client input);
}

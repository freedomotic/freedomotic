/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.japi.resources;

import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.wordnik.swagger.annotations.Api;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author matteo
 */
@Path("commands/hardware")
@Api(value = "hardwareCommands", description = "Operations on hardware commands", position = 6)
public class HardwareCommandResource extends AbstractResource<Command> {

    @Override
    protected URI doCreate(Command o) throws URISyntaxException {
        CommandPersistence.add(o);
        return createUri(o.getUUID());
    }

    @Override
    protected boolean doDelete(String UUID) {
        Command c = CommandPersistence.getCommandByUUID(UUID);
        if (c != null) {
            CommandPersistence.remove(c);
            return true;
        }
        return false;
    }

    @Override
    protected Command doUpdate(Command o) {
        Command c = CommandPersistence.getCommandByUUID(o.getUUID());
        if (c != null) {
            CommandPersistence.remove(c);
            CommandPersistence.add(o);
            return CommandPersistence.getCommandByUUID(o.getUUID());
        }
        return null;

    }

    @Override
    protected List<Command> prepareList() {
        return new ArrayList<Command>(CommandPersistence.getHardwareCommands());
    }

    @Override
    protected Command prepareSingle(String uuid) {
        Command c = CommandPersistence.getCommand(uuid);
        if (c == null) {
            c = CommandPersistence.getCommandByUUID(uuid);
        }
        return c;
    }

    @Override
    protected URI doCopy(String name) {
        Command c;
        try {
            c = CommandPersistence.getCommand(name).clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(HardwareCommandResource.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        CommandPersistence.add(c);
        return createUri(c.getName());
    }

}

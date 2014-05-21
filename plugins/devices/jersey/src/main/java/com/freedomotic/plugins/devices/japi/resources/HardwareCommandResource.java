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
import com.wordnik.swagger.annotations.Api;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;

/**
 *
 * @author matteo
 */
@Path("commands/hardware")
@Api(value = "hardwareCommands", description = "Operations on hardware commands", position = 6)
public class HardwareCommandResource extends AbstractResource<Command> {

    @Override
    protected URI doCreate(Command c) throws URISyntaxException {
        c.setHardwareLevel(true);
        api.commands().create(c);
        return createUri(c.getUUID());
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.commands().delete(UUID);
    }

    @Override
    protected Command doUpdate(Command c) {
        return api.commands().modify(c.getUUID(), c);
    }

    @Override
    protected List<Command> prepareList() {
        List<Command> cl = new ArrayList<Command>();
        cl.addAll(api.commands().getHardwareCommands());
        return cl;
    }

    @Override
    protected Command prepareSingle(String uuid) {
        return api.commands().get(uuid);
    }

    @Override
    protected URI doCopy(String uuid) {
        Command c = api.commands().copy(uuid);
        return createUri(c.getUUID());
    }
}

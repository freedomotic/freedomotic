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
import com.freedomotic.reactions.Trigger;
import com.wordnik.swagger.annotations.Api;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.Path;

/**
 *
 * @author matteo
 */
@Path("triggers")
@Api(value = "/triggers", description = "Operations on triggers", position = 4)
public class TriggerResource extends AbstractResource<Trigger> {

    @Override
    protected URI doCreate(Trigger o) throws URISyntaxException {
        api.triggers().create(o);
        try {
            o.register();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot register trigger", e);
        }
        return createUri(o.getUUID());
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.triggers().delete(UUID);
    }

    @Override
    protected Trigger doUpdate(Trigger o) {
        return api.triggers().modify(o.getUUID(), o);
    }

    @Override
    protected List<Trigger> prepareList() {
        return api.triggers().list();
    }

    @Override
    protected Trigger prepareSingle(String uuid) {
        return api.triggers().get(uuid);
    }

    @Override
    protected URI doCopy(String UUID) {
        Trigger t = api.triggers().copy(UUID);
        try {
            t.register();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot register trigger ", e);
        }
        return createUri(t.getUUID());
    }

}

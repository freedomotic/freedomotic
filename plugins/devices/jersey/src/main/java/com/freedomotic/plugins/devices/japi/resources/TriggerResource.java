/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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

package com.freedomotic.plugins.devices.japi.resources;

import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerPersistence;
import com.wordnik.swagger.annotations.Api;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author matteo
 */
@Path("triggers")
@Api(value = "/triggers", description = "Operations on triggers", position=4)
public class TriggerResource extends AbstractResource<Trigger>{

    @Override
    protected URI doCreate(Trigger o) throws URISyntaxException {
        TriggerPersistence.addAndRegister(o);
        return UriBuilder.fromResource(TriggerResource.class).path(o.getUUID()).build();
    }

    @Override
    protected boolean doDelete(String UUID) {
        Trigger t = TriggerPersistence.getTriggerByUUID(UUID);
        if (t != null){
            TriggerPersistence.remove(t);
            return true;
        }
        return false;
    }

    @Override
    protected Trigger doUpdate(Trigger o) {
        Trigger t = TriggerPersistence.getTriggerByUUID(o.getUUID());
        if (t != null){
            TriggerPersistence.remove(t);
            TriggerPersistence.addAndRegister(o);
            return o;
        }
        return null;
    }

    @Override
    protected List<Trigger> prepareList() {
        return TriggerPersistence.getTriggers();
    }

    @Override
    protected Trigger prepareSingle(String uuid) {
        return TriggerPersistence.getTriggerByUUID(uuid);
    }

    @Override
    protected URI doCopy(String UUID) {
        Trigger t = TriggerPersistence.getTriggerByUUID(UUID).clone();
        TriggerPersistence.addAndRegister(t);
        return createUri(t.getUUID());
    }   
    
}

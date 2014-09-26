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
package com.freedomotic.plugins.devices.restapiv3.resources.jersey;

import com.freedomotic.plugins.devices.restapiv3.representations.ReactionRepresentation;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.reactions.Reaction;
import com.wordnik.swagger.annotations.Api;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Path;

/**
 *
 * @author matteo
 */
@Path("reactions")
@Api(value = "reactions", description = "Operations on reactions", position = 3)
public class ReactionResource extends AbstractResource<ReactionRepresentation> {

    public ReactionResource() {
        authContext = "reactions";
    }

    @Override
    protected URI doCopy(String UUID) {
        api.reactions().copy(UUID);
        return createUri(UUID);
    }

    @Override
    protected URI doCreate(ReactionRepresentation o) throws URISyntaxException {
        Reaction r = new Reaction();
        if (o.getUuid() != null && !o.getUuid().isEmpty()) {
            r.setUuid(o.getUuid());
        }
        r.setTrigger(api.triggers().get(o.getTriggerUuid()));
        for (HashMap<String, String> c : o.getCommands()) {
            r.getCommands().add(api.commands().get(c.get("uuid")));
        }
        r.setConditions(o.getConditions());
        r.setChanged();
        api.reactions().create(r);
        return createUri(r.getUuid());
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.reactions().delete(UUID);
    }

    @Override
    protected ReactionRepresentation doUpdate(ReactionRepresentation o) {
        doDelete(o.getUuid());
        try {
            doCreate(o);
        } catch (URISyntaxException ex) {
            return null;
        }
        return o;
    }

    @Override
    protected List<ReactionRepresentation> prepareList() {
        ArrayList<ReactionRepresentation> list = new ArrayList<ReactionRepresentation>();
        for (Reaction r : api.reactions().list()) {
            list.add(new ReactionRepresentation(r));
        }
        return list;
    }

    @Override
    protected ReactionRepresentation prepareSingle(String uuid) {
        Reaction r = api.reactions().get(uuid);
        if (r != null){
            return new ReactionRepresentation(r);
        }
        return null;
    }

}

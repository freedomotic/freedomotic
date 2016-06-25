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
package com.freedomotic.plugins.devices.restapiv3.resources.jersey;

import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.reactions.Reaction;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.Path;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("OLDreactions")
//@Api(value = "/reactions", description = "Operations on reactions", position=3)
@Deprecated
public class OldReactionResource extends AbstractResource<Reaction> {

    @Override
    protected URI doCreate(Reaction o) throws URISyntaxException {
        api.reactions().create(o);
        return createUri(o.getUuid());
    }

    @Override
    protected boolean doDelete(String UUID) {
        Reaction r = api.reactions().findOne(UUID);
        if (r != null) {
            api.reactions().delete(r);
            return true;
        }
        return false;
    }

    @Override
    protected Reaction doUpdate(String uuid, Reaction o) {
        o.setUuid(uuid);
        return api.reactions().modify(uuid, o);
    }

    @Override
    protected List<Reaction> prepareList() {
        return api.reactions().findAll();
    }

    @Override
    protected Reaction prepareSingle(String uuid) {
        return api.reactions().findOne(uuid);
    }

    @Override
    protected URI doCopy(String UUID) {
        Reaction found = api.reactions().findOne(UUID);
        Reaction r = api.reactions().copy(found);
        return createUri(UUID);
    }

}

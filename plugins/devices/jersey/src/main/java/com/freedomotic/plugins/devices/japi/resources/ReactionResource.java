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
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionPersistence;
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
@Path("reactions")
@Api(value = "/reactions", description = "Operations on reactions", position=3)
public class ReactionResource extends AbstractResource<Reaction>{

    @Override
    protected URI doCreate(Reaction o) throws URISyntaxException {
        ReactionPersistence.add(o);
        return UriBuilder.fromResource(ReactionResource.class).path(o.getUUID()).build();
    }

    @Override
    protected boolean doDelete(String UUID) {
        Reaction r = ReactionPersistence.getReaction(UUID);
        if (r !=null){
            ReactionPersistence.remove(r);
            return true;
        }
        return false;
    }

    @Override
    protected Reaction doUpdate(Reaction o) {
        Reaction r = ReactionPersistence.getReaction(o.getUUID());
        if (r != null){
            ReactionPersistence.remove(r);
            ReactionPersistence.add(o);
            return o;
        } else {
            return null;
        }
    }

    @Override
    protected List<Reaction> prepareList() {
        return ReactionPersistence.getReactions();
    }

    @Override
    protected Reaction prepareSingle(String uuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected URI doCopy(String UUID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

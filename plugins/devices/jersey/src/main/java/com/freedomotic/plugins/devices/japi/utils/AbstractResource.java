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
package com.freedomotic.plugins.devices.japi.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;


/**
 *
 * @author matteo
 * @param <T>
 */
public abstract class AbstractResource<T> implements AnnotatedResourceInterface<T> {
    
    private static final Logger LOG = Logger.getLogger(AbstractResource.class.getName());
    
    @Override
    public Response list() {
        return Response.ok(prepareList()).build();
    }

    @Override
    public Response get(String UUID) {
        return Response.ok(prepareSingle(UUID)).build();
    }

    @Override
    public Response update(T s) {
        try {
        return Response.ok(doUpdate(s)).build();
        }
        catch (Exception e){
            LOG.severe(e.getMessage());
            return Response.notModified(e.getMessage()).build();
        }
    }

    @Override
    public Response create(T s) throws URISyntaxException{
        try {
        return Response.created(doCreate(s)).build();
        } catch (Exception e){
            LOG.severe(e.getMessage());
            return Response.serverError().build(); 
        }
    }

    @Override
    public Response delete(String UUID) {
        if (doDelete(UUID)){
            return Response.ok().build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    
    abstract protected URI doCreate(T o) throws URISyntaxException;
    abstract protected boolean doDelete(String UUID);
    abstract protected T doUpdate(T o);
    
    abstract protected List<T> prepareList();
    abstract protected T prepareSingle(String uuid);
    
}

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

import com.freedomotic.exceptions.DaoLayerException;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.objects.EnvObjectFactory;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import com.wordnik.swagger.annotations.Api;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("objects")
@Api(value = "/objects", description = "Operations on environment objects", position = 2)
public class ObjectResource extends AbstractResource<EnvObject> {

    private String envUUID = null;
    private String roomName = null;

    public ObjectResource() {
    }

    public ObjectResource(String envUUID) {
        this.envUUID = envUUID;
    }

    public ObjectResource(String envUUID, String room) {
        this.envUUID = envUUID;
        this.roomName = room;
    }

    @Override
    protected List<EnvObject> prepareList() {
        List<EnvObject> objects = new ArrayList<EnvObject>();
        if (envUUID == null) {
            for (EnvObjectLogic objLogic : api.objects().list()) {
                objects.add(objLogic.getPojo());
            }
        } else {

            for (EnvObjectLogic objLogic : api.objects().getObjectByEnvironment(envUUID)) {
                objects.add(objLogic.getPojo());
            }
        }
        return objects;
    }

    @Override
    protected EnvObject prepareSingle(String uuid) {
        EnvObjectLogic el = api.objects().get(uuid);
        if (el != null) {
            return el.getPojo();
        } else {
            return null;
        }
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.objects().delete(UUID);
    }

    @Override
    protected EnvObject doUpdate(EnvObject eo) {
        try {
            EnvObjectLogic el = EnvObjectFactory.create(eo);
            if (api.objects().modify(eo.getUUID(), el) != null) {
                return eo;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot modify object " + eo.getName(), e);
            return null;
        }

    }

    @Override
    protected URI doCreate(EnvObject eo) throws URISyntaxException {
        EnvObjectLogic el;
        try {
            el = EnvObjectFactory.create(eo);
            api.objects().create(el);
            return createUri(el.getPojo().getUUID());
        } catch (DaoLayerException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;

    }

    @Override
    protected URI doCopy(String UUID) {
        return createUri(api.objects().copy(UUID).getPojo().getUUID());
    }

}

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

import com.freedomotic.api.Client;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.exceptions.DaoLayerException;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.objects.EnvObjectFactory;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    @POST
    @Path("/{id}/click")
    @ApiOperation(value = "Sends a ObjectClickEvent for related object")
    public Response click(
            @ApiParam(value = "ID of item to click", required = true)
            @PathParam("id") String UUID) {
        try {
            EnvObjectLogic el = api.objects().get(UUID);
            ObjectReceiveClick event = new ObjectReceiveClick(this, el, ObjectReceiveClick.SINGLE_CLICK);
            Freedomotic.sendEvent(event);
            return Response.accepted().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
    
    
    private static final ClientStorage clientStorage = Freedomotic.INJECTOR.getInstance(ClientStorage.class);

    @GET
    @Path("/templates")
    @ApiOperation(value = "Lists available object templates")
    public Response listTemplates() {
        List<EnvObject> templates = new ArrayList<EnvObject>();
        for (Client c : clientStorage.getClients("object")) {
            ObjectPluginPlaceholder opp = (ObjectPluginPlaceholder) c;
            templates.add(opp.getObject().getPojo());
        }
        return Response.ok(templates).build();
    }

    @POST
    @Path("/templates/{name}/instantiate")
    @ApiOperation(value = "Add a new object, based on selected template.")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Template not found"),
        @ApiResponse(code = 201, message = "Creation started")
    })
    public Response instantiateTemplate(
            @ApiParam(value = "Name of object template", required = true)
            @PathParam("name") String name) {
        for (Client c : clientStorage.getClients("object")) {
            if (c.getName().equalsIgnoreCase(name)) {
                c.start();
                return Response.accepted().build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     *
     * @param UUID
     * @return
     */
    @Path("/{id}/behaviors")
    public BehaviorResource behaviors(
            @ApiParam(value = "Object to fetch behaviors from", required = true)
            @PathParam("id") String UUID) {
        return new BehaviorResource(UUID);
    }
    
    @Path("behaviors")
    @Api(value = "behaviors", description = "Operations on object's behaviors")
    private class BehaviorResource extends AbstractResource<Behavior> {

        final private String objUUID;
        final private EnvObjectLogic obj;
        
        public BehaviorResource(String objUUID) {
            this.objUUID = objUUID;
            this.obj = api.getObjectByUUID(objUUID);
        }
        

        @Override
        protected URI doCopy(String UUID) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected URI doCreate(Behavior o) throws URISyntaxException {
            obj.getPojo().getBehaviors().add(o);
            obj.init();
            return createUri(o.getName());
        }

        @Override
        protected boolean doDelete(String behName) {
            obj.getPojo().getBehaviors().remove(obj.getPojo().getBehavior(behName));
            obj.init();
            return true;
        }

        @Override
        protected Behavior doUpdate(Behavior o) {
            obj.getPojo().getBehaviors().remove(obj.getPojo().getBehavior(o.getName()));
            obj.getPojo().getBehaviors().add(o);
            obj.init();
            return obj.getPojo().getBehavior(o.getName());
        }

        @Override
        protected List<Behavior> prepareList() {
           return obj.getPojo().getBehaviors();
        }

        @Override
        protected Behavior prepareSingle(String name) {
            return obj.getPojo().getBehavior(name);
        }
        
    }
}

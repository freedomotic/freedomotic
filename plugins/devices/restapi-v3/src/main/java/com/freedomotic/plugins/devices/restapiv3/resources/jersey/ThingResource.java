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

import com.freedomotic.api.Client;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.reactions.Command;
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
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("things")
@Api(value = "/things", description = "Operations on Things", position = 2)
public class ThingResource extends AbstractResource<EnvObject> {

    private String envUUID = null;
    private String roomUUID = null;

    public ThingResource() {
        authContext = "things";
    }

    public ThingResource(String envUUID) {
        this.envUUID = envUUID;
    }

    public ThingResource(String envUUID, String room) {
        this.envUUID = envUUID;
        this.roomUUID = room;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all things", position = 10)
    @Override
    public Response list() {
        return super.list();
    }

    /**
     * @param UUID
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a thing", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Thing not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "UUID of thing to fetch (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete a thing", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Thing not found")
    })
    public Response delete(
            @ApiParam(value = "UUID of thing to delete (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.delete(UUID);
    }

    /**
     *
     * @param UUID
     * @param s
     * @return
     */
    @Override
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 304, message = "Thing not modified")
    })
    @ApiOperation(value = "Update a thing", position = 40)
    public Response update(
            @ApiParam(value = "UUID of thing to update (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID, EnvObject s) {
        return super.update(UUID, s);
    }

    /**
     *
     * @param s
     * @return
     * @throws URISyntaxException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add a new thing", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New thing created")
    })
    @Override
    public Response create(EnvObject s) throws URISyntaxException {
        return super.create(s);
    }

    @Override
    protected List<EnvObject> prepareList() {
        List<EnvObject> objects = new ArrayList<EnvObject>();
        if (envUUID == null) {
            for (EnvObjectLogic objLogic : api.things().findAll()) {
                objects.add(objLogic.getPojo());
            }
        } else {
            if (roomUUID != null && !roomUUID.isEmpty()) {
                objects.addAll(api.environments().findOne(envUUID).getZoneByUuid(roomUUID).getPojo().getObjects());
            } else {
                for (EnvObjectLogic objLogic : api.things().findByEnvironment(envUUID)) {
                    objects.add(objLogic.getPojo());
                }
            }
        }
        return objects;
    }

    @Override
    protected EnvObject prepareSingle(String uuid) {
        EnvObjectLogic el = api.things().findOne(uuid);
        if (el != null) {
            return el.getPojo();
        } else {
            return null;
        }
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.things().delete(UUID);
    }

    @Override
    protected EnvObject doUpdate(String uuid, EnvObject eo) {
        try {
            eo.setUUID(uuid);
            EnvObjectLogic el = api.thingsFactory().create(eo);
            if (api.things().modify(uuid, el) != null) {
                return eo;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.error("Cannot modify thing {}", eo.getName(), e);
            return null;
        }

    }

    @Override
    protected URI doCreate(EnvObject eo) throws URISyntaxException {
        EnvObjectLogic el;
        try {
            el = api.thingsFactory().create(eo);
            api.things().create(el);
            return createUri(el.getPojo().getUUID());
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage());
        }
        return null;

    }

    @Override
    protected URI doCopy(String UUID) {
        EnvObjectLogic thing = api.things().findOne(UUID);
        EnvObjectLogic thingCopy = api.things().copy(thing);
        return createUri(thingCopy.getPojo().getUUID());
    }

    @POST
    @Path("/{id}/click")
    @ApiOperation(value = "Send a ObjectClickEvent for related thing")
    public Response click(
            @ApiParam(value = "UUID of thing to click", required = true)
            @PathParam("id") String UUID) {
        try {
            EnvObjectLogic el = api.things().findOne(UUID);
            ObjectReceiveClick event = new ObjectReceiveClick(this, el, ObjectReceiveClick.SINGLE_CLICK);
            Freedomotic.sendEvent(event);
            return Response.accepted().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/{id}/move/{x}/{y}")
    @ApiOperation(value = "Move a thing to another position")
    public Response move(
            @ApiParam(value = "UUID of thing to move", required = true) @PathParam("id") String UUID,
            @ApiParam(value = "Left offset", required = true) @PathParam("x") int x,
            @ApiParam(value = "Top offset", required = true) @PathParam("y") int y) {

        EnvObjectLogic el = api.things().findOne(UUID);
        try {
            el.setLocation(x, y);
            return Response.accepted().build();
        } catch (Exception e) {
            el.synchLocation(x, y);
            return Response.accepted().build();
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/behaviorchange/{bid}/{value}")
    @ApiOperation("Fire a behavior change request, using provided data")
    public Response behaviorChange(
            @ApiParam(value = "UUID of thing to click", required = true)
            @PathParam("id") String UUID,
            @ApiParam(value = "name of behavior", required = true)
            @PathParam("bid") String behavior,
            @ApiParam(value = "desired value of behavior", required = true)
            @PathParam("value") String value) {
        if (prepareSingle(UUID) != null) {
            Command c = new Command();
            c.setReceiver("app.events.sensors.behavior.request.objects");
            c.getProperties().setProperty(Command.PROPERTY_BEHAVIOR, behavior);
            c.getProperties().setProperty("value", value);
            c.getProperties().setProperty("object", prepareSingle(UUID).getName());

            Freedomotic.sendCommand(c);
            return Response.accepted().build();
        }
        throw new ItemNotFoundException();
    }

    private static final ClientStorage clientStorage = INJECTOR.getInstance(ClientStorage.class
    );

    @GET
    @Path("/templates")
    @ApiOperation(value = "List all thing templates")
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
    @ApiOperation(value = "Add a new thing, based on selected template")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Template not found"),
        @ApiResponse(code = 201, message = "Thing added")
    })
    public Response instantiateTemplate(
            @ApiParam(value = "Name of thing template (e.g. Light, Thermostat)", required = true)
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
            @ApiParam(value = "UUID of thing to fetch behaviors from", required = true)
            @PathParam("id") String UUID) {
        return new BehaviorResource(UUID);
    }

    //@Path("behaviors")
    @Api(value = "behaviors", description = "Operations on thing's behaviors")
    private class BehaviorResource extends AbstractResource<Behavior> {

        final private String objUUID;
        final private EnvObjectLogic obj;

        public BehaviorResource(String objUUID) {
            this.objUUID = objUUID;
            this.obj = api.things().findOne(objUUID);
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
        protected Behavior doUpdate(String uuid, Behavior o) {
            obj.getPojo().getBehaviors().remove(obj.getPojo().getBehavior(uuid));
            o.setName(uuid);
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

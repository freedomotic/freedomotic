/**
 *
 * Copyright (c) 2009-2018 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.restapiv3.resources.atmosphere;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.freedomotic.api.Client;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Plugin;
import com.freedomotic.events.CommandHasChanged;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.ReactionHasChanged;
import com.freedomotic.events.TriggerHasChanged;
import com.freedomotic.events.ZoneHasChanged;
import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.freedomotic.plugins.devices.restapiv3.representations.MessageCalloutRepresentation;
import com.freedomotic.things.EnvObjectLogic;
import com.wordnik.swagger.annotations.Api;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
@Path(AtmosphereEventResource.PATH)
@Api(value = "ws_event", description = "WS for receiving event notifications", position = 50)
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class},
        path = "/" + RestAPIv3.API_VERSION + "/ws/" + AtmosphereEventResource.PATH,
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class AtmosphereEventResource extends AbstractWSResource {

    private static final Logger LOG = LoggerFactory.getLogger(AtmosphereEventResource.class.getName());
    public final static String PATH = "event";

    @Inject
    private BroadcasterFactory factory;

    @Override
    public void broadcast(EventTemplate message) {
        if (api != null) {
            String msgType = "";
            String payload = "";

            if (message instanceof ObjectHasChangedBehavior) {
                if (api != null) {
                    EnvObjectLogic obj = api.things().findOne(message.getPayload().getStatementValue("object.uuid"));
                    if (obj == null) {
                        payload = "{}";
                    } else {
                        try {
                            payload = om.writeValueAsString(obj.getPojo());
                        } catch (JsonProcessingException ex) {
                            LOG.error("Error processing Json data", ex);
                        }
                    }
                }
                msgType = "object-changed";
            } else if (message instanceof CommandHasChanged) {
                switch (message.getProperty("command.action")) {
                    case "ADD":
                        msgType = "command-added";
                        break;

                    case "REMOVE":
                        msgType = "command-removed";
                        break;

                    case "MODIFY":
                        msgType = "command-edited";
                        break;
                }
            } else if (message instanceof ReactionHasChanged) {
                payload = message.getProperty("reaction.uuid");
                switch (message.getProperty("reaction.action")) {
                    case "ADD":
                        msgType = "reaction-added";
                        break;

                    case "REMOVE":
                        msgType = "reaction-removed";
                        break;

                    case "MODIFY":
                        msgType = "reaction-edited";
                        break;
                }
            } else if (message instanceof TriggerHasChanged) {
                switch (message.getProperty("trigger.action")) {
                    case "ADD":
                        msgType = "trigger-added";
                        break;

                    case "REMOVE":
                        msgType = "trigger-removed";
                        break;

                    case "MODIFY":
                        msgType = "trigger-edited";
                        break;
                }
            } else if (message instanceof ZoneHasChanged) {
                msgType = "zone-changed";

            } else if (message instanceof PluginHasChanged) {
                payload = message.getPayload().getStatementValue("plugin.name");
                if (api != null) {
                    for (Client client : api.getClients("plugin")) {
                        Plugin plugin = (Plugin) client;
                        if (plugin.getName().equalsIgnoreCase(message.getPayload().getStatementValue("plugin.name"))) {
                            try {
                                payload = om.writeValueAsString(plugin);
                            } catch (JsonProcessingException ex) {
                                LOG.error("Error processing Json data", ex);
                            }
                        }
                    }
                }
                switch (message.getProperty("plugin.action")) {
                    case "START":
                        msgType = "plugin-started";
                        break;

                    case "STOP":
                        msgType = "plugin-stopped";
                        break;

                    case "ENQUEUE":
                        msgType = "plugin-installed";
                        break;

                    case "DEQUEUE":
                        msgType = "plugin-uninstalled";
                        break;
                }
            } else if (message instanceof MessageEvent) {
                try {
                    msgType = "message-callout";
                    payload = om.writeValueAsString(new MessageCalloutRepresentation(message.getProperty("message.text")));
                } catch (JsonProcessingException ex) {
                    LOG.error("Error processing Json data", ex);
                }
            }
            // broadcast message
            factory
                    .lookup("/" + RestAPIv3.API_VERSION + "/ws/" + AtmosphereEventResource.PATH)
                    .broadcast(msgType + "#" + payload);
        }
    }
}

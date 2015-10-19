/*
 Copyright (c) Matteo Mazzoni 2013  

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
package com.freedomotic.plugins.devices.roomevents;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.rules.Payload;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

public class RoomEvents extends Protocol {

    final int POLLING_WAIT;
    CommandRepository cp;

    public RoomEvents() {
        //every plugin needs a name and a manifest XML file
        super("RoomEvents", "/room-events/room-events-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", -1);
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
        this.setName("RoomEvents");
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        LOG.info("RoomEvents plugin is started");
        this.setDescription("Starting...");
        this.cp = getApi().commands();
        addEventListener("app.event.sensor.object.behavior.change");
        addEnvCommands();
        addRoomCommands();
        this.setDescription("Started");
    }

    @Override
    protected void onStop() {
        this.setDescription("Stopped");
        LOG.info("RoomEvents plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {

    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        LOG.info("ROOMEVENT: received event " + event.toString());
        // just to see what properties you are receiving
        // do here what you have now in onCommand and send your room status event
        // search related room

        EnvObjectLogic object = getApi().things().findOne(event.getProperty("object.uuid"));

        boolean found = false;
        for (Room z : object.getEnvironment().getRooms()) {
            for (EnvObject obj : z.getPojo().getObjects()) {
                if (obj.getUUID().equalsIgnoreCase(object.getPojo().getUUID())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                notifyRoomStatus(z);
                break;
            }
        }
    }

    private void notifyRoomStatus(Room z) {
        ProtocolRead event = new ProtocolRead(this, "roomevent", z.getPojo().getName());
        //   Freedomotic.logger.info(event.toString());
        int numLightsOn = 0;
        int totLights = 0;
        String roomName = z.getPojo().getName();
        for (EnvObject obj : z.getPojo().getObjects()) {
            if (obj.getType().equalsIgnoreCase("EnvObject.ElectricDevice.Light")) {
                totLights++;
                if (obj.getCurrentRepresentationIndex() == 1) {
                    numLightsOn++;
                }
            }
        }

        if (totLights != 0) {
            String amount = "";
            if (numLightsOn == 0) {
                amount = "no";
            } else if (numLightsOn == totLights) {
                amount = "all";
            } else {
                amount = "some";
            }

            event.addProperty("hasLightsOn", amount);
            event.addProperty("roomName", roomName);
            notifyEvent(event);
            // add and register this 
            String triggerName = "Room " + roomName + " has " + amount + " lights On";
            Trigger t;

            if (getApi().triggers().findByName(triggerName).isEmpty()) {
                t = new Trigger();
                t.setName(triggerName);
                t.setChannel(event);
                Payload p = new Payload();
                p.addStatement("hasLightsOn", amount);
                p.addStatement("roomName", roomName);
                t.setPayload(p);
                
                getApi().triggers().create(t);
            }
        }
    }

    private void addRoomCommands() {
        String cmdName;
        for (EnvironmentLogic env : getApi().environments().findAll()) {
            for (Zone z : env.getPojo().getZones()) {
                if (z.isRoom()) {
                    cmdName = "Turn off devices inside room " + z.getName();
                    for (Command c : cp.findByName(cmdName)) {
                        cp.delete(c);
                    }
                    Command c = new Command();
                    c.setReceiver("app.events.sensors.behavior.request.objects");
                    c.setName(cmdName);
                    c.setDescription(cmdName);
                    c.setProperty(Command.PROPERTY_OBJECT_ZONE, z.getName());
                    c.setProperty(Command.PROPERTY_BEHAVIOR, "powered");
                    c.setProperty("value", "false");
                    HashSet<String> tags = new HashSet<>();
                    tags.add("turn");
                    tags.add("off");
                    tags.add("room");
                    tags.add("devices");
                    tags.add(z.getName());
                    c.setTags(tags);
                    cp.create(c);
                }
            }
        }
    }

    private void addEnvCommands() {
        String cmdName;
        for (EnvironmentLogic env : getApi().environments().findAll()) {
            cmdName = "Turn off devices inside area " + env.getPojo().getName();
            for (Command c : cp.findByName(cmdName)) {
                if (c != null) {
                    cp.delete(c);
                }
            }
            Command c = new Command();
            c.setReceiver("app.events.sensors.behavior.request.objects");
            c.setName(cmdName);
            c.setDescription(cmdName);
            c.setProperty(Command.PROPERTY_OBJECT_ENVIRONMENT, env.getPojo().getName());
            c.setProperty(Command.PROPERTY_BEHAVIOR, "powered");
            c.setProperty("value", "false");
            HashSet<String> tags = new HashSet<>();
            tags.add("turn");
            tags.add("off");
            tags.add("area");
            tags.add("devices");
            tags.add(env.getPojo().getName());
            c.setTags(tags);
            cp.create(c);
        }
    }

    private static final Logger LOG = Logger.getLogger(RoomEvents.class.getName());
}

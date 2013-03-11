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
package it.mazzoni.roomevents;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.ListenEventsOn;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.Payload;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.TriggerPersistence;
import java.io.IOException;
import java.util.HashSet;

public class RoomEvents extends Protocol {

    final int POLLING_WAIT;

    public RoomEvents() {
        //every plugin needs a name and a manifest XML file
        super("RoomEvents", "/it.mazzoni.roomevents/room-events-manifest.xml");
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
        Freedomotic.logger.info("RoomEvents plugin is started");
        this.setDescription("Started");
        addRoomCommands();
    }

    @Override
    protected void onStop() {
        this.setDescription("Stopped");
        Freedomotic.logger.info("RoomEvents plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        // extract object
        String address[] = c.getProperty("address").split(":");
        String command = c.getProperty("command");

        if (command.equals("Turn off room devices")) {
            for (Zone z : Freedomotic.environment.getPojo().getZones()) {
                if (z.getName().equals(address[2])) {
                    for (EnvObject obj : z.getObjects()) {
                        if (obj.getType().startsWith(c.getProperty("devType"))) {
                            Command c2 = CommandPersistence.getCommand("Turn off " + obj.getName());
                            if (c2 != null) {
                                Freedomotic.sendCommand(c2);
                            }
                        }

                    }
                }
            }

        } else {
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void notifyRoomStatus(Zone z) {
        ProtocolRead event = new ProtocolRead(this, "roomevent", z.getName());
        //   Freedomotic.logger.info(event.toString());
        int numLightsOn = 0;
        int totLights = 0;
        for (EnvObject obj : z.getObjects()) {
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
            event.addProperty("roomName", z.getName());
            this.notifyEvent(event);
            // add and register this 
            String triggerName = "Room " + z.getName() + " has " + amount + " lights On";
            Trigger t = TriggerPersistence.getTrigger(triggerName);
            if (t == null) {
                t = new Trigger();
                t.setName(triggerName);
                t.setChannel(event);
                Payload p = new Payload();
                p.addStatement("hasLightsOn", amount);
                p.addStatement("roomName", z.getName());
                t.setPayload(p);

                TriggerPersistence.addAndRegister(t);
            }
        }
    }

    private void addRoomCommands() {
        String cmdName;

        for (Zone z : Freedomotic.environment.getPojo().getZones()) {
            if (z.isRoom()) {
                cmdName = "Turn off devices inside room " + z.getName();
                Command c = CommandPersistence.getCommand(cmdName);
                if (c != null) {
                    CommandPersistence.remove(c);
                }
                c = new Command();
                c.setReceiver("app.actuators.logging.roomevents.in");
                c.setName(cmdName);
                c.setDescription(cmdName);
                c.setProperty("address", "env:room:" + z.getName());
                c.setProperty("command", "Turn off room devices");
                c.setProperty("devType", "EnvObject.ElectricDevice");
                HashSet<String> tags = new HashSet<String>();
                tags.add("turn");
                tags.add("off");
                tags.add("room");
                tags.add("devices");
                tags.add(z.getName());
                c.setTags(tags);
                CommandPersistence.add(c);
            }
        }
    }
    //with this annotation a custom method can receive all events published on the channel in the argument

    @ListenEventsOn(channel = "app.event.sensor.object.behavior.change")
    public void onObjectStateChanges(EventTemplate event) {
        Freedomotic.logger.info("ROOMEVENT: received event " + event.toString()); //just to see what properties you are receiving
        //do here what you have now in onCommand and send your room status event
        // search related room
        String objName = event.getProperty("object.name");

        boolean found = false;
        for (Zone z : Freedomotic.environment.getPojo().getZones()) {
            if (z.isRoom()) {
                for (EnvObject obj : z.getObjects()) {
                    if (obj.getName().equalsIgnoreCase(objName)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                notifyRoomStatus(z);
                break;
            }
        }
    }
}
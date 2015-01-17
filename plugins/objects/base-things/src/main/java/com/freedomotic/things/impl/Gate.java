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
package com.freedomotic.things.impl;

import com.freedomotic.things.GenericGate;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.model.object.Representation;
import com.freedomotic.behaviors.BooleanBehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.util.TopologyUtils;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class Gate extends EnvObjectLogic implements GenericGate {
    //suppose from and to are always reflexive from->to; to->from

    private Room from;
    private Room to;

    /**
     *
     */
    protected RangedIntBehaviorLogic openness;

    /**
     *
     */
    protected BooleanBehaviorLogic open;

    /**
     *
     */
    protected final static String BEHAVIOR_OPEN = "open";

    /**
     *
     */
    protected final static String BEHAVIOR_OPENNESS = "openness";

    @Override
    public void init() {
        super.init();
        //linking this open property with the open behavior defined in the XML
        open = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior(BEHAVIOR_OPEN));
//        open.createCommands(this);
        open.addListener(new BooleanBehaviorLogic.Listener() {
            @Override
            public void onTrue(Config params, boolean fireCommand) {
                //open = true
                setOpen(params);
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                //open = false -> not open
                setClosed(params);
            }
        });

        //linking this property with the behavior defined in the XML
        openness = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_OPENNESS));
//        openness.createCommands(this);
        openness.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                //on value = 0
                setClosed(params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //on value = 100
                setOpen(params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                //on values between 1 to 99
                setOpeness(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(open);
        registerBehavior(openness);
        getPojo().setDescription("Connects no rooms");
        //evaluate witch rooms it connects (based on gate position)
        //the evaluation updates the gate description
        evaluateGate();
    }

    /**
     *
     * @param params
     */
    protected void setClosed(Config params) {
        boolean executed = executeCommand("close", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            open.setValue(false);
            //to mantain the object coerence
            openness.setValue(0);
            //set the light graphical representation
            getPojo().setCurrentRepresentation(0); //points to the first element in the XML views array (closed door)
            setChanged(true);
        }
    }

    /**
     *
     * @param params
     */
    protected void setOpen(Config params) {
        boolean executed = executeCommand("open", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            open.setValue(true);
            //to mantain the object coerence
            openness.setValue(100);
            //set the light graphical representation
            getPojo().setCurrentRepresentation(1); //points to the second element in the XML views array (open door)
            setChanged(true);
        }
    }

    /**
     *
     * @param rangeValue
     * @param params
     */
    protected void setOpeness(int rangeValue, Config params) {
        boolean executed = executeCommand("measured open", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            //here we never had 0 or 100
            open.setValue(true);
            //to mantain the object coerence
            openness.setValue(rangeValue);
            //set the light graphical representation
            getPojo().setCurrentRepresentation(2); //points to the second element in the XML views array (half open door)
            setChanged(true);
        }
    }

    /**
     *
     * @param value
     */
    @Override
    public final void setChanged(boolean value) {
        //update the room that can be reached
        for (EnvironmentLogic env : environmentRepository.findAll()) {
            for (ZoneLogic z : env.getZones()) {
                if (z instanceof Room) {
                    final Room room = (Room) z;
                    //the gate is opened or closed we update the reachable rooms
                    room.visit();
                }
            }

            for (ZoneLogic z : env.getZones()) {
                if (z instanceof Room) {
                    final Room room = (Room) z;
                    room.updateDescription();
                }
            }
        }

        //then executeCommand the super which notifies the event
        super.setChanged(true);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isOpen() {
        return open.getValue();
    }

    /**
     *
     * @return
     */
    @Override
    public Room getFrom() {
        return from;
    }

    /**
     *
     * @return
     */
    @Override
    public Room getTo() {
        return to;
    }

    /**
     *
     * @param x
     * @param y
     */
    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        evaluateGate();
    }

    /**
     *
     */
    @Override
    public void evaluateGate() {
        //checks the intersection with the first view in the list
        //others views are ignored!!!
        Representation representation = getPojo().getRepresentations().get(0);
        FreedomPolygon pojoShape = (FreedomPolygon) representation.getShape();
        int xoffset = representation.getOffset().getX();
        int yoffset = representation.getOffset().getY();
        from = null;
        to = null;

        //REGRESSION
        FreedomPolygon objShape =
                TopologyUtils.rotate(TopologyUtils.translate(pojoShape, xoffset, yoffset),
                (int) representation.getRotation());
        EnvironmentLogic env = environmentRepository.findOne(getPojo().getEnvironmentID());

        if (env != null) {
            for (Room room : env.getRooms()) {
                if (TopologyUtils.intersects(objShape,
                        room.getPojo().getShape())) {
                    if (from == null) {
                        from = (Room) room;
                        to = (Room) room;
                    } else {
                        to = (Room) room;
                    }
                }
            }
        } else {
            LOG.severe("The gate '" + getPojo().getName()
                    + "' is not linked to any any environment");
        }

        if (to != from) {
            getPojo().setDescription("Connects " + from + " to " + to);
            from.addGate(this); //informs the room that it has a gate to another room
            to.addGate(this); //informs the room that it has a gate to another room
        } else {
            //the gate interects two equals zones
            if (from != null) {
                LOG.warning("The gate '" + getPojo().getName() + "' connects the same zones ["
                        + from.getPojo().getName() + "; " + to.getPojo().getName()
                        + "]. This is not possible.");
            }
        }

        //notify if the passage connect two rooms
        LOG.config("The gate '" + getPojo().getName() + "' connects " + from + " to " + to);
    }

    /**
     *
     */
    @Override
    protected void createCommands() {
        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " openness to 50%");
        a.setDescription("the " + getPojo().getName() + " changes its openness");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object",
                getPojo().getName());
        a.setProperty("behavior", BEHAVIOR_OPENNESS);
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName("Increase " + getPojo().getName() + " openness");
        b.setDescription("increases " + getPojo().getName() + " openness of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object",
                getPojo().getName());
        b.setProperty("behavior", BEHAVIOR_OPENNESS);
        b.setProperty("value", "next");

        Command c = new Command();
        c.setName("Decrease " + getPojo().getName() + " openness");
        c.setDescription("decreases " + getPojo().getName() + " openness of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object",
                getPojo().getName());
        c.setProperty("behavior", BEHAVIOR_OPENNESS);
        c.setProperty("value", "previous");

        Command d = new Command();
        d.setName("Set its openness to 50%");
        d.setDescription("set its openness to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", BEHAVIOR_OPENNESS);
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName("Increase its openness");
        e.setDescription("increases its openness of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", BEHAVIOR_OPENNESS);
        e.setProperty("value", "next");

        Command f = new Command();
        f.setName("Decrease its openness");
        f.setDescription("decreases its openness of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", BEHAVIOR_OPENNESS);
        f.setProperty("value", "previous");

        Command g = new Command();
        g.setName("Set its openness to the value in the event");
        g.setDescription("set its openness to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", BEHAVIOR_OPENNESS);
        g.setProperty("value", "@event.value");

        Command h = new Command();
        h.setName("Open " + getPojo().getName());
        h.setDescription(getPojo().getSimpleType() + " opens");
        h.setReceiver("app.events.sensors.behavior.request.objects");
        h.setProperty("object",
                getPojo().getName());
        h.setProperty("behavior", BEHAVIOR_OPEN);
        h.setProperty("value", "true");

        Command i = new Command();
        i.setName("Close " + getPojo().getName());
        i.setDescription(getPojo().getSimpleType() + " closes");
        i.setReceiver("app.events.sensors.behavior.request.objects");
        i.setProperty("object",
                getPojo().getName());
        i.setProperty("behavior", BEHAVIOR_OPEN);
        i.setProperty("value", "false");

        Command l = new Command();
        l.setName("Switch " + getPojo().getName() + " open state");
        l.setDescription("closes/opens " + getPojo().getName());
        l.setReceiver("app.events.sensors.behavior.request.objects");
        l.setProperty("object",
                getPojo().getName());
        l.setProperty("behavior", BEHAVIOR_OPEN);
        l.setProperty("value", "opposite");

        Command m = new Command();
        m.setName("Open this gate");
        m.setDescription("this gate is opened");
        m.setReceiver("app.events.sensors.behavior.request.objects");
        m.setProperty("object", "@event.object.name");
        m.setProperty("behavior", BEHAVIOR_OPEN);
        m.setProperty("value", "true");

        Command n = new Command();
        n.setName("Close this gate");
        n.setDescription("this gate is closed");
        n.setReceiver("app.events.sensors.behavior.request.objects");
        n.setProperty("object", "@event.object.name");
        n.setProperty("behavior",BEHAVIOR_OPEN);
        n.setProperty("value", "false");

        Command o = new Command();
        o.setName("Switch its open state");
        o.setDescription("opens/closes the gate in the event");
        o.setReceiver("app.events.sensors.behavior.request.objects");
        o.setProperty("object", "@event.object.name");
        o.setProperty("behavior", BEHAVIOR_OPEN);
        o.setProperty("value", "opposite");

        CommandPersistence.add(a);
        CommandPersistence.add(b);
        CommandPersistence.add(c);
        CommandPersistence.add(d);
        CommandPersistence.add(e);
        CommandPersistence.add(f);
        CommandPersistence.add(g);
        CommandPersistence.add(h);
        CommandPersistence.add(i);
        CommandPersistence.add(l);
        CommandPersistence.add(m);
        CommandPersistence.add(n);
        CommandPersistence.add(o);
    }

    /**
     *
     */
    @Override
    protected void createTriggers() {
        Trigger clicked = new Trigger();
        clicked.setName("When " + this.getPojo().getName() + " is clicked");
        clicked.setChannel("app.event.sensor.object.behavior.clicked");
        clicked.getPayload().addStatement("object.name",
                this.getPojo().getName());
        clicked.getPayload().addStatement("click", "SINGLE_CLICK");

        Trigger turnsOpen = new Trigger();
        turnsOpen.setName(this.getPojo().getName() + " becomes open");
        turnsOpen.setChannel("app.event.sensor.object.behavior.change");
        turnsOpen.getPayload().addStatement("object.name",
                this.getPojo().getName());
        turnsOpen.getPayload().addStatement("object.behavior."+BEHAVIOR_OPEN, "true");

        Trigger turnsClosed = new Trigger();
        turnsClosed.setName(this.getPojo().getName() + " becomes closed");
        turnsClosed.setChannel("app.event.sensor.object.behavior.change");
        turnsClosed.getPayload().addStatement("object.name",
                this.getPojo().getName());
        turnsClosed.getPayload().addStatement("object.behavior."+BEHAVIOR_OPEN, "false");

        TriggerPersistence.add(clicked);
        TriggerPersistence.add(turnsOpen);
        TriggerPersistence.add(turnsClosed);
    }
    private static final Logger LOG = Logger.getLogger(Gate.class.getName());
}

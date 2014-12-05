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
package com.freedomotic.things.impl;

import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.ListBehavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.behaviors.ListBehaviorLogic;
import com.freedomotic.things.impl.Light;
import com.freedomotic.things.impl.Light;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mauro Cicolella
 */
public class RGBLight
        extends Light {

    private RangedIntBehaviorLogic hue;
    private RangedIntBehaviorLogic saturation;
    protected final static String BEHAVIOR_HUE = "hue";
    protected final static String BEHAVIOR_SATURATION = "saturation";

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        hue = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_HUE));
        hue.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executePowerOff(params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executePowerOn(params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeHue(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(hue);

        saturation = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_SATURATION));
        saturation.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executePowerOff(params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executePowerOn(params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeSaturation(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(saturation);

        super.init();
    }

    @Override
    public void executePowerOff(Config params) {

        hue.setValue(0);
        saturation.setValue(0);
        /*
         * executeCommand the body of the super implementation The super call
         * must be the last call as it executes setChanged(true)
         */
        super.executePowerOff(params);
    }

    @Override
    public void executePowerOn(Config params) {
        /*
         * Not called the setBrightness because this method executeCommand a
         * command here we want only to mantain the system coerent. If we call
         * setBrightness(100, params) the light recalls the onUpperBoundValue.
         * Only ONE command execution per situation
         */
        //executeCommand the body of the super implementation
        super.executePowerOn(params);
    }

    public void executeHue(int rangeValue, Config params) {
        boolean executed = executeCommand("set hue", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            powered.setValue(true);
            hue.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void executeSaturation(int rangeValue, Config params) {
        boolean executed = executeCommand("set saturation", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            powered.setValue(true);
            saturation.setValue(rangeValue);
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " hue to 50%");
        a.setDescription("the light " + getPojo().getName() + " changes its hue");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", BEHAVIOR_HUE);
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName("Increase " + getPojo().getName() + " hue");
        b.setDescription("increases " + getPojo().getName() + " hue of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object",
                getPojo().getName());
        b.setProperty("behavior", BEHAVIOR_HUE);
        b.setProperty("value", Behavior.VALUE_NEXT);

        Command c = new Command();
        c.setName("Decrease " + getPojo().getName() + " hue");
        c.setDescription("decreases " + getPojo().getName() + " hue of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object",
                getPojo().getName());
        c.setProperty("behavior", BEHAVIOR_HUE);
        c.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command d = new Command();
        d.setName("Set its hue to 50%");
        d.setDescription("set its hue to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", BEHAVIOR_HUE);
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName("Increase its hue");
        e.setDescription("increases its hue of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", BEHAVIOR_HUE);
        e.setProperty("value", Behavior.VALUE_NEXT);

        Command f = new Command();
        f.setName("Decrease its hue");
        f.setDescription("decreases its hue of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", BEHAVIOR_HUE);
        f.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command g = new Command();
        g.setName("Set its hue to the value in the event");
        g.setDescription("set its hue to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", BEHAVIOR_HUE);
        g.setProperty("value", "@event.value");

        Command h = new Command();
        h.setName("Set " + getPojo().getName() + " saturation to 50%");
        h.setDescription("the light " + getPojo().getName() + " changes its saturation");
        h.setReceiver("app.events.sensors.behavior.request.objects");
        h.setProperty("object",
                getPojo().getName());
        h.setProperty("behavior", BEHAVIOR_SATURATION);
        h.setProperty("value", "50");

        Command i = new Command();
        i.setName("Increase " + getPojo().getName() + " saturation");
        i.setDescription("increases " + getPojo().getName() + " saturation of one step");
        i.setReceiver("app.events.sensors.behavior.request.objects");
        i.setProperty("object",
                getPojo().getName());
        i.setProperty("behavior", BEHAVIOR_SATURATION);
        i.setProperty("value", Behavior.VALUE_NEXT);

        Command l = new Command();
        l.setName("Decrease " + getPojo().getName() + " saturation");
        l.setDescription("decreases " + getPojo().getName() + " saturation of one step");
        l.setReceiver("app.events.sensors.behavior.request.objects");
        l.setProperty("object",
                getPojo().getName());
        l.setProperty("behavior", BEHAVIOR_SATURATION);
        l.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command m = new Command();
        m.setName("Set its saturation to 50%");
        m.setDescription("set its saturation to 50%");
        m.setReceiver("app.events.sensors.behavior.request.objects");
        m.setProperty("object", "@event.object.name");
        m.setProperty("behavior", BEHAVIOR_SATURATION);
        m.setProperty("value", "50");

        Command n = new Command();
        n.setName("Increase its saturation");
        n.setDescription("increases its saturation of one step");
        n.setReceiver("app.events.sensors.behavior.request.objects");
        n.setProperty("object", "@event.object.name");
        n.setProperty("behavior", BEHAVIOR_SATURATION);
        n.setProperty("value", Behavior.VALUE_NEXT);

        Command o = new Command();
        o.setName("Decrease its saturation");
        o.setDescription("decreases its saturation of one step");
        o.setReceiver("app.events.sensors.behavior.request.objects");
        o.setProperty("object", "@event.object.name");
        o.setProperty("behavior", BEHAVIOR_SATURATION);
        o.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command p = new Command();
        p.setName("Set its saturation to the value in the event");
        p.setDescription("set its saturation to the value in the event");
        p.setReceiver("app.events.sensors.behavior.request.objects");
        p.setProperty("object", "@event.object.name");
        p.setProperty("behavior", BEHAVIOR_SATURATION);
        p.setProperty("value", "@event.value");


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
        CommandPersistence.add(p);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

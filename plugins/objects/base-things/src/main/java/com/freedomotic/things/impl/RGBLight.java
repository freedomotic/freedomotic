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
package com.freedomotic.things.impl;

import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Command;

/**
 *
 * @author Mauro Cicolella
 */
public class RGBLight extends Light {

    private RangedIntBehaviorLogic red;
    private RangedIntBehaviorLogic green;
    private RangedIntBehaviorLogic blue;
    protected final static String BEHAVIOR_RED = "red";
    protected final static String BEHAVIOR_GREEN = "green";
    protected final static String BEHAVIOR_BLUE = "blue";

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        red = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_RED));
        red.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeRed(red.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeRed(red.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeRed(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(red);

        green = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_GREEN));
        green.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeGreen(green.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeGreen(green.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeGreen(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(green);

        blue = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_BLUE));
        blue.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeBlue(blue.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeBlue(blue.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeBlue(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(blue);

        super.init();
    }

    @Override
    public void executePowerOff(Config params) {

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

    public void executeRed(int rangeValue, Config params) {
        boolean executed = executeCommand("set red", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            powered.setValue(true);
            red.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void executeGreen(int rangeValue, Config params) {
        boolean executed = executeCommand("set green", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            powered.setValue(true);
            green.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void executeBlue(int rangeValue, Config params) {
        boolean executed = executeCommand("set blue", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            powered.setValue(true);
            blue.setValue(rangeValue);
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " red to 50%");
        a.setDescription("the light " + getPojo().getName() + " changes its red value");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", BEHAVIOR_RED);
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName("Increase " + getPojo().getName() + " red");
        b.setDescription("increases " + getPojo().getName() + " red of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object",
                getPojo().getName());
        b.setProperty("behavior", BEHAVIOR_RED);
        b.setProperty("value", Behavior.VALUE_NEXT);

        Command c = new Command();
        c.setName("Decrease " + getPojo().getName() + " red");
        c.setDescription("decreases " + getPojo().getName() + " red of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object",
                getPojo().getName());
        c.setProperty("behavior", BEHAVIOR_RED);
        c.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command d = new Command();
        d.setName("Set its red to 50%");
        d.setDescription("set its red to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", BEHAVIOR_RED);
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName("Increase its red");
        e.setDescription("increases its red of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", BEHAVIOR_RED);
        e.setProperty("value", Behavior.VALUE_NEXT);

        Command f = new Command();
        f.setName("Decrease its red");
        f.setDescription("decreases its red of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", BEHAVIOR_RED);
        f.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command g = new Command();
        g.setName("Set its red to the value in the event");
        g.setDescription("set its red to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", BEHAVIOR_RED);
        g.setProperty("value", "@event.value");

        Command h = new Command();
        h.setName("Set " + getPojo().getName() + " green to 50%");
        h.setDescription("the light " + getPojo().getName() + " changes its green value");
        h.setReceiver("app.events.sensors.behavior.request.objects");
        h.setProperty("object",
                getPojo().getName());
        h.setProperty("behavior", BEHAVIOR_GREEN);
        h.setProperty("value", "50");

        Command i = new Command();
        i.setName("Increase " + getPojo().getName() + " green");
        i.setDescription("increases " + getPojo().getName() + " green of one step");
        i.setReceiver("app.events.sensors.behavior.request.objects");
        i.setProperty("object",
                getPojo().getName());
        i.setProperty("behavior", BEHAVIOR_GREEN);
        i.setProperty("value", Behavior.VALUE_NEXT);

        Command l = new Command();
        l.setName("Decrease " + getPojo().getName() + " green");
        l.setDescription("decreases " + getPojo().getName() + " green of one step");
        l.setReceiver("app.events.sensors.behavior.request.objects");
        l.setProperty("object",
                getPojo().getName());
        l.setProperty("behavior", BEHAVIOR_GREEN);
        l.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command m = new Command();
        m.setName("Set its green to 50%");
        m.setDescription("set its green to 50%");
        m.setReceiver("app.events.sensors.behavior.request.objects");
        m.setProperty("object", "@event.object.name");
        m.setProperty("behavior", BEHAVIOR_GREEN);
        m.setProperty("value", "50");

        Command n = new Command();
        n.setName("Increase its green");
        n.setDescription("increases its green of one step");
        n.setReceiver("app.events.sensors.behavior.request.objects");
        n.setProperty("object", "@event.object.name");
        n.setProperty("behavior", BEHAVIOR_GREEN);
        n.setProperty("value", Behavior.VALUE_NEXT);

        Command o = new Command();
        o.setName("Decrease its green");
        o.setDescription("decreases its green of one step");
        o.setReceiver("app.events.sensors.behavior.request.objects");
        o.setProperty("object", "@event.object.name");
        o.setProperty("behavior", BEHAVIOR_GREEN);
        o.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command p = new Command();
        p.setName("Set its green to the value in the event");
        p.setDescription("set its green to the value in the event");
        p.setReceiver("app.events.sensors.behavior.request.objects");
        p.setProperty("object", "@event.object.name");
        p.setProperty("behavior", BEHAVIOR_GREEN);
        p.setProperty("value", "@event.value");

        Command q = new Command();
        q.setName("Set " + getPojo().getName() + " blue to 50%");
        q.setDescription("the light " + getPojo().getName() + " changes its blue value");
        q.setReceiver("app.events.sensors.behavior.request.objects");
        q.setProperty("object",
                getPojo().getName());
        q.setProperty("behavior", BEHAVIOR_BLUE);
        q.setProperty("value", "50");

        Command r = new Command();
        r.setName("Increase " + getPojo().getName() + " blue");
        r.setDescription("increases " + getPojo().getName() + " blue of one step");
        r.setReceiver("app.events.sensors.behavior.request.objects");
        r.setProperty("object",
                getPojo().getName());
        r.setProperty("behavior", BEHAVIOR_BLUE);
        r.setProperty("value", Behavior.VALUE_NEXT);

        Command s = new Command();
        s.setName("Decrease " + getPojo().getName() + " blue");
        s.setDescription("decreases " + getPojo().getName() + " blue of one step");
        s.setReceiver("app.events.sensors.behavior.request.objects");
        s.setProperty("object",
                getPojo().getName());
        s.setProperty("behavior", BEHAVIOR_BLUE);
        s.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command t = new Command();
        t.setName("Set its blue to 50%");
        t.setDescription("set its blue to 50%");
        t.setReceiver("app.events.sensors.behavior.request.objects");
        t.setProperty("object", "@event.object.name");
        t.setProperty("behavior", BEHAVIOR_BLUE);
        t.setProperty("value", "50");

        Command u = new Command();
        u.setName("Increase its blue");
        u.setDescription("increases its blue of one step");
        u.setReceiver("app.events.sensors.behavior.request.objects");
        u.setProperty("object", "@event.object.name");
        u.setProperty("behavior", BEHAVIOR_BLUE);
        u.setProperty("value", Behavior.VALUE_NEXT);

        Command v = new Command();
        v.setName("Decrease its blue");
        v.setDescription("decreases its blue of one step");
        v.setReceiver("app.events.sensors.behavior.request.objects");
        v.setProperty("object", "@event.object.name");
        v.setProperty("behavior", BEHAVIOR_BLUE);
        v.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command z = new Command();
        z.setName("Set its blue to the value in the event");
        z.setDescription("set its blue to the value in the event");
        z.setReceiver("app.events.sensors.behavior.request.objects");
        z.setProperty("object", "@event.object.name");
        z.setProperty("behavior", BEHAVIOR_BLUE);
        z.setProperty("value", "@event.value");

        commandRepository.create(a);
        commandRepository.create(b);
        commandRepository.create(c);
        commandRepository.create(d);
        commandRepository.create(e);
        commandRepository.create(f);
        commandRepository.create(g);
        commandRepository.create(h);
        commandRepository.create(i);
        commandRepository.create(l);
        commandRepository.create(m);
        commandRepository.create(n);
        commandRepository.create(o);
        commandRepository.create(p);
        commandRepository.create(q);
        commandRepository.create(r);
        commandRepository.create(s);
        commandRepository.create(t);
        commandRepository.create(u);
        commandRepository.create(v);
        commandRepository.create(z);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

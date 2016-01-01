/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
public class RGBWLight extends RGBLight {

    private RangedIntBehaviorLogic white;
    protected final static String BEHAVIOR_WHITE = "white";
 
    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        white = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_WHITE));
        white.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeWhite(white.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeWhite(white.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeWhite(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(white);
    

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

    public void executeWhite(int rangeValue, Config params) {
        boolean executed = executeCommand("set white", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            powered.setValue(true);
            white.setValue(rangeValue);
            setChanged(true);
        }
    }

   
    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " white to 50%");
        a.setDescription("the light " + getPojo().getName() + " changes its white value");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", BEHAVIOR_WHITE);
        a.setProperty("value", "127");

        Command b = new Command();
        b.setName("Increase " + getPojo().getName() + " white");
        b.setDescription("increases " + getPojo().getName() + " white of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object",
                getPojo().getName());
        b.setProperty("behavior", BEHAVIOR_WHITE);
        b.setProperty("value", Behavior.VALUE_NEXT);

        Command c = new Command();
        c.setName("Decrease " + getPojo().getName() + " white");
        c.setDescription("decreases " + getPojo().getName() + " white of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object",
                getPojo().getName());
        c.setProperty("behavior", BEHAVIOR_WHITE);
        c.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command d = new Command();
        d.setName("Set its white to 50%");
        d.setDescription("set its white to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", BEHAVIOR_WHITE);
        d.setProperty("value", "127");

        Command e = new Command();
        e.setName("Increase its white");
        e.setDescription("increases its white of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", BEHAVIOR_WHITE);
        e.setProperty("value", Behavior.VALUE_NEXT);

        Command f = new Command();
        f.setName("Decrease its white");
        f.setDescription("decreases its white of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", BEHAVIOR_WHITE);
        f.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command g = new Command();
        g.setName("Set its white to the value in the event");
        g.setDescription("set its white to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", BEHAVIOR_WHITE);
        g.setProperty("value", "@event.value");

       
        commandRepository.create(a);
        commandRepository.create(b);
        commandRepository.create(c);
        commandRepository.create(d);
        commandRepository.create(e);
        commandRepository.create(f);
        commandRepository.create(g);
        
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

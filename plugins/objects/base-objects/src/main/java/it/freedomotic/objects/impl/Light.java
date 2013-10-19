/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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


package it.freedomotic.objects.impl;

import com.google.inject.Inject;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.Behavior;
import it.freedomotic.model.object.RangedIntBehavior;
import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.objects.RangedIntBehaviorLogic;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.util.I18n.I18n;


/**
 *
 * @author Enrico
 */

public class Light
        extends ElectricDevice {

    private RangedIntBehaviorLogic brightness;
    protected final static String BEHAVIOR_BRIGHTNESS = "brightness";

    @Inject
    private I18n I18n;

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        brightness = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_BRIGHTNESS));
        brightness.addListener(new RangedIntBehaviorLogic.Listener() {
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
                setBrightness(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(brightness);
        super.init();
    }

    @Override
    public void executePowerOff(Config params) {
        /*
         * Not called the setBrightness because this method executeCommand a
         * command here we want only to mantain the system coerent. If we call
         * setBrightness(0, params) the light recalls the onLowerBoundValue.
         * Only ONE command execution per situation
         */
        brightness.setValue(0);
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
        brightness.setValue(100);
        //executeCommand the body of the super implementation
        super.executePowerOn(params);
    }

    public void setBrightness(int rangeValue, Config params) {
        boolean executed = executeCommand("set brightness", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            powered.setValue(true);
            brightness.setValue(rangeValue);
            //set the light graphical representation
            getPojo().setCurrentRepresentation(1); //points to the second element in the XML views array (light on image)
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName(I18n.msg("set_X_brightness_to_50", new Object[]{this.getPojo().getName()}));
        a.setDescription("the light " + getPojo().getName() + " changes its brightness");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object",
                getPojo().getName());
        a.setProperty("behavior", BEHAVIOR_BRIGHTNESS);
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName(I18n.msg("increase_X_brightness", new Object[]{this.getPojo().getName()}));
        b.setDescription("increases " + getPojo().getName() + " brigthness of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object",
                getPojo().getName());
        b.setProperty("behavior", BEHAVIOR_BRIGHTNESS);
        b.setProperty("value", Behavior.VALUE_NEXT);

        Command c = new Command();
        c.setName(I18n.msg("decrease_X_brightness", new Object[]{this.getPojo().getName()}));
        c.setDescription("decreases " + getPojo().getName() + " brigthness of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object",
                getPojo().getName());
        c.setProperty("behavior", BEHAVIOR_BRIGHTNESS);
        c.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command d = new Command();
        d.setName(I18n.msg("set_its_brightness_to_50"));
        d.setDescription(I18n.msg("set_its_brightness_to_50"));
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", BEHAVIOR_BRIGHTNESS);
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName(I18n.msg("increase_its_brightness"));
        e.setDescription("increases its brigthness of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", BEHAVIOR_BRIGHTNESS);
        e.setProperty("value", Behavior.VALUE_NEXT);

        Command f = new Command();
        f.setName(I18n.msg("decrease_its_brightness"));
        f.setDescription("decreases its brigthness of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", BEHAVIOR_BRIGHTNESS);
        f.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command g = new Command();
        g.setName(I18n.msg("set_brightness_from_event_value"));
        g.setDescription("set its brighness to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", BEHAVIOR_BRIGHTNESS);
        g.setProperty("value", "@event.value");

        CommandPersistence.add(a);
        CommandPersistence.add(b);
        CommandPersistence.add(c);
        CommandPersistence.add(d);
        CommandPersistence.add(e);
        CommandPersistence.add(f);
        CommandPersistence.add(g);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

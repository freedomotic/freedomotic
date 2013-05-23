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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.objects.impl;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.events.ObjectReceiveClick;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.BooleanBehavior;
import it.freedomotic.objects.BooleanBehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.TriggerPersistence;

/**
 *
 * @author enrico
 */
public class ElectricDevice
        extends EnvObjectLogic {

    protected BooleanBehaviorLogic powered;
    protected final static String BEHAVIOR_POWERED = "powered";
    protected final static String ACTION_TURN_ON = "turn on";
    protected final static String ACTION_TURN_OFF = "turn off";

    @Override
    public void init() {
        powered = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehaviors().get(0));
        //add a listener to values changes
        powered.addListener(new BooleanBehaviorLogic.Listener() {
            @Override
            public void onTrue(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executePowerOn(params); //executes a turn on command and then sets the object behavior to on
                } else {
                    setOn(); //sets the object behavior to on as a result from a notified value
                }
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executePowerOff(params); //executes a turn off command and then sets the object behavior to off
                } else {
                    setOff(); //sets the object behavior to off as a result from a notified value
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(powered);
        //caches hardware level commands and builds user command for the Electric Devices
        super.init();
    }

    /**
     * Causes the execution of the related hardware command to turn on this
     * electric device, updates the object representation and notifies the
     * changes with an event.
     *
     * @param params
     */
    public void executePowerOn(Config params) {
        boolean executed = executeCommand(ACTION_TURN_ON, params);

        if (executed) {
            setOn();
        }
    }

    /**
     * Causes the execution of the related hardware command to turn off this
     * electric device, updates the object representation and notifies the
     * changes with an event.
     *
     * @param params
     */
    public void executePowerOff(Config params) {
        boolean executed = executeCommand(ACTION_TURN_OFF, params);

        if (executed) {
            setOff();
        }
    }

    private void setOn() {
        Freedomotic.logger.config("Setting behavior 'powered' of object '" + getPojo().getName() + "' to true");

        //if not already on
        if (powered.getValue() != true) {
            //setting the object as powered
            powered.setValue(true);
            //setting the second view from the XML list (the one with the on light bulb image)
            getPojo().setCurrentRepresentation(1);
            setChanged(true);
        }
    }

    private void setOff() {
        Freedomotic.logger.config("Setting behavior 'powered' of object '" + getPojo().getName() + "' to false");

        //if not already off
        if (powered.getValue() != false) {
            powered.setValue(false);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    /**
     * Creates user level commands for this class of freedomotic objects
     */
    @Override
    protected void createCommands() {
        Command setOn = new Command();
        setOn.setName("Turn on " + getPojo().getName());
        setOn.setDescription(getPojo().getName() + " turns on");
        setOn.setReceiver("app.events.sensors.behavior.request.objects");
        setOn.setProperty("object",
                getPojo().getName());
        setOn.setProperty("behavior", BEHAVIOR_POWERED);
        setOn.setProperty("value", BooleanBehavior.VALUE_TRUE);

        Command setOff = new Command();
        setOff.setName("Turn off " + getPojo().getName());
        setOff.setDescription(getPojo().getName() + " turns off");
        setOff.setReceiver("app.events.sensors.behavior.request.objects");
        setOff.setProperty("object",
                getPojo().getName());
        setOff.setProperty("behavior", BEHAVIOR_POWERED);
        setOff.setProperty("value", BooleanBehavior.VALUE_FALSE);

        Command switchPower = new Command();
        switchPower.setName("Switch " + getPojo().getName() + " power");
        switchPower.setDescription("switches the power of " + getPojo().getName());
        switchPower.setReceiver("app.events.sensors.behavior.request.objects");
        switchPower.setProperty("object",
                getPojo().getName());
        switchPower.setProperty("behavior", BEHAVIOR_POWERED);
        switchPower.setProperty("value", BooleanBehavior.VALUE_OPPOSITE);

        Command setItOn = new Command();
        setItOn.setName("Turn it on");
        setItOn.setDescription(getPojo().getName() + " turns on");
        setItOn.setReceiver("app.events.sensors.behavior.request.objects");
        setItOn.setProperty("object", "@event.object.name");
        setItOn.setProperty("behavior", BEHAVIOR_POWERED);
        setItOn.setProperty("value", "true");

        Command setItOff = new Command();
        setItOff.setName("Turn it off");
        setItOff.setDescription(getPojo().getName() + " turns off");
        setItOff.setReceiver("app.events.sensors.behavior.request.objects");
        setItOff.setProperty("object", "@event.object.name");
        setItOff.setProperty("behavior", BEHAVIOR_POWERED);
        setItOff.setProperty("value", BooleanBehavior.VALUE_FALSE);

        Command switchItsPower = new Command();
        switchItsPower.setName("Switch its power");
        switchItsPower.setDescription(getPojo().getName() + " switches its power");
        switchItsPower.setReceiver("app.events.sensors.behavior.request.objects");
        switchItsPower.setProperty("object", "@event.object.name");
        switchItsPower.setProperty("behavior", BEHAVIOR_POWERED);
        switchItsPower.setProperty("value", BooleanBehavior.VALUE_OPPOSITE);

        CommandPersistence.add(setOff);
        CommandPersistence.add(setOn);
        CommandPersistence.add(switchPower);
        CommandPersistence.add(setItOff);
        CommandPersistence.add(setItOn);
        CommandPersistence.add(switchItsPower);
    }

    @Override
    protected void createTriggers() {
        Trigger clicked = new Trigger();
        clicked.setName("When " + this.getPojo().getName() + " is clicked");
        clicked.setChannel("app.event.sensor.object.behavior.clicked");
        clicked.getPayload().addStatement("object.name",
                this.getPojo().getName());
        clicked.getPayload().addStatement("click", ObjectReceiveClick.SINGLE_CLICK);
        clicked.setPersistence(false);

        Trigger turnsOn = new Trigger();
        turnsOn.setName(this.getPojo().getName() + " turns on");
        turnsOn.setChannel("app.event.sensor.object.behavior.change");
        turnsOn.getPayload().addStatement("object.name",
                this.getPojo().getName());
        turnsOn.getPayload().addStatement("object.behavior." + BEHAVIOR_POWERED, BooleanBehavior.VALUE_TRUE);

//        XStream stream = FreedomXStream.getXstream();
//        System.out.println(stream.toXML(turnsOn));
        Trigger turnsOff = new Trigger();
        turnsOff.setName(this.getPojo().getName() + " turns off");
        turnsOff.setChannel("app.event.sensor.object.behavior.change");
        turnsOff.getPayload().addStatement("object.name",
                this.getPojo().getName());
        turnsOff.getPayload().addStatement("object.behavior." + BEHAVIOR_POWERED, BooleanBehavior.VALUE_FALSE);

        TriggerPersistence.add(clicked);
        TriggerPersistence.add(turnsOn);
        TriggerPersistence.add(turnsOff);
    }
}

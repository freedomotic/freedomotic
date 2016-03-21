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

import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.BooleanBehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class ElectricDevice extends EnvObjectLogic {

    private static final Logger LOG = LoggerFactory.getLogger(ElectricDevice.class.getName());
    protected BooleanBehaviorLogic powered;
    protected RangedIntBehaviorLogic consumption;
    protected final static String BEHAVIOR_POWERED = "powered";
    protected final static String BEHAVIOR_POWER_CONSUMPTION = "power_consumption";
    protected final static String ACTION_TURN_ON = "turn on";
    protected final static String ACTION_TURN_OFF = "turn off";

    public ElectricDevice() {
    }

    @Override
    public void init() {
        powered = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior(BEHAVIOR_POWERED));
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

        //ADD CONSUMPTION BEHAVIOR
        final RangedIntBehavior consumptionPojo = (RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_POWER_CONSUMPTION);
        if (consumptionPojo != null) {
            consumption = new RangedIntBehaviorLogic(consumptionPojo);
            consumption.addListener(new RangedIntBehaviorLogic.Listener() {

                @Override
                public void onLowerBoundValue(Config params, boolean fireCommand) {
                    setConsumptionValue(consumptionPojo.getMin(), params, fireCommand);
                }

                @Override
                public void onUpperBoundValue(Config params, boolean fireCommand) {
                    setConsumptionValue(consumptionPojo.getMax(), params, fireCommand);
                }

                @Override
                public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                    setConsumptionValue(rangeValue, params, fireCommand);
                }
            });
            //register this behavior to the superclass to make it visible to it
            registerBehavior(consumption);
        }

        //caches hardware level commands and builds user command for the Electric Devices
        super.init();
    }

    /**
     * Update the power consumption value. This behavior is supposed to be read
     * only by design, so no commands are executed even if the fireCommand
     * property is set to true.
     *
     * @param value the new consumption value
     * @param params general request parameters
     * @param fireCommand true if a command should executed by the related
     * plugin, false if this is just a thing status update
     */
    public void setConsumptionValue(int value, Config params, boolean fireCommand) {
        if (fireCommand) {
            // Action on the hardware is required
            LOG.warn("Power consumption behavior of thing ''{}''"
                    + " is supposed to be a read only value. "
                    + "No command is executed!", this.getPojo().getName());
        }
        // Just a change in the virtual thing status
        consumption.setValue(value);
        setChanged(true);
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
        LOG.info("Setting behavior ''powered'' of object ''{}'' to true", getPojo().getName());

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
        LOG.info("Setting behavior ''powered'' of object ''{}'' to false", getPojo().getName());

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
        setOn.setProperty("object", getPojo().getName());
        setOn.setProperty("behavior", BEHAVIOR_POWERED);
        setOn.setProperty("value", BooleanBehavior.VALUE_TRUE);
        commandRepository.create(setOn);

        Command setOff = new Command();
        setOff.setName("Turn off " + getPojo().getName());
        setOff.setDescription(getPojo().getName() + " turns off");
        setOff.setReceiver("app.events.sensors.behavior.request.objects");
        setOff.setProperty("object", getPojo().getName());
        setOff.setProperty("behavior", BEHAVIOR_POWERED);
        setOff.setProperty("value", BooleanBehavior.VALUE_FALSE);
        commandRepository.create(setOff);

        Command switchPower = new Command();
        switchPower.setName("Switch " + getPojo().getName() + " power");
        switchPower.setDescription("switches the power of " + getPojo().getName());
        switchPower.setReceiver("app.events.sensors.behavior.request.objects");
        switchPower.setProperty("object", getPojo().getName());
        switchPower.setProperty("behavior", BEHAVIOR_POWERED);
        switchPower.setProperty("value", BooleanBehavior.VALUE_OPPOSITE);
        commandRepository.create(switchPower);

        Command setItOn = new Command();
        setItOn.setName("Turn it on");
        setItOn.setDescription("Object turns on");
        setItOn.setReceiver("app.events.sensors.behavior.request.objects");
        setItOn.setProperty("object", "@event.object.name");
        setItOn.setProperty("behavior", BEHAVIOR_POWERED);
        setItOn.setProperty("value", "true");
        commandRepository.create(setItOn);

        Command setItOff = new Command();
        setItOff.setName("Turn it off");
        setItOff.setDescription("Object turns off");
        setItOff.setReceiver("app.events.sensors.behavior.request.objects");
        setItOff.setProperty("object", "@event.object.name");
        setItOff.setProperty("behavior", BEHAVIOR_POWERED);
        setItOff.setProperty("value", BooleanBehavior.VALUE_FALSE);
        commandRepository.create(setItOff);

        Command switchItsPower = new Command();
        switchItsPower.setName("Switch its power");
        switchItsPower.setDescription("Object switches its power");
        switchItsPower.setReceiver("app.events.sensors.behavior.request.objects");
        switchItsPower.setProperty("object", "@event.object.name");
        switchItsPower.setProperty("behavior", BEHAVIOR_POWERED);
        switchItsPower.setProperty("value", BooleanBehavior.VALUE_OPPOSITE);
        commandRepository.create(switchItsPower);
    }

    /**
     *
     */
    @Override
    protected void createTriggers() {
        Trigger clicked = new Trigger();
        clicked.setName("When " + this.getPojo().getName() + " is clicked");
        clicked.setChannel("app.event.sensor.object.behavior.clicked");
        clicked.getPayload().addStatement("object.name", this.getPojo().getName());
        clicked.getPayload().addStatement("click", ObjectReceiveClick.SINGLE_CLICK);
        clicked.setPersistence(false);

        Trigger turnsOn = new Trigger();
        turnsOn.setName(this.getPojo().getName() + " turns on");
        turnsOn.setChannel("app.event.sensor.object.behavior.change");
        turnsOn.getPayload().addStatement("object.name", this.getPojo().getName());
        turnsOn.getPayload().addStatement("object.behavior." + BEHAVIOR_POWERED, BooleanBehavior.VALUE_TRUE);

        Trigger turnsOff = new Trigger();
        turnsOff.setName(this.getPojo().getName() + " turns off");
        turnsOff.setChannel("app.event.sensor.object.behavior.change");
        turnsOff.getPayload().addStatement("object.name", this.getPojo().getName());
        turnsOff.getPayload().addStatement("object.behavior." + BEHAVIOR_POWERED, BooleanBehavior.VALUE_FALSE);

        triggerRepository.create(clicked);
        triggerRepository.create(turnsOn);
        triggerRepository.create(turnsOff);
    }
}

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

import com.freedomotic.behaviors.BooleanBehaviorLogic;
import com.freedomotic.behaviors.ListBehaviorLogic;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.model.object.ListBehavior;
import com.freedomotic.reactions.Command;

/**
 * An 'Air Conditioner' thing abstraction. Type is
 * EnvObject.ElectricDevice.AirConditioner <p>
 *
 * @author Mauro Cicolella
 */
public class AirConditioner extends ElectricDevice {

    private BooleanBehaviorLogic swingMode;
    private RangedIntBehaviorLogic conditioningTemperature;
    private RangedIntBehaviorLogic fanSpeed;
    private ListBehaviorLogic conditioningMode;
    protected final static String BEHAVIOR_SWING_MODE = "swing-mode";
    protected final static String BEHAVIOR_CONDITIONING_TEMPERATURE = "conditioning-temperature";
    protected final static String BEHAVIOR_FAN_SPEED = "fan-speed";
    protected final static String BEHAVIOR_CONDITIONING_MODE = "conditioning-mode";

    @Override
    public void init() {

        swingMode = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior(BEHAVIOR_SWING_MODE));
        //add a listener to values changes
        swingMode.addListener(new BooleanBehaviorLogic.Listener() {

            @Override
            public void onTrue(Config params, boolean fireCommand) {
                startSwing(params);
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                stopSwing(params);
            }
        });

        // Set the conditioning temparature
        conditioningTemperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_CONDITIONING_TEMPERATURE));
        conditioningTemperature.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                setConditioningTemperature(conditioningTemperature.getMin(), params, fireCommand);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                setConditioningTemperature(conditioningTemperature.getMax(), params, fireCommand);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                setConditioningTemperature(rangeValue, params, fireCommand);
            }
        });

        // Set the speed at which the conditioner fan should spin
        fanSpeed = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_FAN_SPEED));
        fanSpeed.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                setFanSpeed(fanSpeed.getMin(), params, fireCommand);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                setFanSpeed(fanSpeed.getMax(), params, fireCommand);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                setFanSpeed(rangeValue, params, fireCommand);
            }
        });

        // Sets the conditioning mode (auto, dry, cool, heat, fan, humidity ...)
        conditioningMode = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_CONDITIONING_MODE));
        conditioningMode.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                setConditioningMode(params.getProperty("value"), params, fireCommand);
            }
        });


        //register new behaviors to the superclass to make it visible to it
        registerBehavior(swingMode);
        registerBehavior(conditioningTemperature);
        registerBehavior(fanSpeed);
        registerBehavior(conditioningMode);
        super.init();
    }

    @Override
    public void executePowerOff(Config params) {
        // Resume normal poweroff procedure from superclass
        super.executePowerOff(params);
    }

    /**
     *
     * @param params
     */
    protected void stopSwing(Config params) {
        boolean executed = executeCommand("set swing mode", params);

        if (executed) {
            swingMode.setValue(false);
            setChanged(true);
        }
    }

    /**
     *
     * @param params
     */
    protected void startSwing(Config params) {
        boolean executed = executeCommand("set swing mode", params);

        if (executed) {
            swingMode.setValue(true);
            setChanged(true);
        }
    }
    

    /**
     * Updates the internal state of the air conditioner related to its
     * conditioning temperature behavior.
     *
     * @param value the new conditioning temperature
     * @param params set of behavior related additional parameters
     * @param fireCommand decide if it is just an update or if should also
     * execute something on the hardware
     */
    public void setConditioningTemperature(int value, Config params, boolean fireCommand) {
        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("set conditioning temperature", params)) {
                //Executed succesfully, update the value
                conditioningTemperature.setValue(value);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            conditioningTemperature.setValue(value);
            setChanged(true);
        }
    }

    /**
     * Updates the internal state of the air conditioner related to its fan
     * rotation speed.
     *
     * @param value the new fan rotation speed
     * @param params set of behavior related additional parameters
     * @param fireCommand decide if it is just an update or if should also
     * execute something on the hardware
     */
    public void setFanSpeed(int value, Config params, boolean fireCommand) {

        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("set fan speed", params)) {
                //Executed succesfully, update the value
                fanSpeed.setValue(value);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            fanSpeed.setValue(value);
            setChanged(true);
        }
    }

    /**
     * Updates the internal state of the air conditioner related to the
     * currently conditioning mode.
     *
     * @param selectedMode the current conditioning mode
     * @param params set of behavior related additional parameters
     * @param fireCommand decide if it is just an update or if should also
     * execute something on the hardware
     */
    public void setConditioningMode(String selectedMode, Config params, boolean fireCommand) {
        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("set conditioning mode", params)) {
                //Executed succesfully, update the value
                conditioningMode.setSelected(selectedMode);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            conditioningMode.setSelected(selectedMode);
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command increaseCondTemp = new Command();
        increaseCondTemp.setName("Increase " + getPojo().getName() + " conditioning temperature");
        increaseCondTemp.setDescription("increases " + getPojo().getName() + " conditioning temperature of one step");
        increaseCondTemp.setReceiver("app.events.sensors.behavior.request.objects");
        increaseCondTemp.setProperty("object", getPojo().getName());
        increaseCondTemp.setProperty("behavior", BEHAVIOR_CONDITIONING_TEMPERATURE);
        increaseCondTemp.setProperty("value", Behavior.VALUE_NEXT);

        Command decreaseCondTemp = new Command();
        decreaseCondTemp.setName("Decrease " + getPojo().getName() + " conditioning temperature");
        decreaseCondTemp.setDescription("decreases " + getPojo().getName() + " conditioning temperature of one step");
        decreaseCondTemp.setReceiver("app.events.sensors.behavior.request.objects");
        decreaseCondTemp.setProperty("object", getPojo().getName());
        decreaseCondTemp.setProperty("behavior", BEHAVIOR_CONDITIONING_TEMPERATURE);
        decreaseCondTemp.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command nextCondMode = new Command();
        nextCondMode.setName(getPojo().getName() + " next conditioning mode");
        nextCondMode.setDescription("select the " + getPojo().getName() + " next conditioning mode");
        nextCondMode.setReceiver("app.events.sensors.behavior.request.objects");
        nextCondMode.setProperty("object", getPojo().getName());
        nextCondMode.setProperty("behavior", BEHAVIOR_CONDITIONING_MODE);
        nextCondMode.setProperty("value", Behavior.VALUE_NEXT);

        Command prevCondMode = new Command();
        prevCondMode.setName(getPojo().getName() + " previous conditioning mode");
        prevCondMode.setDescription("select the " + getPojo().getName() + " previous conditioning mode");
        prevCondMode.setReceiver("app.events.sensors.behavior.request.objects");
        prevCondMode.setProperty("object", getPojo().getName());
        prevCondMode.setProperty("behavior", BEHAVIOR_CONDITIONING_MODE);
        prevCondMode.setProperty("value", Behavior.VALUE_PREVIOUS);

        //TODO: add missing commands!
        commandRepository.create(increaseCondTemp);
        commandRepository.create(decreaseCondTemp);
        commandRepository.create(prevCondMode);
        commandRepository.create(nextCondMode);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

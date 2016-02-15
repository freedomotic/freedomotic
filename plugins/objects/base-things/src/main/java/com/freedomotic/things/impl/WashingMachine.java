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
 * A 'Washing Machine' thing abstraction. Type is
 * EnvObject.ElectricDevice.WashingMachine
 * <p>
 * Behavior:
 * <ul>
 * <li>Any change to washing machine settings make the machine to power on
 * first</li>
 * <li>Powering off makes the washing machine to pause washing first</li>
 * <li>When selecting custom temperature or spinning rpm the washing program is
 * considered 'Custom'</li>
 * <li>All settings became read only after the washing machine startup</li>
 * </ul>
 * </p>
 * <p>
 * Notes for template configurators:
 * <ul>
 * <li>Do not remove or alter the 'Custom' washing program</li>
 * <li>Do not remove or alter the 'Finished' washing cycle</li>
 * </ul>
 *
 * @author Mauro Cicolella
 */
public class WashingMachine extends ElectricDevice {

    private BooleanBehaviorLogic washing;
    private RangedIntBehaviorLogic washingTemperature;
    private RangedIntBehaviorLogic spinningRpm;
    private ListBehaviorLogic washingProgram;
    private ListBehaviorLogic washingCycle;

    // The two main parameters of a basic washing machine
    protected final static String BEHAVIOR_WASHING = "washing";
    protected final static String BEHAVIOR_WASHING_TEMPERATURE = "washing-temperature";
    protected final static String BEHAVIOR_SPINNING_RPM = "spinning-rpm";
    // The current phase which the machine is executing
    protected final static String BEHAVIOR_WASHING_CYCLE = "washing-cycle";
    // Contains presets for temperature and spinning defined in the XML
    protected final static String BEHAVIOR_WASHING_PROGRAM = "washing-program";

    @Override
    public void init() {
        washing = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior(BEHAVIOR_WASHING));
        //add a listener to values changes
        washing.addListener(new BooleanBehaviorLogic.Listener() {
            @Override
            public void onTrue(Config params, boolean fireCommand) {
                startWashing(params, fireCommand);
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                pauseWashing(params, fireCommand);
            }
        });

        // Set the temparature at which the washing machine should work
        washingTemperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_WASHING_TEMPERATURE));
        washingTemperature.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                setWashingTemperature(washingTemperature.getMin(), params, fireCommand);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                setWashingTemperature(washingTemperature.getMax(), params, fireCommand);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                setWashingTemperature(rangeValue, params, fireCommand);
            }
        });

        // Set the RPM at which the washing machine should spinn
        spinningRpm = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_SPINNING_RPM));
        spinningRpm.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                setSpinningRpm(spinningRpm.getMin(), params, fireCommand);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                setSpinningRpm(spinningRpm.getMax(), params, fireCommand);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                setSpinningRpm(rangeValue, params, fireCommand);
            }
        });

        // Sets the washing program (whites, wool, ...)
        // TODO: This behavior should contain temperature, spinning rpm, duration, cycles list data for each program (as washing machine presets)
        washingProgram = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_WASHING_PROGRAM));
        washingProgram.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                setWashingProgram(params.getProperty("value"), params, fireCommand);
            }
        });

        // Controls the current cycle whitin a washing program (ready, washing, rinsing, spinning, finished)
        washingCycle = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_WASHING_CYCLE));
        washingCycle.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                setWashingCycle(params.getProperty("value"), params, fireCommand);
            }
        });

        //register new behaviors to the superclass to make it visible to it
        registerBehavior(washing);
        registerBehavior(washingTemperature);
        registerBehavior(spinningRpm);
        registerBehavior(washingProgram);
        registerBehavior(washingCycle);
        super.init();
    }

    @Override
    public void executePowerOff(Config params) {
        // Pause washing before turning off
        pauseWashing(params, true);
        // Resume normal poweroff procedure from superclass
        super.executePowerOff(params);
    }

    public void startWashing(Config params, boolean fireCommand) {
        // Cannot change settings after startup
        washingTemperature.setReadOnly(true);
        washingCycle.setReadOnly(true);
        washingProgram.setReadOnly(true);
        spinningRpm.setReadOnly(true);
        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("start washing", params)) {
                //Executed succesfully, update the value
                washing.setValue(true);
                getPojo().setCurrentRepresentation(2);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            washing.setValue(true);
            setChanged(true);
        }
    }

    public void pauseWashing(Config params, boolean fireCommand) {
        // Allow to change settings again
        washingTemperature.setReadOnly(false);
        washingCycle.setReadOnly(false);
        washingProgram.setReadOnly(false);
        spinningRpm.setReadOnly(false);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("pause washing", params)) {
                //Executed succesfully, update the value
                washing.setValue(false);
                getPojo().setCurrentRepresentation(1);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            washing.setValue(false);
            setChanged(true);
        }
    }

    /**
     * Updates the internal state of the washing machine related to the washing
     * temperature behavior.
     *
     * @param value the new washing temperature
     * @param params set of behavior related additional parameters
     * @param fireCommand decide if it is just an update or if should also
     * execute something on the hardware
     */
    public void setWashingTemperature(int value, Config params, boolean fireCommand) {
        // Makes the program a custom one
        washingProgram.setSelected("Custom");
        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("set washing temperature", params)) {
                //Executed succesfully, update the value
                washingTemperature.setValue(value);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            washingTemperature.setValue(value);
            setChanged(true);
        }
    }

    /**
     * Updates the internal state of the washing machine related to the spinning
     * rotation per minutes of the washing machine.
     *
     * @param value the new washing spinning rotation
     * @param params set of behavior related additional parameters
     * @param fireCommand decide if it is just an update or if should also
     * execute something on the hardware
     */
    public void setSpinningRpm(int value, Config params, boolean fireCommand) {
        // Makes to program a custom one
        washingProgram.setSelected("Custom");
        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("set spinning rpm", params)) {
                //Executed succesfully, update the value
                spinningRpm.setValue(value);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            spinningRpm.setValue(value);
            setChanged(true);
        }
    }

    /**
     * Updates the internal state of the washing machine related to the
     * currently active washing cycle.
     *
     * The washing cycle can be also used to notify it has FINISHED (last
     * cycle). Do not change or remove the 'Finishes' washing cycle from the
     * template definition
     *
     * @param selectedCycle the cycle that the machine is currently doing or the
     * one to use as washing machine startup
     * @param params set of behavior related additional parameters
     * @param fireCommand decide if it is just an update or if should also
     * execute something on the hardware
     */
    public void setWashingCycle(String selectedCycle, Config params, boolean fireCommand) {
        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("set washing cycle", params)) {
                //Executed succesfully, update the value
                washingCycle.setSelected(selectedCycle);
                setChanged(true);
                //Has completely finished its job
                if (washingCycle.getSelected().equalsIgnoreCase("Finished")) {
                    pauseWashing(params, fireCommand);
                }
            }
        } else {
            // Just a change in the virtual thing status
            washingCycle.setSelected(selectedCycle);
            setChanged(true);
            //Has completely finished its job
            if (washingCycle.getSelected().equalsIgnoreCase("Finished")) {
                pauseWashing(params, fireCommand);
            }
        }
    }

    /**
     * Updates the internal state of the washing machine related to the washing
     * program. A washing program is a preset of washing time, washing
     * temperature and cycles to execute hardcoded in the washing machine
     * firmware.
     *
     * @param selectedProgram the cycle that the machine is currently doing
     * @param params set of behavior related additional parameters
     * @param fireCommand decide if it is just an update or if should also
     * execute something on the hardware
     */
    public void setWashingProgram(String selectedProgram, Config params, boolean fireCommand) {
        //Turn it on first (executed only if not already powered)
        executePowerOn(params);
        if (fireCommand) {
            // Action on the hardware is required
            if (executeCommand("set washing program", params)) {
                //Executed succesfully, update the value
                washingProgram.setSelected(selectedProgram);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            washingProgram.setSelected(selectedProgram);
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command increareWashTemp = new Command();
        increareWashTemp.setName("Increase " + getPojo().getName() + " washing temperature");
        increareWashTemp.setDescription("increases " + getPojo().getName() + " washing temperature of one step");
        increareWashTemp.setReceiver("app.events.sensors.behavior.request.objects");
        increareWashTemp.setProperty("object", getPojo().getName());
        increareWashTemp.setProperty("behavior", BEHAVIOR_WASHING_TEMPERATURE);
        increareWashTemp.setProperty("value", Behavior.VALUE_NEXT);

        Command decreaseWashTemp = new Command();
        decreaseWashTemp.setName("Decrease " + getPojo().getName() + " washing temperature");
        decreaseWashTemp.setDescription("decreases " + getPojo().getName() + " washing temperature of one step");
        decreaseWashTemp.setReceiver("app.events.sensors.behavior.request.objects");
        decreaseWashTemp.setProperty("object", getPojo().getName());
        decreaseWashTemp.setProperty("behavior", BEHAVIOR_WASHING_TEMPERATURE);
        decreaseWashTemp.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command nextWashCycle = new Command();
        nextWashCycle.setName(getPojo().getName() + " next washing cycle");
        nextWashCycle.setDescription("select the " + getPojo().getName() + " next washing cycle");
        nextWashCycle.setReceiver("app.events.sensors.behavior.request.objects");
        nextWashCycle.setProperty("object", getPojo().getName());
        nextWashCycle.setProperty("behavior", BEHAVIOR_WASHING_PROGRAM);
        nextWashCycle.setProperty("value", Behavior.VALUE_NEXT);

        Command prevWashCycle = new Command();
        prevWashCycle.setName(getPojo().getName() + " previous washing cycle");
        prevWashCycle.setDescription("select the " + getPojo().getName() + " previous washing cycle");
        prevWashCycle.setReceiver("app.events.sensors.behavior.request.objects");
        prevWashCycle.setProperty("object", getPojo().getName());
        prevWashCycle.setProperty("behavior", BEHAVIOR_WASHING_PROGRAM);
        prevWashCycle.setProperty("value", Behavior.VALUE_PREVIOUS);

        //TODO: add missing commands!
        commandRepository.create(increareWashTemp);
        commandRepository.create(decreaseWashTemp);
        commandRepository.create(prevWashCycle);
        commandRepository.create(nextWashCycle);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

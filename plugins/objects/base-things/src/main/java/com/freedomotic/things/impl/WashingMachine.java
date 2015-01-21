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

import com.freedomotic.behaviors.ListBehaviorLogic;
import com.freedomotic.things.impl.ElectricDevice;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.model.object.ListBehavior;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;

/**
 *
 * @author Mauro Cicolella
 *
 * This class represents a 'Washing machine' thing template extending an
 * ElectricDevice with behaviors: washing-temperature, washing-cycle
 *
 */
public class WashingMachine
        extends ElectricDevice {

    private RangedIntBehaviorLogic washingTemperature;
    private ListBehaviorLogic washingCycle;
    protected final static String BEHAVIOR_WASHING_TEMPERATURE = "washing-temperature";
    protected final static String BEHAVIOR_WASHING_CYCLE = "washing-cycle";

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        washingTemperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_WASHING_TEMPERATURE));

        washingTemperature.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeSetWashingTemperature(washingTemperature.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeSetWashingTemperature(washingTemperature.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeSetWashingTemperature(rangeValue, params);
            }
        });

        //linking this property with the behavior defined in the XML
        washingCycle = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_WASHING_CYCLE));

        washingCycle.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetWashingCycle(params);
                } else {
                    setWashingCycle(params.getProperty("value"));
                }
            }
        });
        //register new behaviors to the superclass to make it visible to it
        registerBehavior(washingTemperature);
        registerBehavior(washingCycle);
        super.init();
    }

    public void executeSetWashingTemperature(int rangeValue, Config params) {
        boolean executed = executeCommand("set washing temperature", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            washingTemperature.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void executeSetWashingCycle(Config params) {
        boolean executed = executeCommand("set washing cycle", params);
        if (executed) {
            setWashingCycle(params.getProperty("value"));
        }
    }

    public void setWashingCycle(String value) {
        washingCycle.setSelected(value);
        setChanged(true);
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Increase " + getPojo().getName() + " washing-temperature");
        a.setDescription("increases " + getPojo().getName() + " washing-temperature of one step");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", BEHAVIOR_WASHING_TEMPERATURE);
        a.setProperty("value", Behavior.VALUE_NEXT);

        Command b = new Command();
        b.setName("Decrease " + getPojo().getName() + " washing-temperature");
        b.setDescription("decreases " + getPojo().getName() + " washing-temperature of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object", getPojo().getName());
        b.setProperty("behavior", BEHAVIOR_WASHING_TEMPERATURE);
        b.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command c = new Command();
        c.setName(getPojo().getName() + " next washing cycle");
        c.setDescription("select the " + getPojo().getName() + " next washing cycle");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object", getPojo().getName());
        c.setProperty("behavior", BEHAVIOR_WASHING_CYCLE);
        c.setProperty("value", Behavior.VALUE_NEXT);

        Command d = new Command();
        d.setName(getPojo().getName() + " previous washing cycle");
        d.setDescription("select the " + getPojo().getName() + " previous washing cycle");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", getPojo().getName());
        c.setProperty("behavior", BEHAVIOR_WASHING_CYCLE);
        c.setProperty("value", Behavior.VALUE_PREVIOUS);

        CommandPersistence.add(a);
        CommandPersistence.add(b);
        CommandPersistence.add(c);
        CommandPersistence.add(d);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

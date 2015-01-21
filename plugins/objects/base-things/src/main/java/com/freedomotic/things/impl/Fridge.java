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

import com.freedomotic.things.impl.ElectricDevice;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;

/**
 *
 * @author Mauro Cicolella
 *
 * This class represents a 'Fridge' thing template extending an ElectricDevice
 * Behaviors: fridge-temperature freezer-temperature
 *
 */
public class Fridge
        extends ElectricDevice {

    private RangedIntBehaviorLogic fridgeTemperature;
    private RangedIntBehaviorLogic freezerTemperature;
    protected final static String BEHAVIOR_FRIDGE_TEMPERATURE = "fridge-temperature";
    protected final static String BEHAVIOR_FREEZER_TEMPERATURE = "freezer-temperature";

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        fridgeTemperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_FRIDGE_TEMPERATURE));
        freezerTemperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_FREEZER_TEMPERATURE));

        fridgeTemperature.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeSetFridgeTemperature(fridgeTemperature.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeSetFridgeTemperature(fridgeTemperature.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeSetFridgeTemperature(rangeValue, params);
            }
        });

        freezerTemperature.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeSetFreezerTemperature(freezerTemperature.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeSetFreezerTemperature(freezerTemperature.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                executeSetFreezerTemperature(rangeValue, params);
            }
        });
        //register new behaviors to the superclass to make it visible to it
        registerBehavior(fridgeTemperature);
        registerBehavior(freezerTemperature);
        super.init();
    }

    public void executeSetFridgeTemperature(int rangeValue, Config params) {
        boolean executed = executeCommand("set fridge temperature", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            setFridgeTemperature(rangeValue);
        }
    }

    public void executeSetFreezerTemperature(int rangeValue, Config params) {
        boolean executed = executeCommand("set freezer temperature", params); //executes the developer level command associated with 'set brightness' action

        if (executed) {
            setFreezerTemperature(rangeValue);
        }
    }

    private void setFridgeTemperature(int value) {
        fridgeTemperature.setValue(value);
        setChanged(true);
    }

    private void setFreezerTemperature(int value) {
        freezerTemperature.setValue(value);
        setChanged(true);
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Increase " + getPojo().getName() + " fridge-temperature");
        a.setDescription("increases " + getPojo().getName() + " fridge-temperature of one step");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", BEHAVIOR_FRIDGE_TEMPERATURE);
        a.setProperty("value", Behavior.VALUE_NEXT);

        Command b = new Command();
        b.setName("Decrease " + getPojo().getName() + " fridge-temperature");
        b.setDescription("decreases " + getPojo().getName() + " fridge-temperature of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object", getPojo().getName());
        b.setProperty("behavior", BEHAVIOR_FRIDGE_TEMPERATURE);
        b.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command c = new Command();
        c.setName("Set its fridge-temperature to the value in the event");
        c.setDescription("set its fridge-temperature to the value in the event");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object", "@event.object.name");
        c.setProperty("behavior", BEHAVIOR_FRIDGE_TEMPERATURE);
        c.setProperty("value", "@event.value");

        Command d = new Command();
        d.setName("Increase " + getPojo().getName() + " freezer-temperature");
        d.setDescription("increases " + getPojo().getName() + " freezer-temperature of one step");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", getPojo().getName());
        d.setProperty("behavior", BEHAVIOR_FREEZER_TEMPERATURE);
        d.setProperty("value", Behavior.VALUE_NEXT);

        Command e = new Command();
        e.setName("Decrease " + getPojo().getName() + " freezer-temperature");
        e.setDescription("decreases " + getPojo().getName() + " freezer-temperature of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", getPojo().getName());
        e.setProperty("behavior", BEHAVIOR_FREEZER_TEMPERATURE);
        e.setProperty("value", Behavior.VALUE_PREVIOUS);

        Command f = new Command();
        f.setName("Set its freezer-temperature to the value in the event");
        f.setDescription("set its freezer-temperature to the value in the event");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", BEHAVIOR_FREEZER_TEMPERATURE);
        f.setProperty("value", "@event.value");

        CommandPersistence.add(a);
        CommandPersistence.add(b);
        CommandPersistence.add(c);
        CommandPersistence.add(d);
        CommandPersistence.add(e);
        CommandPersistence.add(f);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

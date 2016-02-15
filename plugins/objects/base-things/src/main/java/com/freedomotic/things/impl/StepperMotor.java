/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
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
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Command;

/**
 *
 * @author gpt
 */
public class StepperMotor
        extends ElectricDevice {

    public RangedIntBehaviorLogic position;
    protected final static String BEHAVIOR_POSITION = "position";

    @Override
    public void init() {
        super.init();

        //linking this property with the behavior defined in the XML
        position = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_POSITION));
        position.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                //turnPowerOff(params);
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum                	
            		onRangeValue(position.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //turnPowerOn(params);
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set maximum                	
            		onRangeValue(position.getMax(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetPosition(rangeValue, params);
                } else {
                    setPosition(rangeValue);
                }
            }
        });
        registerBehavior(position);
    }

    public void executeSetPosition(int rangeValue, Config params) {
        boolean executed = executeCommand("set position", params); //executes the developer level command associated with 'set volume' action

        if (executed) {
            setPosition(rangeValue);

            //TODO: set the light graphical representation
            //setCurrentRepresentation(1); //points to the second element in the XML views array (light on image)
        }
    }

    public void setPosition(int rangeValue) {
        if (position.getValue() != rangeValue) {
            position.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void setPositionUp(Config params) {
        boolean executed = executeCommand("move up", params); //executes the developer level command associated with 'set channel' action

        if (executed) {
            if (position.getValue() != position.getMax()) {
                position.setValue(position.getValue() + 1);
                setChanged(true);
            }
        }
    }

    public void setPositionDown(Config params) {
        boolean executed = executeCommand("move down", params); //executes the developer level command associated with 'set channel' action

        if (executed) {
            if (position.getValue() != position.getMin()) {
                position.setValue(position.getValue() - 1);
                setChanged(true);
            }
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " position to 50%");
        a.setDescription("the StepperMotor " + getPojo().getName() + " changes its position to 50%");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object",
                getPojo().getName());
        a.setProperty("behavior", "position");
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName(getPojo().getName() + " position up");
        b.setDescription("increases " + getPojo().getName() + " position of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object",
                getPojo().getName());
        b.setProperty("behavior", "position");
        b.setProperty("value", "next");

        Command c = new Command();
        c.setName(getPojo().getName() + " position down");
        c.setDescription("decreases " + getPojo().getName() + " position of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object",
                getPojo().getName());
        c.setProperty("behavior", "position");
        c.setProperty("value", "previous");

        Command d = new Command();
        d.setName("Set its position to 50%");
        d.setDescription("set its position to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", "position");
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName("Increase its position");
        e.setDescription("increases its position of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", "position");
        e.setProperty("value", "next");

        Command f = new Command();
        f.setName("Decrease its position");
        f.setDescription("decreases its position of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", "position");
        f.setProperty("value", "previous");

        Command g = new Command();
        g.setName("Set its position to the value in the event");
        g.setDescription("set its position to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", "position");
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

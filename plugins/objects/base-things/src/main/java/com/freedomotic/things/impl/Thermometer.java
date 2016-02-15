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
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Trigger;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Thermometer
        extends EnvObjectLogic {

    private static final Logger LOG = Logger.getLogger(Thermometer.class.getName());
    private RangedIntBehaviorLogic temperature;
    private static final String BEHAVIOR_TEMPERATURE = "temperature";

    @Override
    public void init() {
//linking this property with the behavior defined in the XML
        temperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_TEMPERATURE));
        temperature.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum
            		onRangeValue(temperature.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set maximum
            		onRangeValue(temperature.getMax(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetTemperature(rangeValue, params);
                } else {
                    setTemperature(rangeValue);
                }
            }
        });
//register this behavior to the superclass to make it visible to it
        registerBehavior(temperature);
        super.init();
    }

    public void executeSetTemperature(int rangeValue, Config params) {
        boolean executed = executeCommand("set temperature", params);
        if (executed) {
            temperature.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setTemperature(int value) {
        LOG.config("Setting behavior 'temperature' of object '" + getPojo().getName() + "' to "
                + value);
        temperature.setValue(value);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }

    /**
     * Creates user level commands for this class of freedomotic objects
     */
    @Override
    protected void createCommands() {
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
        triggerRepository.create(clicked);
    }
}

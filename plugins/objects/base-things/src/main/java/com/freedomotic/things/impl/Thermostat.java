/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.things.impl;

import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Trigger;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */

public class Thermostat
        extends Thermometer {

    private static final Logger LOG = Logger.getLogger(Thermostat.class.getName()); 
    private RangedIntBehaviorLogic setpoint;
    private static final String BEHAVIOR_TEMPERATURE_SETPOINT = "setpoint";

    @Override
    public void init() {
        
        if ( getPojo().getBehavior(BEHAVIOR_TEMPERATURE_SETPOINT) == null){
            RangedIntBehavior setpointbeh = new RangedIntBehavior();
            setpointbeh.setName(BEHAVIOR_TEMPERATURE_SETPOINT);
            getPojo().getBehaviors().add(setpointbeh);
        }
        setpoint = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_TEMPERATURE_SETPOINT));
        setpoint.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executeSetTemperatureSetpoint(setpoint.getMin(), params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executeSetTemperatureSetpoint(setpoint.getMax(), params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetTemperatureSetpoint(rangeValue, params);
                } else {
                    setTemperatureSetpoint(rangeValue);
                }
            }
        });
        registerBehavior(setpoint);
        
        super.init();
    }

    public void executeSetTemperatureSetpoint(int rangeValue, Config params) {
        boolean executed = executeCommand("set setpoint", params);

        if (executed) {
            setpoint.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setTemperatureSetpoint(int value) {
        LOG.config("Setting behavior 'setpoint' of object '" + getPojo().getName() + "' to "
                + value);
        setpoint.setValue(value);
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

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
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Trigger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */

public class GenericSensor
        extends EnvObjectLogic {

    private static final Logger LOG = Logger.getLogger(GenericSensor.class.getName()); 
    private RangedIntBehaviorLogic readValue;
    

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        // being this a GENERIC object, we cannot foresee the name given to related behavior, so we must address it by index 
        readValue = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehaviors().get(0));
        readValue.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum                	
            		onRangeValue(readValue.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set maximum                	
            		onRangeValue(readValue.getMax(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetReadValue(rangeValue, params);
                } else {
                    setReadValue(rangeValue);
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(readValue);
        super.init();
    }

    public void executeSetReadValue(int rangeValue, Config params) {
        boolean executed = executeCommand("set read value", params);

        if (executed) {
            readValue.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setReadValue(int value) {
        LOG.log(Level.INFO, "Setting behavior ''{0}'' of object ''{1}'' to {2}", new Object[]{readValue.getName(), getPojo().getName(), value});
        readValue.setValue(value);
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

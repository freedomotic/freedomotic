/**
 *
 * Copyright (c) 2009-2017 Freedomotic team http://freedomotic.com
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerMeter
        extends ElectricDevice {

    private static final Logger LOG = LoggerFactory.getLogger(GenericSensor.class.getName());
    private static final String VALUE_ORIGINAL = "value.original";
    private static final String VALUE = "value";
    private RangedIntBehaviorLogic current;
    private RangedIntBehaviorLogic voltage;
    private RangedIntBehaviorLogic power;
    private RangedIntBehaviorLogic energy;
    private RangedIntBehaviorLogic powerFactor;

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        current = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("current"));
        addListenerToCurrent();
        //register this behavior to the superclass to make it visible to it
        registerBehavior(current);

        voltage = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("voltage"));
        addListenerToVoltage();
        //register this behavior to the superclass to make it visible to it
        registerBehavior(voltage);

        power = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("power"));
        addListenerToPower();
        //register this behavior to the superclass to make it visible to it
        registerBehavior(power);

        powerFactor = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("power-factor"));
        addListenerToPowerFactor();
        //register this behavior to the superclass to make it visible to it
        registerBehavior(powerFactor);

        energy = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("energy"));
        addListenerToEnergy();
        //register this behavior to the superclass to make it visible to it
        registerBehavior(energy);

        super.init();
    }

    private void addListenerToEnergy() {
        energy.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set minimum
                    onRangeValue(energy.getMin(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set maximum
                    onRangeValue(energy.getMax(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetEnergy(rangeValue, params);
                } else {
                    setEnergy(rangeValue);
                }
            }
        });
    }

    private void addListenerToPowerFactor() {
        powerFactor.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set minimum
                    onRangeValue(powerFactor.getMin(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set maximum
                    onRangeValue(powerFactor.getMax(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetPowerFactor(rangeValue, params);
                } else {
                    setPowerFactor(rangeValue);
                }
            }
        });
    }

    private void addListenerToPower() {
        power.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set minimum
                    onRangeValue(power.getMin(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set maximum
                    onRangeValue(power.getMax(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetPower(rangeValue, params);
                } else {
                    setPower(rangeValue);
                }
            }
        });
    }

    private void addListenerToVoltage() {
        voltage.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set minimum
                    onRangeValue(voltage.getMin(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set maximum
                    onRangeValue(voltage.getMax(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetVoltage(rangeValue, params);
                } else {
                    setVoltage(rangeValue);
                }
            }
        });
    }

    private void addListenerToCurrent() {
        current.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
                    //ok here, just trying to set minimum
                    onRangeValue(current.getMin(), params, fireCommand);
                } else {
                    //there is an hardware read error
                }
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                if (params.getProperty(VALUE_ORIGINAL).equals(params.getProperty(VALUE))) {
//ok here, just trying to set maximum
                    onRangeValue(current.getMax(), params, fireCommand);
                } else {
//there is an hardware read error
                }
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetCurrent(rangeValue, params);
                } else {
                    setCurrent(rangeValue);
                }
            }
        });
    }

    public void executeSetCurrent(int rangeValue, Config params) {
        boolean executed = executeCommand("set current", params);

        if (executed) {
            current.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setCurrent(int value) {
        LOG.info("Setting behavior \"current\" of thing \"{0}\" to {1}", getPojo().getName(), value);
        current.setValue(value);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }

    public void executeSetVoltage(int rangeValue, Config params) {
        boolean executed = executeCommand("set voltage", params);

        if (executed) {
            voltage.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setVoltage(int value) {
        LOG.info("Setting behavior \"voltage\" of thing \"{0}\" to {1}", getPojo().getName(), value);
        voltage.setValue(value);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }

    public void executeSetPower(int rangeValue, Config params) {
        boolean executed = executeCommand("set power", params);

        if (executed) {
            power.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setPower(int value) {
        LOG.info("Setting behavior \"power\" of thing \"{0}\" to {1}", getPojo().getName(), value);
        power.setValue(value);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }

    public void executeSetPowerFactor(int rangeValue, Config params) {
        boolean executed = executeCommand("set power-factor", params);

        if (executed) {
            powerFactor.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setPowerFactor(int value) {
        LOG.info("Setting behavior \"power-factor\" of thing \"{0}\" to {1}", getPojo().getName(), value);
        powerFactor.setValue(value);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }

    public void executeSetEnergy(int rangeValue, Config params) {
        boolean executed = executeCommand("set energy", params);

        if (executed) {
            energy.setValue(rangeValue);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setEnergy(int value) {
        LOG.info("Setting behavior \"energy\" of thing \"{0}\" to {1}", getPojo().getName(), value);
        energy.setValue(value);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }
}

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

import com.freedomotic.things.impl.ElectricDevice;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import java.util.logging.Logger;

public class PowerMeter
        extends ElectricDevice {

    private static final Logger LOG = Logger.getLogger(GenericSensor.class.getName()); 
    private RangedIntBehaviorLogic current;
    private RangedIntBehaviorLogic voltage;
    private RangedIntBehaviorLogic power;
    private RangedIntBehaviorLogic energy;
    private RangedIntBehaviorLogic powerFactor;

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        current = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("current"));
        current.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum                	
            		onRangeValue(current.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
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
        //register this behavior to the superclass to make it visible to it
        registerBehavior(current);

        voltage = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("voltage"));
        voltage.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum                	
            		onRangeValue(voltage.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
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
        //register this behavior to the superclass to make it visible to it
        registerBehavior(voltage);

        power = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("power"));
        power.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum                	
            		onRangeValue(power.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
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
        //register this behavior to the superclass to make it visible to it
        registerBehavior(power);

        powerFactor = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("power-factor"));
        powerFactor.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum                	
            		onRangeValue(powerFactor.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
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
        //register this behavior to the superclass to make it visible to it
        registerBehavior(powerFactor);

        energy = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("energy"));
        energy.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
//ok here, just trying to set minimum                	
            		onRangeValue(energy.getMin(), params, fireCommand);
            	} else {
//there is an hardware read error
            	}
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	if (params.getProperty("value.original").equals(params.getProperty("value"))) {
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
        //register this behavior to the superclass to make it visible to it
        registerBehavior(energy);

        super.init();
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
        LOG.config("Setting behavior 'current' of object '" + getPojo().getName() + "' to "
                + value);
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
        LOG.config("Setting behavior 'voltage' of object '" + getPojo().getName() + "' to "
                + value);
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
        LOG.info("Setting behavior 'power' of object '" + getPojo().getName() + "' to " + value);
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
        LOG.config("Setting behavior 'power-factor' of object '" + getPojo().getName() + "' to "
                + value);
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
        LOG.info("Setting behavior 'energy' of object '" + getPojo().getName() + "' to " + value);
        energy.setValue(value);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }

    /**
     * Creates user level commands for this class of freedomotic objects
     */
    @Override
    protected void createCommands() {
        super.createCommands();
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

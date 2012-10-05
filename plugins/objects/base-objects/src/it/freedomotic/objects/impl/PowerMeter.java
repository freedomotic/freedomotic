/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.objects.impl;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.RangedIntBehavior;
import it.freedomotic.objects.RangedIntBehaviorLogic;

/**
 *
 * @author Mauro Cicolella
 */
public class PowerMeter extends ElectricDevice {

    private RangedIntBehaviorLogic current;
    private RangedIntBehaviorLogic voltage;
    private RangedIntBehaviorLogic power;
    private RangedIntBehaviorLogic energy;

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        current = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("current"));
        current.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                //there is an hardware read error
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //there is as hardware read error
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
                //there is an hardware read error
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //there is as hardware read error
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
                //there is an hardware read error
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //there is as hardware read error
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

        energy = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("energy"));
        energy.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                //there is an hardware read error
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //there is as hardware read error
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
        Freedomotic.logger.config("Setting behavior 'current' of object '" + getPojo().getName() + "' to " + value);
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
        Freedomotic.logger.config("Setting behavior 'voltage' of object '" + getPojo().getName() + "' to " + value);
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
        Freedomotic.logger.config("Setting behavior 'power' of object '" + getPojo().getName() + "' to " + value);
        power.setValue(value);
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
        Freedomotic.logger.config("Setting behavior 'energy' of object '" + getPojo().getName() + "' to " + value);
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

/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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
import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.model.object.ListBehavior;
import com.freedomotic.reactions.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class Weather extends EnvObjectLogic {

    private static final Logger LOG = LoggerFactory.getLogger(Weather.class.getName());
    private RangedIntBehaviorLogic temperature;
    private RangedIntBehaviorLogic pressure;
    private RangedIntBehaviorLogic humidity;
    private RangedIntBehaviorLogic rain;
    private RangedIntBehaviorLogic windSpeed;
    private RangedIntBehaviorLogic windDegree;
    private ListBehaviorLogic conditions;
    private static final String BEHAVIOR_TEMPERATURE = "temperature";
    private static final String BEHAVIOR_PRESSURE = "pressure";
    private static final String BEHAVIOR_HUMIDITY = "humidity";
    private static final String BEHAVIOR_RAIN = "rain";
    private static final String BEHAVIOR_WIND_SPEED = "wind-speed";
    private static final String BEHAVIOR_WIND_DEGREE = "wind-degree";
    private static final String BEHAVIOR_CONDITIONS = "conditions";

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        temperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_TEMPERATURE));
        temperature.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
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

        //linking this property with the behavior defined in the XML
        pressure = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_PRESSURE));
        pressure.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetPressure(rangeValue, params);
                } else {
                    setPressure(rangeValue);
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(pressure);

        //linking this property with the behavior defined in the XML
        humidity = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_HUMIDITY));
        humidity.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetHumidity(rangeValue, params);
                } else {
                    setHumidity(rangeValue);
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(humidity);

        //linking this property with the behavior defined in the XML
        rain = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_RAIN));
        rain.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetRain(rangeValue, params);
                } else {
                    setRain(rangeValue);
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(rain);

        //linking this property with the behavior defined in the XML
        windSpeed = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_WIND_SPEED));
        windSpeed.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetWindSpeed(rangeValue, params);
                } else {
                    setWindSpeed(rangeValue);
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(windSpeed);

        //linking this property with the behavior defined in the XML
        windDegree = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior(BEHAVIOR_WIND_DEGREE));
        windDegree.addListener(new RangedIntBehaviorLogic.Listener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetWindDegree(rangeValue, params);
                } else {
                    setWindDegree(rangeValue);
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(windDegree);

        conditions = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_CONDITIONS));
        conditions.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                setConditions(params.getProperty("value"), params, fireCommand);
            }
        });

        //register new behaviors to the superclass to make it visible to it
        registerBehavior(conditions);

        super.init();
    }

    public void executeSetTemperature(int rangeValue, Config params) {
        boolean executed = executeCommand("set temperature", params);
        if (executed) {
            temperature.setValue(rangeValue);
            //getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setTemperature(int value) {
        LOG.info("Setting behavior \"temperature\" of thing \"" + getPojo().getName() + "\" to \""
                + value + "\"");
        temperature.setValue(value);
        setChanged(true);
    }

    public void executeSetPressure(int rangeValue, Config params) {
        boolean executed = executeCommand("set pressure", params);
        if (executed) {
            pressure.setValue(rangeValue);
            setChanged(true);
        }
    }

    private void setPressure(int value) {
        LOG.info("Setting behavior \"pressure\" of thing \"" + getPojo().getName() + "\" to \""
                + value + "\"");
        pressure.setValue(value);
        setChanged(true);
    }

    public void executeSetHumidity(int rangeValue, Config params) {
        boolean executed = executeCommand("set humidity", params);
        if (executed) {
            humidity.setValue(rangeValue);
            setChanged(true);
        }
    }

    private void setHumidity(int value) {
        LOG.info("Setting behavior \"humidity\" of thing \"" + getPojo().getName() + "\" to \""
                + value + "\"");
        humidity.setValue(value);
        setChanged(true);
    }

    public void executeSetRain(int rangeValue, Config params) {
        boolean executed = executeCommand("set rain", params);
        if (executed) {
            rain.setValue(rangeValue);
            //getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    private void setRain(int value) {
        LOG.info("Setting behavior \"rain\" of thing \"" + getPojo().getName() + "\" to \""
                + value + "\"");
        rain.setValue(value);
        setChanged(true);
    }

    public void executeSetWindSpeed(int rangeValue, Config params) {
        boolean executed = executeCommand("set wind speed", params);
        if (executed) {
            windSpeed.setValue(rangeValue);
            setChanged(true);
        }
    }

    private void setWindSpeed(int value) {
        LOG.info("Setting behavior \"wind speed\" of thing \"" + getPojo().getName() + "\" to \""
                + value + "\"");
        windSpeed.setValue(value);
        setChanged(true);
    }

    public void executeSetWindDegree(int rangeValue, Config params) {
        boolean executed = executeCommand("set wind degree", params);
        if (executed) {
            windDegree.setValue(rangeValue);
            setChanged(true);
        }
    }

    private void setWindDegree(int value) {
        LOG.info("Setting behavior \"wind degree\" of thing \"" + getPojo().getName() + "\" to \""
                + value + "\"");
        windDegree.setValue(value);
        setChanged(true);
    }

    public void setConditions(String selectedCondition, Config params, boolean fireCommand) {
        if (fireCommand) {
            if (executeCommand("set conditions", params)) {
                //Executed succesfully, update the value
                conditions.setSelected(selectedCondition);
                setChanged(true);
            }
        } else {
            // Just a change in the virtual thing status
            conditions.setSelected(selectedCondition);
            setChanged(true);
        }
        setIcon();
    }

    private void setIcon() {
        //getPojo().setCurrentRepresentation(1);
        if (conditions.getSelected().equals("day clear sky")) {
            getPojo().setCurrentRepresentation(1);
        } else if (conditions.getSelected().equals("day few clouds")) {
            getPojo().setCurrentRepresentation(2);
        } else if (conditions.getSelected().equals("day scattered clouds")) {
            getPojo().setCurrentRepresentation(3);
        } else if (conditions.getSelected().equals("day broken clouds")) {
            getPojo().setCurrentRepresentation(4);
        } else if (conditions.getSelected().equals("day shower rain")) {
            getPojo().setCurrentRepresentation(5);
        } else if (conditions.getSelected().equals("day rain")) {
            getPojo().setCurrentRepresentation(6);
        } else if (conditions.getSelected().equals("day thunderstorm")) {
            getPojo().setCurrentRepresentation(7);
        } else if (conditions.getSelected().equals("day snow")) {
            getPojo().setCurrentRepresentation(8);
        } else if (conditions.getSelected().equals("day mist")) {
            getPojo().setCurrentRepresentation(9);
        } else if (conditions.getSelected().equals("night clear sky")) {
            getPojo().setCurrentRepresentation(10);
        } else if (conditions.getSelected().equals("nigh few clouds")) {
            getPojo().setCurrentRepresentation(11);
        } else if (conditions.getSelected().equals("night scattered clouds")) {
            getPojo().setCurrentRepresentation(12);
        } else if (conditions.getSelected().equals("night broken clouds")) {
            getPojo().setCurrentRepresentation(13);
        } else if (conditions.getSelected().equals("night shower rain")) {
            getPojo().setCurrentRepresentation(14);
        } else if (conditions.getSelected().equals("night rain")) {
            getPojo().setCurrentRepresentation(15);
        } else if (conditions.getSelected().equals("night thunderstorm")) {
            getPojo().setCurrentRepresentation(16);
        } else if (conditions.getSelected().equals("night snow")) {
            getPojo().setCurrentRepresentation(17);
        } else if (conditions.getSelected().equals("night mist")) {
            getPojo().setCurrentRepresentation(18);
        }
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

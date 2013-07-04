/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.objects.impl;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ObjectReceiveClick;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.RangedIntBehavior;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.RangedIntBehaviorLogic;
import it.freedomotic.reactions.TriggerPersistence;
import it.freedomotic.reactions.Trigger;

/**
 *
 * @author enrico
 */
public class Thermostat extends EnvObjectLogic {

    private RangedIntBehaviorLogic temperature;

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        temperature = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehaviors().get(0));
        temperature.addListener(new RangedIntBehaviorLogic.Listener() {

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
        Freedomotic.logger.info("Setting behavior 'temperature' of object '" + getPojo().getName() + "' to " + value);
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
        clicked.getPayload().addStatement("object.name", this.getPojo().getName());
        clicked.getPayload().addStatement("click", ObjectReceiveClick.SINGLE_CLICK);
        clicked.setPersistence(false);

        TriggerPersistence.add(clicked);
    }
}

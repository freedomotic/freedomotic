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
public class GenericSensor extends EnvObjectLogic {

    private RangedIntBehaviorLogic readValue;

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        readValue = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehaviors().get(0));
        readValue.addListener(new RangedIntBehaviorLogic.Listener() {

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
        Freedomotic.logger.config("Setting behavior 'readValue' of object '" + getPojo().getName() + "' to " + value);
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
        clicked.getPayload().addStatement("object.name", this.getPojo().getName());
        clicked.getPayload().addStatement("click", ObjectReceiveClick.SINGLE_CLICK);
        clicked.setPersistence(false);

        TriggerPersistence.add(clicked);
    }
}

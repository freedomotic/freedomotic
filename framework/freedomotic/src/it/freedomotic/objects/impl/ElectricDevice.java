/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.objects.impl;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.BooleanBehavior;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.BooleanBehaviorListener;
import it.freedomotic.objects.BooleanBehaviorLogic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.reactions.Command;

/**
 *
 * @author enrico
 */
public class ElectricDevice extends EnvObjectLogic {

    protected BooleanBehaviorLogic powered;
    
    @Override
    public void init() {
        powered = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior("powered"));
        //add a listener to values changes
        powered.addListener(new BooleanBehaviorListener() {

            @Override
            public void onTrue(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executePowerOn(params); //executes a turn on command and then sets the object behavior to on
                } else {
                    setOn(); //sets the object behavior to on as a result from a notified value
                }
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executePowerOff(params); //executes a turn off command and then sets the object behavior to off
                } else {
                    setOff(); //sets the object behavior to off as a result from a notified value
                }
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(powered);
        //caches hardware level commands and builds user command for the Electric Devices
        super.init();
    }

    /**
     * Causes the execution of the related hardware command to turn on this electric device,
     * updates the object representation and notifies the changes with an event.
     * @param params
     */
    public void executePowerOn(Config params) {
        boolean executed = executeCommand("turn on", params);
        if (executed) {
            setOn();
        }
    }

    /**
     * Causes the execution of the related hardware command to turn off this electric device,
     * updates the object representation and notifies the changes with an event.
     * @param params
     */
    public void executePowerOff(Config params) {
        boolean executed = executeCommand("turn off", params);
        if (executed) {
            setOff();
        }
    }

    private void setOn() {
        Freedomotic.logger.config("Setting behavior 'powered' of object '" + getPojo().getName() + "' to true");
        //if not already on
        if (powered.getValue() != true) {
            //setting the object as powered
            powered.setValue(true);
            //setting the second view from the XML list (the one with the on light bulb image)
            getPojo().setCurrentRepresentation(1);
            setChanged(true);
        }
    }

    private void setOff() {
        Freedomotic.logger.config("Setting behavior 'powered' of object '" + getPojo().getName() + "' to false");
        //if not already off
        if (powered.getValue() != false) {
            powered.setValue(false);
            getPojo().setCurrentRepresentation(0);
            setChanged(true);
        }
    }

    /**
     * Creates user level commands for this class of freedomotic objects
     */
    @Override
    protected void createCommands() {
        Command setOn = new Command();
        setOn.setName("Turn on " + getPojo().getName());
        setOn.setDescription(getPojo().getName() + " turns on");
        setOn.setReceiver("app.events.sensors.behavior.request.objects");
        setOn.setProperty("object", getPojo().getName());
        setOn.setProperty("behavior", "powered");
        setOn.setProperty("value", "true");

        Command setOff = new Command();
        setOff.setName("Turn off " + getPojo().getName());
        setOff.setDescription(getPojo().getName() + " turns off");
        setOff.setReceiver("app.events.sensors.behavior.request.objects");
        setOff.setProperty("object", getPojo().getName());
        setOff.setProperty("behavior", "powered");
        setOff.setProperty("value", "false");

        Command switchPower = new Command();
        switchPower.setName("Switch " + getPojo().getName() + " power");
        switchPower.setDescription("switches the power of " + getPojo().getName());
        switchPower.setReceiver("app.events.sensors.behavior.request.objects");
        switchPower.setProperty("object", getPojo().getName());
        switchPower.setProperty("behavior", "powered");
        switchPower.setProperty("value", "opposite");


        Command setItOn = new Command();
        setItOn.setName("Turn it on");
        setItOn.setDescription(getPojo().getName() + " turns on");
        setItOn.setReceiver("app.events.sensors.behavior.request.objects");
        setItOn.setProperty("object", "@event.object.name");
        setItOn.setProperty("behavior", "powered");
        setItOn.setProperty("value", "true");

        Command setItOff = new Command();
        setItOff.setName("Turn it off");
        setItOff.setDescription(getPojo().getName() + " turns off");
        setItOff.setReceiver("app.events.sensors.behavior.request.objects");
        setItOff.setProperty("object", "@event.object.name");
        setItOff.setProperty("behavior", "powered");
        setItOff.setProperty("value", "false");

        Command switchItsPower = new Command();
        switchItsPower.setName("Switch its power");
        switchItsPower.setDescription(getPojo().getName() + " switches its power");
        switchItsPower.setReceiver("app.events.sensors.behavior.request.objects");
        switchItsPower.setProperty("object", "@event.object.name");
        switchItsPower.setProperty("behavior", "powered");
        switchItsPower.setProperty("value", "opposite");
        
        CommandPersistence.add(setOff);
        CommandPersistence.add(setOn);
        CommandPersistence.add(switchPower);
        CommandPersistence.add(setItOff);
        CommandPersistence.add(setItOn);
        CommandPersistence.add(switchItsPower);
    }
}

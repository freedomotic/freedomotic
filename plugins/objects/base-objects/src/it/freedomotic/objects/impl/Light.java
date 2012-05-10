/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.objects.impl;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.RangedIntBehavior;
import it.freedomotic.objects.RangedIntBehaviorListener;
import it.freedomotic.objects.RangedIntBehaviorLogic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.reactions.Command;

/**
 *
 * @author Enrico
 */
public class Light extends ElectricDevice {

    private RangedIntBehaviorLogic brightness;

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        brightness = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("brightness"));
        brightness.addListener(new RangedIntBehaviorListener() {

            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                executePowerOff(params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                executePowerOn(params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                setBrightness(rangeValue, params);
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(brightness);
        super.init();
    }

    @Override
    public void executePowerOff(Config params) {
        //executeCommand the body of the super implementation
        super.executePowerOff(params);
        /*
         * Not called the setBrightness because this method executeCommand a command
         * here we want only to mantain the system coerent.
         * If we call setBrightness(0, params) the light recalls the onLowerBoundValue.
         * Only ONE command execution per situation
         */
        brightness.setValue(0);
    }

    @Override
    public void executePowerOn(Config params) {
        //executeCommand the body of the super implementation
        super.executePowerOn(params);
        /*
         * Not called the setBrightness because this method executeCommand a command
         * here we want only to mantain the system coerent.
         * If we call setBrightness(100, params) the light recalls the onUpperBoundValue.
         * Only ONE command execution per situation
         */
        brightness.setValue(100);
    }

    public void setBrightness(int rangeValue, Config params) {
        boolean executed = executeCommand("set brightness", params); //executes the developer level command associated with 'set brightness' action
        if (executed) {
            powered.setValue(true);
            brightness.setValue(rangeValue);
            //set the light graphical representation
            getPojo().setCurrentRepresentation(1); //points to the second element in the XML views array (light on image)
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " brightness to 50%");
        a.setDescription("the light " + getPojo().getName() + " changes its brightness");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", "brightness");
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName("Increase " + getPojo().getName() + " brightness");
        b.setDescription("increases " + getPojo().getName() + " brigthness of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object", getPojo().getName());
        b.setProperty("behavior", "brighness");
        b.setProperty("value", "next");

        Command c = new Command();
        c.setName("Decrease " + getPojo().getName() + " brightness");
        c.setDescription("decreases " + getPojo().getName() + " brigthness of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object", getPojo().getName());
        c.setProperty("behavior", "brighness");
        c.setProperty("value", "previous");

        Command d = new Command();
        d.setName("Set its brightness to 50%");
        d.setDescription("set its brighness to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", "brighness");
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName("Increase its brightness");
        e.setDescription("increases its brigthness of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", "brighness");
        e.setProperty("value", "next");

        Command f = new Command();
        f.setName("Decrease its brightness");
        f.setDescription("decreases its brigthness of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", "brighness");
        f.setProperty("value", "previous");


        Command g = new Command();
        g.setName("Set its brightness to the value in the event");
        g.setDescription("set its brighness to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", "brighness");
        g.setProperty("value", "@event.value");




        CommandPersistence.add(a);
        CommandPersistence.add(b);
        CommandPersistence.add(c);
        CommandPersistence.add(d);
        CommandPersistence.add(e);
        CommandPersistence.add(f);
        CommandPersistence.add(g);
    }
}

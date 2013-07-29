package it.freedom.objects.impl;

import it.freedom.model.ds.Config;
import it.freedom.model.object.BooleanBehavior;
import it.freedom.model.object.ListBehavior;
import it.freedom.model.object.RangedIntBehavior;
import it.freedom.objects.BooleanBehaviorListener;
import it.freedom.objects.BooleanBehaviorLogic;
import it.freedom.objects.ListBehaviorListener;
import it.freedom.objects.ListBehaviorLogic;
import it.freedom.objects.RangedIntBehaviorListener;
import it.freedom.objects.RangedIntBehaviorLogic;
import it.freedom.persistence.CommandPersistence;
import it.freedom.reactions.Command;

/**
 *
 * @author gpt
 */
public class StepperMotor extends ElectricDevice {

    public RangedIntBehaviorLogic position;
    //public RangedIntBehaviorLogic channel; //Maybe the channell is not a behavior. Only an action.
    //(In the Kuro TV can't ask to the current channel
    //public ListBehaviorLogic input; // I defined it as a RangedIt because each TV model has its own inputs
    //public BooleanBehaviorLogic muted;
    //public ListBehaviorLogic avSelection;
    //public ListBehaviorLogic screenMode;

    @Override
    public void init() {
        super.init();

        //linking this property with the behavior defined in the XML
        position = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("position"));
        position.addListener(new RangedIntBehaviorListener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                //turnPowerOff(params);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //turnPowerOn(params);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetPosition(rangeValue, params);
                } else {
                    setPosition(rangeValue);
                }
            }
        });
        registerBehavior(position);
    }

    public void executeSetPosition(int rangeValue, Config params) {
        boolean executed = executeCommand("set position", params); //executes the developer level command associated with 'set volume' action
        if (executed) {
            setPosition(rangeValue);
            //TODO: set the light graphical representation
            //setCurrentRepresentation(1); //points to the second element in the XML views array (light on image)
            setChanged(true);
        }
    }

    public void setPosition(int rangeValue) {
        if (position.getValue() != rangeValue) {
            position.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void setPositionUp(Config params) {
        boolean executed = executeCommand("set position up", params); //executes the developer level command associated with 'set channel' action
        if (executed) {
            if (position.getValue() != position.getMax()) {
                position.setValue(position.getValue() + 1);
                setChanged(true);
            }
        }
    }

    public void setPositionDown(Config params) {
        boolean executed = executeCommand("set position down", params); //executes the developer level command associated with 'set channel' action
        if (executed) {
            if (position.getValue() != position.getMin()) {
                position.setValue(position.getValue() - 1);
                setChanged(true);
            }
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " position to 50%");
        a.setDescription("the StepperMotor " + getPojo().getName() + " changes its position to 50%");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", "position");
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName(getPojo().getName() + " position up");
        b.setDescription("increases " + getPojo().getName() + " position of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object", getPojo().getName());
        b.setProperty("behavior", "position");
        b.setProperty("value", "next");

        Command c = new Command();
        c.setName(getPojo().getName() + " position down");
        c.setDescription("decreases " + getPojo().getName() + " position of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object", getPojo().getName());
        c.setProperty("behavior", "position");
        c.setProperty("value", "previous");

        Command d = new Command();
        d.setName("Set its position to 50%");
        d.setDescription("set its position to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", "position");
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName("Increase its position");
        e.setDescription("increases its position of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", "position");
        e.setProperty("value", "next");

        Command f = new Command();
        f.setName("Decrease its position");
        f.setDescription("decreases its position of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", "position");
        f.setProperty("value", "previous");


        Command g = new Command();
        g.setName("Set its position to the value in the event");
        g.setDescription("set its position to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", "position");
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

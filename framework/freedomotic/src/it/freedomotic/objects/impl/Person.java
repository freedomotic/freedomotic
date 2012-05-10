package it.freedomotic.objects.impl;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.BooleanBehavior;
import it.freedomotic.model.object.ListBehavior;
import it.freedomotic.model.object.PropertiesBehavior;
import it.freedomotic.objects.BooleanBehaviorListener;
import it.freedomotic.objects.BooleanBehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.ListBehaviorListener;
import it.freedomotic.objects.ListBehaviorLogic;
import it.freedomotic.objects.PropertiesBehaviorListener;
import it.freedomotic.objects.PropertiesBehaviorLogic;

/**
 *
 * @author Enrico
 */
public class Person extends EnvObjectLogic {

    protected BooleanBehaviorLogic present;
    protected ListBehaviorLogic activity;
    protected PropertiesBehaviorLogic properties;

    @Override
    public void init() {
        present = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior("present"));
        //add a listener to values changes
        present.addListener(new BooleanBehaviorListener() {

            @Override
            public void onTrue(Config params, boolean fireCommand) {
                setPresent();
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                setNotPresent();
            }
        });

        activity = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior("activity"));
        activity.addListener(new ListBehaviorListener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                String oldActivity = activity.getSelected();
                //in "value" property is stored the name of the new selection. It is a value from the list for sure and it is not the current one, already checked.
                activity.setSelected(params.getProperty("value"));
                Freedomotic.logger.severe("Person '" + getPojo().getName() + "' has changed its activity from " + oldActivity + " to " + activity.getSelected());
                setChanged(true);
            }
        });

        properties = new PropertiesBehaviorLogic((PropertiesBehavior) getPojo().getBehavior("properties"));
        properties.addListener(new PropertiesBehaviorListener() {

            @Override
            public void propertyChanged(String key, String newValue, Config params, boolean fireCommand) {
                //in "value" property is stored the name of the new selection. It is a value from the list for sure and it is not the current one, already checked.
               // properties.setProperty(key, newValue);
                Freedomotic.logger.severe("Person '" + getPojo().getName() + "' has changed its property from " + params.getProperty(key) + " to " + newValue);
                setChanged(true);
            }
        });

        //register this behavior to the superclass to make it visible to it
        registerBehavior(present);
        registerBehavior(activity);
        registerBehavior(properties);
        //caches hardware level commands and builds user command
        super.init();
    }

    private void setPresent() {
        present.setValue(true);
        getPojo().setCurrentRepresentation(1);
        setChanged(true);
    }

    private void setNotPresent() {
        present.setValue(false);
        getPojo().setCurrentRepresentation(0);
        setChanged(true);
    }

    @Override
    protected void createCommands() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}

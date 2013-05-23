/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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
package it.freedomotic.objects.impl;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.events.ObjectReceiveClick;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.BooleanBehavior;
import it.freedomotic.model.object.ListBehavior;
import it.freedomotic.model.object.PropertiesBehavior;

import it.freedomotic.objects.BooleanBehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.ListBehaviorLogic;
import it.freedomotic.objects.PropertiesBehaviorLogic;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.TriggerPersistence;

/**
 *
 * @author Enrico
 */
public class Person
        extends EnvObjectLogic {

    protected BooleanBehaviorLogic present;
    protected ListBehaviorLogic activity;
    protected PropertiesBehaviorLogic properties;

    @Override
    public void init() {
        present = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehaviors().get(0));
        //add a listener to values changes
        present.addListener(new BooleanBehaviorLogic.Listener() {
            @Override
            public void onTrue(Config params, boolean fireCommand) {
                setPresent();
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                setNotPresent();
            }
        });

        activity = new ListBehaviorLogic((ListBehavior) getPojo().getBehaviors().get(1));
        activity.addListener(new ListBehaviorLogic.Listener() {
            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                String oldActivity = activity.getSelected();
                //in "value" property is stored the name of the new selection. It is a value from the list for sure and it is not the current one, already checked.
                activity.setSelected(params.getProperty("value"));
                Freedomotic.logger.severe("Person '" + getPojo().getName()
                        + "' has changed its activity from " + oldActivity + " to "
                        + activity.getSelected());
                setChanged(true);
            }
        });

        properties = new PropertiesBehaviorLogic((PropertiesBehavior) getPojo().getBehaviors().get(2));
        properties.addListener(new PropertiesBehaviorLogic.Listener() {
            @Override
            public void propertyChanged(String key, String newValue, Config params, boolean fireCommand) {
                //in "value" property is stored the name of the new selection. It is a value from the list for sure and it is not the current one, already checked.
                // properties.setProperty(key, newValue);
                Freedomotic.logger.severe("Person '" + getPojo().getName()
                        + "' has changed its property from " + params.getProperty(key) + " to "
                        + newValue);
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

        TriggerPersistence.add(clicked);
    }
}

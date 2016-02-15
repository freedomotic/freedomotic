/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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

import com.google.inject.Inject;
import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.model.object.DataBehavior;
import com.freedomotic.things.DataBehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.util.I18n.I18n;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class DataChart extends EnvObjectLogic {

    private DataBehaviorLogic data;
    protected final static String BEHAVIOR_DATA = "data";
    //private List<DataToPersist> assocData = new ArrayList<DataToPersist>();
   // private String JSONData;
    @Inject 
    private I18n I18n;

    @Override
    public void init() {       
        data = new DataBehaviorLogic((DataBehavior) getPojo().getBehavior(BEHAVIOR_DATA));
        data.addListener(new DataBehaviorLogic.Listener() {
            @Override
            public void onReceiveData(Config params, boolean fireCommand) {
                String JSONdata = params.getProperty("value");
                
                if (JSONdata != null && !JSONdata.isEmpty()) {
                    data.setData(JSONdata);
                }
            }

        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(data);
        //caches hardware level commands and builds user command for the Electric Devices
        super.init();

    }
    
    @Override
    protected void createTriggers() {
        super.createTriggers();
        Trigger clicked = new Trigger();
        clicked.setName(I18n.msg("when_X_is_clicked", new Object[]{this.getPojo().getName()}));
        clicked.setChannel("app.event.sensor.object.behavior.clicked");
        clicked.getPayload().addStatement("object.name",
                this.getPojo().getName());
        clicked.getPayload().addStatement("click", ObjectReceiveClick.SINGLE_CLICK);
        clicked.setPersistence(false);
    }

    @Override
    protected void createCommands() {
                Command setOn = new Command();
        setOn.setName("Update " + getPojo().getName() + "data");
        setOn.setDescription(getPojo().getName() + " requests data update");
        setOn.setReceiver("app.events.sensors.behavior.request.objects");
        setOn.setProperty("object",
                getPojo().getName());
        setOn.setProperty("behavior", BEHAVIOR_DATA);
        setOn.setProperty("value", BooleanBehavior.VALUE_TRUE);
    }

}

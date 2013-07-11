/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package it.freedomotic.objects.impl;

import com.google.inject.Inject;
import es.gpulido.harvester.persistence.DataFrame;
import es.gpulido.harvester.persistence.DataToPersist;
import it.freedomotic.events.ObjectReceiveClick;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.BooleanBehavior;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.util.I18n.I18n;
import it.mazzoni.harvester.object.DataBehavior;
import it.mazzoni.harvester.object.DataBehaviorLogic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.POJONode;

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
        data = new DataBehaviorLogic((DataBehavior) getPojo().getBehaviors().get(0));
        data.addListener(new DataBehaviorLogic.Listener() {
            @Override
            public void onReceiveData(Config params, boolean fireCommand) {
                String JSONdata = params.getProperty("value");
                
                if (JSONdata != null && !JSONdata.isEmpty()) {
                    data.setData(JSONdata);
                    /*
                     * ObjectMapper om = new ObjectMapper();
                    try {
                        DataFrame df = om.readValue(JSONdata, DataFrame.class);
                        switch (df.getType()){
                            case DataFrame.FULL_UPDATE:
                                //assocData = df.getData();
                                data.setData(JSONdata);
                                break;
                            case DataFrame.INCREMENTAL_UPDATE:
                                //assocData.addAll(df.getData());
                                break;
                            default:        
                        }
                      
                    } catch (IOException ex) {
                        Logger.getLogger(DataChart.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    */
                }
            }

        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(data);
        //caches hardware level commands and builds user command for the Electric Devices
        super.init();

    }
    
  /*  public List<DataToPersist> getAssocData(){
        return this.assocData;
    }
*/
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

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
package it.mazzoni.harvester.object;

import it.freedomotic.model.ds.Config;
import it.freedomotic.objects.BehaviorLogic;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class DataBehaviorLogic implements BehaviorLogic{
    
    private DataBehavior data;
    private boolean changed;
    private DataBehaviorLogic.Listener listener;
    
    public interface Listener {
        
           public void onReceiveData(Config params, boolean fireCommand);
           
    }
    
    public DataBehaviorLogic(DataBehavior pojo){
        this.data=pojo;
    }
    
    public void addListener(DataBehaviorLogic.Listener dataBehaviorListener) {
        this.listener = dataBehaviorListener;
    }
    
    @Override
    public void filterParams(Config params, boolean fireCommand) {
        // send hardware command: ask Harvester for data /subscribe
        //if (params.getProperty("behaviorValue") != null){
        //change behavior value: send data to the Graph Object
        listener.onReceiveData(params, fireCommand);
        //}
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public void setChanged(boolean value) {
        changed = value;
    }

    @Override
    public boolean isActive() {
        return data.isActive();
    }

    @Override
    public boolean isReadOnly() {
        return data.isReadOnly();
    }

    @Override
    public String getValueAsString() {
        return data.getJSON();
    }
    
    public void setData(String JSON){
        data.setJSON(JSON);
    }
    
    
}

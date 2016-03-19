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
package com.freedomotic.behaviors;

import com.freedomotic.model.charting.UsageData;
import com.freedomotic.model.charting.UsageDataFrame;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.DataBehavior;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Matteo Mazzoni
 */
public class DataBehaviorLogic implements BehaviorLogic {

    private static final Logger LOG = LoggerFactory.getLogger(DataBehaviorLogic.class.getName());
    private DataBehavior data;
    private boolean changed;
    private DataBehaviorLogic.Listener listener;
    private static ObjectMapper om = new ObjectMapper();

    /**
     *
     */
    public interface Listener {

        /**
         *
         * @param params
         * @param fireCommand
         */
        public void onReceiveData(Config params, boolean fireCommand);

    }

    /**
     *
     * @param pojo
     */
    public DataBehaviorLogic(DataBehavior pojo) {
        this.data = pojo;
    }

    /**
     *
     * @param dataBehaviorListener
     */
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

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return data.getName();
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isChanged() {
        return changed;
    }

    /**
     *
     * @param value
     */
    @Override
    public void setChanged(boolean value) {
        changed = value;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isActive() {
        return data.isActive();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isReadOnly() {
        return data.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean value) {
        data.setReadOnly(value);
    }

    /**
     *
     * @return
     */
    @Override
    public String getValueAsString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            om.writeValue(os, data.getData());
            return os.toString();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    /**
     *
     * @param JSON
     */
    public void setData(String JSON) {
        UsageDataFrame df;

        //     om.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            df = om.readValue(JSON, UsageDataFrame.class);
            if (df.getFrameType() == UsageDataFrame.FULL_UPDATE) {
                this.data.setData(df.getData());
            } else if (df.getFrameType() == UsageDataFrame.INCREMENTAL_UPDATE) {
                this.data.addData(df.getData());
            }
            setChanged(true);
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }

    }

    /**
     *
     * @return
     */
    public List<UsageData> getData() {
        return data.getData();
    }
}

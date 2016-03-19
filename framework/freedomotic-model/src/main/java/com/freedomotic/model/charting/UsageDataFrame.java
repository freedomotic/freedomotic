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
package com.freedomotic.model.charting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Matteo Mazzoni
 */
public class UsageDataFrame implements Serializable {

    /**
     *
     */
    public final static int FULL_UPDATE = 1;

    /**
     *
     */
    public final static int INCREMENTAL_UPDATE = 2;
    private int frameType;
    private List<UsageData> data;

    /**
     *
     * @param type
     * @param data
     */
    public UsageDataFrame(int type, List<UsageData> data) {
        this.frameType = type;
        this.data = data;
    }
    // used by Jackson , do not remove

    /**
     *
     */
    public UsageDataFrame() {
    }

    /**
     *
     * @return
     */
    public int getFrameType() {
        return this.frameType;
    }

    /**
     *
     * @return
     */
    public List<UsageData> getData() {
        return this.data;
    }

    /**
     *
     * @return
     */
    public List<HashMap<String, String>> getDataAsMap() {
        List<HashMap<String, String>> lst = new ArrayList<HashMap<String, String>>();
        for (UsageData dat : data) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("address", dat.objaddress);
            map.put("uuid", dat.uuid);
            map.put("rowId", String.valueOf(dat.id));
            map.put("datetime", dat.datetime.toString());
            map.put("behavior", dat.objbehavior);
            map.put("name", dat.objname);
            map.put("protocol", dat.objprotocol);
            map.put("behaviorValue", dat.objvalue);
            lst.add(map);
        }
        return lst;
    }

    /**
     *
     * @param type
     */
    public void setDataFrameType(int type) {
        this.frameType = type;
    }

    /**
     *
     * @param data
     */
    public void setData(List<UsageData> data) {
        this.data = data;
    }

    /**
     *
     * @param data
     */
    public void addData(List<UsageData> data) {
        this.data.addAll(data);
    }

}

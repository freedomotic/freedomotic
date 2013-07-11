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

import es.gpulido.harvester.persistence.DataToPersist;
import it.freedomotic.model.object.Behavior;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class DataBehavior extends Behavior{
    
   // private List<DataToPersist> value;
    private String searchtype;
    private String searchfilter;
    private Date startdate;
    private Date enddate;
    
    private volatile String JSON;
    
    public String getType(){
        return searchtype;
    }
  /*  public void setData(List <DataToPersist> data){      
        this.value = data;
    }
    
    public void addData(List<DataToPersist> data){
        this.value.addAll(data);
    }
    
    */
    @Override
    public String toString(){
        return JSON;
    }
    
    public String getJSON(){
        return JSON;
    }
    
    public void setJSON(String json){
        this.JSON = json;
    }
    
    
}

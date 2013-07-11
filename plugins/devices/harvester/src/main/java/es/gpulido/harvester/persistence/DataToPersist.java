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
package es.gpulido.harvester.persistence;

import es.gpulido.harvester.DTPIface;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
@Entity(name = "harvester")
@NamedQueries({
    @NamedQuery(name = "powered", query = "SELECT x FROM harvester x WHERE (x.datetime >= :startDate AND x.datetime <= :stopDate) AND x.objbehavior = 'powered' AND x.objaddress LIKE :address AND x.objprotocol LIKE :protocol AND x.uuid LIKE :uuid"),
    @NamedQuery(name = "noDate", query = "SELECT x FROM harvester x WHERE x.objbehavior LIKE :behavior AND x.objaddress = :address AND x.objvalue LIKE :value"),
    @NamedQuery(name = "rangedPowered", query = "SELECT x.* , min(y.datetime) as offtime FROM harvester x, harvester y WHERE  (x.datetime >= :startDate AND x.datetime <= :stopDate) AND x.objbehavior = 'powered' AND x.objaddress LIKE :address AND x.objvalue = 'true' AND x.objprotocol LIKE '%' and y.objname = x.objname AND y.objbehavior = x.objbehavior AND y.objvalue = 'false' AND y.datetime > x.datetime GROUP by x.datetime order by x.datetime, y.datetime")        
})
public class DataToPersist implements DTPIface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Temporal(TemporalType.TIMESTAMP)
    Date datetime;
    String objprotocol;
    String objname;
    String objaddress;
    String objbehavior;
    String objvalue;
    String uuid;
    
    @Override
    public DataToPersist clone() {
        DataToPersist cloned = new DataToPersist();
        cloned.objaddress = this.objaddress;
        cloned.objprotocol = this.objprotocol;
        cloned.objname = this.objname;
        cloned.datetime = this.datetime;
        cloned.uuid = this.uuid;
        return cloned;
    }

    public DataToPersist() {
    }
    
    public String getObjProtocol() {
        return objprotocol;
    }

    public String getObjAddress() {
        return objaddress;
    }

    public String getObjBehavior() {
        return objbehavior;
    }

    public String getObjName() {
        return objname;
    }

    public String getObjValue() {
        return objvalue;
    }
    public Date getDateTime(){
        return datetime;
    }
    public String getUuid(){
        return uuid;
    }

    public void setDateTime(Date datetime){
        this.datetime = datetime;
    }
     public void setObjValue(String val){
         this.objvalue = val;
     }
     public void setObjProtocol(String val){
         this.objprotocol = val;
     }
     public void setObjName(String val){
         this.objname = val;
     }
     public void setObjBehavior(String val){
         this.objbehavior = val;
     }
     public void setObjAddress(String val){
         this.objaddress = val;
     }
     public void setUuid(String val){
         this.uuid = val;
     }
     
    @Override
    public String toJSON() {
        ObjectMapper om = new ObjectMapper();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            om.writeValue(os, this);
            return os.toString();
        } catch (Exception ex) {
            Logger.getLogger(DataToPersist.class.getName()).severe(ex.getLocalizedMessage());
        }
        return "";
    }
    

}

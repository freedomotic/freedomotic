/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.model.charting;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
@Entity(name = "harvester")
@NamedQueries({
    @NamedQuery(name = "powered", query = "SELECT x FROM harvester x WHERE (x.datetime >= :startDate AND x.datetime <= :stopDate) AND x.objbehavior = 'powered' AND x.objprotocol LIKE :protocol AND x.uuid LIKE :uuid"),
    @NamedQuery(name = "noDate", query = "SELECT x FROM harvester x WHERE x.objbehavior LIKE :behavior AND x.objaddress LIKE :address AND x.objvalue LIKE :value"),
    @NamedQuery(name = "rangedPowered", query = "SELECT x.* , min(y.datetime) as offtime FROM harvester x, harvester y WHERE  (x.datetime >= :startDate AND x.datetime <= :stopDate) AND x.objbehavior = 'powered' AND x.objaddress LIKE :address AND x.objvalue = 'true' AND x.objprotocol LIKE '%' and y.objname = x.objname AND y.objbehavior = x.objbehavior AND y.objvalue = 'false' AND y.datetime > x.datetime GROUP by x.datetime order by x.datetime, y.datetime")
})
public class UsageData implements Cloneable {

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

    /**
     *
     * @return
     */
    @Override
    public UsageData clone() {
        UsageData cloned = new UsageData();
        cloned.setObjName(this.objaddress);
        cloned.setObjProtocol(this.objprotocol);
        cloned.setObjName(this.objname);
        cloned.setDateTime(this.datetime);
        cloned.setUuid(this.uuid);
        return cloned;
    }

    /**
     *
     */
    public UsageData() {
    }

    /**
     *
     * @return
     */
    public String getObjProtocol() {
        return objprotocol;
    }

    /**
     *
     * @return
     */
    public String getObjAddress() {
        return objaddress;
    }

    /**
     *
     * @return
     */
    public String getObjBehavior() {
        return objbehavior;
    }

    /**
     *
     * @return
     */
    public String getObjName() {
        return objname;
    }

    /**
     *
     * @return
     */
    public String getObjValue() {
        return objvalue;
    }

    /**
     *
     * @return
     */
    public Date getDateTime() {
        return datetime;
    }

    /**
     *
     * @return
     */
    public int getID() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getUuid() {
        return uuid;
    }

    /**
     *
     * @param datetime
     */
    public void setDateTime(Date datetime) {
        this.datetime = datetime;
    }

    /**
     *
     * @param val
     */
    public void setObjValue(String val) {
        this.objvalue = val;
    }

    /**
     *
     * @param val
     */
    public void setObjProtocol(String val) {
        this.objprotocol = val;
    }

    /**
     *
     * @param val
     */
    public void setObjName(String val) {
        this.objname = val;
    }

    /**
     *
     * @param val
     */
    public void setObjBehavior(String val) {
        this.objbehavior = val;
    }

    /**
     *
     * @param val
     */
    public void setObjAddress(String val) {
        this.objaddress = val;
    }

    /**
     *
     * @param val
     */
    public void setUuid(String val) {
        this.uuid = val;
    }


}

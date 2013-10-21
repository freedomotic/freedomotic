/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.model.charting;

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
    @NamedQuery(name = "powered", query = "SELECT x FROM harvester x WHERE (x.datetime >= :startDate AND x.datetime <= :stopDate) AND x.objbehavior = 'powered' AND x.objaddress LIKE :address AND x.objprotocol LIKE :protocol AND x.uuid LIKE :uuid"),
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

    public UsageData() {
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

    public Date getDateTime() {
        return datetime;
    }

    public int getID() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setDateTime(Date datetime) {
        this.datetime = datetime;
    }

    public void setObjValue(String val) {
        this.objvalue = val;
    }

    public void setObjProtocol(String val) {
        this.objprotocol = val;
    }

    public void setObjName(String val) {
        this.objname = val;
    }

    public void setObjBehavior(String val) {
        this.objbehavior = val;
    }

    public void setObjAddress(String val) {
        this.objaddress = val;
    }

    public void setUuid(String val) {
        this.uuid = val;
    }


}

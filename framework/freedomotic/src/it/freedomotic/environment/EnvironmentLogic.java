package it.freedomotic.environment;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.model.geometry.FreedomPolygon;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.impl.Gate;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.util.Graph;
import it.freedomotic.util.Info;
import it.freedomotic.util.UidGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author enrico
 */
public class EnvironmentLogic {

    private static Graph graph;
    private static Environment pojo;
    private static ArrayList<ZoneLogic> zones = new ArrayList<ZoneLogic>();

    public EnvironmentLogic() {
    }

    public Environment getPojo() {
        return pojo;
    }

    public void setPojo(Environment pojo) {
        this.pojo = pojo;
    }

    public static Graph getGraph() {
        return graph;
    }

    public ArrayList<Room> getRooms() {
        ArrayList<Room> rooms = new ArrayList<Room>();
        for (ZoneLogic zone : getZones()) {
            if (zone instanceof Room) {
                rooms.add((Room) zone);
            }
        }
        return rooms;
    }

    public void addRoom(ZoneLogic zone) {
        //null and duplicate check
        if (zone == null) {
            zone = new ZoneLogic();
            zone.setPojo(new Zone());
        }

        if (zones.contains(zone)) {
            Freedomotic.logger.warning("Attempt to add a null or already existent room");
            return;
        }

        //check for vaild name

        if ((zone.getPojo().getName() == null) || (zone.getPojo().getName().isEmpty())) {
            zone.getPojo().setName("Unamed Zone " + UidGenerator.getNextStringUid());
        }

        zone.getPojo().setDescription("");
        //check for valid shape
        if (zone.getPojo().getShape() == null) {
            //a default shape
            FreedomPolygon p = new FreedomPolygon();
            p.append(0, 0);
            p.append(200, 0);
            p.append(200, 200);
            p.append(0, 200);
            zone.getPojo().setShape(p);
        }
        //append to list and initilize

        getPojo().getZones().add(zone.getPojo());
        zones.add(zone);

        zone.init();

        if (zone.getPojo().isRoom()) {
            Room room = (Room) zone;
            room.init();
            Iterator it = EnvObjectPersistence.iterator();
            //check if this rooms has gates
            while (it.hasNext()) {
                EnvObjectLogic obj = (EnvObjectLogic) it.next();
                if (obj instanceof Gate) {
                    Gate gate = (Gate) obj;
                    gate.evaluateGate();
                }
            }
            room.setChanged();
        } else {
            zone.setChanged();
        }
    }

    public void removeZone(ZoneLogic zone) {
        getPojo().getZones().remove(zone.getPojo());
        zones.remove(zone);
    }

    public int getLastObjectIndex() {
        return EnvObjectPersistence.size();
    }

    public void clear() {
        //release resources
        try {
            getPojo().clear();
            graph = null;
            zones.clear();
            zones = null;
            EnvObjectPersistence.clear();
        } catch (Exception e) {
        }
    }

    public void init() {
        graph = new Graph(); //the graph data structure that describes how rooms are connected through gates
        if (zones == null) {
            zones = new ArrayList<ZoneLogic>();
        }
        for (Zone z : getPojo().getZones()) {
            z.init();
            //null and duplicate check
            if (z != null) {
                if (z.isRoom()) {
                    Room room = new Room();
                    room.setPojo(z);
                    room.init();
                    if (!zones.contains(room)) {
                        Freedomotic.logger.info("Adding room " + room);
                        this.zones.add(room);
                    } else {
                        Freedomotic.logger.warning("Attempt to add a null or an already existent room " + room);
                    }
                } else {
                    ZoneLogic zoneLogic = new ZoneLogic();
                    zoneLogic.setPojo(z);
                    zoneLogic.init();
                    if (!zones.contains(zoneLogic)) {
                        Freedomotic.logger.info("Adding zone " + zoneLogic);
                        this.zones.add(zoneLogic);
                    } else {
                        Freedomotic.logger.warning("Attempt to add a null or an already existent zone " + zoneLogic);
                    }
                }
            }
        }
    }

    public Iterable<ZoneLogic> getZones() {
        return zones;
    }
}

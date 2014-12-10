/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.environment;

import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.util.UidGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author enrico
 */
public final class EnvironmentLogic {

    private static final Logger LOG = Logger.getLogger(EnvironmentLogic.class.getName());

    private Graph graph = null;
    private Environment pojo = null;
    private List<ZoneLogic> zones = new ArrayList<ZoneLogic>();
    private File source = null;

    /**
     * Instantiation is disabled outside this package.
     */
    public EnvironmentLogic() {

    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public Environment getPojo() {
        return pojo;
    }

    /**
     *
     * @param pojo
     */
    @RequiresPermissions("environments:update")
    public void setPojo(Environment pojo) {
        if ((pojo.getUUID() == null) || pojo.getUUID().isEmpty()) {
            pojo.setUUID(UUID.randomUUID().toString());
        }

        this.pojo = pojo;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public Graph getGraph() {
        return graph;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<Room>();

        for (ZoneLogic zone : getZones()) {
            if (zone instanceof Room) {
                rooms.add((Room) zone);
            }
        }

        return rooms;
    }

    /**
     *
     * @param zone
     */
    @RequiresPermissions({"environments:update", "zones:create"})
    public void addRoom(ZoneLogic zone) {
        //null and duplicate check
        if (zone == null) {
            zone = new ZoneLogic(new Zone());
        }

        if (zones.contains(zone)) {
            LOG.warning("Attempt to add a null or already existent room");

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

        zone.init(this);

        //REGRESSION
//        if (zone.getPojo().isRoom()) {
//            Room room = (Room) zone;
//            room.init(this);
//            Iterator<EnvObjectLogic> it = api.things().findAll().iterator();
//            //check if this rooms has gates
//            while (it.hasNext()) {
//                EnvObjectLogic obj = it.next();
//                if (obj instanceof GenericGate) {
//                    GenericGate gate = (GenericGate) obj;
//                    gate.evaluateGate();
//                }
//            }
//            try {
//                room.setChanged();
//            } catch (Exception e) {
//                LOG.warning("Cannot notify room changes");
//            }
//        } else {
//            try {
//                zone.setChanged();
//            } catch (Exception e) {
//                LOG.warning("Cannot notify room changes");
//            }
//        }
    }

    /**
     *
     * @param zone
     */
    @RequiresPermissions("environments:delete")
    public void removeZone(ZoneLogic zone) {
        getPojo().getZones().remove(zone.getPojo());
        zones.remove(zone);
    }

    /**
     *
     */
    @RequiresPermissions("environments:delete")
    public void clear() {
        //release resources
        try {
            getPojo().clear();
            graph = null;
            zones.clear();
            zones = null;
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    @RequiresPermissions("environments:read")
    public void init() {
        graph = new Graph(); //the graph data structure that describes how rooms are connected through gates

        if (zones == null) {
            zones = new ArrayList<ZoneLogic>();
        }

        for (Zone z : getPojo().getZones()) {
            z.init();
            if (z.isRoom()) {
                Room room = new Room(z);
                room.init(this);

                if (!zones.contains(room)) {
                    LOG.config("Adding room " + room);

                    this.zones.add(room);
                } else {
                    LOG.warning("Attempt to add a null or an already existent room " + room);
                }
            } else {
                ZoneLogic zoneLogic = new ZoneLogic(z);
                zoneLogic.init(this);

                if (!zones.contains(zoneLogic)) {
                    LOG.config("Adding zone " + zoneLogic);
                    this.zones.add(zoneLogic);
                } else {
                    LOG.warning("Attempt to add a null or an already existent zone " + zoneLogic);
                }
            }
        }
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public List<ZoneLogic> getZones() {
        return zones;
    }

    /**
     *
     * @param zoneName
     * @return
     */
    @RequiresPermissions({"environments:read", "zones:read"})
    public ZoneLogic getZone(String zoneName) {
        for (ZoneLogic zone : zones) {
            if (zone.getPojo().getName().equalsIgnoreCase(zoneName) /*&& api.getAuth().isPermitted("zone:read" + zoneName)*/) {
                return zone;
            }
        }

        return null;
    }

    /**
     *
     * @param uuid
     * @return
     */
    @RequiresPermissions({"environments:read", "zones:read"})
    public ZoneLogic getZoneByUuid(String uuid) {
        for (ZoneLogic zone : zones) {
            if (zone.getPojo().getUuid().equalsIgnoreCase(uuid) /*&& api.getAuth().isPermitted("zone:read" + uuid)*/) {
                return zone;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public File getSource() {
        return source;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public File getObjectFolder() {
        return new File(source.getParent() + "/data/obj/");
    }

    /**
     *
     * @param source
     */
    @RequiresPermissions("environments:update,create")
    public void setSource(File source) {
        this.source = source;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    @Override
    public String toString() {
        return this.getPojo().getName();
    }

}

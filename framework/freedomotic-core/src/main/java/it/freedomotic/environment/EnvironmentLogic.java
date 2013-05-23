/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package it.freedomotic.environment;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.model.geometry.FreedomPolygon;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.objects.impl.Gate;
import it.freedomotic.security.Auth;
import it.freedomotic.util.Graph;
import it.freedomotic.util.UidGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author enrico
 */
public final class EnvironmentLogic {

    private Graph graph = null;
    private Environment pojo = null;
    private List<ZoneLogic> zones = new ArrayList<ZoneLogic>();
    private File source = null;

    public EnvironmentLogic() {
    }

    @RequiresPermissions("environments:read")
    public Environment getPojo() {
        if (Auth.isPermitted("environments:read:" + pojo.getUUID().substring(0, 5))){
            return pojo;
        }
        return null;
    }

    @RequiresPermissions("environments:update")
    public void setPojo(Environment pojo) {
        if ((pojo.getUUID() == null) || pojo.getUUID().isEmpty()) {
            pojo.setUUID(UUID.randomUUID().toString());
        }

        this.pojo = pojo;
    }

    @RequiresPermissions("environments:read")
    public Graph getGraph() {
        return graph;
    }

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

    @RequiresPermissions({"environments:update", "zones:create"})
    public void addRoom(ZoneLogic zone) {
        //null and duplicate check
        if (zone == null) {
            zone = new ZoneLogic(new Zone());
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

        zone.init(this);

        if (zone.getPojo().isRoom()) {
            Room room = (Room) zone;
            room.init(this);
            Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator();
            //check if this rooms has gates
            while (it.hasNext()) {
                EnvObjectLogic obj = it.next();
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

    @RequiresPermissions("environments:delete")
    public void removeZone(ZoneLogic zone) {
        getPojo().getZones().remove(zone.getPojo());
        zones.remove(zone);
    }

    @Deprecated
    @RequiresPermissions("environments:read")
    public int getLastObjectIndex() {
        return EnvObjectPersistence.size();
    }

    @RequiresPermissions("environments:delete")
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

    @RequiresPermissions("environments:read")
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
                    Room room = new Room(z);
                    room.init(this);

                    if (!zones.contains(room)) {
                        Freedomotic.logger.info("Adding room " + room);
                        this.zones.add(room);
                    } else {
                        Freedomotic.logger.warning("Attempt to add a null or an already existent room " + room);
                    }
                } else {
                    ZoneLogic zoneLogic = new ZoneLogic(z);
                    zoneLogic.init(this);

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

    @RequiresPermissions("environments:read")
    public List<ZoneLogic> getZones() {
        return zones;
    }

    @RequiresPermissions({"environments:read", "zones:read"})
    public ZoneLogic getZone(String zoneName) {
        for (ZoneLogic zone : zones) {
            if (zone.getPojo().getName().equalsIgnoreCase(zoneName)
                    && SecurityUtils.getSubject().isPermitted("zone:read" + zoneName)) {
                return zone;
            }
        }

        return null;
    }

    @RequiresPermissions("environments:read")
    public File getSource() {
        return source;
    }

    @RequiresPermissions("environments:read")
    public File getObjectFolder() {
        return new File(source.getParent() + "/data/obj/");
    }

    @RequiresPermissions("environments:update,create")
    public void setSource(File source) {
        this.source = source;
    }

    @RequiresPermissions("environments:read")
    public String toString() {
        return this.getPojo().getName();
    }
}

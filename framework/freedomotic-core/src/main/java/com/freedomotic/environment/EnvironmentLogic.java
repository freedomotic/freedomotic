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
package com.freedomotic.environment;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.util.UidGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;

/**
 *
 * @author Enrico Nicoletti
 */
public final class EnvironmentLogic {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentLogic.class.getName());

    private Graph graph = null;
    private Environment pojo = null;
    private List<ZoneLogic> zones = new ArrayList<>();
    private File source = null;

    /**
     * Instantiation is disabled outside this package.
     */
    public EnvironmentLogic() {
        // Default constructor
    }

    /**
     * Returns the environment POJO.
     *
     * @return the environment pojo
     */
    @RequiresPermissions("environments:read")
    public Environment getPojo() {
        return pojo;
    }

    /**
     * Sets an environment POJO.
     *
     * @param pojo the pojo to set
     */
    @RequiresPermissions("environments:update")
    public void setPojo(Environment pojo) {
        if ((pojo.getUUID() == null) || pojo.getUUID().isEmpty()) {
            pojo.setUUID(UUID.randomUUID().toString());
        }

        this.pojo = pojo;
    }

    /**
     * Returns a graph that describes how rooms are connected through gates.
     *
     * @return the environment rooms graph
     */
    @RequiresPermissions("environments:read")
    public Graph getGraph() {
        return graph;
    }

    /**
     * Returns a list of rooms in the environment.
     *
     * @return the list of rooms
     */
    @RequiresPermissions("environments:read")
    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();

        for (ZoneLogic zone : getZones()) {
            if (zone instanceof Room) {
                rooms.add((Room) zone);
            }
        }
        return rooms;
    }

    /**
     * Adds a room. Null and already existent rooms are not allowed.
     *
     * @param zone the zone to add
     */
    @RequiresPermissions({"environments:update", "zones:create"})
    public void addRoom(ZoneLogic zone) {
        //null and duplicate check
        if (zone == null) {
            zone = new ZoneLogic(new Zone());
        }
        if (zones.contains(zone)) {
            LOG.warn("Attempt to add a null or already existent room");
            return;
        }
        //check for a valid name
        if ((zone.getPojo().getName() == null) || (zone.getPojo().getName().isEmpty())) {
            zone.getPojo().setName("Unamed Zone " + UidGenerator.getNextStringUid());
        }

        zone.getPojo().setDescription("");

        //check for a valid shape
        if (zone.getPojo().getShape() == null) {
            //a default shape
            FreedomPolygon p = new FreedomPolygon();
            p.append(0, 0);
            p.append(200, 0);
            p.append(200, 200);
            p.append(0, 200);
            zone.getPojo().setShape(p);
        }

        //append to the list and initialize
        getPojo().getZones().add(zone.getPojo());
        zones.add(zone);

        zone.init(this);

    }

    /**
     * Removes a zone.
     *
     * @param zone the zone to remove
     */
    @RequiresPermissions("environments:delete")
    public void removeZone(ZoneLogic zone) {
        getPojo().getZones().remove(zone.getPojo());
        zones.remove(zone);
    }

    /**
     * Deletes all zones.
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
            LOG.error(Freedomotic.getStackTraceInfo(e));
        }
    }

    /**
     * Initializes the environment. The graph data structure describes how rooms
     * are connected through gates.
     */
    @RequiresPermissions("environments:read")
    public void init() {
        graph = new Graph(); //the graph data structure that describes how rooms are connected through gates

        if (zones == null) {
            zones = new ArrayList<>();
        }

        for (Zone z : getPojo().getZones()) {
            z.init();
            if (z.isRoom()) {
                Room room = new Room(z);
                room.init(this);

                if (!zones.contains(room)) {
                    LOG.info("Adding room \"{}\"", room);

                    this.zones.add(room);
                } else {
                    LOG.warn("Attempt to add a null or an already existent room \"{}\"", room);
                }
            } else {
                ZoneLogic zoneLogic = new ZoneLogic(z);
                zoneLogic.init(this);

                if (!zones.contains(zoneLogic)) {
                    LOG.info("Adding zone \"{}\"", zoneLogic);
                    this.zones.add(zoneLogic);
                } else {
                    LOG.warn("Attempt to add a null or an already existent zone \"{}\"", zoneLogic);
                }
            }
        }
    }

    /**
     * Returns a list of zones.
     *
     * @return the list of zones
     */
    @RequiresPermissions("environments:read")
    public List<ZoneLogic> getZones() {
        return zones;
    }

    /**
     * Gets a zone by its name.
     *
     * @param zoneName the name of the zone to retrieve
     * @return the zone
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
     * Gets a zone by its UUID.
     *
     * @param uuid the uuid of the zone to retrieve
     * @return the zone
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
     * Return the environment source file.
     *
     * @return the environment source file
     */
    @RequiresPermissions("environments:read")
    public File getSource() {
        return source;
    }

    /**
     * Returns the folder where the files representing objects are stored.
     *
     * @return the objects folder
     */
    @RequiresPermissions("environments:read")
    public File getObjectFolder() {
        return new File(source.getParent() + "/data/obj/");
    }

    /**
     * Sets the environment source file.
     *
     * @param source the environment source file to set
     */
    @RequiresPermissions("environments:update,create")
    public void setSource(File source) {
        this.source = source;
    }

    /**
     * Returns the environment name. Intended for debugging use.
     *
     * @return the environment name
     */
    @RequiresPermissions("environments:read")
    @Override
    public String toString() {
        return this.getPojo().getName();
    }

}

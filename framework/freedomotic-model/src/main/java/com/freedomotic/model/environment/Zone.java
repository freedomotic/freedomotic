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
package com.freedomotic.model.environment;

import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.object.EnvObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class Zone
        implements Serializable {

    private static final long serialVersionUID = 4668625650384850879L;
    private String name;
    private String description;
    private boolean room;
    private FreedomPolygon shape;
    @XmlTransient
    private ArrayList<EnvObject> objects;
    private String texture;
    private String uuid;

    public Zone() {
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Returns the zone name.
     *
     * @return the zone name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the zone shape.
     *
     * @return the zone shape
     */
    public FreedomPolygon getShape() {
        return this.shape;
    }

    /**
     * Checks if the zone is a room.
     *
     * @return true if the zone is a room, false otherwise
     */
    public boolean isRoom() {
        return room;
    }

    /**
     * Sets the zone as a room.
     *
     * @param room true if the zone is set as room, false otherwise
     */
    public void setAsRoom(boolean room) {
        this.setRoom(room);
    }

    /**
     * Gets the zone description.
     *
     * @return the zone description
     */
    public String getDescription() {
        if (description == null) {
            description = "";
        }
        return description;
    }

    /**
     * Sets the zone description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the zone texture.
     *
     * @return the texture
     */
    public String getTexture() {
        return texture;
    }

    /**
     * Sets the zone name.
     *
     * @param name the zone name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setsthe zone shape.
     *
     * @param shape the zone shape to set
     */
    public void setShape(FreedomPolygon shape) {
        this.shape = shape;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * Sets the zone texture.
     *
     * @param file the texture path
     */
    public void setTexture(String file) {
        this.texture = file;
        //file.getName();
    }

    /**
     * Gets the objets list in the zone.
     *
     * @return a list of objects
     */
    @XmlTransient
    public ArrayList<EnvObject> getObjects() {
        if (objects == null) {
            objects = new ArrayList<EnvObject>();
        }
        return objects;
    }

    /**
     * Sets the list of objects in the zone.
     *
     * @param objects the objects list to set
     */
    public void setObjects(ArrayList<EnvObject> objects) {
        this.objects = objects;
    }

    /**
     *
     */
    public void init() {
    }

    /**
     *
     */
    public void setChanged() {
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Zone other = (Zone) obj;

        if ((this.name == null) ? (other.name != null) : (!this.name.equalsIgnoreCase(other.name))) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = (17 * hash) + ((this.name != null) ? this.name.hashCode() : 0);

        return hash;
    }

    /**
     * @param room the room to set
     */
    public void setRoom(boolean room) {
        this.room = room;
    }

    /**
     * Gets the zone uuid.
     *
     * @return the uuid of the zone
     */
    public String getUuid() {
        if (uuid == null || uuid.equals("")) {
            this.uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    /**
     * Sets the zone uuid.
     *
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        if (uuid != null && !uuid.equals("")) {
            this.uuid = uuid;
        }
    }
}

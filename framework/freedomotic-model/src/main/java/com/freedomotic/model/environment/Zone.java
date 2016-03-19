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

    /**
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    public Zone() {
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     *
     * @return
     */
    public FreedomPolygon getShape() {
        return this.shape;
    }

    /**
     *
     * @return
     */
    public boolean isRoom() {
        return room;
    }

    /**
     *
     * @param room
     */
    public void setAsRoom(boolean room) {
        this.setRoom(room);
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        if (description == null) {
            description = "";
        }

        return description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public String getTexture() {
        return texture;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param shape
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
     *
     * @param file
     */
    public void setTexture(String file) {
        this.texture = file;
        //file.getName();
    }

    /**
     *
     * @return
     */
    @XmlTransient
    public ArrayList<EnvObject> getObjects() {
        if (objects == null) {
            objects = new ArrayList<EnvObject>();
        }

        return objects;
    }

    /**
     *
     * @param objects
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
     * @return the uuid
     */
    public String getUuid() {
        if (uuid == null || uuid.equals("")) {
            this.uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        if (uuid != null && !uuid.equals("")) {
            this.uuid = uuid;
        }
    }
}

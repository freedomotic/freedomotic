//Copyright 2009 Enrico Nicoletti
//eMail: enrico.nicoletti84@gmail.co m
//
//This file is part of EventEngine.
//
//EventEngine is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//EventEngine is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with EventEngine; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
package it.freedomotic.model.environment;


import it.freedomotic.model.geometry.FreedomPolygon;
import it.freedomotic.model.object.EnvObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author enrico
 */
public class Zone implements Serializable {

    private String name;
    private String description;
    private boolean room;
    private FreedomPolygon shape;
    private ArrayList<EnvObject> objects;
    private String texture;

    public String getName() {
        return this.name;
    }

    public FreedomPolygon getShape() {
        return this.shape;
    }

    public boolean isRoom() {
        return room;
    }

    public void setAsRoom(boolean room) {
        this.room = room;
    }

    public String getDescription() {
        if (description == null) {
            description = "";
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTexture() {
        return texture;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShape(FreedomPolygon shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public void setTexture(String file) {
        
            this.texture = file;
        
        //file.getName();
    }

    public ArrayList<EnvObject> getObjects() {
        if (objects == null) {
            objects = new ArrayList<EnvObject>();
        }
        return objects;
    }

    public void setObjects(ArrayList<EnvObject> objects) {
        this.objects = objects;
    }

    public void init() {
    }

    public void setChanged() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Zone other = (Zone) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equalsIgnoreCase(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}

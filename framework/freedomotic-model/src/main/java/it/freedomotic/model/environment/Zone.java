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
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author enrico
 */
public class Zone
        implements Serializable {

    private static final long serialVersionUID = 4668625650384850879L;
	
	private String name;
    private String description;
    private boolean room;
    private FreedomPolygon shape;
    private ArrayList<EnvObject> objects;
    private String texture;

    @RequiresPermissions("zones:read")
    public String getName() {
        return this.name;
    }

    @RequiresPermissions("zones:read")
    public FreedomPolygon getShape() {
        return this.shape;
    }

    @RequiresPermissions("zones:read")
    public boolean isRoom() {
        return room;
    }

    @RequiresPermissions("zones:update")
    public void setAsRoom(boolean room) {
        this.room = room;
    }

    @RequiresPermissions("zones:read")
    public String getDescription() {
        if (description == null) {
            description = "";
        }

        return description;
    }

    @RequiresPermissions("zones:update")
    public void setDescription(String description) {
        this.description = description;
    }

    @RequiresPermissions("zones:read")
    public String getTexture() {
        return texture;
    }

    @RequiresPermissions("zones:update")
    public void setName(String name) {
        this.name = name;
    }

    @RequiresPermissions("zones:update")
    public void setShape(FreedomPolygon shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @RequiresPermissions("zones:update")
    public void setTexture(String file) {

        this.texture = file;

        //file.getName();
    }

    @RequiresPermissions("zones:read")
    public ArrayList<EnvObject> getObjects() {
        if (objects == null) {
            objects = new ArrayList<EnvObject>();
        }

        return objects;
    }

    @RequiresPermissions("zones:update")
    public void setObjects(ArrayList<EnvObject> objects) {
        this.objects = objects;
    }

    public void init() {
    }

    public void setChanged() {
    }

    @Override
    @RequiresPermissions("zones:read")
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

    @Override
    @RequiresPermissions("zones:read")
    public int hashCode() {
        int hash = 5;
        hash = (17 * hash) + ((this.name != null) ? this.name.hashCode() : 0);

        return hash;
    }
}

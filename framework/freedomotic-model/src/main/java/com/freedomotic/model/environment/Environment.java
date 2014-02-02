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
//eMail: enrico.nicoletti84@gmail.com
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
package com.freedomotic.model.environment;

import com.freedomotic.model.geometry.FreedomColor;
import com.freedomotic.model.geometry.FreedomPolygon;
import java.io.Serializable;
import java.util.ArrayList;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author enrico
 */
public class Environment
        implements Serializable {

    private static final long serialVersionUID = 2461804483322891608L;
	
	private String name;
    private int width;
    private int height;
    private String renderer;
    private FreedomColor backgroundColor;
    private String backgroundImage;
    private ArrayList<Zone> zones = new ArrayList<Zone>();
    private String uuid;

    /**
     *
     */
    public Environment() {
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public String getUUID() {
        return this.uuid;
    }

    /**
     *
     * @param uuid
     */
    @RequiresPermissions("environments:update")
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public String getRenderer() {
        return renderer;
    }

    /**
     *
     * @param renderer
     */
    @RequiresPermissions("environments:update")
    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public String getEnvironmentName() {
        return name;
    }

    /**
     *
     * @return
     */
    @Override
    @RequiresPermissions("environments:read")
    public String toString() {
        return name;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public FreedomPolygon getShape() {
        //it returns the first zone in the environment. It is considered the Indoor
        return zones.get(0).getShape();
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public FreedomColor getBackgroundColor() {
        return backgroundColor;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public String getBackgroundImage() {
        if (backgroundImage == null) {
            return "environment-map.png";
        } else {
            return backgroundImage;
        }
    }

    /**
     *
     * @param backgroundImage
     */
    @RequiresPermissions("environments:update")
    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    @RequiresPermissions("environments:update")
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param index
     * @return
     */
    @RequiresPermissions("environments:read,zones:read")
    public Zone getZone(int index) {
        return zones.get(index);
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read,zones:read")
    public ArrayList<Zone> getZones() {
        return zones;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read,zones:read")
    public int getLastZoneIndex() {
        return zones.size();
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public int getWidth() {
        return width;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("environments:read")
    public int getHeight() {
        return height;
    }

    /**
     *
     */
    @RequiresPermissions("environments:update")
    public void clear() {
        zones.clear();
        zones = null;
        backgroundColor = null;
    }
}

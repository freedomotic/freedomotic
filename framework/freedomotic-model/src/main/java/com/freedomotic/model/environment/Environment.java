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

import com.freedomotic.model.geometry.FreedomColor;
import com.freedomotic.model.geometry.FreedomPolygon;
import java.io.Serializable;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
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
    public String getUUID() {
        return this.uuid;
    }

    /**
     *
     * @param uuid
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     *
     * @return
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     *
     * @param renderer
     */
    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    /**
     *
     * @return
     */
    public String getEnvironmentName() {
        return name;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     *
     * @return
     */
    public FreedomPolygon getShape() {
        //it returns the first zone in the environment. It is considered the Indoor
        return zones.get(0).getShape();
    }

    /**
     *
     * @return
     */
    public FreedomColor getBackgroundColor() {
        return backgroundColor;
    }

    /**
     *
     * @return
     */
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
    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
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
     * @param index
     * @return
     */
    public Zone getZone(int index) {
        return zones.get(index);
    }

    /**
     *
     * @return
     */
    public ArrayList<Zone> getZones() {
        return zones;
    }

    /**
     *
     * @return
     */
    public int getLastZoneIndex() {
        return zones.size();
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     */
    public void clear() {
        zones.clear();
        zones = null;
        backgroundColor = null;
    }
}

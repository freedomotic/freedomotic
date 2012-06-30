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
package it.freedomotic.model.environment;

import it.freedomotic.model.geometry.FreedomColor;
import it.freedomotic.model.geometry.FreedomPolygon;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author enrico
 */
public class Environment implements Serializable {

    private String name;
    private int width;
    private int height;
    private String renderer;
    private FreedomColor backgroundColor;
    private String backgroundImage;
    private ArrayList<Zone> zones = new ArrayList<Zone>();

    public Environment() {
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public String getEnvironmentName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public FreedomPolygon getShape() {
        //it returns the first zone in the environment. It is considered the Indoor
        return zones.get(0).getShape();
    }

    public FreedomColor getBackgroundColor() {
        return backgroundColor;
    }

    public String getBackgroundImage() {
        if (backgroundImage == null) {
            return "environment-map.png";
        } else {
            return backgroundImage;
        }
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Zone getZone(int index) {
        return zones.get(index);
    }

    public ArrayList<Zone> getZones() {
        return zones;
    }

    public int getLastZoneIndex() {
        return zones.size();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void clear() {
        zones.clear();
        zones = null;
        backgroundColor = null;
    }
}

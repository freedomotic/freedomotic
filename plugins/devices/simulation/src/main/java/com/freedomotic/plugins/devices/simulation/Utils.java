/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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
package com.freedomotic.plugins.devices.simulation;

import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.geometry.FreedomPolygon;

/**
 *
 * @author Mauro Cicolella
 */
public class Utils {

    /**
     * Returns the coordinates of the center of the polygon.
     *
     * @param input the polygon
     * @return the coordinates of the center
     */
    public static FreedomPoint getPolygonCenter(FreedomPolygon input) {
        double x = 0;
        double y = 0;
        for (FreedomPoint v : input.getPoints()) {
            x += v.getX();
            y += v.getY();
        }
        x /= input.getPoints().size();
        y /= input.getPoints().size();
        return new FreedomPoint((int) x, (int) y);
    }
}

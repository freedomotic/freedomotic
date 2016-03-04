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
package com.freedomotic.model.geometry;

import java.io.Serializable;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class FreedomPolygon
        implements FreedomShape,
        Serializable {

    private static final long serialVersionUID = -3740479951903880574L;

    private ArrayList<FreedomPoint> points = new ArrayList<FreedomPoint>();

    /**
     *
     */
    public FreedomPolygon() {
    }

    /**
     *
     * @param point
     */
    public void append(FreedomPoint point) {
        points.add(point);
    }

    /**
     *
     * @param x
     * @param y
     */
    public void append(int x, int y) {
        FreedomPoint point = new FreedomPoint(x, y);
        points.add(point);
    }

    /**
     *
     * @param nextTo
     * @return
     */
    public FreedomPoint insert(FreedomPoint nextTo) {
        int index = points.indexOf(nextTo);
        FreedomPoint currentPoint = null;
        FreedomPoint nextPoint = null;

        try {
            nextPoint = points.get((index + 1) % points.size());
            currentPoint = points.get(index);
        } catch (Exception e) {
        }

        if ((currentPoint != null) && (nextPoint != null)) {
            int x = (int) Math.abs((currentPoint.getX() - nextPoint.getX())) / 2;
            int y = (int) Math.abs((currentPoint.getY() - nextPoint.getY())) / 2;
            FreedomPoint newPoint = new FreedomPoint(x, y);
            points.add(index + 1, newPoint);

            return newPoint;
        }

        return null;
    }

    /**
     *
     * @param point
     */
    public void remove(FreedomPoint point) {
        points.remove(point);
    }

    /**
     *
     * @return
     */
    public ArrayList<FreedomPoint> getPoints() {
        if (points != null) {
            return points;
        } else {
            return new ArrayList<FreedomPoint>();
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(points.size() + " points ");

        for (FreedomPoint p : points) {
            buff.append("(").append(p.getX()).append(",").append(p.getY()).append(")");
        }

        return buff.toString();
    }
}

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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class FreedomPoint implements Serializable {

    private static final long serialVersionUID = -54024055228629609L;
    private int x;
    private int y;

    /**
     *
     * @param x
     * @param y
     */
    public FreedomPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     */
    public FreedomPoint() {
        this.x = 0;
        this.y = 0;
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     *
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof FreedomPoint) {
            FreedomPoint point = (FreedomPoint) object;

            if ((point.getX() == this.getX()) && (point.getY() == this.getY())) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     *
     * @param x
     * @param y
     */
    public void setLocation(int x, int y) {
        setX(x);
        setY(y);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return this.getX() + "," + this.getY();
    }
}

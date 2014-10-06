/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.jfrontend;

import java.awt.Color;
import java.awt.Shape;

/**
 * Created with IntelliJ IDEA.
 * User: Bastiaan Visser
 * Date: 7/2/13
 * Time: 10:54 AM
 */
public class Indicator
{
    private Shape shape;
    private Color color = new Color(0, 0, 255, 50);

    /**
     *
     * @param shape
     */
    public Indicator(Shape shape) {
        this.shape = shape;
    }

    /**
     *
     * @param shape
     * @param color
     */
    public Indicator(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    /**
     *
     * @return
     */
    public Shape getShape() {
        return shape;
    }

    /**
     *
     * @return
     */
    public Color getColor() {
        return color;
    }
}

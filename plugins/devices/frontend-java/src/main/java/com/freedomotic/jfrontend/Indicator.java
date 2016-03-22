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
package com.freedomotic.jfrontend;

import java.awt.Color;
import java.awt.Shape;
import java.util.Random;

/**
 * @author Bastiaan Visser
 */
public class Indicator {

    //the default opacity
    private static final int OPACITY = Renderer.DEFAULT_OPACITY;
    private Shape shape;
    //the default shape fill color, blue
    private Color color = new Color(0, 0, 255, OPACITY);

    /**
     *
     * @param shape
     */
    public Indicator(Shape shape) {
        this.shape = shape;
        //this.color = new Color(rand(0, 255), rand(0, 255), rand(0, 255), OPACITY);
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

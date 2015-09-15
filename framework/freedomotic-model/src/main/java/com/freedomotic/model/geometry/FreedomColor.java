/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
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
package com.freedomotic.model.geometry;

import java.io.Serializable;

/**
 *
 * @author Enrico
 */
public class FreedomColor implements Serializable{
    
	private static final long serialVersionUID = 5934085311849971561L;
	
	private int red;
    private int green;
    private int blue;
    private int alpha;

    /**
     *
     */
    public FreedomColor() {
    }

    /**
     *
     * @return
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     *
     * @param alpha
     */
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    /**
     *
     * @return
     */
    public int getBlue() {
        return blue;
    }

    /**
     *
     * @param blue
     */
    public void setBlue(int blue) {
        this.blue = blue;
    }

    /**
     *
     * @return
     */
    public int getGreen() {
        return green;
    }

    /**
     *
     * @param green
     */
    public void setGreen(int green) {
        this.green = green;
    }

    /**
     *
     * @return
     */
    public int getRed() {
        return red;
    }

    /**
     *
     * @param red
     */
    public void setRed(int red) {
        this.red = red;
    }
}

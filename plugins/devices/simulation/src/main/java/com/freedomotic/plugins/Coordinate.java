/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.plugins;

/**
 *
 * @author Enrico Nicoletti
 */
public class Coordinate {

    private int id;
    private int x;
    private int y;
    private int time;
    private String userId; 

    /**
     * Returns the x coordinate.
     * 
     * @return x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate.
     * 
     * @return y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @return 
     */
    public int getTime() {
        return time;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getUserId() {
        return userId;
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
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     *
     * @param time
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     *
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}

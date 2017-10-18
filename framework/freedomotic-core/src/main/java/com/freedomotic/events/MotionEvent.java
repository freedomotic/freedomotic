/**
 *
 * Copyright (c) 2009-2017 Freedomotic team http://freedomotic.com
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
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomPoint;

/**
 *
 * @author Mauro Cicolella
 */
public class MotionEvent
        extends EventTemplate {

    private static final long serialVersionUID = 4965942901211451802L;
    private static final String DEFAULT_DESTINATION = "app.event.sensor.motion";
    private String zoneName;
    private int motionArea;
    private int centerOfGravity;
    private int distance;
    private FreedomPoint location;
    private ThreeAxisValue threeAxisValue;
    private ThreeAxisAcceleration threeAxisAcc;

    /**
     * @param source
     * @param zone
     */
    public MotionEvent(Object source, Zone zone) {
        this.setSender(source);
        zoneName = zone.getName();
        generateEventPayload();
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone.name", zoneName);

    }

    public String getZoneName() {
        return zoneName;
    }

    /**
     * Distance from motion sensor
     *
     * @return
     */
    public int getDistance() {
        return distance;
    }

    public void setDistance(int dist) {
        distance = dist;
        payload.addStatement("distance", dist);

    }

    /**
     * Location point of motion (x,y,z coordinates)
     *
     * @return
     */
    public FreedomPoint getLocation() {
        return location;
    }

    public void setLocation(FreedomPoint loc) {
        location = loc;
        payload.addStatement("location-x", loc.getX());
        payload.addStatement("location-y", loc.getY());

    }

    /**
     * 3-axis values from accelerometers (x,y,z angles)
     *
     * @return
     */
    public ThreeAxisValue getThreeAxisValue() {
        return threeAxisValue;
    }

    public void setThreeAxisValue(ThreeAxisValue value) {
        threeAxisValue = value;
        payload.addStatement("angle-x", value.getX());
        payload.addStatement("angle-y", value.getY());
        payload.addStatement("angle-z", value.getZ());
    }

    /**
     * 3-axis acceleration from accelerometers (x,y,z angles)
     *
     * @return
     */
    public ThreeAxisAcceleration getThreeAxisAcceleration() {
        return threeAxisAcc;
    }

    public void setThreeAxisAcceleration(ThreeAxisAcceleration value) {
        threeAxisAcc = value;
        payload.addStatement("angle-x-acceleration", value.getX());
        payload.addStatement("angle-y-acceleration", value.getY());
        payload.addStatement("angle-z-acceleration", value.getZ());
    }

    /**
     * Center of gravity (the center of motion area in webcams/netcams)
     *
     * @return
     */
    public int getCenterOfGravity() {
        return centerOfGravity;
    }

    public void setCenterOfGravity(int cog) {
        centerOfGravity = cog;
        payload.addStatement("center-of-gravity", cog);

    }

    /**
     * Percentage of complete image pixels area that has been changed between
     * two consecutive images
     *
     * @return
     */
    public int getMotionArea() {
        return motionArea;
    }

    public void setMotionArea(int area) {
        motionArea = area;
        payload.addStatement("motion-area", area);

    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return ("Motion detected in zone " + zoneName);
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return DEFAULT_DESTINATION;
    }

    /**
     * Class used for data from 3-axis accelerometers
     *
     */
    private class ThreeAxisValue {

        private int x;
        private int y;
        private int z;

        /**
         *
         * @param x X-axis angle
         * @param y Y-axis angle
         * @param z Z-axis angle
         */
        public ThreeAxisValue(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         *
         */
        public ThreeAxisValue() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
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
         * @return
         */
        public int getZ() {
            return z;
        }

        /**
         *
         * @param z
         */
        public void setZ(int z) {
            this.z = z;
        }

        /**
         *
         * @param x
         * @param y
         * @param y
         */
        public void setValue(int x, int y, int z) {
            setX(x);
            setY(y);
            setZ(z);
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return this.getX() + " X," + this.getY() + " Y," + this.getZ() + " Z";
        }
    }

    /**
     * Class used for acceleration values from 3-axis accelerometers
     * Acceleration along one or more axis represents a motion
     */
    public class ThreeAxisAcceleration {

        private int x;
        private int y;
        private int z;

        /**
         *
         * @param x X-axis acceleration
         * @param y Y-axis acceleration
         * @param z Z-axis acceleration
         */
        public ThreeAxisAcceleration(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         *
         */
        public ThreeAxisAcceleration() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
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
         * @return
         */
        public int getZ() {
            return z;
        }

        /**
         *
         * @param z
         */
        public void setZ(int z) {
            this.z = z;
        }

        /**
         *
         * @param x
         * @param y
         */
        public void setAcceleration(int x, int y, int z) {
            setX(x);
            setY(y);
            setZ(z);
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return this.getX() + " X," + this.getY() + " Y," + this.getZ() + " Z";
        }
    }
}

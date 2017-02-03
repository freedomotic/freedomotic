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
package com.freedomotic.plugins.devices.thingspeak;

/**
 *
 * @author Mauro Cicolella
 */
public class ThingSpeakObj {

    String thingName = null;
    String thingBehavior = null;
    Integer thingspeakChannel = 0;
    Integer thingspeakField = 0;

    public ThingSpeakObj(String thingName, String thingBehavior, Integer thingspeakChannel, Integer thingspeakField) {

        setThingName(thingName);
        setThingBehavior(thingBehavior);
        setThingSpeakChannel(thingspeakChannel);
        setThingSpeakField(thingspeakField);
    }

    public void setThingName(String thingName) {
        this.thingName = thingName;
    }

    public void setThingBehavior(String thingBehavior) {
        this.thingBehavior = thingBehavior;
    }

    public void setThingSpeakChannel(Integer thingspeakChannel) {
        this.thingspeakChannel = thingspeakChannel;
    }

    public void setThingSpeakField(Integer thingspeakField) {
        this.thingspeakField = thingspeakField;
    }

    public String getThingName() {
        return this.thingName;
    }

    public String getThingBehavior() {
        return this.thingBehavior;
    }

    public Integer getThingSpeakChannel() {
        return this.thingspeakChannel;
    }

    public Integer getThingSpeakField() {
        return this.thingspeakField;
    }

}

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

package com.freedomotic.plugins.devices.mysensors;

/**
 *
 * @author mauro
 */
public enum PresentationSubType {

    S_DOOR("Door and window sensors"),
    S_MOTION("Motion sensors"),
    S_SMOKE("Smoke sensor"),
    S_LIGHT("Light Actuator (on/off)"),
    S_DIMMER("Dimmable device of some kind"),
    S_COVER("Window covers or shades"),
    S_TEMP("Temperature sensor"),
    S_HUM("Humidity sensor"),
    S_BARO("Barometer sensor (Pressure)"),
    S_WIND("Wind sensor"),
    S_RAIN("Rain sensor"),
    S_UV("UV sensor"),
    S_WEIGHT("Weight sensor for scales etc."),
    S_POWER("Power measuring device, like power meters"),
    S_HEATER("Heater device"),
    S_DISTANCE("Distance sensor"),
    S_LIGHT_LEVEL("Light sensor"),
    S_ARDUINO_NODE("Arduino node device"),
    S_ARDUINO_RELAY("Arduino repeating node device"),
    S_LOCK("Lock device"),
    S_IR("Ir sender/receiver device"),
    S_WATER("Water meter"),
    S_AIR_QUALITY("Air quality sensor e.g. MQ-2"),
    S_CUSTOM("Use this for custom sensors where no other fits."),
    S_DUST("Dust level sensor"),
    S_SCENE_CONTROLLER("Scene controller device");
    private String description;
    private static PresentationSubType[] values = null;

    private PresentationSubType(String description) {
        this.description = description;
    }

    public static PresentationSubType fromInt(int i) {
        if (PresentationSubType.values == null) {
            PresentationSubType.values = PresentationSubType.values();
        }
        return PresentationSubType.values[i];
    }

    public String getDescription() {
        return description;
    }
}

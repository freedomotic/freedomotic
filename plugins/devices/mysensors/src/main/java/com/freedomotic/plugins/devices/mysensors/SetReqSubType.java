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
public enum SetReqSubType {

    V_TEMP("Temperature"),
    V_HUM("Humidity"),
    V_LIGHT("Light status. 0=off 1=on"),
    V_DIMMER("Dimmer value. 0-100%"),
    V_PRESSURE("Atmospheric Pressure"),
    V_FORECAST("Whether forecast. One of 'stable', 'sunny', 'cloudy', 'unstable', 'thunderstorm' or 'unknown'"),
    V_RAIN("Amount of rain"),
    V_RAINRATE("Rate of rain"),
    V_WIND("Windspeed"),
    V_GUST("Gust"),
    V_DIRECTION("Wind direction"),
    V_UV("UV light level"),
    V_WEIGHT("Weight(for scales etc)"),
    V_DISTANCE("Distance"),
    V_IMPEDANCE("Impedance value"),
    V_ARMED("Armed status of a security sensor. 1=Armed, 0=Bypassed"),
    V_TRIPPED("Tripped status of a security sensor. 1=Tripped, 0=Untripped"),
    V_WATT("Watt value for power meters"),
    V_KWH("Accumulated number of KWH for a power meter"),
    V_SCENE_ON("Turn on a scene"),
    V_SCENE_OFF("Turn of a scene"),
    V_HEATER("Mode of header. One of 'Off', 'HeatOn', 'CoolOn', or 'AutoChangeOver'"),
    V_HEATER_SW("Heater switch power. 1=On, 0=Off"),
    V_LIGHT_LEVEL("Light level. 0-100%"),
    V_VAR1("Custom value"),
    V_VAR2("Custom value"),
    V_VAR3("Custom value"),
    V_VAR4("Custom value"),
    V_VAR5("Custom value"),
    V_UP("Window covering. Up."),
    V_DOWN("Window covering. Down."),
    V_STOP("Window covering. Stop."),
    V_IR_SEND("Send out an IR-command"),
    V_IR_RECEIVE("This message contains a received IR-command"),
    V_FLOW("Flow of water (in meter)"),
    V_VOLUME("Water volume"),
    V_LOCK_STATUS("Set or get lock status. 1=Locked, 0=Unlocked"),
    V_DUST_LEVEL("Dust level"),
    V_VOLTAGE("Voltage level"),
    V_CURRENT("Current level");
    private String description;
    private static SetReqSubType[] values = null;

    private SetReqSubType(String description) {
        this.description = description;
    }

    public static SetReqSubType fromInt(int i) {
        if (SetReqSubType.values == null) {
            SetReqSubType.values = SetReqSubType.values();
        }
        return SetReqSubType.values[i];
    }

    public String getDescription() {
        return description;
    }
}

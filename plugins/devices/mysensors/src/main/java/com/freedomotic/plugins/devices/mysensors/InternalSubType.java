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
public enum InternalSubType {

    I_BATTERY_LEVEL("Use this to report the battery level (in percent 0-100)."),
    I_TIME("Sensors can request the current time from the Controller using this message. The time will be reported as the seconds since 1970"),
    I_VERSION("Sensors report their library version at startup using this message type"),
    I_ID_REQUEST("Use this to request a unique node id from the controller."),
    I_ID_RESPONSE("Id response back to sensor. Payload contains sensor id."),
    I_INCLUSION_MODE("Start/stop inclusion mode of the Controller (1=start, 0=stop)."),
    I_CONFIG("Config request from node. Reply with (M)etric or (I)mperal back to sensor."),
    I_FIND_PARENT("When a sensor starts up, it broadcast a search request to all neighbor nodes. They reply with a I_FIND_PARENT_RESPONSE."),
    I_FIND_PARENT_RESPONSE("Reply message type to I_FIND_PARENT request."),
    I_LOG_MESSAGE("Sent by the gateway to the Controller to trace-log a message"),
    I_CHILDREN("A message that can be used to transfer child sensors (from EEPROM routing table) of a repeating node."),
    I_SKETCH_NAME("Optional sketch name that can be used to identify sensor in the Controller GUI"),
    I_SKETCH_VERSION("Optional sketch version that can be reported to keep track of the version of sensor in the Controller GUI."),
    I_REBOOT("Used by OTA firmware updates. Request for node to reboot."),
    I_GATEWAY_READY("Send by gateway to controller when startup is complete.");
    private String description;
    private static InternalSubType[] values = null;

    private InternalSubType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static InternalSubType fromInt(int i) {
        if (InternalSubType.values == null) {
            InternalSubType.values = InternalSubType.values();
        }
        return InternalSubType.values[i];
    }
}

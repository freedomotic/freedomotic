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

package com.freedomotic.plugins.devices.hwgste;

/**
 * @autor Mauro Cicolella
 */

public class Board {

    private String alias;
    private String ipAddress;
    private int snmpPort;
    private int port;
    private int sensorsNumber;

    public Board(String alias, String ipAddress, int port, int sensorsNumber) {
        setAlias(alias);
        setIpAddress(ipAddress);
        setPort(port);
        setSensorsNumber(sensorsNumber);

    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSnmpPort(int snmpPort) {
        this.snmpPort = snmpPort;
    }

    public int getSensorsNumber() {
        return sensorsNumber;
    }

    public void setSensorsNumber(int sensorsNumber) {

        this.sensorsNumber = sensorsNumber;
    }
}

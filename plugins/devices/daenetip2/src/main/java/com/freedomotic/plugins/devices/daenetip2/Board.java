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
package com.freedomotic.plugins.devices.daenetip2;

/**
 *
 * @author mauro
 */
public class Board {

    private String ipAddress = null;
    private String alias = null;
    private String readOnlyCommunity = null;
    private String readWriteCommunity = null;
    private int snmpPort;
    private int P3Status = 2000;
    private int P5Status = 2000;
    private int[] P6Status = {0, 0, 0, 0, 0, 0, 0, 0};

    /**
     *
     * @param alias
     * @param ipAddress
     * @param snmpPort
     * @param readOnlyCommunity
     * @param readWriteCommunity
     */
    public Board(String alias, String ipAddress, int snmpPort, String readOnlyCommunity, String readWriteCommunity) {
        setAlias(alias);
        setIpAddress(ipAddress);
        setSnmpPort(snmpPort);
        setP3Status(-1);
        setP5Status(-1);
        setReadOnlyCommunity(readOnlyCommunity);
        setReadWriteCommunity(readWriteCommunity);
    }

    /**
     *
     * @return
     */
    public String getAlias() {
        return alias;
    }

    /**
     *
     * @param alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     *
     * @return
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     *
     * @param ipAddress
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     *
     * @return
     */
    public int getSnmpPort() {
        return snmpPort;
    }

    /**
     *
     * @param port
     */
    public void setSnmpPort(int port) {
        this.snmpPort = port;
    }

    /**
     *
     * @return
     */
    public int getP3Status() {
        return P3Status;
    }

    /**
     *
     * @param P3Status
     */
    public void setP3Status(int P3Status) {
        this.P3Status = P3Status;
    }

    /**
     *
     * @return
     */
    public int getP5Status() {
        return P5Status;
    }

    /**
     *
     * @param P5Status
     */
    public void setP5Status(int P5Status) {
        this.P5Status = P5Status;
    }

    /**
     *
     * @param i
     * @return
     */
    public int getP6Status(int i) {
        return P6Status[i];
    }

    /**
     *
     * @param pin
     * @param value
     */
    public void setP6Status(int pin, int value) {
        this.P6Status[pin] = value;
    }

    /**
     *
     * @return
     */
    public String getReadOnlyCommunity() {
        return readOnlyCommunity;
    }

    /**
     *
     * @param readOnlyCommunity
     */
    public void setReadOnlyCommunity(String readOnlyCommunity) {
        this.readOnlyCommunity = readOnlyCommunity;
    }

    /**
     *
     * @return
     */
    public String getReadWriteCommunity() {
        return readWriteCommunity;
    }

    /**
     *
     * @param readWriteCommunity
     */
    public void setReadWriteCommunity(String readWriteCommunity) {
        this.readWriteCommunity = readWriteCommunity;
    }
}

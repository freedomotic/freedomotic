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

package com.freedomotic.plugins.devices.phwsw;

public class Board {

    private String ipAddress = null;
    private String lineToMonitorize;
    private int port;
    private int relayNumber;
    private int analogInputNumber;
    private int digitalInputNumber;
    private int startingRelay;

    public Board(String ipAddress, int port, int relayNumber, int analogInputNumber,
            int digitalInputNumber, String lineToMonitorize, int startingRelay) {
        setIpAddress(ipAddress);
        setPort(port);
        setRelayNumber(relayNumber);
        setAnalogInputNumber(analogInputNumber);
        setDigitalInputNumber(digitalInputNumber);
        setLineToMonitorize(lineToMonitorize);
        setStartingRelay(startingRelay);

    }

    public int getAnalogInputNumber() {
        return analogInputNumber;
    }

    public void setAnalogInputNumber(int analogInputNumber) {
        this.analogInputNumber = analogInputNumber;
    }

    public int getDigitalInputNumber() {
        return digitalInputNumber;
    }

    public void setDigitalInputNumber(int digitalInputNumber) {
        this.digitalInputNumber = digitalInputNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLineToMonitorize() {
        return lineToMonitorize;
    }

    public void setLineToMonitorize(String lineToMonitorize) {
        this.lineToMonitorize = lineToMonitorize;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRelayNumber() {
        return relayNumber;
    }

    public void setRelayNumber(int relayNumber) {
        this.relayNumber = relayNumber;
    }

    public int getStartingRelay() {
        return startingRelay;
    }

    public void setStartingRelay(int startingRelay) {
        this.startingRelay = startingRelay;
    }
}

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
package com.freedomotic.plugins.devices.flyport;

/**
 *
 * @author Mauro Cicolella
 */
public class Board {

    private String ipAddress = null;
    private String lineToMonitorize;
    private int port;
    private int potNumber;
    private int ledNumber;
    private int btnNumber;
    private int startingValue;

    /**
     *
     * @param ipAddress
     * @param port
     * @param potNumber
     * @param ledNumber
     * @param btnNumber
     * @param lineToMonitorize
     * @param startingValue
     */
    public Board(String ipAddress, int port, int potNumber, int ledNumber,
            int btnNumber, String lineToMonitorize, int startingValue) {
        setIpAddress(ipAddress);
        setPort(port);
        setPotNumber(potNumber);
        setLedNumber(ledNumber);
        setBtnNumber(btnNumber);
        setLineToMonitorize(lineToMonitorize);
        setStartingValue(startingValue);

    }

    /**
     *
     * @return
     */
    public int getPotNumber() {
        return potNumber;
    }

    /**
     *
     * @param potNumber
     */
    public void setPotNumber(int potNumber) {
        this.potNumber = potNumber;
    }

    /**
     *
     * @return
     */
    public int getLedNumber() {
        return ledNumber;
    }

    /**
     *
     * @param ledNumber
     */
    public void setLedNumber(int ledNumber) {
        this.ledNumber = ledNumber;
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
    public String getLineToMonitorize() {
        return lineToMonitorize;
    }

    /**
     *
     * @param lineToMonitorize
     */
    public void setLineToMonitorize(String lineToMonitorize) {
        this.lineToMonitorize = lineToMonitorize;
    }

    /**
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     *
     * @return
     */
    public int getBtnNumber() {
        return btnNumber;
    }

    /**
     *
     * @param btnNumber
     */
    public void setBtnNumber(int btnNumber) {
        this.btnNumber = btnNumber;
    }

    /**
     *
     * @return
     */
    public int getStartingValue() {
        return startingValue;
    }

    /**
     *
     * @param startingValue
     */
    public void setStartingValue(int startingValue) {
        this.startingValue = startingValue;
    }
}

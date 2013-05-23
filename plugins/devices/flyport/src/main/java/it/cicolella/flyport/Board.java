/*
 Copyright FILE Mauro Cicolella, 2012-2013

 This file is part of FREEDOMOTIC.

 FREEDOMOTIC is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 FREEDOMOTIC is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Freedomotic.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cicolella.flyport;

public class Board {

    private String ipAddress = null;
    private String lineToMonitorize;
    private int port;
    private int potNumber;
    private int ledNumber;
    private int btnNumber;
    private int startingValue;

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

    public int getPotNumber() {
        return potNumber;
    }

    public void setPotNumber(int potNumber) {
        this.potNumber = potNumber;
    }

    public int getLedNumber() {
        return ledNumber;
    }

    public void setLedNumber(int ledNumber) {
        this.ledNumber = ledNumber;
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

    public int getBtnNumber() {
        return btnNumber;
    }

    public void setBtnNumber(int btnNumber) {
        this.btnNumber = btnNumber;
    }

    public int getStartingValue() {
        return startingValue;
    }

    public void setStartingValue(int startingValue) {
        this.startingValue = startingValue;
    }
}
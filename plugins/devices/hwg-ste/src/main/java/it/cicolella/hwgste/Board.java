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

package it.cicolella.hwgste;

public class Board {

    private String ipAddress = null;
    private int snmpPort;
    private int port;
    private int sensorsNumber;

    public Board(String ipAddress, int port, int sensorsNumber) {
        setIpAddress(ipAddress);
        setPort(port);
        setSensorsNumber(sensorsNumber);

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

    public void setSnmpPort(int port) {
        this.snmpPort = port;
    }

    public int getSensorsNumber() {
        return sensorsNumber;
    }

    public void setSensorsNumber(int sensorsNumber) {

        this.sensorsNumber = sensorsNumber;
    }
}

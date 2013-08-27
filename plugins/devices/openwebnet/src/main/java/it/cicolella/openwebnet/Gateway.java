/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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

package it.cicolella.openwebnet;

public class Gateway {

    private String ipAddress = null;
    private String alias = null;
    private String gatewayType = null;
    private int portNumber;
    private int passwordOpen;
    boolean connected;
    BTicinoSocketReadManager gestSocketMonitor;
    BTicinoSocketWriteManager gestSocketCommands;

    public Gateway(String alias, String ipAddress, int portNumber, String gatewayType) {
        setIpAddress(ipAddress);
        setPortNumber(portNumber);
        setAlias(alias);
        setGatewayType(gatewayType);
        setConnected(false);
        setPasswordOpen(0);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public int getPasswordOpen() {
        return passwordOpen;
    }

    public void setPasswordOpen(int passwordOpen) {
        this.passwordOpen = passwordOpen;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean getConnected() {
        return connected;
    }

    public BTicinoSocketReadManager getGestSocketMonitor() {
        return gestSocketMonitor;
    }

    // inserire riferimento ad alias
    public void SetGestSocketMonitor(OpenWebNet freedomoticSensor, String alias) {
        // this.gestSocketMonitor = new BTicinoSocketReadManager(freedomoticSensor, alias);
    }
}

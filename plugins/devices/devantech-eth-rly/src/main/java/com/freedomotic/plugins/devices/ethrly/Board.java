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
package com.freedomotic.plugins.devices.ethrly;

/**
 *
 * @author Mauro Cicolella
 */
public class Board {

    private String ipAddress = null;
    private String autoConfiguration;
    private String objectClass;
    private String username;
    private String password;
    private String httpAuthentication;
    private String alias = null;
    private int port;
    private int relayNumber;
    private int[] relayStatus;

    /**
     *
     * @param ipAddress
     * @param port
     * @param alias
     * @param relayNumber
     * @param autoConfiguration
     * @param objectClass
     * @param username
     * @param password
     * @param httpAuthentication
     */
    public Board(String ipAddress, int port, String alias, int relayNumber, String autoConfiguration, String objectClass, String username, String password, String httpAuthentication) {
        setIpAddress(ipAddress);
        setPort(port);
        setAlias(alias);
        setUsername(username);
        setPassword(password);
        setHttpAuthentication(httpAuthentication);
        setRelayNumber(relayNumber);
        setAutoConfiguration(autoConfiguration);
        setObjectClass(objectClass);
        initializeRelayStatus(relayNumber);
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
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return
     */
    public String getHttpAuthentication() {
        return httpAuthentication;
    }

    /**
     *
     * @param httpAuthentication
     */
    public void setHttpAuthentication(String httpAuthentication) {
        this.httpAuthentication = httpAuthentication;
    }

    /**
     *
     * @return
     */
    public int getRelayNumber() {
        return relayNumber;
    }

    /**
     *
     * @param relayNumber
     */
    public void setRelayNumber(int relayNumber) {
        this.relayNumber = relayNumber;
    }

    /**
     *
     * @return
     */
    public String getAutoConfiguration() {
        return autoConfiguration;
    }

    /**
     *
     * @param autoConfiguration
     */
    public void setAutoConfiguration(String autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }

    /**
     *
     * @return
     */
    public String getObjectClass() {
        return objectClass;
    }

    /**
     *
     * @param objectClass
     */
    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    /**
     *
     * @param relayNumber
     * @return
     */
    public int getRelayStatus(int relayNumber) {
        return relayStatus[relayNumber];
    }

    /**
     *
     * @param relayNumber
     * @param value
     */
    public void setRelayStatus(int relayNumber, int value) {
        relayStatus[relayNumber] = value;
    }

    private void initializeRelayStatus(int relayNumber) {
        relayStatus = new int[relayNumber];
        for (int i = 0; i < relayNumber; i++) {
            relayStatus[i] = -1;
        }
    }
}

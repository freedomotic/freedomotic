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

package com.freedomotic.plugins.devices.tcw122bcm;

public class Board {

    private String alias = null;
    private String ipAddress = null;
    private int portNumber;
    private String digitalInput1 = null;
    private String digitalInput2 = null;
    private String relay1 = null;
    private String relay2 = null;
    private String analogInput1 = null;
    private String analogInput2 = null;
    private String temperature1 = null;
    private String temperature2 = null;
    private String humidity1 = null;
    private String humidity2 = null;
    private String relayObjectTemplate = null;
    private String temperatureObjectTemplate = null;
    private String humidityObjectTemplate = null;

    public Board(String alias, String ipAddress, int portNumber, String digitalInput1, String digitalInput2, String relay1, String relay2,
            String analogInput1, String analogInput2, String temperature1, String temperature2, String humidity1, String humidity2, String relayObjetcTemplate,
            String temperatureObjectTemplate, String humidityObjectTemplate) {
        setAlias(alias);
        setIpAddress(ipAddress);
        setPortNumber(portNumber);
        setDigitalInput1(digitalInput1);
        setDigitalInput2(digitalInput2);
        setRelay1(relay1);
        setRelay2(relay2);
        setAnalogInput1(analogInput1);
        setAnalogInput2(analogInput2);
        setTemperature1(temperature1);
        setTemperature2(temperature2);
        setHumidity1(humidity1);
        setHumidity2(humidity2);
        setRelayObjectTemplate(relayObjectTemplate);
        setTemperatureObjectTemplate(temperatureObjectTemplate);
        setHumidityObjectTemplate(humidityObjectTemplate);
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

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getDigitalInput1() {
        return digitalInput1;
    }

    public void setDigitalInput1(String digitalInput1) {
        this.digitalInput1 = digitalInput1;
    }

    public String getDigitalInput2() {
        return digitalInput2;
    }

    public void setDigitalInput2(String digitalInput2) {
        this.digitalInput2 = digitalInput2;
    }

    public String getRelay1() {
        return relay1;
    }

    public void setRelay1(String relay1) {
        this.relay1 = relay1;
    }

    public String getRelay2() {
        return relay2;
    }

    public void setRelay2(String relay2) {
        this.relay2 = relay2;
    }

    public String getAnalogInput1() {
        return analogInput1;
    }

    public void setAnalogInput1(String analogInput1) {
        this.analogInput1 = analogInput1;
    }

    public String getAnalogInput2() {
        return analogInput2;
    }

    public void setAnalogInput2(String analogInput2) {
        this.analogInput2 = analogInput2;
    }

    public String getTemperature1() {
        return temperature1;
    }

    public void setTemperature1(String temperature1) {
        this.temperature1 = temperature1;
    }

    public String getTemperature2() {
        return temperature2;
    }

    public void setTemperature2(String temperature2) {
        this.temperature2 = temperature2;
    }

    public String getHumidity1() {
        return humidity1;
    }

    public void setHumidity1(String humidity1) {
        this.humidity1 = humidity1;
    }

    public String getHumidity2() {
        return humidity2;
    }

    public void setHumidity2(String humidity2) {
        this.humidity2 = humidity2;
    }

    public String getRelayObjectTemplate() {
        return relayObjectTemplate;
    }

    public void setRelayObjectTemplate(String relayObjectTemplate) {
        this.relayObjectTemplate = relayObjectTemplate;
    }

    public String getTemperatureObjectTemplate() {
        return temperatureObjectTemplate;
    }

    public void setTemperatureObjectTemplate(String temperatureObjectTemplate) {
        this.temperatureObjectTemplate = temperatureObjectTemplate;
    }

    public String getHumidityObjectTemplate() {
        return humidityObjectTemplate;
    }

    public void setHumidityObjectTemplate(String humidityObjectTemplate) {
        this.humidityObjectTemplate = humidityObjectTemplate;
    }
}
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

package com.freedomotic.plugins.devices.k8055;

class Board {

    private String deviceToQuery = null;
    private int digitalOutputNumber;
    private int analogOutputNumber;
    private int analogInputNumber;
    private int digitalInputNumber;
    private int startingValue;
    private boolean[] digitalInput;
    private int[] analogInput;

    public Board(String deviceToQuery, int digitalOutputNumber, int analogOutputNumber, int analogInputNumber,
            int digitalInputNumber, int startingValue) {
        int i;

        setDeviceToQuery(deviceToQuery);
        setDigitalOutputNumber(digitalOutputNumber);
        setAnalogOutputNumber(analogOutputNumber);
        setAnalogInputNumber(analogInputNumber);
        setDigitalInputNumber(digitalInputNumber);
        setStartingValue(startingValue);
        digitalInput = new boolean[digitalInputNumber];
        analogInput = new int[analogInputNumber];
        //Initialize array that store input status
        for (i = 0; i < digitalInputNumber; i++) {
            digitalInput[i] = false;
        }
        for (i = 0; i < analogInputNumber; i++) {
            analogInput[i] = 0;
        }


    }

    public String getDeviceToQuery() {
        return deviceToQuery;
    }

    public void setDeviceToQuery(String deviceToQuery) {
        this.deviceToQuery = deviceToQuery;
    }

    public int getDigitalOutputNumber() {
        return digitalOutputNumber;
    }

    public void setDigitalOutputNumber(int digitalOutputNumber) {
        this.digitalOutputNumber = digitalOutputNumber;
    }

    public int getAnalogOutputNumber() {
        return analogOutputNumber;
    }

    public void setAnalogOutputNumber(int analogOutputNumber) {
        this.analogOutputNumber = analogOutputNumber;
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

    public int getStartingValue() {
        return startingValue;
    }

    public void setStartingValue(int startingValue) {
        this.startingValue = startingValue;
    }

    public int getAnalogValue(int index) {
        return analogInput[index];
    }

    public void setAnalogValue(int index, int value) {
        this.analogInput[index] = value;
    }

    public boolean getDigitalValue(int index) {
        return digitalInput[index];
    }

    public void setDigitalValue(int index, boolean value) {
        this.digitalInput[index] = value;
    }
}
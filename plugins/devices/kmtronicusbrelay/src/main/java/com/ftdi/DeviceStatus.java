/*
 * The MIT License
 *
 * Copyright 2011 Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ftdi;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Modem and line statuses of device.
 * @author Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>
 */
public enum DeviceStatus {

    /**
     * Clear To Send
     */
    CTS(0x10),
    /**
     * Data Set Ready
     */
    DSR(0x20),
    /**
     * Ring Indicator
     */
    RI(0x40),
    /**
     * Data Carrier Detect
     */
    DCD(0x80),
    /**
     * Overrun Error
     */
    OE(0x02),
    /**
     * Parity Error
     */
    PE(0x04),
    /**
     * Framing Error
     */
    FE(0x08),
    /**
     * Break Interrupt
     */
    BI(0x10);
    private final int constant;

    private DeviceStatus(int constant) {
        this.constant = constant;
    }

    int constant() {
        return this.constant;
    }
    
    static EnumSet<DeviceStatus> parseToEnumset(int val){
        ArrayList<DeviceStatus> enu = new ArrayList<DeviceStatus>();
        for (DeviceStatus curr : DeviceStatus.values()) {
            if((curr.constant() & val) != 0){
                enu.add(curr);
            }
        }
        return EnumSet.copyOf(enu);
    }
}

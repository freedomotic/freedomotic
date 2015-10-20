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

/**
 * Bit Modes
 * @author Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>
 */
public enum BitModes {

    BITMODE_RESET(0x00),
    BITMODE_ASYNC_BITBANG(0x01),
    BITMODE_MPSSE(0x02),
    BITMODE_SYNC_BITBANG(0x04),
    BITMODE_MCU_HOST(0x08),
    BITMODE_FAST_SERIAL(0x10),
    BITMODE_CBUS_BITBANG(0x20),
    BITMODE_SYNC_FIFO(0x40);
    private final int constant;

    private BitModes(int constant) {
        this.constant = constant;
    }

    int constant() {
        return this.constant;
    }

    static BitModes parse(int val) {
        for (BitModes curr : BitModes.values()) {
            if (curr.constant() == val) {
                return curr;
            }
        }
        return null;
    }
}

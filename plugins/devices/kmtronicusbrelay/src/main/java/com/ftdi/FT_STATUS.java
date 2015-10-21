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
 * FT_STATUS (DWORD)
 * @author Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>
 */
enum FT_STATUS {

    OK,
    INVALID_HANDLE,
    DEVICE_NOT_FOUND,
    DEVICE_NOT_OPENED,
    IO_ERROR,
    INSUFFICIENT_RESOURCES,
    INVALID_PARAMETER,
    INVALID_BAUD_RATE,
    DEVICE_NOT_OPENED_FOR_ERASE,
    DEVICE_NOT_OPENED_FOR_WRITE,
    FAILED_TO_WRITE_DEVICE,
    EEPROM_READ_FAILED,
    EEPROM_WRITE_FAILED,
    EEPROM_ERASE_FAILED,
    EEPROM_NOT_PRESENT,
    EEPROM_NOT_PROGRAMMED,
    INVALID_ARGS,
    NOT_SUPPORTED,
    OTHER_ERROR;
    
    int constant(){
        return this.ordinal();
    }
}

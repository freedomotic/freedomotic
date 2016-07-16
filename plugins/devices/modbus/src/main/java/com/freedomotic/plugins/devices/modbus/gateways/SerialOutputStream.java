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
package com.freedomotic.plugins.devices.modbus.gateways;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class that wraps a {@link SerialPort} to provide {@link OutputStream}
 * functionality.
 * <br>
 * It is instantiated by passing the constructor a {@link SerialPort} instance.
 * Do not create multiple streams for the same serial port unless you implement
 * your own synchronization.
 *
 * @author Charles Hache <chalz@member.fsf.org>
 *
 * Attribution: https://github.com/therealchalz/java-simple-serial-connector
 *
 */
public class SerialOutputStream extends OutputStream {

    SerialPort serialPort;

    /**
     * Instantiates a SerialOutputStream for the given {@link SerialPort} Do not
     * create multiple streams for the same serial port unless you implement
     * your own synchronization.
     *
     * @param sp The serial port to stream.
     */
    public SerialOutputStream(SerialPort sp) {
        serialPort = sp;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            serialPort.writeInt(b);
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);

    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] buffer = new byte[len];
        System.arraycopy(b, off, buffer, 0, len);
        try {
            serialPort.writeBytes(buffer);
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }
}

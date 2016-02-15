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


package com.freedomotic.plugins.devices.PioneerKuro;

import com.freedomotic.serial.SerialConnectionProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Class declaration
 *
 *
 * @author
 * @version 1.10, 08/04/00
 */
public class PioneerTest {

    static public final char STX = 0x02;
    static public final char ETX = 0x03;

    /**
     * Method declaration
     *
     *
     * @param args
     *
     * @see
     */
    public static void main(String[] args) {
        try {
//                Properties config = new Properties();
//                String manifest ="PioneerKuroGateway.properties";
//                config.setProperty("port", "COM1");
//                config.setProperty("baudrate","19200");
//                config.setProperty("data-bits","8");
//                config.setProperty("stop-bits","1");
//                config.setProperty("parity","0");
//                config.store(new FileOutputStream(manifest),"");

//                SerialConnectionProvider usb = PioneerKuroGateway.getInstance();

//                PioneerKuroActuator testActuator = new PioneerKuroActuator();
//                testActuator.test();
            SerialConnectionProvider usb;
            System.out.println("Test started ");

            usb = new SerialConnectionProvider();
            usb.setPortName("/dev/ttyUSB10");
            usb.setPortBaudrate(9600);
            usb.setPortDatabits(8);
            usb.setPortParity(0);
            usb.setPortStopbits(1);
            System.out.println("\nTesting write to serial");
            System.out.println("stx: " + STX);
            System.out.println("etx: " + ETX);
            System.out.println("pos eso");
            String msg = STX + "**POF" + ETX;
            System.out.println("msg: " + msg);
            String output = usb.send(msg);
            System.out.println("Salida del puerto: " + output);

            usb.disconnect();
            //assertEquals(1, 1);
            // sudo socat pty,link=/dev/ttyUSB10,waitslave,ignoreeof tcp:192.168.1.5:54321 &
            // sudo socat tcp-l:54321,reuseaddr,fork file:/dev/ttyUSB0,nonblock,waitlock=/var/run/ttyUSB0.lock
            // sudo socat tcp-l:54321,reuseaddr,fork file:/dev/ttyUSB0,nonblock,waitlock=/var/run/ttyUSB0.lock
            //testActuator.close();
            //Thread.sleep(100);
        } catch (Exception ex) {
            System.out.println("Error");
            // Logger.getLogger(KuroTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

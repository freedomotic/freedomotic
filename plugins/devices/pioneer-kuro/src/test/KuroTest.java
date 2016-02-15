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

//import gnu.io.SerialPort;
//import gnu.io.SerialPort;
import com.freedomotic.serial.SerialConnectionProvider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gpt
 */

public class KuroTest {

    public KuroTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public static final char STX = 0x02;
    public static final char ETX = 0x03;

    /**
     * Test of read method, of class SerialConnectionProvider.
     */
    @Test
    public void testRead() {
        try {
            SerialConnectionProvider usb;
            System.out.println("Test started ");

            usb = new SerialConnectionProvider();
            usb.setPortName("/dev/ttyUSB11");
            usb.setPortBaudrate(9600);
//            usb.setPortDatabits(SerialPort.DATABITS_8);
//            usb.setPortParity(SerialPort.PARITY_NONE);
//            usb.setPortStopbits(SerialPort.STOPBITS_1);
            System.out.println("\nTesting write to serial");
            usb.connect();
            System.out.println("stx: " + STX);
            System.out.println("etx: " + ETX);
            System.out.println("pos eso");
            String msg = STX + "**PON" + ETX;
            System.out.println("pos eso 222");
            System.out.println("msg: " + msg);
            String output = usb.send(msg);
            System.out.println("Salida del puerto: " + output);
            usb.disconnect();
            //assertEquals(1, 1);
            // sudo socat pty,link=/dev/ttyUSB10,waitslave,ignoreeof tcp:192.168.1.5:54321 &
            // sudo socat tcp-l:54321,reuseaddr,fork file:/dev/ttyUSB0,nonblock,waitlock=/var/run/ttyUSB0.lock
            // sudo socat tcp-l:54321,reuseaddr,fork file:/dev/ttyUSB0,nonblock,waitlock=/var/run/ttyUSB0.lock
        } catch (Exception ex) {
            Logger.getLogger(KuroTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

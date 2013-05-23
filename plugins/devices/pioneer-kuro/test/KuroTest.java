/*  
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//import gnu.io.SerialPort;
//import gnu.io.SerialPort;
import it.freedomotic.serial.SerialConnectionProvider;
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

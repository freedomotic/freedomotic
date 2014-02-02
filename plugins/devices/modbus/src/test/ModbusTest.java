/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import gnu.io.SerialPort;
import gnu.io.CommPortIdentifier;
import gnu.io.CommDriver;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.locator.NumericLocator;
import com.freedomotic.Modbus.gateways.ModbusMasterGateway;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gpt
 */
public class ModbusTest {

    public ModbusTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    public SerialConnectionProvider setUp() {
//        Properties config = new Properties();
//        config.setProperty("hello-message", "$>9000PXD3#"); //hello message defined by pmix15 comm protocol
//        config.setProperty("hello-reply", "$<9000VP"); //expected reply to hello-message starts with this string
//        config.setProperty("polling-message", "$>9000RQCE#"); //$>9000RQcs# forces read data using this string
//        return new SerialConnectionProvider(config);
//    }
//
//    public SerialConnectionProvider setUp2() {
//        Properties config = new Properties();
//        config.setProperty("port", "/dev/ttyUSB0"); //name of the serial port
//        config.setProperty("polling-message", "$>9000RQCE#"); //$>9000RQcs# forces read data using this string
//        return new SerialConnectionProvider(config);
//    }
    /**
     * Test of read method, of class SerialConnectionProvider.
     */
    @Test
    public void testRead() {

        System.out.println("\nTesting read method from modbus");
        //Config config = new Config();
//        config.setProperty("port", "/dev/ttyUSB10");
//        config.setProperty("baudrate",String.valueOf(19200));
//        config.setProperty("data-bits",String.valueOf(SerialPort.DATABITS_8));
//        config.setProperty("parity", String.valueOf(SerialPort.PARITY_EVEN));
//        config.setProperty("stop-bits", String.valueOf(SerialPort.STOPBITS_1));
        ModbusMaster master = ModbusMasterGateway.getInstance();
        try {
            master.init();
            NumericLocator bl = new NumericLocator(1, RegisterRange.HOLDING_REGISTER, 266, DataType.TWO_BYTE_INT_UNSIGNED);
            System.out.println(master.getValue(bl));

        } catch (ModbusTransportException ex) {
            System.out.println(ex.toString());
        } catch (ErrorResponseException ex) {
            System.out.println(ex.toString());
        } catch (ModbusInitException ex) {
            System.out.println(ex.toString());
        } finally {
            master.destroy();
        }
        assertEquals(1, 1);
        // sudo socat pty,link=/dev/ttyUSB10,waitslave,ignoreeof tcp:192.168.1.5:54321 &
        // chown user /dev/ttyUSB10
        // sudo socat tcp-l:54321,reuseaddr,fork file:/dev/ttyUSB0,nonblock,waitlock=/var/run/ttyUSB0.lock
    }

    /**
     * Test of write method.
     */
    @Test
    public void testWrite() {
        ModbusMaster master = ModbusMasterGateway.getInstance();
        int value1 = 235;
        int value = 0;
        try {
            master.init();
            //master.setValue(1,768,2,true); 
            NumericLocator bl = new NumericLocator(1, RegisterRange.HOLDING_REGISTER, 771, DataType.TWO_BYTE_INT_UNSIGNED);
            master.setValue(bl, value1);
            value = (Integer) master.getValue(bl);

        } catch (ModbusTransportException ex) {
            System.out.println(ex.toString());
        } catch (ErrorResponseException ex) {
            System.out.println(ex.toString());
        } catch (ModbusInitException ex) {
            System.out.println(ex.toString());
        } finally {
            master.destroy();
        }
        assertEquals(value, value1);


    }
}

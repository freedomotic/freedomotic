/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.Modbus.gateways;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.NumericLocator;
import gnu.io.SerialPort;
import it.freedomotic.model.ds.Config;

/**
 *
 * @author gpt
 */
public class ModbusTestMain {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("\nTesting read method from modbus");

        Config config = new Config();
        config.setProperty("ModbusProtocol", "TCP");
        
        //Serial test
        config.setProperty("port", "/dev/ttyUSB0");
        config.setProperty("baudrate", String.valueOf(19200));
        config.setProperty("data-bits", String.valueOf(SerialPort.DATABITS_8));
        config.setProperty("parity", String.valueOf(SerialPort.PARITY_EVEN));
        config.setProperty("stop-bits", String.valueOf(SerialPort.STOPBITS_1));
        
        //TCP Test
        config.setProperty("host", "192.168.1.9");
        config.setProperty("tcpport", String.valueOf(502));
        



        ModbusMaster master = ModbusMasterGateway.getInstance(config);
        try {
            master.init();            
            NumericLocator bl = new NumericLocator(1, RegisterRange.HOLDING_REGISTER, 266,DataType.TWO_BYTE_INT_UNSIGNED);
            System.out.println("readed value: " + master.getValue(bl));
            master.setValue(bl, 1);

        } catch (ModbusTransportException ex) {
            System.out.println("error1: " + ex.toString());
        } catch (ErrorResponseException ex) {
            System.out.println("error2: " + ex.toString());
        } catch (ModbusInitException ex) {
            System.out.println("error3:" + ex.toString());
        } finally {
            master.destroy();
        }

    }
}

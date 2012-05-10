/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.freedom.Modbus.gateways;

import com.serotonin.io.serial.SerialParameters;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import gnu.io.SerialPort;
import it.freedom.model.ds.Config;
import java.util.Properties;

/**
 *
 * @author gpt
 */
public class ModbusMasterGateway {

	//class attributes
	private static ModbusMaster master= null;  //Singleton reference

//        private static String PORT_NAME = "/dev/ttyUSB10";
//        private static int PORT_BAUDRATE = 19200;
//        private static int PORT_DATABITS =8;
//        private static int PORT_PARITY = 2;//even
//        private static int PORT_STOPBITS = 1;
//        //private static boolean echo = false;
//        private static int receiveTimeout = 10000;//10 seconds
//        private static int retries = 1;



    public ModbusMasterGateway() {
    }

    public static ModbusMaster getInstance()
    {
        return getInstance(new Config());
    }
    public static ModbusMaster getInstance(Config configuration) {
        if (master != null) {
            return master;
        } else {
            ModbusFactory factory = new ModbusFactory();
            SerialParameters params = new SerialParameters();
            String port = configuration.getStringProperty("port", "/dev/ttyUSB10");
            int baudrate = configuration.getIntProperty("baudrate",19200);
            System.out.println("baudrate: " +baudrate);
            int databits =configuration.getIntProperty("data-bits",SerialPort.DATABITS_8);
             System.out.println("databits: " +databits);
            int parity  = configuration.getIntProperty("parity", SerialPort.PARITY_EVEN);
            System.out.println("parity: " +parity);
            int stopbits = configuration.getIntProperty("stop-bits", SerialPort.STOPBITS_1);
            System.out.println("stopbits: " +stopbits);
            params.setCommPortId(port);
            params.setBaudRate(baudrate);
            params.setDataBits(databits);
            params.setParity(parity);
            params.setStopBits(stopbits);

            //private static boolean echo = false;
            int receiveTimeout = configuration.getIntProperty("timeout", 5000);//5 seconds
            int retries = configuration.getIntProperty("retries", 1);
            master = factory.createRtuMaster(params);
            master.setTimeout(receiveTimeout);
            master.setRetries(retries);
            return master;
        }
    }

















     
}



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

import com.serotonin.modbus4j.serial.*;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.serial.SerialMaster;
//import gnu.io.SerialPort;
import com.freedomotic.model.ds.Config;
//import net.wimpi.modbus.util.SerialParameters;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public class ModbusMasterGateway {

    //class attributes
    private static ModbusMaster master = null;  //Singleton reference
    private static String connectionInfo = "No connected";
    private static final String PORT_NAME = "/dev/ttyUSB10";
    private static final int PORT_BAUDRATE = 19200;
    private static final int PORT_DATABITS = 8;
    private static final int PORT_PARITY = 2;//even
    private static final int PORT_STOPBITS = 1;
    private static final int PORT_FLOW_CONTROL_IN = 1; // to check
    private static final int PORT_FLOW_CONTROL_OUT = 1; // to check

//        //private static boolean echo = false;
//        private static int receiveTimeout = 10000;//10 seconds
//        private static int retries = 1;
    /**
     *
     */
    public ModbusMasterGateway() {
    }

    /**
     *
     * @return
     */
    public static ModbusMaster getInstance() {
        return getInstance(new Config());
    }

    /**
     *
     * @param configuration
     * @return
     */
    public static ModbusMaster getInstance(Config configuration) {
        if (master != null) {
            return master;
        } else {
            String modbusProtocol = configuration.getStringProperty("modbus-protocol", "TCP");
            if ("TCP".equals(modbusProtocol)) {
                configureTCP(configuration);
            } else {
                configureSerial(configuration);
            }
            //private static boolean echo = false;
            int receiveTimeout = configuration.getIntProperty("timeout", 5000);//5 seconds
            int retries = configuration.getIntProperty("retries", 1);
            boolean multiwrites = configuration.getBooleanProperty("multiwrite-always", false);
            master.setTimeout(receiveTimeout);
            master.setRetries(retries);
            master.setMultipleWritesOnly(multiwrites);
            return master;
        }

    }

    /**
     *
     * @return
     */
    public static String ConnectionInfo() {
        return connectionInfo;

    }

    private static void configureSerial(Config configuration) {
        ModbusFactory factory = new ModbusFactory();
        //SerialParameters params = new SerialParameters();
        String commPortId = configuration.getStringProperty("port", PORT_NAME);
        System.out.println("port name: " + commPortId);
        int baudRate = configuration.getIntProperty("baudrate", PORT_BAUDRATE);
        System.out.println("baudrate: " + baudRate);
        int dataBits = configuration.getIntProperty("data-bits", PORT_DATABITS);
        System.out.println("databits: " + dataBits);
        int parity = configuration.getIntProperty("parity", PORT_PARITY);
        System.out.println("parity: " + parity);
        int stopBits = configuration.getIntProperty("stop-bits", PORT_STOPBITS);
        System.out.println("stopbits: " + stopBits);
        int flowControlIn = configuration.getIntProperty("flow-control-in", PORT_FLOW_CONTROL_IN);
        System.out.println("flowcontrolin: " + flowControlIn);
        int flowControlOut = configuration.getIntProperty("flow-control-out", PORT_FLOW_CONTROL_OUT);
        System.out.println("flowcontrolout: " + flowControlOut);
        SerialPortWrapperImpl params = new SerialPortWrapperImpl(commPortId, baudRate, dataBits, stopBits, parity, flowControlIn,
                flowControlOut);
        //params.setCommPortId(port);
        //params.setBaudRate(baudrate);
        //params.setDataBits(databits);
        //params.setParity(parity);
        //params.setStopBits(stopbits);
        master = factory.createRtuMaster(params);
        connectionInfo = "Serial Connection to: " + commPortId;
    }

    private static void configureTCP(Config configuration) {
        ModbusFactory factory = new ModbusFactory();
        IpParameters params = new IpParameters();
        String host = configuration.getStringProperty("host", "localhost");
        System.out.println("host: " + host);
        int tcpport = configuration.getIntProperty("tcp-port", 502);
        System.out.println("tcpport: " + tcpport);
        Boolean encap = configuration.getBooleanProperty("encapsulated", false);
        params.setEncapsulated(encap);
        params.setHost(host);
        params.setPort(tcpport);
        master = factory.createTcpMaster(params, true);
        connectionInfo = "TCP Connection to: " + host + ":" + tcpport;
    }
    
}

/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
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

package com.freedomotic.plugins.devices.modbus.gateways;

import com.serotonin.io.serial.SerialParameters;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.serial.SerialMaster;
import gnu.io.SerialPort;
import com.freedomotic.model.ds.Config;

/**
 *
 * @author gpt
 */

public class ModbusMasterGateway {

    //class attributes
    private static ModbusMaster master = null;  //Singleton reference
    private static String connectionInfo = "No connected";

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

    public static ModbusMaster getInstance() {
        return getInstance(new Config());
    }

    public static ModbusMaster getInstance(Config configuration) {
        if (master != null) {
            return master;
        } else {
            String modbusProtocol = configuration.getStringProperty("modbusProtocol", "TCP");
            if (modbusProtocol == "TCP") {
                configureTCP(configuration);
            } else {
                configureSerial(configuration);
            }
            //private static boolean echo = false;
            int receiveTimeout = configuration.getIntProperty("timeout", 5000);//5 seconds
            int retries = configuration.getIntProperty("retries", 1);
            master.setTimeout(receiveTimeout);
            master.setRetries(retries);
            return master;
        }

    }

    public static String ConnectionInfo() {
        return connectionInfo;

    }

    private static void configureSerial(Config configuration) {
        ModbusFactory factory = new ModbusFactory();
        SerialParameters params = new SerialParameters();
        String port = configuration.getStringProperty("port", "/dev/ttyUSB10");
        int baudrate = configuration.getIntProperty("baudrate", 19200);
        System.out.println("baudrate: " + baudrate);
        int databits = configuration.getIntProperty("data-bits", SerialPort.DATABITS_8);
        System.out.println("databits: " + databits);
        int parity = configuration.getIntProperty("parity", SerialPort.PARITY_EVEN);
        System.out.println("parity: " + parity);
        int stopbits = configuration.getIntProperty("stop-bits", SerialPort.STOPBITS_1);
        System.out.println("stopbits: " + stopbits);
        params.setCommPortId(port);
        params.setBaudRate(baudrate);
        params.setDataBits(databits);
        params.setParity(parity);
        params.setStopBits(stopbits);
        master = factory.createRtuMaster(params, SerialMaster.SYNC_FUNCTION);
        connectionInfo = "Serial Connection to: " + port;
    }

    private static void configureTCP(Config configuration) {
        ModbusFactory factory = new ModbusFactory();
        IpParameters params = new IpParameters();
        String host = configuration.getStringProperty("host", "localhost");
        System.out.println("host: " + host);
        int tcpport = configuration.getIntProperty("tcpport", 502);
        System.out.println("tcpport: " + tcpport);
        params.setHost(host);
        params.setPort(tcpport);
        master = factory.createTcpMaster(params, true);
        connectionInfo = "TCP Connection to: " + host + ":" + tcpport;
    }
}

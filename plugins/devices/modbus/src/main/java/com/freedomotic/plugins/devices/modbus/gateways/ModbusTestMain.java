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

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.NumericLocator;
import gnu.io.SerialPort;
import com.freedomotic.model.ds.Config;

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
            NumericLocator bl = new NumericLocator(1, RegisterRange.HOLDING_REGISTER, 266, DataType.TWO_BYTE_INT_UNSIGNED);
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

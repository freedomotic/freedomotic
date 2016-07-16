package com.freedomotic.plugin.devices.modbus.test;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.model.ds.Config;
import com.freedomotic.plugins.devices.modbus.gateways.ModbusMasterGateway;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BinaryLocator;
import com.serotonin.modbus4j.locator.NumericLocator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author matteo
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class ModbusTest {

    Config config = new Config();
    ModbusMaster master;

    //@Before
    public void prepare() {
        config.setProperty("ModbusProtocol", "TCP");

        //TCP Test
        config.setProperty("host", "192.168.2.57");
        config.setProperty("tcp-port", "4001");
        config.setProperty("encapsulated", "true");
        config.setProperty("timeout", "1000");

        master = ModbusMasterGateway.getInstance(config);
        try {
            master.init();
            master.setMultipleWritesOnly(true);
        } catch (ModbusInitException ex) {
            Logger.getLogger(ModbusTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void joking(){}
    
   // @Test
    public void manageoutput() throws InterruptedException {
        try {
            boolean[] valori = {true, false, true, false, true, false, true, false};
            short[] vals = {1, 0, 1, 0, 1, 0, 1, 0};

            for (int i = 0; i < 8; i++) {
                BinaryLocator bl = new BinaryLocator(2, RegisterRange.HOLDING_REGISTER, 13 + i, 0);
                boolean t = master.getValue(bl);
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "READING OUTPUT " + i + " VALUE: " + t);
            }
            for (int i = 0; i < 8; i++) {
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "CONFIG I/O " + i);
                NumericLocator nl = new NumericLocator(2, RegisterRange.HOLDING_REGISTER, 5 + i, DataType.TWO_BYTE_INT_UNSIGNED);
                master.setValue(nl, 0);
            }
            for (int i = 0; i < 8; i++) {
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "WRITING OUTPUT " + i);
                NumericLocator nl = new NumericLocator(2, RegisterRange.HOLDING_REGISTER, 13 + i, DataType.TWO_BYTE_INT_UNSIGNED);
                master.setValue(nl, vals[i]);
            }
            for (int i = 0; i < 8; i++) {
                BinaryLocator bl = new BinaryLocator(2, RegisterRange.HOLDING_REGISTER, 13 + i, 0);
                boolean t = master.getValue(bl);
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "READING OUTPUT " + i + " VALUE: " + t);
                Assert.assertEquals(valori[i], t);
            }
            for (int i = 0; i < 8; i++) {
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "WRITING OUTPUT " + i);
                NumericLocator nl = new NumericLocator(2, RegisterRange.HOLDING_REGISTER, 13 + i, DataType.TWO_BYTE_INT_UNSIGNED);
                master.setValue(nl, 1 - vals[i]);
            }
            for (int i = 0; i < 8; i++) {
                BinaryLocator bl = new BinaryLocator(2, RegisterRange.HOLDING_REGISTER, 13 + i, 0);
                boolean t = master.getValue(bl);
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "READING OUTPUT " + i + " VALUE: " + t);
                Assert.assertEquals(!valori[i], t);
            }

            for (int i = 0; i < 8; i++) {
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "WRITING OUTPUT " + i);
                NumericLocator nl = new NumericLocator(2, RegisterRange.HOLDING_REGISTER, 13 + i, DataType.TWO_BYTE_INT_UNSIGNED);
                master.setValue(nl, 0);
            }
            // init output
            for (int i = 0; i < 8; i++) {
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "CONFIG I/O " + i);
                NumericLocator nl = new NumericLocator(2, RegisterRange.HOLDING_REGISTER, 5 + i, DataType.TWO_BYTE_INT_UNSIGNED);
                master.setValue(nl, 300);
            }
            for (int i = 0; i < 8; i++) {
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "WRITING OUTPUT " + i);
                NumericLocator nl = new NumericLocator(2, RegisterRange.HOLDING_REGISTER, 13 + i, DataType.TWO_BYTE_INT_UNSIGNED);
                master.setValue(nl, 1);
            }

        } catch (ModbusTransportException ex) {
            System.out.println("error1: " + ex.toString());
        } catch (ErrorResponseException ex) {
            System.out.println("error2: " + ex.toString());
        }
    }
// @Test
    public void readinput() {
        System.out.println("\nTesting read method from modbus");

        try {
            boolean[] valori = {true, false, false, false, false, false, false, true};

            for (int i = 0; i < 8; i++) {

                BinaryLocator bl = new BinaryLocator(2, RegisterRange.HOLDING_REGISTER, 36 + i, 0);
                boolean t = master.getValue(bl);
                Logger.getLogger(ModbusTest.class.getName()).log(Level.INFO, "READING INPUT: " + i);
                Assert.assertEquals(valori[i], t);
            }
            //NumericLocator nl = new NumericLocator(2, RegisterRange.HOLDING_REGISTER, 13, DataType.TWO_BYTE_INT_UNSIGNED);
            //master.setValue(nl, 1);

        } catch (ModbusTransportException ex) {
            System.out.println("error1: " + ex.toString());
        } catch (ErrorResponseException ex) {
            System.out.println("error2: " + ex.toString());
        }
    }

   // @After
    public void dismiss() {
        master.destroy();
    }
}

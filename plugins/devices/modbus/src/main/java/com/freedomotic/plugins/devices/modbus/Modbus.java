/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.modbus;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.freedomotic.plugins.devices.modbus.gateways.ModbusMasterGateway;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpt
 */
public class Modbus extends Protocol {

    private static final Logger LOG = Logger.getLogger(Modbus.class.getName());
    private int numRegisters;
    private BatchRead<String> batchRead = new BatchRead<String>();
    private BatchResults<String> results;
    private List<FreedomModbusLocator> points = new ArrayList<FreedomModbusLocator>();
    private int pollingTime;
    private ModbusMaster master;

    public Modbus() {
        super("Modbus", "/modbus/modbus.xml");
    }

    /*
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();

        batchRead.setContiguousRequests(true);
        //ModBus General Configuration
        pollingTime = configuration.getIntProperty("PollingTime", 1000);
        points.clear();
        //Modbus registers configuration        
        for (int i = 0; i < configuration.getTuples().size(); i++) {
            FreedomModbusLocator locator = new FreedomModbusLocator(configuration, i);
            points.add(locator);
            locator.updateBatchRead(batchRead);
        }
        master = ModbusMasterGateway.getInstance(configuration);
        setPollingWait(pollingTime);


    }

    @Override
    public void onStop() {
        super.onStop();
        master.destroy();
        this.setDescription("Disconnected");
        setPollingWait(-1); // disable polling
    }

    @Override
    protected void onRun() {
        try {
            try {
                master.init();
                results = master.send(batchRead);
                sendEvents();
            } catch (ModbusTransportException ex) {
                Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
                die(ex.getLocalizedMessage());
            } catch (ErrorResponseException ex) {
                Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
                die(ex.getLocalizedMessage());
            } catch (ModbusInitException ex) {
                Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
                die(ex.getLocalizedMessage());
            }
            Thread.sleep(pollingTime);
        } catch (InterruptedException ex) {
            Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void die(String message) {
        master.destroy();
        //we can use the plugin description to provide usefull information
        this.setDescription("Plugin stoped: " + message);
        stop(); //stops the plugin on errors
    }

    private void sendEvents() {
        for (FreedomModbusLocator point : points) {
            //TODO: Generate the modified point. At this moment, the points ArrayList is of ModbusLocator.            
//            GenericEvent event = new GenericEvent(this);                                                
//            point.fillEvent(results, event);
//            Freedomotic.logger.info("Sending Modbus Generic read event: '" + event.toString() + " with value: "+ event.getProperty("value"));
//            notifyEvent(event); //sends the event on the messaging bus

            //use of Protocol Reads

            ProtocolRead protocolEvent = new ProtocolRead(this, "Modbus", point.getName());
            point.fillProtocolEvent(results, protocolEvent);
            LOG.info("Sending Modbus protocol read event for eventName name: " + point.getName() + " value: " + protocolEvent.getProperty("behaviorValue"));
            Freedomotic.sendEvent(protocolEvent);
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            FreedomModbusLocator locator = new FreedomModbusLocator(c.getProperties(), 0);
            //TODO: Syncronize the master?            
            master.init();
            Object value = locator.parseValue(c.getProperties(), 0);
            master.setValue(locator.getModbusLocator(), value);
            //TODO: read OK response?
            //master.getValue(locator.getModbusLocator());

            //TODO: manage the exceptions        
        } catch (ModbusTransportException ex) {
            Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ErrorResponseException ex) {
            Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ModbusInitException ex) {
            Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            master.destroy();
        }

    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
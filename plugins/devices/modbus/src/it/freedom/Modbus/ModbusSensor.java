/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedom.Modbus;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import it.freedom.api.Sensor;
import it.freedom.exceptions.UnableToExecuteException;
import java.io.IOException;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import gnu.io.NoSuchPortException;
import it.freedom.Modbus.gateways.ModbusMasterGateway;
import it.freedom.events.GenericEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpt
 */
public class ModbusSensor extends Sensor {

    private int numRegisters;
    private BatchRead<String> batchRead = new BatchRead<String>();
    private BatchResults<String> results;
    private List<FreedomModbusLocator> points = new ArrayList<FreedomModbusLocator>();
    private int pollingTime;
    private ModbusMaster master;

    public ModbusSensor() {
        super("Modbus Sensor", "/es.gpulido.modbus/modbus-sensor.xml");
        batchRead.setContiguousRequests(true);
        //ModBus General Configuration
        pollingTime = configuration.getIntProperty("PollingTime", 1000);
        numRegisters = configuration.getIntProperty("NumRegisters", 1);
        //Modbus registers configuration
        for (int i = 0; i < numRegisters; i++) {
            FreedomModbusLocator locator = new FreedomModbusLocator(configuration, i);
            points.add(locator);
            locator.updateBatchRead(batchRead);
        }
        master = ModbusMasterGateway.getInstance(configuration);
        this.setAsPollingSensor();
        start();
    }

    @Override
    protected void onInformationRequest() throws IOException, UnableToExecuteException {
        //maybe a publishResults could be use here
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        try {
            try {
                master.init();
                results = master.send(batchRead);
                sendEvents();
            } catch (ModbusTransportException ex) {
                Logger.getLogger(ModbusSensor.class.getName()).log(Level.SEVERE, null, ex);
                die(ex.getLocalizedMessage());
            } catch (ErrorResponseException ex) {
                Logger.getLogger(ModbusSensor.class.getName()).log(Level.SEVERE, null, ex);
                die(ex.getLocalizedMessage());
            } catch (ModbusInitException ex) {
                Logger.getLogger(ModbusSensor.class.getName()).log(Level.SEVERE, null, ex);
                die(ex.getLocalizedMessage());
            }
            Thread.sleep(pollingTime);
        } catch (InterruptedException ex) {
            Logger.getLogger(ModbusSensor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void die(String message) {
        master.destroy();
        //we can use the plugin description to provide usefull information
        this.setDescription("Plugin stoped: " + message);
        stop(); //stops the plugin on errors
    }

    @Override
    protected void onStart() {
    }

    private void sendEvents() {

        System.out.println("antes del for de sendEvents(): " + points.toString());
        for (int i = 0; i < points.size(); i++) {
            //TODO: Generate the modified point. At this moment, the points ArrayList is of ModbusLocator.
            //TODO: Use a more especific event (using the eventname property of the xml)
            GenericEvent event = new GenericEvent(this);
            points.get(i).fillEvent(results, event);
            notifyEvent(event); //sends the event on the messaging bus
        }
    }
}

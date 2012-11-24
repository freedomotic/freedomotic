/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.Modbus;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import it.freedomotic.Modbus.gateways.ModbusMasterGateway;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.GenericEvent;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpt
 */
public class Modbus extends Protocol{
    
    private int numRegisters;
    private BatchRead<String> batchRead = new BatchRead<String>();
    private BatchResults<String> results;
    private List<FreedomModbusLocator> points = new ArrayList<FreedomModbusLocator>();
    private int pollingTime;
    private ModbusMaster master;
        
    
    public Modbus()
    {
        super("Modbus", "/es.gpulido.modbus/modbus.xml");
    }

    
     /* Sensor side */
    @Override
    public void onStart() {
        super.onStart();
        
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
        setPollingWait(pollingTime);
        this.setDescription(ModbusMasterGateway.ConnectionInfo());
        
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
        for (int i = 0; i < points.size(); i++) {
            //TODO: Generate the modified point. At this moment, the points ArrayList is of ModbusLocator.
            //TODO: Use a more especific event (using the eventname property of the xml)            
            GenericEvent event = new GenericEvent(this);
            points.get(i).fillEvent(results, event);
            Freedomotic.logger.info("Sending Modbus Generic read event: '" + event.toString());
            notifyEvent(event); //sends the event on the messaging bus
        }
    }
    
    
    
    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            FreedomModbusLocator locator = new FreedomModbusLocator(c.getProperties(), 0);
            //TODO: Syncronize the master?            
            master.init();            
            Object value = locator.parseValue(c.getProperties(),0);
            master.setValue(locator.getModbusLocator(),value);
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

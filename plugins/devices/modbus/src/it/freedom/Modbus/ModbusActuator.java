/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.freedom.Modbus;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import it.freedom.Modbus.gateways.ModbusMasterGateway;
import it.freedom.api.Actuator;
import it.freedom.exceptions.UnableToExecuteException;
import it.freedom.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpt
 */
public class ModbusActuator extends Actuator {

   private ModbusMaster master;

    public ModbusActuator(){
        super("Modbus Actuator", "/es.gpulido.modbus/modbus-actuator.xml");
        master = ModbusMasterGateway.getInstance(configuration);
        start();
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
            Logger.getLogger(ModbusActuator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ErrorResponseException ex) {
            Logger.getLogger(ModbusActuator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ModbusInitException ex) {
            Logger.getLogger(ModbusActuator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            master.destroy();
        }        
        
    }


    protected void setTargetObjectBehavior(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}

package it.freedom.X10;

import it.freedom.X10.gateways.PMix35Gateway;
import it.freedom.api.Actuator;
import it.freedom.exceptions.UnableToExecuteException;
import it.freedom.reactions.Command;
import it.freedom.util.Info;
import it.nicoletti.serial.SerialConnectionProvider;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author roby
 */
public class X10Actuator extends Actuator {

    SerialConnectionProvider usb;

    public X10Actuator() {
        super("X10Actuator", "/it.nicoletti.x10/x10actuator.xml");
        usb = PMix35Gateway.getInstance();
        start();
    }

    public boolean canExecute(Command c) {
        return false;
    }

    /* this plugin can accept this values:
     * address: in form of X10 address (HOUSECODE+UNITCODE)of the target device. eg: P01, P02, A01, A02, ...
     * HOUSECODE is a letter from A to P
     * UNITCODE is a numner from 01 to 16
     * X10 supported commands:
     *      HOUSECODE+ON. eg: PON
     *      HOUSECODE+OFF eg: POFF
     *      HOUSECODE+BGT: 4% brighter
     *      HOUSECODE+DIM: 4% darker
     * A complete message to the device will be
     * A01A01AONAON (X10 requires double repetition of address and commans)
     */
    @Override
    public void onCommand(Command c) throws IOException, UnableToExecuteException {
        String housecode = c.getProperty("address").substring(0, 1);
        String command = c.getProperty("command");
        String message;
        if (X10Sensor.isCommand(command)) {
            message = c.getProperty("address") + c.getProperty("address") + " " + command + command;
        } else {
            command= (housecode + command).toUpperCase();
            message = c.getProperty("address") + c.getProperty("address") + " " +  command + command;
        }
        String readed = null;
        try {
            Date time = new Date();
            System.out.println(time.toString() + " sending '" + PMix35Gateway.composeMessage(message) + "' to X10 controller");
            message= PMix35Gateway.composeMessage(message);
            readed = usb.send(message);
            //if (!readed.equals("$<9000!4A#")){ //PMIX35 ACK
                System.err.println("X10 reply to command: " + readed);
            //}
        } catch (IOException iOException) {
            System.err.println("Device X10 Controller not connected. Last message not sent");
            throw new UnableToExecuteException();
            //alternatively you can use the c.setExecuted(false); method
        }
    }
}

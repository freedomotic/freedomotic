package it.cicolella.openwebnet;

import it.freedomotic.api.Actuator;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.AddonManager;
import it.freedomotic.reactions.Command;
import java.io.*;

/**
 * A plugin for OpenWebNet protocol
 * author Mauro Cicolella - www.emmecilab.net
 *
 */
public class OpenWebNetActuator extends Actuator {

    private String address = null;
    private String host = null;
    private Integer port = 0;
    static BTicinoSocketWriteManager gestSocketCommands;
    static Integer passwordOpen = 0;

    /* OWN Diagnostic Frames */
    final static String LIGHTNING_DIAGNOSTIC_FRAME = "*#1*0##";
    final static String AUTOMATIONS_DIAGNOSTIC_FRAME = "*#2*0##";
    final static String ALARM_DIAGNOSTIC_FRAME = "*#5##";
    final static String POWER_MANAGEMENT_DIAGNOSTIC_FRAME = "*#3##";
    
    /* OWN Control Messages */
    final static String MSG_OPEN_ACK = "*#*1##";
    final static String MSG_OPEN_NACK = "*#*0##";

    
    public OpenWebNetActuator() {
        super("OpenWebNet Actuator", "/it.cicolella.own/own-actuator.xml");
        // get connection parameters for ethernet gateway
        host = configuration.getProperty("host");
        port = Integer.parseInt(configuration.getProperty("port"));
    }

    @Override
    public void onStart() {
        super.onStart();
        for (Plugin p : AddonManager.getLoadedPlugins()) {
            if (p.getName().equalsIgnoreCase("OpenWebNet Sensor")) {
                p.start();
                if (p.isRunning()) {
                    //initSystem();
                } else {
                    this.stop(); //stop OWN actuator
                    this.setDescription("Stopped for problem with Sensor loading");
                }
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            super.stop();
        }
    }

    @Override
    public void onCommand(Command c) throws IOException, UnableToExecuteException {
        sendFrame(OWNUtilities.createFrame(c));
        } // close  on command

      
   public void sendFrame(String frame) throws IOException, UnableToExecuteException {
        gestSocketCommands = new BTicinoSocketWriteManager();
        if (gestSocketCommands.getSocketCommandState() == 0) { // not connected
            if (gestSocketCommands.connect(host, port, passwordOpen)) {
                BTicinoWriteThread writer = null;
                writer = new BTicinoWriteThread(frame);
                writer.start();
                int returnCommandValue = writer.returnValue();
                if (returnCommandValue != 0) {
                    throw new UnableToExecuteException(); // command not executed - object status not changed
                }
            }
        } else if (gestSocketCommands.getSocketCommandState() == 3) { // already connected
            BTicinoWriteThread writer = null;
            writer = new BTicinoWriteThread(frame);
            writer.start();
            int returnCommandValue = writer.returnValue();
            if (returnCommandValue != 0) {
                throw new UnableToExecuteException();
            }
        } //close lenght
    } // close  on command

   
    // sends diagnostic frames to inizialize system
    public void initSystem() {
        try {
            Freedomotic.logger.info("Sending " + LIGHTNING_DIAGNOSTIC_FRAME + " frame to inizialize LIGHTNING");
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Sending " + LIGHTNING_DIAGNOSTIC_FRAME + " (inizialize LIGHTNING)");
            sendFrame(LIGHTNING_DIAGNOSTIC_FRAME);
            Freedomotic.logger.info("Sending " + AUTOMATIONS_DIAGNOSTIC_FRAME + " frame to inizialize AUTOMATIONS");
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Sending " + AUTOMATIONS_DIAGNOSTIC_FRAME + " (inizialize AUTOMATIONS)");
            sendFrame(AUTOMATIONS_DIAGNOSTIC_FRAME);
            Freedomotic.logger.info("Sending " + ALARM_DIAGNOSTIC_FRAME + " frame to inizialize ALARM");
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Sending " + ALARM_DIAGNOSTIC_FRAME + " (inizialize ALARM)");
            sendFrame(ALARM_DIAGNOSTIC_FRAME);
            Freedomotic.logger.info("Sending " + POWER_MANAGEMENT_DIAGNOSTIC_FRAME + " frame to inizialize POWER MANAGEMENT");
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Sending " + POWER_MANAGEMENT_DIAGNOSTIC_FRAME + " (inizialize POWER MANAGEMENT)");
            sendFrame(POWER_MANAGEMENT_DIAGNOSTIC_FRAME);
        } catch (UnableToExecuteException e) {
        } catch (IOException e) {  }
        ;
    }
    
    
    public boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

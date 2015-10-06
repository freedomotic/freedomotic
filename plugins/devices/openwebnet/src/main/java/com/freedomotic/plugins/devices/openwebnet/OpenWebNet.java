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
* along with Freedomotic; see the file COPYING. If not, see
* <http://www.gnu.org/licenses/>.
*/

package com.freedomotic.plugins.devices.openwebnet;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.myhome.fcrisciani.connector.MyHomeJavaConnector;
import com.myhome.fcrisciani.exception.MalformedCommandOPEN;


public class OpenWebNet extends Protocol {
    /*
     * Initializations
     */

    public static final Logger LOG = Logger.getLogger(OpenWebNet.class.getName());
    private String address = null;
    private String host = configuration.getProperty("host");
    private Integer port = Integer.parseInt(configuration.getProperty("port"));
    String _IP_VALUE = configuration.getProperty("host");
    String _PORT_OPEN_VALUE = configuration.getProperty("port");
    private final String _MODE_VALUE = configuration.getProperty("mode");
    static MyHomeJavaConnector myPlant = null;
    MonitorSessionThread monitorSessionThread = null;
    private String _regFilterString = "";
    static String frame;
    private static int POLLING_TIME = 1000;
    OWNFrame JFrame = new OWNFrame(this);

    /*
     *
     * OWN Diagnostic Frames
     *
     */
    final static String LIGHTNING_DIAGNOSTIC_FRAME = "*#1*0##";
    final static String AUTOMATIONS_DIAGNOSTIC_FRAME = "*#2*0##";
    final static String ALARM_DIAGNOSTIC_FRAME = "*#5##";
    final static String POWER_MANAGEMENT_DIAGNOSTIC_FRAME = "*#3##";

    /*
     *
     * OWN Control Messages
     *
     */
    final static String MSG_OPEN_ACK = "*#*1##";
    final static String MSG_OPEN_NACK = "*#*0##";

    public OpenWebNet() {
        super("OpenWebNet", "/openwebnet/openwebnet-manifest.xml");
    }

    protected void onShowGui() {
        bindGuiToPlugin(JFrame);
    }


    /*
     *
     * Sensor side
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        setPollingWait(POLLING_TIME);
        // create thread 
        monitorSessionThread = new MonitorSessionThread(this, _IP_VALUE, Integer.parseInt(_PORT_OPEN_VALUE));
        // start thread 
        monitorSessionThread.start();
        // syncronizes the software with the system status
        initSystem();
    }

    @Override
    protected void onRun() {
    }

    /*
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            myPlant.sendCommandAsync(OWNUtilities.createFrame(c), 1);
        } catch (MalformedCommandOPEN ex) {
            Logger.getLogger(OpenWebNet.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // close  on command

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onStop() {
        super.onStop();
        this.setDescription("Disconnected");
        setPollingWait(-1); // disable polling
    }

    // sends diagnostic frames to syncronize the software with the real system
    public void initSystem() {
        try {
            LOG.info("Sending " + LIGHTNING_DIAGNOSTIC_FRAME + " frame to inizialize LIGHTNING");
            OWNFrame.writeAreaLog(MonitorSessionThread.getDateTime() + " Act:" + "Sending " + LIGHTNING_DIAGNOSTIC_FRAME + " (inizialize LIGHTNING)");
            myPlant.sendCommandAsync(LIGHTNING_DIAGNOSTIC_FRAME, 1);
            LOG.info("Sending " + AUTOMATIONS_DIAGNOSTIC_FRAME + " frame to inizialize AUTOMATIONS");
            OWNFrame.writeAreaLog(MonitorSessionThread.getDateTime() + " Act:" + "Sending " + AUTOMATIONS_DIAGNOSTIC_FRAME + " (inizialize AUTOMATIONS)");
            myPlant.sendCommandAsync(AUTOMATIONS_DIAGNOSTIC_FRAME, 1);
            LOG.info("Sending " + ALARM_DIAGNOSTIC_FRAME + " frame to inizialize ALARM");
            OWNFrame.writeAreaLog(MonitorSessionThread.getDateTime() + " Act:" + "Sending " + ALARM_DIAGNOSTIC_FRAME + " (inizialize ALARM)");
            myPlant.sendCommandAsync(ALARM_DIAGNOSTIC_FRAME, 1);
            LOG.info("Sending " + POWER_MANAGEMENT_DIAGNOSTIC_FRAME + " frame to inizialize POWER MANAGEMENT");
            OWNFrame.writeAreaLog(MonitorSessionThread.getDateTime() + " Act:" + "Sending " + POWER_MANAGEMENT_DIAGNOSTIC_FRAME + " (inizialize POWER MANAGEMENT)");
            myPlant.sendCommandAsync(POWER_MANAGEMENT_DIAGNOSTIC_FRAME, 1);
        } catch (MalformedCommandOPEN ex) {
            Logger.getLogger(OpenWebNet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

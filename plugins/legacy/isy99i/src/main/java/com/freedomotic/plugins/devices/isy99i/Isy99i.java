/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.plugins.devices.isy99i;

import com.udi.insteon.client.InsteonConstants;
import com.udi.insteon.client.InsteonOps;
import com.universaldevices.client.NoDeviceException;
import com.universaldevices.device.model.UDNode;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.persistence.EnvObjectPersistence;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * A sensor for the gateway Isy99i developed by www.universal-devices.com author
 * Mauro Cicolella - www.emmecilab.net For more details please refer to
 */
public class Isy99i extends Protocol {

    public static final Logger LOG = Logger.getLogger(Isy99i.class.getName());
    public static AuxClass aux = null;
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private static String IP_ADDRESS = null;
    private static int PORT_NUMBER = 80;
    private static String UUID = null;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    public String USERID = configuration.getProperty("userid");
    public String PASSWORD = configuration.getProperty("password");
    private String delimiter = configuration.getProperty("address-delimiter");
    private MyISYInsteonClient myISY = null;
    Isy99iFrame JFrame = new Isy99iFrame(this);

    /**
     * Initializations
     */
    public Isy99i() {
        super("Isy99i", "/isy99i/isy99i.xml");
        setPollingWait(POLLING_TIME);
    }

    protected void onShowGui() {
        bindGuiToPlugin(JFrame);
    }

    protected UDNode getNode(String address) {
        if (address == null) {
            LOG.severe("Missing Device/Scene address");
            Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() + ": Missing Device/Scene address");
            return null;
        }
        try {
            UDNode node = myISY.getNodes().get(address);
            if (node == null) {
                LOG.severe("Address points to a non existing Insteon Device");
                return null;
            }
            return node;
        } catch (NoDeviceException e) {
            LOG.severe("NoDeviceException " + e);
            Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() + ": NoDeviceException " + e);
            return null;
        }
    }

    //   }
    protected void getStatus(String address) {
        //String tmp = tk.nextToken(); //device address
        UDNode node = getNode(address);
        if (node == null) {
            return;
        }
        String status = (String) myISY.getISY().getCurrValue(node, InsteonConstants.DEVICE_STATUS);
        LOG.severe("The current status for " + node.address + "/" + node.name + " is " + status);
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();
        aux = new AuxClass(this);
        POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
        IP_ADDRESS = configuration.getProperty("ip-address");
        PORT_NUMBER = configuration.getIntProperty("port-number", 80);
        UUID = configuration.getProperty("uuid");
        setPollingWait(POLLING_TIME);
        myISY = new MyISYInsteonClient();
        try {
            myISY.getISY().start("uuid:" + UUID, "http://" + IP_ADDRESS + ":" + PORT_NUMBER);
        } catch (Exception e) {
            this.stop();
            setDescription(configuration.getStringProperty("description", "Unable to connect"));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        myISY.getISY().stop();
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Isy99i"));
    }

    @Override
    protected void onRun() {
    }

    public void SystemInit() {
        //   for (EnvObjectLogic object: EnvObjectPersistence.getObjectbyProtocol("Isy99i") ){
        //if (object.equalsIgnoreCase("Isy99i") {
        //sdk.subscribe(object.getAddress());
        // }
        //}
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        String address = c.getProperty("address");
        String control = c.getProperty("control");
        String action = c.getProperty("action");
        if (action == "") {
            action = null;
        }
        if (c.getProperty("address").equalsIgnoreCase("DIM")) {
            //converts percent dim value to Insteon format (hex)
            int value = InsteonOps.convertPercentToOnLevel(Integer.parseInt(action));
            action = String.valueOf(value);
        }

        //try {
        myISY.getISY().changeNodeState(control, action, address);
        LOG.severe("Sending changeNodeState(" + control + "," + action + "," + address + ")");

        /*
         * try { connected = connect(address[0], Integer.parseInt(address[1]));
         * } catch (ArrayIndexOutOfBoundsException outEx) {
         * Freedomotic.logger.severe("The object address '" +
         * c.getProperty("address") + "' is not properly formatted. Check it!");
         * throw new UnableToExecuteException(); } catch (NumberFormatException
         * numberFormatException) { Freedomotic.logger.severe(address[1] + " is
         * not a valid ethernet port to connect to"); throw new
         * UnableToExecuteException(); }
         *
         * if (connected) { String restURL = createRestURL(c); String
         * expectedReply = c.getProperty("expected-reply"); try { String reply =
         * sendToBoard(restURL); Freedomotic.logger.severe("Isy99i reply: " +
         * reply); if ((reply != null) && (!reply.equals(expectedReply))) {
         * //TODO: implement reply check } } catch (IOException iOException) {
         * setDescription("Unable to send the message to host " + address[0] + "
         * on port " + address[1]); Freedomotic.logger.severe("Unable to send
         * the message to host " + address[0] + " on port " + address[1]); throw
         * new UnableToExecuteException(); } finally { disconnect(); } } else {
         * throw new UnableToExecuteException(); }
         */
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

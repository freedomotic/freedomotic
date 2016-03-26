/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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

package com.freedomotic.plugins.devices.btspeechrecognition;

/*
 * EchoServer is a threaded RFCOMM service with the specified UUID and name.
 */
import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.bluetooth.*;

/**
 * @author Mauro Cicolella 
 *Adapted from original code by Andrew Davison
 * ad@fivedots.coe.psu.ac.th, February 2011
 */
public class BTServer
        extends Thread {

    BTSpeechRecognition pluginRef;
    // UUID and name of the echo service
    private static final String UUID_STRING = "11111111111111111111111111111111";
    // 32 hex digits which will become a 128 bit ID
    private static final String SERVICE_NAME = "speechRecognition";   // use lowercase
    private StreamConnectionNotifier server;
    private ArrayList<ThreadedBTClientHandler> handlers;
    private volatile boolean isRunning = false;

    /**
     *
     * @param pluginRef
     */
    public BTServer(BTSpeechRecognition pluginRef) {

        this.pluginRef = pluginRef;
        handlers = new ArrayList<ThreadedBTClientHandler>();

    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                closeDown();
            }
        });
        try {
            initDevice();
        } catch (BluetoothStateException ex) {
            // throws the exception to BTSpeechRecognition to raise a PluginStartupException
            throw new RuntimeException();
        }
        createRFCOMMConnection();
        processClients();
    }  // end of EchoServer()

    private void initDevice() throws BluetoothStateException {
        try {  // make the server's device discoverable
            LocalDevice local = LocalDevice.getLocalDevice();
            pluginRef.pluginLog().info("Found local device: {}", local.getFriendlyName() + " with Bluetooth Address: " + local.getBluetoothAddress());
            boolean res = local.setDiscoverable(DiscoveryAgent.GIAC);
            pluginRef.pluginLog().info("Discoverability set: '{}'", res);
        } catch (BluetoothStateException ex) {
            // throws the exception to caller thread run()
            throw ex;
        }
    }  // end of initDevice()

    private void createRFCOMMConnection() /*
     * Create a RFCOMM connection notifier for the server, with the given UUID
     * and name. This also creates a service record.
     */ {
        try {
            pluginRef.pluginLog().info("Start advertising ''{}'' service ...", SERVICE_NAME);
            server = (StreamConnectionNotifier) Connector.open(
                    "btspp://localhost:" + UUID_STRING
                    + ";name=" + SERVICE_NAME + ";authenticate=false");
            /*
             * for most devices, with authenticate=false there should be no need
             * for pin pairing
             */
        } catch (IOException e) {
            pluginRef.pluginLog().error("Error creating RFCOMM connection for ", e);
        }
    }  // end of createRFCOMMConnection()

    /*
     * Wait for client connections, creating a handler for each one
     *
     */
    private void processClients() {
        isRunning = true;
        try {
            while (isRunning) {
                pluginRef.pluginLog().info("Waiting for incoming connection...");
                StreamConnection conn = server.acceptAndOpen();
                /*
                 * wait for a client connection acceptAndOpen() also adds the
                 * service record to the device's SDDB, making the service
                 * visible to clients
                 */
                pluginRef.pluginLog().info("Connection requested...");
                ThreadedBTClientHandler hand = new ThreadedBTClientHandler(conn, pluginRef);
                // create client handler
                handlers.add(hand);
                hand.start();
            }
        } catch (IOException e) {
            pluginRef.pluginLog().error(" ", e);
        }
    }  // end of processClients()

    /*
     * Stop accepting any further client connections, and close down all the
     * handlers.
     */
    private void closeDown() {
        pluginRef.pluginLog().info("Closing down server");
        if (isRunning) {
            isRunning = false;
            try {
                // close connection, and remove service record from SDDB
                server.close();
            } catch (IOException e) {
                pluginRef.pluginLog().error(" ", e);
            }

            // close down all the handlers
            for (ThreadedBTClientHandler hand : handlers) {
                hand.closeDown();
            }
            handlers.clear();
        }
    }
}

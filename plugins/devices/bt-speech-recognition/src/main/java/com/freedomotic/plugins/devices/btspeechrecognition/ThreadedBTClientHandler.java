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
 * A threaded handler, called by EchoServer to deal with a client. When a
 * message comes in, it is sent back converted to uppercase. closeDown()
 * terminates the handler. ThreadedEchoHandler uses the same readData() and
 * sendMessage() methods as EchoClient.
 */
import java.io.*;
import javax.microedition.io.*;
import javax.bluetooth.*;

/**
 * @author Mauro Cicolella Adapted from original code by Andrew Davison
 * ad@fivedots.coe.psu.ac.th, February 2011
 */
public class ThreadedBTClientHandler extends Thread {

    private StreamConnection conn; // client connection
    private BTSpeechRecognition pluginRef;
    private InputStream in;
    private OutputStream out;
    private volatile boolean isRunning = false;
    private String clientName;

    /**
     *
     * @param conn
     * @param pluginRef
     */
    public ThreadedBTClientHandler(StreamConnection conn, BTSpeechRecognition pluginRef) {
        this.conn = conn;
        this.pluginRef = pluginRef;

        // store the name of the connected client
        clientName = reportDeviceName(conn);
        pluginRef.pluginLog().info("Handler spawned for BT client: ''{}''", clientName);
    } // end of ThreadedEchoHandler()

    private String reportDeviceName(StreamConnection conn) /*
     * Return the 'friendly' name of the device being examined, or "device ??"
     */ {
        String devName;
        try {
            RemoteDevice rd = RemoteDevice.getRemoteDevice(conn);
            devName = rd.getFriendlyName(false);  // to reduce connections
        } catch (IOException e) {
            devName = "device ??";
        }
        return devName;
    }  // end of reportDeviceName()

    public void run() /*
     * Get an InputStream and OutputStream from the stream connection, and start
     * processing client messages.
     */ {
        try {
            // Get I/O streams from the stream connection
            in = conn.openInputStream();
            out = conn.openOutputStream();
            processMsgs();
            pluginRef.pluginLog().info("Closing ''{}'' connection", clientName);
            if (conn != null) {
                in.close();
                out.close();
                conn.close();
            }
        } catch (IOException e) {
            pluginRef.pluginLog().error(" ", e);
        }
    }  // end of run()

    private void processMsgs() {
        isRunning = true;
        String line;
        while (isRunning) {
            if ((line = readData()) == null) {
                isRunning = false;
            } else {  // there was some input
                pluginRef.pluginLog().info("From BT client " + clientName + " --> \"" + line + "\"");
                pluginRef.sendCommand(line);
                if (line.trim().equals("bye$$")) {
                    isRunning = false;
                }
            }
        }
    }  // end of processMsgs()

    /**
     *
     */
    public void closeDown() {
        isRunning = false;
    }

    // --------------- IO methods ---------------------------
    // Same as the methods in EchoClient
    private String readData() /*
     * Read a message in the form "<length> msg". The length allows us to know
     * exactly how many bytes to read to get the complete message. Only the
     * message part (msg) is returned, or null if there's been a problem.
     */ {
        String receivedMessage = null;
        byte[] data = null;
        try {
            int len = in.read();    // get the message length
            if (len <= 0) {
                pluginRef.pluginLog().error("{} : Message Length Error", clientName);
                return null;
            }

            final byte[] buffer = new byte[1024];
            final int readBytes = in.read(buffer);
            if (readBytes > 0) {
                receivedMessage = new String(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            pluginRef.pluginLog().error(" readData(): {}", e);
            return null;
        }
        return receivedMessage;
    }   // end of readData()

    //TO BE REMOVED
    private boolean sendMessage(String msg) // the message format is "<length> msg" in byte form
    {
        try {
            out.write(msg.length());
            out.write(msg.getBytes());
            out.flush();
            return true;
        } catch (Exception e) {
            pluginRef.pluginLog().error("{} sendMessage(): {}", clientName, e);
            return false;
        }
    }  // end of sendMessage()
}

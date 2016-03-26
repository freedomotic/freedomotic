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
package com.freedomotic.plugins.devices.arduinoremotecontroller;

import java.io.IOException;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class UDPServer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(UDPServer.class.getName());
    private final static int BUFFER = 1024;
    private DatagramSocket socket;
    ArduinoRemoteController plugin; // reference to the plugin class

    /**
     *
     * @param plugin
     * @throws IOException
     */
    public UDPServer(ArduinoRemoteController plugin) throws IOException {
        this.plugin = plugin;
        socket = new DatagramSocket(plugin.UDP_SERVER_PORT, InetAddress.getByName(plugin.UDP_SERVER_HOSTNAME));
        LOG.info("ServerUDP listen to {}", socket.getLocalSocketAddress());
    }

    public void run() {
        byte[] buf = new byte[BUFFER];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                //remove this packets
                if (!packet.getAddress().toString().contains("0.0.0.0")) {
                    extractData(packet);
                }
            } catch (Exception ex) {
                LOG.error("Reading UDP packet exception {}", ex);
            }
        }
    }

    // this method extracts data from udp packets
    private void extractData(DatagramPacket packet) {
        String content = null;
        String fields[] = null;
        String clientAddress = null;
        String sensorConnector = null;
        String sensorType = null;
        String sensorValue = null;

        content = new String(packet.getData());
        content = content.substring(0, packet.getLength());
        fields = content.split(plugin.DELIMITER);
        sensorConnector = fields[0];
        sensorType = fields[1];
        sensorValue = fields[2];
        clientAddress = packet.getAddress().toString().substring(1, packet.getAddress().toString().length());
        plugin.sendEvent(clientAddress + ":" + sensorConnector, sensorType, sensorValue);
    }
}

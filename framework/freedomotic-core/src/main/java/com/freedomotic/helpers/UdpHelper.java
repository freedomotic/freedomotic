/*
 /**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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
package com.freedomotic.helpers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements an UDP server listening for incoming packets and provides a method to send UDP
 * packets to a specified server
 * 
 * @author Mauro Cicolella
 */
public class UdpHelper {

    private static final Logger LOG = Logger.getLogger(UdpHelper.class.getName());
    private static UDPThreadServer server;
    private static DatagramSocket serverDatagramSocket = null;

    public UdpHelper() {
    }

    /**
     * Starts an UDP server
     *
     * @param serverAddress Server UDP address
     * @param serverPort Server UDP port number
     * @param consumer UdpListener for incoming packets
     * @return
     * @throws IOException
     */
    public void startServer(String serverAddress, int serverPort, final UdpListener consumer) {
        server = new UDPThreadServer(serverAddress, serverPort, consumer);
        server.start();
    }

    
    /**
     * Sends an UDP packet to the server
     *
     * @param serverAddress Server UDP address
     * @param serverPort Server UDP port number
     * @param message Message to send
     * @return
     * @throws UnknownHostException
     * @throws SocketException
     * @throws IOException
     */
    public void send(String serverAddress, int serverPort, String payload) {
        DatagramSocket datagramSocket = null;
        int BUFFER_SIZE = 1024;
        byte[] buffer;

        try {
            InetAddress inetAddress = InetAddress.getByName(serverAddress);
            int port = serverPort;
            datagramSocket = new DatagramSocket();

            DatagramPacket out_datagramPacket = new DatagramPacket(
                    payload.getBytes(),
                    payload.length(),
                    inetAddress,
                    port);

            datagramSocket.send(out_datagramPacket);

        } catch (UnknownHostException ex) {
            LOG.log(Level.WARNING, "Unknown UDP server. Packet not sent", ex.getMessage());
        } catch (SocketException ex) {
            LOG.log(Level.WARNING, "Socket exception. Packet not sent", ex.getMessage());
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "IOException. Packet not sent", ex.getMessage());
        } finally {
            datagramSocket.close();
        }
    }

    /*
     *  Nested threaded class for UDP server 
     *  Starts an UDP server in a separate thread
     */
    private static class UDPThreadServer extends Thread {

        String serverAddress;
        int serverPort;
        UdpListener consumer;

        UDPThreadServer(String serverAddress, int serverPort, final UdpListener consumer) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.consumer = consumer;
            LOG.log(Level.CONFIG, "Starting threaded server on " + serverAddress + ":" + serverPort);
        }

        public void run() {
            try {
                serverDatagramSocket = new DatagramSocket(null);
                InetSocketAddress address = new InetSocketAddress(serverAddress, serverPort);
                serverDatagramSocket.bind(address);
                //buffer to receive incoming data
                byte[] buffer = new byte[65536];
                DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
                LOG.log(Level.INFO, "Server UDP started. Waiting for incoming data on " + serverAddress + ":" + serverPort);
                //communication loop
                while (true) {
                    serverDatagramSocket.receive(inPacket);
                    byte[] payload = inPacket.getData();
                    String sourceAddress = inPacket.getAddress().getHostAddress();
                    Integer sourcePort = inPacket.getPort();
                    String data = new String(payload, 0, inPacket.getLength());
                    LOG.log(Level.CONFIG, "Received packet from " + sourceAddress + ":" + sourcePort);
                    consumer.onDataAvailable(sourceAddress, sourcePort, data);
                }
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Server UDP not started for ", e);
            }
        }
    }
}

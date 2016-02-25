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
package com.freedomotic.helpers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements an UDP server listening for incoming packets and
 * provides a method to send UDP packets to a specified server
 *
 * @author Mauro Cicolella
 */
public class UdpHelper {

    private static final Logger LOG = LoggerFactory.getLogger(UdpHelper.class.getName());
    private static UDPThreadServer server;
    private static DatagramSocket serverDatagramSocket;

    /**
     * Starts an UDP server listening on the given address and port. It allows
     * to bind a consumer which will be notified of any message received by the
     * UDP server.
     *
     * @param serverAddress Server UDP address
     * @param serverPort Server UDP port number
     * @param consumer UdpListener for incoming packets
     */
    public void startServer(String serverAddress, int serverPort, final UdpListener consumer) {
        server = new UDPThreadServer(serverAddress, serverPort, consumer);
        server.start();
    }

    /**
     * Stops the UDP server closing the socket.
     */
    public void stopServer() {
        if (serverDatagramSocket.isBound()) {
            serverDatagramSocket.close();
            LOG.info("UDP server listening on {}:{} stopped.", new Object[]{serverDatagramSocket.getInetAddress(), serverDatagramSocket.getPort()});
        }
    }

    /**
     * Sends an UDP packet to the server
     *
     * @param serverAddress UDP server address
     * @param serverPort UDP server port number
     * @param payload data to send
     * @throws java.io.IOException
     */
    public void send(String serverAddress, int serverPort, String payload) throws IOException {
        DatagramSocket datagramSocket = null;

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
            LOG.debug("Sending UDP packet to {}:{}", new Object[]{serverAddress, serverPort});
        } catch (UnknownHostException ex) {
            throw new IOException("Unknown UDP server. Packet not sent", ex);
        } catch (SocketException ex) {
            throw new IOException("Socket exception. Packet not sent", ex);
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
    }

    /**
     * UDP server thread
     */
    private static class UDPThreadServer extends Thread {

        String serverAddress;
        int serverPort;
        UdpListener consumer;

        UDPThreadServer(String serverAddress, int serverPort, final UdpListener consumer) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.consumer = consumer;
            LOG.debug("UDP server starting on {}:{}", new Object[]{serverAddress, serverPort});
        }

        @Override
        public void run() {
            try {
                serverDatagramSocket = new DatagramSocket(null);
                serverDatagramSocket.setReuseAddress(true);
                InetSocketAddress address = new InetSocketAddress(serverAddress, serverPort);
                serverDatagramSocket.bind(address);
                //buffer to receive incoming data
                byte[] buffer = new byte[65536];
                DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
                LOG.info("UDP server started. Waiting for incoming data on {}:{}", new Object[]{serverAddress, serverPort});
                //communication loop
                while (true) {
                    serverDatagramSocket.receive(inPacket);
                    byte[] payload = inPacket.getData();
                    String sourceAddress = inPacket.getAddress().getHostAddress();
                    Integer sourcePort = inPacket.getPort();
                    String data = new String(payload, 0, inPacket.getLength());
                    LOG.debug("UDP server receives packet from {}:{}", new Object[]{sourceAddress, sourcePort});
                    consumer.onDataAvailable(sourceAddress, sourcePort, data);
                }
            } catch (IOException e) {
                LOG.error("UDP server not started for ", e);
            }
        }
    }
}

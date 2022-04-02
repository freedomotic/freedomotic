/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This class offers a timeout feature on socket connections. A maximum length
 * of time allowed for a connection can be specified, along with a host and
 * port.
 * 
* @author David Reilly
 */
public class TCPHelper {
    /**
     * Polling delay for socket checks (in milliseconds)
     */
    private static final int POLL_DELAY = 100;

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(TCPHelper.class.getName());

    private TCPHelper() {}

    /**
     * Attempts to connect to a service at the specified address and port, for a
     * specified maximum amount of time.
     *
     * @param host Hostname of machine
     * @param port Port of service
     * @param delay Delay in milliseconds
     * @return new {@link Socket} from <code>host</code> and <code>port</code>
     * @throws java.io.InterruptedIOException
     * if connection is not established before <code>delay</code> seconds have elapsed.
     */
    public static Socket getSocket(String host, int port, int delay) throws IOException {
        // Convert host into an InetAddress, and call getSocket method
        InetAddress inetAddr = InetAddress.getByName(host);
        return getSocket(inetAddr, port, delay);
    }

    /**
     * Attempts to connect to a service at the specified address and port, for a
     * specified maximum amount of time.
     *
     * @param addr Address of host
     * @param port Port of service
     * @param delay Delay in milliseconds
     * @return new {@link Socket} from <code>addr</code> and <code>port</code>
     * @throws java.io.InterruptedIOException
     * if connection is not established before <code>delay</code> seconds have elapsed.

     */
    public static Socket getSocket(InetAddress addr, int port, int delay) throws IOException {
        // Create a new socket thread, and start it running
        SocketThread st = new SocketThread(addr, port);
        st.start();
        int timer = 0;
        Socket sock;
        for (;;) {
            // Check to see if a connection is established
            if (st.isConnected()) {
                // Yes ... assign to sock variable, and break out of loop
                sock = st.getSocket();
                break;
            } else {
                // Check to see if an error occurred
                if (st.isError()) {
                    // No connection could be established
                    throw (st.getException());
                }
                try {
                    // Sleep for a short period of time
                    Thread.sleep(POLL_DELAY);
                } catch (InterruptedException ie) {
                    LOG.warn("Interrupted exception: ", ie);
                    Thread.currentThread().interrupt();
                }
                // Increment timer
                timer += POLL_DELAY;
                // Check to see if time limit exceeded
                if (timer > delay) {
                    // Can't connect to server
                    throw new InterruptedIOException("Could not connect for " + delay + " milliseconds");
                }
            }
        }
        return sock;
    }

    /**
     * Inner class for establishing a socket thread
     * within another thread to prevent blocking.
     */
    private static class SocketThread extends Thread {
        /**
         * Socket connection to remote host
         */
        private volatile Socket mConnection = null;

        /**
         * Hostname to connect to
         */
        private String mHost = null;

        /**
         * Internet Address to connect to
         */
        private InetAddress mInet = null;

        /**
         * Port number to connect to
         */
        private int mPort = 0;

        /**
         * Exception in the event a connection error occurs
         */
        private IOException mException = null;

        /**
         * Connects to the specified host and port number
         * @param host specified host name
         * @param port specified port number
         */
        SocketThread(String host, int port) {
            // Assign to member variables
            mHost = host;
            mPort = port;
        }

        /**
         * Connects to the specified host IP and port number
         * @param inetAddr specified host IP address
         * @param port specified port number
         */
        SocketThread(InetAddress inetAddr, int port) {
            // Assign to member variables
            mInet = inetAddr;
            mPort = port;
        }

        @Override
        public void run() {
            // Socket used for establishing a connection
            Socket sock;
            try {
                // Was a string or an inet specified
                if (mHost != null) {
                    // Connect to a remote host - BLOCKING I/O
                    sock = new Socket(mHost, mPort);
                } else {
                    // Connect to a remote host - BLOCKING I/O
                    sock = new Socket(mInet, mPort);
                }
            } catch (IOException ioe) {
                // Assign to our exception member variable
                mException = ioe;
                return;
            }
            // If socket constructor returned without error,
            // then connection finished
            mConnection = sock;
        }

        public boolean isConnected() {
            return mConnection != null;
        }

        public boolean isError() {
            return mException != null;
        }

        public Socket getSocket() {
            return mConnection;
        }

        public IOException getException() {
            return mException;
        }
    }
}

package it.cicolella.arduinows;

import java.net.*;
import java.io.*;

//
// TimedSocket.java
//
// Written by David Reilly, for JavaWorld
//
/**
 * This class offers a timeout feature on socket connections. A maximum length
 * of time allowed for a connection can be specified, along with a host and
 * port.
 *
 * @author	David Reilly
 */
public class TimedSocket {

    /**
     * Attempts to connect to a service at the specified address and port, for a
     * specified maximum amount of time.
     *
     * @param	addr	Address of host
     * @param	port	Port of service
     * @param	delay	Delay in milliseconds
     */
    public static Socket getSocket(InetAddress addr, int port, int delay) throws InterruptedIOException, IOException {
        // Create a new socket thread, and start it running
        SocketThread st = new SocketThread(addr, port);
        st.start();

        int timer = 0;
        Socket sock = null;

        for (;;) {
            // Check to see if a connection is established
            if (st.isConnected()) {
                // Yes ...  assign to sock variable, and break out of loop
                sock = st.getSocket();
                break;
            } else {
                // Check to see if an error occurred
                if (st.isError()) {
                    System.out.println("st is error");
                    // No connection could be established
                    throw (st.getException());
                }

                try {
                    System.out.println("Thread is sleep");
                    // Sleep for a short period of time
                    Thread.sleep(POLL_DELAY);
                } catch (InterruptedException ie) {
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
     * Attempts to connect to a service at the specified address and port, for a
     * specified maximum amount of time.
     *
     * @param	host	Hostname of machine
     * @param	port	Port of service
     * @param	delay	Delay in milliseconds
     */
    public static Socket getSocket(String host, int port, int delay) throws InterruptedIOException, IOException {
        // Convert host into an InetAddress, and call getSocket method
        InetAddress inetAddr = InetAddress.getByName(host);

        return getSocket(inetAddr, port, delay);
    }

    /*public static void main(String args[]) throws Exception
     {
     try
     {
     //InetAddress addr = InetAddress.getByName("192.168.0.3");
     //Socket s = TimedSocket.getSocket (addr, 80, 5000);
     Socket s = TimedSocket.getSocket ("192.168.0.3", 80, 5000);
     s.close();
     System.out.println ("connected");
     }
     catch (IOException ioe)
     {
     System.out.println ("time out");
     }

     }*/
    // Inner class for establishing a socket thread
    // within another thread, to prevent blocking.
    static class SocketThread extends Thread {
        // Socket connection to remote host

        volatile private Socket m_connection = null;
        // Hostname to connect to
        private String m_host = null;
        // Internet Address to connect to
        private InetAddress m_inet = null;
        // Port number to connect to
        private int m_port = 0;
        // Exception in the event a connection error occurs
        private IOException m_exception = null;

        // Connect to the specified host and port number
        public SocketThread(String host, int port) {
            // Assign to member variables
            m_host = host;
            m_port = port;
        }

        // Connect to the specified host IP and port number
        public SocketThread(InetAddress inetAddr, int port) {
            System.out.println("SocketThread is created : InetAddress-" + inetAddr + ", port-" + port);
            // Assign to member variables
            m_inet = inetAddr;
            m_port = port;
        }

        public void run() {
            // Socket used for establishing a connection
            Socket sock = null;
            System.out.println("Start RUN...");

            try {
                // Was a string or an inet specified
                if (m_host != null) {
                    System.out.println("m_host is not null...");
                    // Connect to a remote host - BLOCKING I/O
                    sock = new Socket(m_host, m_port);
                    System.out.println("new Socket...");
                } else {
                    System.out.println("m_host is null...");
                    // Connect to a remote host - BLOCKING I/O
                    sock = new Socket(m_inet, m_port);
                    System.out.println("new Socket...");
                }
            } catch (IOException ioe) {
                // Assign to our exception member variable
                m_exception = ioe;
                return;
            }

            // If socket constructor returned without error,
            // then connection finished
            m_connection = sock;
        }

        // Are we connected?
        public boolean isConnected() {
            System.out.println("isConnected ?" + m_connection);
            if (m_connection == null) {
                return false;
            } else {
                return true;
            }
        }

        // Did an error occur?
        public boolean isError() {
            if (m_exception == null) {
                return false;
            } else {
                return true;
            }
        }

        // Get socket
        public Socket getSocket() {
            return m_connection;
        }

        // Get exception
        public IOException getException() {
            return m_exception;
        }
    }
    // Polling delay for socket checks (in milliseconds)
    private static final int POLL_DELAY = 100;
}

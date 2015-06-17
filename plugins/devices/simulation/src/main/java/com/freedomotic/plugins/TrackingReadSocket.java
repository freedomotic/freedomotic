/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class TrackingReadSocket extends Protocol {

    private static final Logger LOG = Logger.getLogger(TrackingReadSocket.class.getName());
    private static final int PORT = 7777;
    private ServerSocket serverSocket;
    private static final int maxConnections = -1;
    private OutputStream out;
    private final boolean connected = false;
    private final int SLEEP_TIME = 1000;
    private final int NUM_MOTE = 3;

    /**
     *
     */
    public TrackingReadSocket() {
        super("Tracking Simulator (Read Socket)", "/simulation/tracking-simulator-read-socket.xml");
        setDescription("It simulates a motes WSN that send information about movable sensors position, read from a socket (port:"
                + PORT + ")");
        setPollingWait(-1);
    }

    @Override
    public void onStart() {
        createServerSocket();
    }

    private void createServerSocket() {

        try {
            serverSocket = new ServerSocket(PORT);
            LOG.log(Level.INFO, "Start listening on server socket " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "IOException on socket listen: " + ioe);
            //ioe.printStackTrace();
        }
    }

    private void parseInput(String in) {
        int id = 0;
        int x = -1;
        int y = -1;
        StringTokenizer tokenizer = null;

        try {
            tokenizer = new StringTokenizer(in);
            id = new Integer(tokenizer.nextToken()).intValue();
            x = new Integer(tokenizer.nextToken()).intValue();
            y = new Integer(tokenizer.nextToken()).intValue();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error while parsing client input." + "\n" + in + "\ntoken count: "
                    + tokenizer.countTokens());
        }

//          MUST BE REIMPLEMENTED
//        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
//            if (object instanceof com.freedomotic.things.impl.Person){
//                Person person = (Person)object;
//                Point position = inventPosition();
//                person.getPojo().setCurrentRepresentation(0);
//                person.getPojo().getCurrentRepresentation().setOffset((int)position.getX(), (int)position.getY());
//                person.setChanged(true);
//            }
//        }
    }

    @Override
    protected void onRun() {
        int i = 0;

        while (((i++ < maxConnections) || (maxConnections == -1))) {
            try {
                ClientInputReader clientConnection;
                Socket clientSocket = serverSocket.accept();
                clientConnection = new ClientInputReader(clientSocket);

                Thread t = new Thread(clientConnection);
                t.start();
            } catch (IOException ioe) {
                LOG.log(Level.SEVERE, "IOException on socket listen: " + ioe);
            }
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        //do nothing
    }

    @Override
    protected boolean canExecute(Command c) {
        //do nothing
        return true;
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //do nothing
    }

    private class ClientInputReader
            implements Runnable {

        private Socket client;
        private String line;
        private String input;

        ClientInputReader(Socket client) {
            this.client = client;
            LOG.log(Level.INFO, "New client connected to server on " + client.getInetAddress());
        }

        public void run() {
            try {
                // Get input from the client
                DataInputStream in = new DataInputStream(client.getInputStream());
                PrintStream out = new PrintStream(client.getOutputStream());

                while (((line = in.readLine()) != null) && !line.equals(".") && isRunning()) {
                    LOG.log(Level.INFO, "Readed from socket: " + line);
                    parseInput(line);
                }

                LOG.log(Level.INFO, "Closing socket connection " + client.getInetAddress());
                client.close();
            } catch (IOException ioe) {
                LOG.log(Level.SEVERE, "IOException on socket listen: " + ioe);
                ioe.printStackTrace();
            }
        }
    }
}

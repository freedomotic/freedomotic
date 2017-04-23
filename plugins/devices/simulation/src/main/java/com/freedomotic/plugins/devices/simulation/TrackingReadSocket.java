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
package com.freedomotic.plugins.devices.simulation;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.LocationEvent;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.reactions.Command;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.GenericPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 *
 * @author Enrico Nicoletti
 */
public class TrackingReadSocket extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(TrackingReadSocket.class);
    private ServerSocket serverSocket;
    private final int SOCKET_SERVER_PORT = configuration.getIntProperty("socket-server-port", 7777);
    private final int MAX_CONNECTIONS = configuration.getIntProperty("max-connections", -1);
    private final String STOP_CONNECTION_CHAR = configuration.getStringProperty("stop-connection-char", ".");

    /**
     *
     */
    public TrackingReadSocket() {
        super("Tracking Simulator (Read Socket)", "/simulation/tracking-simulator-read-socket.xml");
        setDescription("Simulates tracking system. Positions read from a socket (port:"
                + SOCKET_SERVER_PORT + ")");
        setPollingWait(-1);
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            createServerSocket();
        } catch (IOException ioe) {
            throw new PluginStartupException("IOException on socket listening: \"{}\"", ioe);
        }
    }

    /**
     * Creates a server socket.
     *
     * @throws IOException
     */
    private void createServerSocket() throws IOException {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(SOCKET_SERVER_PORT));
            LOG.info("Start listening to server socket \"{}:{}\"", serverSocket.getInetAddress(), serverSocket.getLocalPort());
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    /**
     * Parses the string input.
     *
     * @param in input string to parse
     */
    private void parseInput(String in) {
        int id = 0;
        int x = -1;
        int y = -1;
        FreedomPoint location;
        StringTokenizer tokenizer = null;

        try {
            tokenizer = new StringTokenizer(in);
            id = Integer.parseInt(tokenizer.nextToken());
            x = Integer.parseInt(tokenizer.nextToken());
            y = Integer.parseInt(tokenizer.nextToken());
            location = new FreedomPoint(x, y);
            movePerson(id, location);
        } catch (Exception ex) {
            LOG.error("Error while parsing client input. \n {} \ntoken count: {}", in, tokenizer.countTokens());
        }
    }

    /**
     * Moves a 'Person' thing to a specific location.
     *
     * @param userId user's id associated to its address field
     * @param location new location
     */
    private void movePerson(int userId, FreedomPoint location) {
        for (EnvObjectLogic object : getApi().things().findAll()) {
            if ((object instanceof GenericPerson) && (object.getPojo().getPhisicalAddress().equalsIgnoreCase(String.valueOf(userId)))) {
                GenericPerson person = (GenericPerson) object;
                LocationEvent event = new LocationEvent(this, person.getPojo().getUUID(), location);
                notifyEvent(event);
            }
        }
    }

    @Override
    protected void onRun() {
        int i = 0;

        while ((i++ < MAX_CONNECTIONS) || (MAX_CONNECTIONS == -1)) {
            try {
                ClientInputReader clientConnection;
                Socket clientSocket = serverSocket.accept();
                clientConnection = new ClientInputReader(clientSocket);
                Thread t = new Thread(clientConnection);
                t.start();
            } catch (IOException ioe) {
                LOG.error("IOException on socket listen: \"{}\"", ioe);
            }
        }
    }

    @Override
    public void onStop() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            LOG.error("IOException on socket closing: \"{}\"", ex);
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

    /**
     * This class reads data input from clients until "stop-connection-char" is
     * received.
     *
     */
    private class ClientInputReader
            implements Runnable {

        private Socket client;

        ClientInputReader(Socket client) {
            this.client = client;
            LOG.info("New client connected to server on \"{}\"", client.getInetAddress());
        }

        public void run() {
            try {
                String line;
                // Get input from the client
                DataInputStream in = new DataInputStream(client.getInputStream());

                while (((line = in.readLine()) != null) && !line.equals(STOP_CONNECTION_CHAR) && isRunning()) {
                    LOG.info("Readed from socket: \"{}\"", line);
                    parseInput(line);
                }

                LOG.info("Closing socket connection \"{}\"", client.getInetAddress());
                client.close();
            } catch (IOException ioe) {
                LOG.error("IOException on socket listening: \"{}\"", ioe);
            }
        }
    }
}

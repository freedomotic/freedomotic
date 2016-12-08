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
package com.freedomotic.plugins;

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
import java.io.OutputStream;
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
    private OutputStream out;
    private final boolean connected = false;
    private final int PORT = configuration.getIntProperty("socket-server-port", 7777);
    private final int SLEEP_TIME = configuration.getIntProperty("sleep-time", 1000);
    private final int MAX_CONNECTIONS = configuration.getIntProperty("max-connections", -1);

    /**
     *
     */
    public TrackingReadSocket() {
        super("Tracking Simulator (Read Socket)", "/simulation/tracking-simulator-read-socket.xml");
        setDescription("Simulates tracking system. Positions read from a socket (port:"
                + PORT + ")");
        setPollingWait(-1);
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            createServerSocket();
        } catch (IOException ioe) {
            throw new PluginStartupException("IOException on socket listen: {}", ioe);
        }
    }

    private void createServerSocket() throws IOException {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(PORT));
            LOG.info("Start listening on server socket {}:{}", serverSocket.getInetAddress(), serverSocket.getLocalPort());
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    private void parseInput(String in) {
        int id = 0;
        int x = -1;
        int y = -1;
        FreedomPoint location;
        StringTokenizer tokenizer = null;

        try {
            tokenizer = new StringTokenizer(in);
            id = new Integer(tokenizer.nextToken()).intValue();
            x = new Integer(tokenizer.nextToken()).intValue();
            y = new Integer(tokenizer.nextToken()).intValue();
            location = new FreedomPoint(x, y);
            movePerson(id, location);

        } catch (Exception ex) {
            LOG.error("Error while parsing client input. \n {} \ntoken count: {}", in, tokenizer.countTokens());
        }
    }

    // user ID must be associated to object address field
    private void movePerson(int ID, FreedomPoint location) {
        for (EnvObjectLogic object : getApi().things().findAll()) {
            if ((object instanceof GenericPerson) && (object.getPojo().getPhisicalAddress().equalsIgnoreCase(String.valueOf(ID)))) {
                GenericPerson person = (GenericPerson) object;
                LocationEvent event = new LocationEvent(this, person.getPojo().getUUID(), location);
                notifyEvent(event);
            }
        }
    }

    @Override
    protected void onRun() {
        int i = 0;

        while (((i++ < MAX_CONNECTIONS) || (MAX_CONNECTIONS == -1))) {
            try {
                ClientInputReader clientConnection;
                Socket clientSocket = serverSocket.accept();
                clientConnection = new ClientInputReader(clientSocket);

                Thread t = new Thread(clientConnection);
                t.start();
            } catch (IOException ioe) {
                LOG.error("IOException on socket listen: {}", ioe);
            }
        }
    }

    @Override
    public void onStop() {
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
            LOG.info("New client connected to server on {}", client.getInetAddress());
        }

        public void run() {
            try {
                // Get input from the client
                DataInputStream in = new DataInputStream(client.getInputStream());
                PrintStream out = new PrintStream(client.getOutputStream());

                while (((line = in.readLine()) != null) && !line.equals(".") && isRunning()) {
                    LOG.info("Readed from socket: {}", line);
                    parseInput(line);
                }

                LOG.info("Closing socket connection {}", client.getInetAddress());
                client.close();
            } catch (IOException ioe) {
                LOG.error("IOException on socket listen: {}", ioe);
                ioe.printStackTrace();
            }
        }
    }
}

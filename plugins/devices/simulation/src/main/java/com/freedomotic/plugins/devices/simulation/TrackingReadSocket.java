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
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.events.LocationEvent;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.reactions.Command;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.GenericPerson;
import java.io.BufferedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Enrico Nicoletti
 */
public class TrackingReadSocket extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(TrackingReadSocket.class);
    private ServerSocket serverSocket;
    private final int SOCKET_SERVER_PORT = configuration.getIntProperty("socket-server-port", 7777);
    private final int MAX_CONNECTIONS = configuration.getIntProperty("max-connections", -1);
    private final String FIELD_DELIMITER = configuration.getStringProperty("field-delimiter", ",");
    private final String STOP_CONNECTION_CHAR = configuration.getStringProperty("stop-connection-char", ".");
    private final String DATA_TYPE = configuration.getStringProperty("data-type", "coordinates");
    private AtomicInteger activeConnections;

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
            throw new PluginStartupException("IOException on server socket creating: \"{}\"", ioe);
        }
    }

    /**
     * Creates a server socket.
     *
     * @throws IOException
     */
    private void createServerSocket() throws IOException {
        try {
            activeConnections = new AtomicInteger();
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(SOCKET_SERVER_PORT));
            LOG.info("Start listening to server socket \"{}:{}\"", serverSocket.getInetAddress(), serverSocket.getLocalPort());
        } catch (IOException ioe) {
            LOG.error("IOException on server socket creating: \"{}\"", ioe);
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
                LOG.info("Moving Person thing with address \"{}\" to ({}) coordinates", userId, location.toString());
                return;
            }
        }
        LOG.error("No Person thing found with physical address \"{}\". Skipped movement", userId);
    }

    @Override
    protected void onRun() {

        while ((activeConnections.get() < MAX_CONNECTIONS) || (MAX_CONNECTIONS == -1)) {
            if (!serverSocket.isClosed()) {
                try {
                    ClientInputReader clientConnection;
                    Socket clientSocket = serverSocket.accept();
                    clientConnection = new ClientInputReader(clientSocket);
                    Thread t = new Thread(clientConnection);
                    t.start();
                } catch (SocketException e) {
                    //thrown when server.close() is called and the socket server is waiting on accept
                    LOG.error("Server SocketException: " + e.getMessage());
                } catch (IOException ioe) {
                    LOG.error("IOException on socket listen: ", ioe);
                }
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
    protected void onCommand(Command c) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canExecute(Command c) {
        //do nothing
        return true;
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException();
    }

    /**
     * This class reads data input from clients until "stop-connection-char" is
     * received.
     *
     */
    private class ClientInputReader
            implements Runnable {

        private final Socket client;

        ClientInputReader(Socket client) {
            this.client = client;
            LOG.info("New client connected to server on \"{}\". Currently {} active connection(s)", client.getInetAddress(), activeConnections.incrementAndGet());
        }

        /**
         * Parses the string input in coordinates form.
         *
         * @param in input string to parse
         */
        private void parseInputAsCoordinates(String in) {
            int id = 0;
            int x = -1;
            int y = -1;
            int tokenCounter = 0;
            StringTokenizer tokenizer = new StringTokenizer(in, FIELD_DELIMITER);
            FreedomPoint location;

            try {
                tokenCounter = tokenizer.countTokens();
                id = Integer.parseInt(tokenizer.nextToken());
                x = Integer.parseInt(tokenizer.nextToken());
                y = Integer.parseInt(tokenizer.nextToken());
                location = new FreedomPoint(x, y);
                movePerson(id, location);
            } catch (Exception ex) {
                LOG.error("Error while parsing client input: \"{}\". Token count: {}", in, tokenCounter);
            }
        }

        /**
         * Parses the string input in rooms form.
         *
         * @param in input string to parse
         */
        private void parseInputAsRooms(String in) {
            int id = 0;
            int tokenCounter = 0;
            String roomName;
            StringTokenizer tokenizer = new StringTokenizer(in, FIELD_DELIMITER);

            try {
                tokenCounter = tokenizer.countTokens();
                id = Integer.parseInt(tokenizer.nextToken());
                roomName = tokenizer.nextToken();
                ZoneLogic zone = getApi().environments().findAll().get(0).getZone(roomName);
                if (zone != null) {
                    FreedomPoint roomCenterCoordinate = Utils.getPolygonCenter(zone.getPojo().getShape());
                    movePerson(id, roomCenterCoordinate);
                } else {
                    LOG.warn("Room \"{}\" not found.", roomName);
                }
            } catch (Exception ex) {
                LOG.error("Error while parsing client input: \"{}\". Token count: {}", in, tokenCounter);
            }
        }

        public void run() {
            try {
                String line;
                // Get input from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (((line = in.readLine()) != null) && !line.equals(STOP_CONNECTION_CHAR) && isRunning()) {
                    LOG.info("Readed from socket: \"{}\"", line);
                    switch (DATA_TYPE) {
                        case "coordinates":
                            parseInputAsCoordinates(line);
                            break;

                        case "rooms":
                            parseInputAsRooms(line);
                            break;

                        default:
                            throw new PluginStartupException("<data-type> property wrong in manifest file");
                    }
                }
                client.close();
                LOG.info("Closing socket connection \"{}\". Currently {} active connection(s)", client.getInetAddress(), activeConnections.decrementAndGet());
            } catch (IOException ioe) {
                LOG.error("IOException on socket listening: \"{}\"", ioe);
            } catch (PluginStartupException ex) {
                LOG.error("TrackingReadSocket startup exception: ", ex);
            }
        }
    }
}

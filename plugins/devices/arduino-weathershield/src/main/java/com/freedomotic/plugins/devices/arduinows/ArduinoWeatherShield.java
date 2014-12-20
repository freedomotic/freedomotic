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
package com.freedomotic.plugins.devices.arduinows;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.HttpHelper;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A sensor for the Arduino Weather Shield developed by www.ethermania.com
 * author Mauro Cicolella - www.emmecilab.net For details please refer to
 * http://www.ethermania.com/shop/index.php?main_page=product_info&cPath=91_104&products_id=612
 */
public class ArduinoWeatherShield extends Protocol {

    private static final Logger LOG = Logger.getLogger(ArduinoWeatherShield.class.getName());
    private int POLLING_TIME = configuration.getIntProperty("time-between-reads", 1000);
    private int SOCKET_TIMEOUT = 10000;
    private String IP_TO_QUERY;
    private String DELIMITER;
    private int PORT_TO_QUERY;
    private int BOARD_NUMBER = configuration.getTuples().size();
    private ArrayList<Board> boards = null;
    HttpHelper http = new HttpHelper();

    public ArduinoWeatherShield() {
        super("Arduino WeatherShield", "/arduino-weathershield/arduinows-manifest.xml");
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        for (int i = 0; i < BOARD_NUMBER; i++) {
            // filter the tuples with "object.class" property
            String result = configuration.getTuples().getProperty(i, "object.class");
            // if the tuple hasn't an "object.class" property it's a board configuration one
            if (result == null) {
                IP_TO_QUERY = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.0.150");
                PORT_TO_QUERY = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
                POLLING_TIME = configuration.getTuples().getIntProperty(i, "polling-time", 1000);
                SOCKET_TIMEOUT = configuration.getTuples().getIntProperty(i, "socket-timeout", 1000);
                DELIMITER = configuration.getTuples().getStringProperty(i, "delimiter", "|");
                Board board = new Board(IP_TO_QUERY, PORT_TO_QUERY, POLLING_TIME, SOCKET_TIMEOUT, DELIMITER);
                boards.add(board);
            }
        }
    }

    @Override
    public void onStart() {
        setPollingWait(POLLING_TIME);
        loadBoards();
    }

    @Override
    public void onStop() {
        //release resources
        boards.clear();
        boards = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "ArduinoWeatherShield"));
    }

    @Override
    protected void onRun() {
        for (Board board : boards) {
            try {
                readValues(board);
            } catch (UnableToExecuteException ex) {
                LOG.severe(ex.getMessage());
            } catch (IOException ex) {
                LOG.severe("Exception during data retrieving for " + ex.getCause());
            }
        }
    }

    private void readValues(Board board) throws UnableToExecuteException, IOException {
        try {
            String data = http.retrieveContent("http://" + board.getIpAddress() + ":" + board.getPort());
            LOG.info("Read data: " + data);
            sendChanges(board, data);
        } catch (IOException ex) {
            throw ex;
        }
    }

    private void sendChanges(Board board, String parametersValue) throws IllegalArgumentException, NumberFormatException {
        String address = board.getIpAddress() + DELIMITER + board.getPort();
        String values[] = parametersValue.split(board.getDelimiter());

        if (!(values.length == 3)) {
            throw new IllegalArgumentException("Retrieved data insufficient: 3 values required");
        } else {
            for (int i = 0; i < 3; i++) {
                if (!isNumber(values[i])) {
                    throw new NumberFormatException("'" + values[i] + "' is not a number");
                }
            }
        }

        //building the event
        ProtocolRead event = new ProtocolRead(this, "arduino-weathershield", address + DELIMITER + "T");
        //adding some optional information to the event
        event.addProperty("board.ip", board.getIpAddress());
        event.addProperty("board.port", new Integer(board.getPort()).toString());
        event.addProperty("sensor.temperature", values[0]);
        event.addProperty("object.class", "Thermometer");
        event.addProperty("object.name", "Arduino WeatherShield Thermometer");
        //publish the event on the messaging bus
        this.notifyEvent(event);

        event = new ProtocolRead(this, "arduino-weathershield", address + DELIMITER + "P");
        //adding some optional information to the event
        event.addProperty("board.ip", board.getIpAddress());
        event.addProperty("board.port", new Integer(board.getPort()).toString());
        event.addProperty("sensor.pressure", values[1]);
        event.addProperty("object.class", "Barometer");
        event.addProperty("object.name", "Arduino WeatherShield Barometer");
        //publish the event on the messaging bus
        this.notifyEvent(event);

        event = new ProtocolRead(this, "arduino-weathershield", address + DELIMITER + "H");
        //adding some optional information to the event
        event.addProperty("board.ip", board.getIpAddress());
        event.addProperty("board.port", new Integer(board.getPort()).toString());
        event.addProperty("sensor.humidity", values[2]);
        event.addProperty("object.class", "Hygrometer");
        event.addProperty("object.name", "Arduino WeatherShield Hygrometer");
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    private boolean isNumber(String stringNumber) {
        return stringNumber.matches("-?\\d+(\\.\\d+)?");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

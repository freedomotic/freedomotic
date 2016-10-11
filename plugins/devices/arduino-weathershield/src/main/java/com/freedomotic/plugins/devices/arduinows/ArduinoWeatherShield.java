/**
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 * <p>
 * This file is part of Freedomotic
 * <p>
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * <p>
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.arduinows;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginRuntimeException;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.HttpHelper;
import com.freedomotic.reactions.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A sensor for the Arduino Weather Shield developed by www.ethermania.com
 *
 * @author Mauro Cicolella
 */
public class ArduinoWeatherShield extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(ArduinoWeatherShield.class.getName());
    private final int POLLING_TIME = configuration.getIntProperty("time-between-reads", 1000);
    private final String DELIMITER = configuration.getStringProperty("delimiter", ":");
    private final int TUPLES_COUNT = configuration.getTuples().size();
    private final int MAX_FAILURES = configuration.getIntProperty("max-failures", 0);
    private List<Board> boards = new ArrayList<>();
    private HttpHelper http = new HttpHelper();

    /**
     *
     */
    public ArduinoWeatherShield() {
        super("Arduino WeatherShield", "/arduino-weathershield/arduinows-manifest.xml");
    }

    @Override
    public void onStart() throws PluginStartupException {
        setPollingWait(POLLING_TIME);
        http.setConnectionTimeout(5000);
        loadBoards();
        if (boards.isEmpty()) {
            throw new PluginStartupException("No boards defined in confguration file or configuration format is wrong");
        }
        setDescription(boards.size() + " board(s) configured");
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
    protected void onRun() throws PluginRuntimeException {
        for (Board board : boards) {
            readValuesWithRetry(board, MAX_FAILURES);
        }
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<>();
        }
        for (int i = 0; i < TUPLES_COUNT; i++) {
            // filter the tuples with "object.class" property
            String result = configuration.getTuples().getProperty(i, "object.class");
            // if the tuple hasn't an "object.class" property it's a board configuration one
            if (result == null) {
                String IP_TO_QUERY = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.0.150");
                int PORT_TO_QUERY = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
                Board board = new Board(IP_TO_QUERY, PORT_TO_QUERY);
                LOG.info("Arduino Weathershield board configured to be"
                        + " queried at {}:{}", new Object[]{IP_TO_QUERY, PORT_TO_QUERY});
                boards.add(board);
            }
        }
    }

    private void readValues(Board board) throws IOException {
        String urlString = "http://" + board.getIpAddress() + ":" + board.getPort();
        String data = http.retrieveContent(urlString);
        String dataSensors = parseBody(data);
        if (!dataSensors.isEmpty()) {
            LOG.info("Read data from Arduino Weathershield: {}", dataSensors);
            try {
                notifyReadValues(board, dataSensors);
            } catch (IllegalArgumentException ex) {
                notifyCriticalError("Error while notifying Arduino Weathershield data", ex);
            }
        }
    }

    private void readValuesWithRetry(Board board, int maxFailures) throws PluginRuntimeException {
        try {
            readValues(board);
        } catch (IOException e) {
            if (maxFailures > 0) {
                readValuesWithRetry(board, --maxFailures);
            } else {
                throw new PluginRuntimeException("Cannot connect to Arduino Weathershield board at "
                        + board.getIpAddress() + ":" + board.getPort(), ex);
            }
        }
    }

    private void notifyReadValues(Board board, String parametersValue) throws IllegalArgumentException {
        String address = board.getIpAddress() + DELIMITER + board.getPort();
        String values[] = parametersValue.split(DELIMITER);

        if (!(values.length == 3)) {
            throw new IllegalArgumentException("Cannot parse string " + parametersValue
                    + " to extract three numeric values separated by '" + DELIMITER + "'");
        }
        // disabled control for now - looking for a good isNumber implementation
        //else {
        //  for (int i = 0; i < 3; i++) {
        //    if (!isNumber(values[i])) {
        //      throw new IllegalArgumentException("Error while parsing string '"
        //            + parametersValue + "'" + values[i] + "' is not a number");
        //  }
        //}
        //}

        //building the event
        ProtocolRead event = new ProtocolRead(this, "arduino-weathershield", address + DELIMITER + "T");
        //adding some optional information to the event
        event.addProperty("board.ip", board.getIpAddress());
        event.addProperty("board.port", Integer.toString(board.getPort()));
        event.addProperty("sensor.temperature", values[0]);
        event.addProperty("object.class", "Thermometer");
        event.addProperty("autodiscovery.allow-clones", "false");
        event.addProperty("object.name", "Arduino WeatherShield Thermometer");
        //publish the event on the messaging bus
        this.notifyEvent(event);

        event = new ProtocolRead(this, "arduino-weathershield", address + DELIMITER + "P");
        //adding some optional information to the event
        event.addProperty("board.ip", board.getIpAddress());
        event.addProperty("board.port", Integer.toString(board.getPort()));
        event.addProperty("sensor.pressure", values[1]);
        event.addProperty("object.class", "Barometer");
        event.addProperty("autodiscovery.allow-clones", "false");
        event.addProperty("object.name", "Arduino WeatherShield Barometer");
        //publish the event on the messaging bus
        this.notifyEvent(event);

        event = new ProtocolRead(this, "arduino-weathershield", address + DELIMITER + "H");
        //adding some optional information to the event
        event.addProperty("board.ip", board.getIpAddress());
        event.addProperty("board.port", Integer.toString(board.getPort()));
        event.addProperty("sensor.humidity", values[2]);
        event.addProperty("object.class", "Hygrometer");
        event.addProperty("autodiscovery.allow-clones", "false");
        event.addProperty("object.name", "Arduino WeatherShield Hygrometer");
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    private boolean isNumber(String stringNumber) {
        //TODO: this regex is far from being precise, test it with http://www.regexr.com/
        return stringNumber.matches("-?\\d+(\\.\\d+)?");
    }

    @Override
    protected void onCommand(Command c) throws UnableToExecuteException {
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

    // extract sensors data from <body></body> tags
    private static String parseBody(String html) {
        Pattern p = Pattern.compile("<body.*?>(?<Content>([^<]|<[^/]|</[^b]|</b[^o])*)",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        if (m.find()) {
            String element = m.group(1);
            return element;
        } else {
            return "";
        }
    }
}

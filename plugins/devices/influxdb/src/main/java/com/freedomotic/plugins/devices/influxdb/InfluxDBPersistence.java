/**
 *
 * Copyright (c) 2009-2018 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.influxdb;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.FreedomoticRuntimeException;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class InfluxDBPersistence
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(InfluxDBPersistence.class.getName());
    private final String DB_URL = configuration.getStringProperty("db-url", "http://127.0.0.1:8086");
    private final String DB_NAME = configuration.getStringProperty("db-name", "freedomotic");
    private final String USERNAME = configuration.getStringProperty("username", "root");
    private final String PASSWORD = configuration.getStringProperty("password", "root");
    private final String RETENTION_POLICY = configuration.getStringProperty("retention-policy", "autogen");
    private InfluxDB influxDB;
    private boolean connected;

    /**
     *
     */
    public InfluxDBPersistence() {
        super("InfluxDB", "/influxdb-persistence/influxdb-persistence-manifest.xml");
        setPollingWait(-1); // disable onRun()
    }

    @Override
    protected void onShowGui() {
    }

    @Override
    protected void onHideGui() {
    }

    @Override
    protected void onRun() {
        // disabled
    }

    @Override
    protected void onStart() throws PluginStartupException {
        connect();
        if (!connected) {
            throw new PluginStartupException("No connection available to InfluxDB server");
        }
        // check if database exists
        if (!databaseExists(DB_NAME)) {
            influxDB.createDatabase(DB_NAME);
        }
        setDescription("Connected to DB: " + DB_URL);
        LOG.info("InfluxDB plugin started");
    }

    @Override
    protected void onStop() {
        disconnect();
        LOG.info("InfluxDB plugin stopped");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        if (isRunning()) {
            if (c.getProperty("command") == null || c.getProperty("command").isEmpty() || c.getProperty("command").equalsIgnoreCase("SAVE-DATA")) {
                saveData(c);
            } else if (c.getProperty("command").equals("EXTRACT-DATA")) { //extract data
                // TODO add data extraction procedure
            }
        }

    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {

    }

    /**
     * Disconnects from database server.
     *
     */
    private void disconnect() {
        if (isConnected() && !(influxDB == null)) {
            influxDB.close();
            influxDB = null;
            connected = false;
        }
    }

    /**
     * Checks if the connection to the database is established.
     *
     * @return true if the connection to the database is established, false
     * otherwise
     */
    private boolean isConnected() {
        return connected;
    }

    /**
     * Connects to the database server.
     *
     */
    private void connect() {
        influxDB = (InfluxDB) InfluxDBFactory.connect(DB_URL, USERNAME, PASSWORD);
        try {
            Pong response = influxDB.ping();
            if (!response.getVersion().equalsIgnoreCase("unknown")) {
                connected = true;
                LOG.info("Connected to database server InfluxDB");
            } else {
                LOG.error("Database server InfluxDB not responding. Please check your configuration and/or connection");
                connected = false;
            }
        } catch (FreedomoticRuntimeException e) {
            connected = false;
            LOG.error("Impossible to connect to database server InfluxDB for {}", e.getMessage());
        }
    }

    /**
     * Checks if the database exists given its name.
     *
     * @param dbName database name
     * @return true if the database exists, false otherwise
     */
    private boolean databaseExists(String dbName) {
        return influxDB.databaseExists(dbName);
    }

    /**
     * Saves data to the database.
     *
     * @param c command including all data to save
     */
    private void saveData(Command c) {
        try {
            String objName = c.getProperty("current.object.name");
            String objProtocol = c.getProperty("current.object.protocol");
            String objAddress = c.getProperty("current.object.address");
            String objUuid = c.getProperty("current.object.uuid");

            //search for all object's behaviors changes    
            Pattern pat = Pattern.compile("^current\\.object\\.behavior\\.(.*)");
            for (Entry<Object, Object> entry : c.getProperties().entrySet()) {
                String key = (String) entry.getKey();
                Matcher fits = pat.matcher(key);
                if (fits.find() && !fits.group(1).equals("data")) { //exclude unwanted behaviors
                    String objBehavior = fits.group(1);
                    String behaviorValue = convertBehaviorValue((String) entry.getValue());
                    Point point = Point
                            .measurement(objBehavior)
                            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                            .addField("freedomotic_instance_uuid", Freedomotic.getInstanceID())
                            .addField("object_name", objName)
                            .addField("object_protocol", objProtocol)
                            .addField("object_address", objAddress)
                            .addField("object_uuid", objUuid)
                            // value is stored as Double to use aggregation functions
                            .addField("value", Double.valueOf(behaviorValue))
                            .build();
                    influxDB.write(DB_NAME, RETENTION_POLICY, point);
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }

    /**
     * Converts boolean values to numeric.
     *
     *
     * @param value string value to convert
     * @return 0 or 1 for boolean values, otherwise the same string passed as
     * parameter
     */
    private String convertBehaviorValue(String value) {
        String convertedValue = "";

        switch (value) {
            case "true":
                convertedValue = "1";
                break;

            case "false":
                convertedValue = "0";
                break;

            default:
                convertedValue = value;
                break;
        }
        return convertedValue;
    }
}

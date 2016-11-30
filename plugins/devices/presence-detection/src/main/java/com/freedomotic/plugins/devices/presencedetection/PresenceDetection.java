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
package com.freedomotic.plugins.devices.presencedetection;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.reactions.Command;
import com.freedomotic.things.GenericPerson;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mauro
 */
public class PresenceDetection
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(PresenceDetection.class.getName());
    private final int POLLING_TIME;
    private final int MAX_FAILED_REQUESTS;
    String DETECTION_METHOD = null;
    private Map<String, Integer> usersList = new HashMap<>();

    /**
     *
     */
    public PresenceDetection() {
        super("Presence Detection", "/presence-detection/presence-detection-manifest.xml");
        POLLING_TIME = configuration.getIntProperty("polling-time", 2000);
        MAX_FAILED_REQUESTS = configuration.getIntProperty("max-failed-requests", 50);
        DETECTION_METHOD = configuration.getStringProperty("detection-method", "ip");
        setPollingWait(POLLING_TIME);
    }

    @Override
    protected void onShowGui() {

    }

    @Override
    protected void onHideGui() {

    }

    @Override
    protected void onRun() {
        Boolean isPresent = false;
        ProtocolRead event;
        for (EnvObjectLogic object : getApi().things().findAll()) {
            if (object instanceof GenericPerson) {
                GenericPerson person = (GenericPerson) object;
                try {
                    //isPresent = ping(person.getPojo().getPhisicalAddress());
                    isPresent = isHostReachable(person.getPojo().getPhisicalAddress());
                    event = new ProtocolRead(this, "presence-detection", person.getPojo().getPhisicalAddress());
                    event.addProperty("person.isPresent", String.valueOf(isPresent));
                    event.addProperty("person.id", person.getPojo().getName());
                    // if present=true or the plugin is starting for the first time
                    if (isPresent == true || usersList.get(person.getPojo().getPhisicalAddress()) == -1) {
                        usersList.put(person.getPojo().getPhisicalAddress(), 0);
                        notifyEvent(event);
                    } else {
                        if (usersList.get(person.getPojo().getPhisicalAddress()) >= MAX_FAILED_REQUESTS) {
                            notifyEvent(event);
                        } else {
                            usersList.put(person.getPojo().getPhisicalAddress(), usersList.get(person.getPojo().getPhisicalAddress()) + 1);
                        }
                    }
                    LOG.debug("Address: " + person.getPojo().getPhisicalAddress() + " status:" + person.getPojo().getBehavior("present") + " failed requests: " + usersList.get(person.getPojo().getPhisicalAddress()));
                } catch (IOException ex) {
                    LOG.error(ex.getMessage());
                }
            }
        }
    }

    @Override
    protected void onStart() {
        LOG.info("Presence Detection plugin started");
        initializeUsersList();
    }

    @Override
    protected void onStop() {
        LOG.info("Presence Detection plugin stopped ");

    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {

    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Detects if an ip host is reachable by using 'ping' tool.
     *
     * @param host ip address
     * @return true if host is reachable, false otherwise
     */
    private boolean ping(String host) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        ProcessBuilder processBuilder = new ProcessBuilder("ping", isWindows ? "-n" : "-c", "1", host);
        Process proc = processBuilder.start();

        int returnVal = proc.waitFor();
        return returnVal == 0;
    }

    /**
     * Detects if a host is reachable.
     * 
     * @param host host ip address to check
     * @return true if the host is reachable, false otherwise
     */
    private boolean isHostReachable(String host) throws UnknownHostException, IOException {
        InetAddress address = InetAddress.getByName(host);
        return address.isReachable(5000);
    }

    /**
     *
     *
     */
    private void initializeUsersList() {
        for (EnvObjectLogic object : getApi().things().findAll()) {
            if (object instanceof GenericPerson) {
                GenericPerson person = (GenericPerson) object;
                usersList.put(person.getPojo().getPhisicalAddress(), -1);
            }
        }

    }
}

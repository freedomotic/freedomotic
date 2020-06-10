/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://freedomotic.com
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
/*
 * @author Mauro Cicolella 
 */
package com.freedomotic.plugins.devices.thingspeak;

import com.angryelectron.thingspeak.Channel;
import com.angryelectron.thingspeak.Entry;
import com.angryelectron.thingspeak.ThingSpeakException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.HashMap;
import java.util.Map;

public class ThingSpeak extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(ThingSpeak.class.getName());
    private String API_KEY = configuration.getStringProperty("api-key", "<api-key>");
    private Integer POLLING_TIME = configuration.getIntProperty("polling-time", 60000);

    private Map<String, ThingSpeakObj> tsObjects = new HashMap<String, ThingSpeakObj>();

    public ThingSpeak() {
        super("ThingSpeak", "/thingspeak/thingspeak-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    @Override
    protected void onShowGui() {
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
    }

    @Override
    protected void onRun() {
        for (Map.Entry<String, ThingSpeakObj> entry : tsObjects.entrySet()) {
            if (!getApi().things().findByName(entry.getKey()).isEmpty()) {
                String value = getApi().things().findByName(entry.getKey()).get(0).getBehavior(entry.getValue().getThingBehavior()).getValueAsString();
                publish(entry.getValue().getThingSpeakChannel(), API_KEY, entry.getValue().getThingSpeakField(), value);
            } else {
                LOG.error("Thing {} doesn't exist", entry.getKey());
            }
        }
    }

    @Override
    protected void onStart() throws PluginStartupException {
        loadConfiguration();

    }

    @Override
    protected void onStop() {
        tsObjects.clear();
        LOG.info("ThingSpeak stopped");
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
     * Loads the things configuration from manifest file.
     * 
     */
    private void loadConfiguration() {

        String thingName;
        String thingBehavior;
        Integer thingspeakChannel;
        Integer thingspeakField;
        ThingSpeakObj tsObj;

        for (int i = 0; i < configuration.getTuples().size(); i++) {
            thingName = configuration.getTuples().getStringProperty(i, "thing-name", "");
            thingBehavior = configuration.getTuples().getStringProperty(i, "thing-behavior", "");
            thingspeakChannel = configuration.getTuples().getIntProperty(i, "thingspeak-channel", 0);
            thingspeakField = configuration.getTuples().getIntProperty(i, "thingspeak-field", 0);
            tsObj = new ThingSpeakObj(thingName, thingBehavior, thingspeakChannel, thingspeakField);
            tsObjects.put(thingName, tsObj);
        }
    }

    /**
     * Published the data on ThingSpeak.com
     * 
     * @param channel channel ID
     * @param ApiKey  api key of ThingSpeak
     * @param field   field number (1-8)
     * @param value   thing value 
     */
    private void publish(Integer channel, String ApiKey, Integer field, String value) {

        Channel tsChannel = new Channel(channel, API_KEY);
        Entry entry = new Entry();
        entry.setField(field, value);
        try {
            tsChannel.update(entry);
        } catch (UnirestException ex) {
            LOG.error(ex.getLocalizedMessage());
        } catch (ThingSpeakException ex) {
            LOG.error(ex.getLocalizedMessage());
        }

    }
}

/**
 *
 * Copyright (c) 2009-2017 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.twilight;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.plugins.devices.twilight.providers.EarthToolsWI;
import com.freedomotic.plugins.devices.twilight.providers.OpenWeatherMapWI;
import java.io.IOException;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 */
public class Twilight extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(Twilight.class.getName());
    private final int POLLING_WAIT;
    private final String Latitude;
    private final String Longitude;
    private final TwilightUtils TLU;
    private final WeatherInfo provider;
    private final String providerName;

    /**
     *
     */
    public Twilight() {
        super("Twilight", "/twilight/twilight-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("polling-time", 10000);
        Latitude = configuration.getStringProperty("latitude", "0.0");
        Longitude = configuration.getStringProperty("longitude", "0.0");
        providerName = configuration.getStringProperty("provider", "openweathermap");

        switch (providerName) {

            case "openweathermap":
                provider = new OpenWeatherMapWI(Latitude, Longitude);
                break;

            default:
                provider = new EarthToolsWI(Latitude, Longitude);
        }

        TLU = new TwilightUtils(POLLING_WAIT, provider);
        setPollingWait(-1); // disabled onRun()
    }

    @Override
    protected void onShowGui() {
        // no GUI is used
    }

    @Override
    protected void onHideGui() {
        // no GUI is used
    }

    @Override
    protected void onRun() {
        EventTemplate ev = TLU.prepareEvent(ZonedDateTime.now());
        LOG.info(ev.getPayload().toString().replace("\n", " "));
        notifyEvent(ev);

    }

    @Override
    protected void onStart() throws PluginStartupException {
        try {
            LOG.info("Twilight plugin started");
            provider.updateData();
            setPollingWait(POLLING_WAIT);
            setDescription("Sunrise: " + provider.getNextSunrise().toLocalTime() + " Sunset: " + provider.getNextSunset().toLocalTime());
        } catch (Exception ex) {
            throw new PluginStartupException("Error retrieving data from provider {}", ex);
        }
    }

    @Override
    protected void onStop() {
        LOG.info("Twilight plugin stopped ");
        setPollingWait(-1);
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        String command = c.getProperty("command");
        if (command.equals("Update Twilight Data")) {
            try {
                provider.updateData();
                setDescription("Sunrise: " + provider.getNextSunrise().toLocalTime() + " Sunset: " + provider.getNextSunset().toLocalTime());
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
            }

        }
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
}

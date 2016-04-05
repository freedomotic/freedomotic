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
package com.freedomotic.plugins.devices.twilight;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.plugins.devices.twilight.providers.EarthToolsWI;
import com.freedomotic.plugins.devices.twilight.providers.OpenWeatherMapWI;
import java.io.IOException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 */
public class Twilight extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(Twilight.class.getName());
    private int POLLING_WAIT;
    private String Latitude;
    private String Longitude;
    private TwilightUtils TLU;
    private WeatherInfo provider;
    private String providerName;

    /**
     *
     */
    public Twilight() {
        //every plugin needs a name and a manifest XML file
        super("Twilight", "/twilight/twilight-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/it.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("polling-time", 10000);
        Latitude = configuration.getStringProperty("latitude", "0.0");
        Longitude = configuration.getStringProperty("longitude", "0.0");
        providerName = configuration.getStringProperty("provider", "openweathermap");
        if (providerName.equalsIgnoreCase("openweathermap")) {
            provider = new OpenWeatherMapWI(Latitude, Longitude);
        } else {
            provider = new EarthToolsWI(Latitude, Longitude);
        }
        TLU = new TwilightUtils(POLLING_WAIT, provider);
        //default value if the property does not exist in the manifest
        setPollingWait(-1); //millisecs interval between hardware device status reads
    }

    @Override
    protected void onShowGui() {
        /**
         * uncomment the line below to add a GUI to this plugin the GUI can be
         * started with a right-click on plugin list on the desktop frontend
         * (it.freedomotic.jfrontend plugin)
         */
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
        //implement here what to do when the this plugin GUI is closed
        //for example you can change the plugin description
        setDescription("My GUI is now hidden");
    }

    @Override
    protected void onRun() {
        EventTemplate ev = TLU.prepareEvent(DateTime.now());
        LOG.info(ev.getPayload().toString().replace("\n", " "));
        notifyEvent(ev);

    }

    @Override
    protected void onStart() {
        try {
            LOG.info("Twilight plugin started");
            provider.updateData();
            setPollingWait(POLLING_WAIT);
            setDescription("Sunrise: " + provider.getNextSunrise().toLocalTime() + " Sunset: " + provider.getNextSunset().toLocalTime());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            stop();
        }
    }

    @Override
    protected void onStop() {
        LOG.info("Twilight plugin is stopped ");
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

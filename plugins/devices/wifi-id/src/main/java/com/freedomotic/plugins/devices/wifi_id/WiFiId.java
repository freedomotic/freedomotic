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
package com.freedomotic.plugins.devices.wifi_id;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.io.InputStream;
import java.io.*;
import java.net.*;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginRuntimeException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.EnvObjectPersistence;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WiFiId extends Protocol {

    private static final Logger LOG = Logger.getLogger(WiFiId.class.getName());

    public WiFiId() {
        super("WiFi Presence", "/wifi-id/wifi_id-manifest.xml");
        setPollingWait(configuration.getIntProperty("polling_rate", 5000)); //waits 2000ms in onRun method before call onRun() again
    }

    @Override
    public void onStart() {
        //called when the user starts the plugin from UI
    }

    @Override
    public void onStop() {
        //called when the user stops the plugin from UI
    }

    @Override
    protected void onRun() throws PluginRuntimeException {
            //called in a loop while this plugin is running
        //loops waittime is specified using setPollingWait()
        URL url = null;
        InputStream is = null;
        BufferedReader br;
        String line, html = null;
        ProtocolRead event;
        Authenticator.setDefault(new MyAuthenticator());

        try {
            url = new URL(configuration.getStringProperty("url", ""));
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                html = html + line;
            }
        } catch (IOException ioe) {
            setDescription(ioe.getMessage());
        }
        //Freedomotic.logger.config(html);
        html = html.toLowerCase();
        LOG.config("HTML: " + html);
        for (EnvObjectLogic object : EnvObjectPersistence.getObjectByProtocol("wifi_id")) {
            String mac_address = object.getPojo().getPhisicalAddress();
            String name = object.getPojo().getName();
            if (html.contains(mac_address.toLowerCase())) {
                // user exist
                event = new ProtocolRead(this, "wifi_id", mac_address);
                event.addProperty("wifi_id.present", "true");
                notifyEvent(event);
            } else {
                // user not exist
                event = new ProtocolRead(this, "wifi_id", mac_address);
                event.addProperty("wifi_id.present", "false");
                notifyEvent(event);
            }
        }
        try {
            is.close();
        } catch (IOException iOException) {
        }

            //print the string in the freedomotic log using INFO level
        //Freedomotic.logger.info(buffer.toString());
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        //this method receives freedomotic commands send on channel app.actuators.protocol.arduinousb.in
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class MyAuthenticator extends Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            String kuser, kpass;
            // I haven't checked getRequestingScheme() here, since for NTLM
            // and Negotiate, the usrname and password are all the same.
            //System.err.println("Feeding username and password for " + getRequestingScheme());
            kuser = configuration.getStringProperty("url_username", "");
            kpass = configuration.getStringProperty("url_password", "");
            return (new PasswordAuthentication(kuser, kpass.toCharArray()));
        }
    }
}

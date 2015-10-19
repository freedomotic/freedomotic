/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.purl;


import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Purl extends Protocol {

    private static final Logger LOG = Logger.getLogger(Purl.class.getName());

    public Purl() {
        super("pURL", "/pURL/purl-manifest.xml");
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
    protected void onRun() {
        //called in a loop while this plugin is running
        //loops waittime is specified using setPollingWait()
        Authenticator.setDefault(new MyAuthenticator());
        String pageContent = "";
        String url = "";
        try {
            url = URLDecoder.decode(configuration.getStringProperty("url", ""), "UTF-8");
            pageContent = readPage(new URL(url));
        } catch (Exception ex) {
            LOG.severe(ex.getLocalizedMessage());
        }

        if (!pageContent.isEmpty()) {
            GenericEvent event = new GenericEvent(this);
            event.setDestination("app.event.sensor.url");
            event.addProperty("url", url);
            event.addProperty("url.content", pageContent);
            event.addProperty("url.content.length", String.valueOf(pageContent.length()));
            notifyEvent(event);
            setDescription("Readed " + pageContent.length() + "characters");
        }
    }

    private static String readPage(URL url) throws Exception {

        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url.toURI());
        HttpResponse response = client.execute(request);

        Reader reader = null;
        try {
            reader = new InputStreamReader(response.getEntity().getContent());

            StringBuilder sb = new StringBuilder();
            int read;
            char[] cbuf = new char[1024];
            while ((read = reader.read(cbuf)) != -1) {
                sb.append(cbuf, 0, read);
            }
            return sb.toString();

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class MyAuthenticator extends Authenticator {

        @Override
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

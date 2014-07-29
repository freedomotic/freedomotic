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
package com.freedomotic.plugins.devices.push;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;



public class Push
        extends Protocol {

    private static final Logger LOG = Logger.getLogger(Push.class.getName());
    private final HashMap<String,Integer> tupleMap = new HashMap<>();
    
    public Push() {
        //every plugin needs a name and a manifest XML file
        super("Push", "/push/push-manifest.xml");
        setPollingWait(-1); //millisecs interval between hardware device status reads
        for (int i = 0; i < configuration.getTuples().size(); i++){
            tupleMap.put(configuration.getTuples().getProperty(i, "provider"),i);
        } 
}

    @Override
    protected void onShowGui() {
    }


    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        LOG.info("Push plugin is started");
    }

    @Override
    protected void onStop() {
        LOG.info("Push plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        try {
            int t = tupleMap.get(c.getProperty("push.provider"));
            
            URIBuilder ub = new URIBuilder()
                    .setScheme(configuration.getTuples().getStringProperty(t, "scheme", "http"))
                    .setHost(configuration.getTuples().getStringProperty(t, "host", "localhost"))
                    .setPort(configuration.getTuples().getIntProperty(t, "port", 
                            configuration.getTuples().getStringProperty(t, "scheme", "http").equalsIgnoreCase("https") ? 443: 80))
                    .setPath(configuration.getTuples().getStringProperty(t, "path", "/"));
            
            // prepare substitution tokens
            HashMap<String, String> valuesMap = new HashMap<>();
            for (Entry<Object,Object> entry : c.getProperties().entrySet()){
                valuesMap.put(entry.getKey().toString(),entry.getValue().toString());
            }
            
            // add extra parameters
            for (String key : configuration.getTuples().getTuple(t).keySet()){
                if (key.startsWith("param.")){
                    // do substitutions
                    StrSubstitutor sub = new StrSubstitutor(valuesMap);
                    String resolvedString = sub.replace(configuration.getTuples().getStringProperty(t, key, ""));
                    
                    ub.setParameter(key.substring(6), resolvedString);
                }
            }
            
            LOG.info(ub.build().toString());
            
            HttpClientBuilder hcb = HttpClientBuilder.create();
            HttpClient client = hcb.build();
            
            // set http method to use
            HttpRequestBase request;
            if (configuration.getTuples().getStringProperty(t, "method", "get").equalsIgnoreCase("POST")){
                request = new HttpPost(ub.build());
            } else {
                request = new HttpGet(ub.build());
            }
            
            int responseCode = client.execute(request).getStatusLine().getStatusCode();
            LOG.info("Push request got code: " + responseCode);
            
        } catch (URISyntaxException ex) {
            Logger.getLogger(Push.class.getName()).log(Level.SEVERE, null, ex);
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

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
package com.freedomotic.plugins.devices.push;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.rules.Statement;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
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
    private final HashMap<String, Integer> tupleMap = new HashMap<>();

    public Push() {
        //every plugin needs a name and a manifest XML file
        super("Push", "/push/push-manifest.xml");
        setPollingWait(-1); //millisecs interval between hardware device status reads

    }

    @Override
    protected void onShowGui() {
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        int j=0;
        for (int i = 0; i < configuration.getTuples().size(); i++) {
            String channel = "app.event.sensor.messages."
                    + configuration.getTuples().getProperty(i, "category")
                    + "." + configuration.getTuples().getProperty(i, "provider");
            tupleMap.put(channel, i);
            
            if (configuration.getTuples().getBooleanProperty(i, "active", false)) {
                addEventListener(channel);
                j++;
            }
        }
        setDescription("Push plugin: "+j+" providers registered");
    }

    @Override
    protected void onStop() {
        setDescription("Push plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        MessageEvent mess = new MessageEvent(null, c.getProperty("push.message"));
        mess.setType(c.getProperty("push.category") + "." + c.getProperty("push.provider"));
        // populate event with originating command properties
        for (Entry<Object,Object> entry : c.getProperties().entrySet()){
            mess.getPayload().addStatement(entry.getKey().toString(), entry.getValue().toString());
        }
        notifyEvent(mess);
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate e) {
        try {
            MessageEvent event = (MessageEvent) e;
            int t = tupleMap.get(event.getDefaultDestination());

            URIBuilder ub = new URIBuilder()
                    .setScheme(configuration.getTuples().getStringProperty(t, "scheme", "http"))
                    .setHost(configuration.getTuples().getStringProperty(t, "host", "localhost"))
                    .setPort(configuration.getTuples().getIntProperty(t, "port",
                                    configuration.getTuples().getStringProperty(t, "scheme", "http").equalsIgnoreCase("https") ? 443 : 80))
                    .setPath(configuration.getTuples().getStringProperty(t, "path", "/"));

            // prepare substitution tokens
            HashMap<String, String> valuesMap = new HashMap<>();
            Iterator<Statement> it = event.getPayload().iterator();
            while (it.hasNext()){
                Statement s = it.next();
                if (s.getOperand().equalsIgnoreCase(Statement.EQUALS)) {
                    valuesMap.put(s.getAttribute(), s.getValue());
                }
            }
            StrSubstitutor sub = new StrSubstitutor(valuesMap);

            // add extra parameters
            for (String key : configuration.getTuples().getTuple(t).keySet()) {
                if (key.startsWith("param.")) {
                    String toBeReplaced = configuration.getTuples().getStringProperty(t, key, "");
                    // replace default string with the one provided into payload
                    if (event.getPayload().getStatementValue(key) != null && !event.getPayload().getStatementValue(key).isEmpty()){
                        toBeReplaced = event.getPayload().getStatementValue(key);
                    }
                    // do substitutions
                    String resolvedString = sub.replace(toBeReplaced);

                    ub.setParameter(key.substring(6), resolvedString);
                }
            }

            // override default message (with variable substitution) if a new one is specified in MessageEvent.text
            if (event.getText() != null & !event.getText().isEmpty()) {
                ub.setParameter(
                        configuration.getTuples().getStringProperty(t, "mapMessageToParam", "message"),
                        sub.replace(event.getText()));
            }

            LOG.info(ub.build().toString());

            HttpClientBuilder hcb = HttpClientBuilder.create();
            HttpClient client = hcb.build();

            // set http method to use
            HttpRequestBase request;
            if (configuration.getTuples().getStringProperty(t, "method", "get").equalsIgnoreCase("POST")) {
                request = new HttpPost(ub.build());
            } else {
                request = new HttpGet(ub.build());
            }

            int responseCode = client.execute(request).getStatusLine().getStatusCode();
            LOG.info("Push request got code: " + responseCode);

        } catch (URISyntaxException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}

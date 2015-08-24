/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.twitter;

import com.freedomotic.plugins.devices.twitter.gateways.TwitterGateway;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 *
 * @author gpt
 */
public class TwitterActuator extends Protocol {

    private static final Logger LOG = Logger.getLogger(TwitterActuator.class.getName());
    private Twitter twitter;

    public TwitterActuator() {
        super("TwitterActuator", "/twitter4f/twitter-actuator.xml");
        setPollingWait(-1);
        //start(); //or set the property startup-time at value "on load" in the config file
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        if (isRunning()) {
            try {
                //Maybe we can use the async api
                //First implementation. We can extend sending a mes to an specific user (for example)
                String statusmess = c.getProperty("status");
                Status status = twitter.updateStatus(statusmess);
                System.out.println("Successfully updated the status to [" + status.getText() + "].");
            } catch (TwitterException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected void onStart() {
        try {
            twitter = TwitterGateway.getInstance(configuration);
            setDescription("Connected as " + twitter.getScreenName());
        } catch (Exception e) {
            LOG.severe("Cannot start TwitterGateway for the followin reason:" + e.getMessage());
            stop();
        }
    }

    @Override
    protected void onStop() {
        twitter = null;
        setDescription("Not Connected");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onEvent(EventTemplate event) {
    }
}

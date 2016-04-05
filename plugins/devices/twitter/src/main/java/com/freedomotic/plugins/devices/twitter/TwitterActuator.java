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
package com.freedomotic.plugins.devices.twitter;

import com.freedomotic.plugins.devices.twitter.gateways.TwitterGateway;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public class TwitterActuator extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterActuator.class.getName());
    private Twitter twitter;

    /**
     *
     */
    public TwitterActuator() {
        super("TwitterActuator", "/twitter/twitter-actuator.xml");
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
                LOG.info("Successfully updated the status to [" + status.getText() + "].");
            } catch (TwitterException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

    @Override
    protected void onStart() {
        try {
            twitter = TwitterGateway.getInstance(configuration);
            setDescription("Connected as " + twitter.getScreenName());
        } catch (Exception e) {
            LOG.error("Cannot start TwitterGateway for the followin reason:" + e.getMessage());
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

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
package com.freedomotic.plugins.devices.btspeechrecognition;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import javax.bluetooth.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class BTSpeechRecognition
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(BTSpeechRecognition.class.getName());

    /**
     *
     */
    public BTSpeechRecognition() {
        super("BT Speech Recognition", "/bt-speech-recognition/bt-speech-recognition-manifest.xml");
        setPollingWait(-1); // stop polling
    }

    @Override
    protected void onShowGui() {
    }

    @Override
    protected void onHideGui() {
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        BTServer server = new BTServer(this);
        server.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                try {
                    throw new PluginStartupException("Error initializing bluetooth device for " + e.getMessage(), e);
                } catch (PluginStartupException ex) {
                }
            }
        });
        server.start();

    }

    @Override
    protected void onStop() {
        LOG.info("BT Speech Recognition stopped");
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
     *
     * @return
     */
    public Logger pluginLog() {
        return LOG;
    }

    /**
     *
     * @param command The command string to execute
     */
    public void sendCommand(String command) {
        if (!command.trim().isEmpty()) {
            String commandRecognized = command.substring(0, command.length() - 1);
            LOG.info("Trying to send command ''{}''", commandRecognized);
            Command nlpCommand = new Command();
            nlpCommand.setName("Recognize text with NLP");
            nlpCommand.setReceiver("app.commands.interpreter.nlp");
            nlpCommand.setDescription("A free-form text command to be interpreded by an NLP module");
            nlpCommand.setProperty("text", commandRecognized);
            nlpCommand.setReplyTimeout(10000);
            Command reply = send(nlpCommand);

            if (reply != null) {
                String executedCommand = reply.getProperty("result");
                if (executedCommand != null) {
                    setDescription("Recognized command: " + executedCommand);
                    LOG.info("Recognized command ''{}''", executedCommand);
                } else {
                    setDescription("No similar command exists");
                    LOG.info("No valid command similar to ''{}''", commandRecognized);
                }
            } else {
                setDescription("Unreceived reply within given timeout (10 seconds)");
                LOG.info("Unreceived reply within given timeout (10 seconds)");
            }
        }
    }
}

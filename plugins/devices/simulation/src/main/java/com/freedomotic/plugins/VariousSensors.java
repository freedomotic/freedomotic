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
package com.freedomotic.plugins;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.plugins.gui.VariousSensorsGui;
import com.freedomotic.reactions.Command;
import java.io.IOException;

/**
 *
 * @author Enrico Nicoletti
 */
public class VariousSensors extends Protocol {

    private Boolean powered = false;

    /**
     *
     */
    public VariousSensors() {
        super("Sensors Simulator", "/simulation/sensors-simulator.xml");
        setPollingWait(2000);
    }

    @Override
    protected void onShowGui() {
        bindGuiToPlugin(new VariousSensorsGui(this));
    }

    /**
     *
     */
    public void askSomething() {
        final Command c = new Command();
        c.setName("Ask something silly to user");
        c.setDelay(0);
        c.setExecuted(true);
        c.setEditable(false);
        c.setReceiver("app.actuators.frontend.javadesktop.in");
        c.setProperty("question", "<html><h1>Do you like Freedomotic?</h1></html>");
        c.setProperty("options", "Yes, it's good; No, it sucks; I don't know");
        c.setReplyTimeout(10000); //10 seconds

        new Thread(new Runnable() {
            @Override
            public void run() {
                VariousSensorsGui guiHook = (VariousSensorsGui) gui;
                Command reply = send(c);

                if (reply != null) {
                    String userInput = reply.getProperty("result");

                    if (userInput != null) {
                        guiHook.updateDescription("The reply to the test question is " + userInput);
                    } else {
                        guiHook.updateDescription("The user has not responded to the question within the given time");
                    }
                } else {
                    guiHook.updateDescription("Unreceived reply within given timeout (10 seconds)");
                }
            }
        }).start();
    }

    /**
     * 
     * @param text 
     */
    public void executeNlpCommand(String text) {
        final Command nlpCommand = new Command();
        nlpCommand.setName("Recognize text with NLP");
        nlpCommand.setReceiver("app.commands.interpreter.nlp");
        nlpCommand.setDescription("A free-form text command to be interpreded by an NLP module");
        nlpCommand.setProperty("text", text);
        nlpCommand.setReplyTimeout(10000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                VariousSensorsGui guiHook = (VariousSensorsGui) gui;
                Command reply = send(nlpCommand);

                if (reply != null) {
                    String executedCommand = reply.getProperty("result");

                    if (executedCommand != null) {
                        guiHook.updateDescription("Recognized command: " + executedCommand);
                    } else {
                        guiHook.updateDescription("No similar command exists");
                    }
                } else {
                    guiHook.updateDescription("Unreceived reply within given timeout (10 seconds)");
                }
            }
        }).start();
    }

    @Override
    protected void onRun() {
        //sends a fake sensor read event
        ProtocolRead event = new ProtocolRead(this, "test", "test");
        event.getPayload().addStatement("value",
                powered.toString());
        event.getPayload().addStatement("object.class", "Light");
        event.getPayload().addStatement("object.name", "Created by VariousSensors");
        //invert the value for the next round
        notifyEvent(event);

        if (powered) {
            powered = false;
        } else {
            powered = true;
        }

        //wait two seconds before sending another event
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

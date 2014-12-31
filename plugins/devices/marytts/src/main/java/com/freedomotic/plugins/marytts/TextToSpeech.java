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
package com.freedomotic.plugins.marytts;


import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.util.Info;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class TextToSpeech extends Protocol {

    private static final Logger LOG = Logger.getLogger(TextToSpeech.class.getName());
    

    public TextToSpeech() {
        super("Text to Speech", "/marytts/text-to-speech-manifest.xml");
    }

    @Override
    protected void onStart() {
        
    }

    @Override
    protected void onShowGui() {
        bindGuiToPlugin(new TextToSpeechGui(this));
    }

    
    

    public void say(String message) {
        try {
            new TextToSpeech.Speaker(message).start();
        } catch (Exception e) {
            LOG.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        String message = c.getProperty("say");
        say(message);
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    private class Speaker extends Thread {

        String message = "";

        private Speaker(String message) {
            this.message = message;
        }

        @Override
        public synchronized void run() {
            try {
            //    voice.speak(message);
            } catch (Exception e) {
            }
        }
    }
}

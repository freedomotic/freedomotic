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
package com.freedomotic.plugins.marytts;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.modules.synthesis.Voice;
import marytts.util.data.audio.AudioPlayer;

public class MaryTTS extends Protocol {

    private static final Logger LOG = Logger.getLogger(MaryTTS.class.getName());
    private MaryInterface marytts;
    private Voice defaultVoice;
    private String VOICE = configuration.getProperty("voice");

    public MaryTTS() {
        super("MaryTTS", "/marytts/marytts-manifest.xml");
    }

    @Override
    protected void onStart() throws PluginStartupException {
        try {
            marytts = new LocalMaryInterface();
            Locale currentLocale = Locale.getDefault();
            if (marytts.getAvailableLocales().contains(currentLocale)) {
                defaultVoice = Voice.getDefaultVoice(currentLocale);
            }
            if (defaultVoice == null) {
                // set the first available voice
                defaultVoice = Voice.getVoice(marytts.getAvailableVoices().iterator().next());
            }
        } catch (MaryConfigurationException ex) {
            throw new PluginStartupException("Plugin can't start for " + ex.getMessage());
        }
    }

    @Override
    protected void onShowGui() {
        bindGuiToPlugin(new MaryTTSGui(this));
    }

    public void say(String message) {
        try {
            new MaryTTS.Speaker(message).start();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Impossible to speak for ''{0}''", Freedomotic.getStackTraceInfo(e));
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

    private void getInfo() {

        LOG.log(Level.INFO, "Available voices: ''{0}''", marytts.getAvailableVoices());
        LOG.log(Level.INFO, "Available languages: ''{0}''", marytts.getAvailableLocales());
    }

    private class Speaker extends Thread {

        String message = "";

        private Speaker(String message) {
            this.message = message;
        }

        @Override
        public synchronized void run() {
            AudioInputStream audio = null;
            Voice voice = null;
            if (VOICE != "") {

                LOG.log(Level.INFO, "Voice ''{0}'' not found. Using default voice", VOICE);
                voice = defaultVoice;
            } else {
                voice = Voice.getVoice(VOICE);
            }
            try {
                marytts.setVoice(voice.getName());
                audio = marytts.generateAudio(message);
                AudioPlayer player = new AudioPlayer(audio);
                player.start();
                player.join();
            } catch (SynthesisException ex) {
                LOG.log(Level.SEVERE, "Error during audio generating for ''{0}''", ex.getLocalizedMessage());
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, "Error during speaking for ''{0}''", ex.getLocalizedMessage());
            } finally {
                try {
                    audio.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Error during audio closing for ''{0}''", ex.getLocalizedMessage());
                }
            }
        }
    }
}

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
import com.freedomotic.util.Info;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
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
    private Voice voice;
    private AudioInputStream audio;
    private String VOICE_JAR_FILE = configuration.getProperty("voice-jar-file");
    private String VOICE = configuration.getProperty("voice");

    public MaryTTS() {
        super("MaryTTS", "/marytts/marytts-manifest.xml");
    }

    @Override
    protected void onStart() throws PluginStartupException {
        try {
            //add voices folder to classpath
            addPath(Info.PATHS.PATH_DEVICES_FOLDER + System.getProperty("file.separator") + "marytts/lib/" + VOICE_JAR_FILE);
            System.setProperty("mary.base", Info.PATHS.PATH_DEVICES_FOLDER + System.getProperty("file.separator") + "marytts");
            marytts = new LocalMaryInterface();
            // print available voices and languages
            getInfo();
            // set default voice
            defaultVoice = Voice.getVoice("cmu-slt-hsmm");
        } catch (MaryConfigurationException ex) {
            throw new PluginStartupException("Plugin can't start for " + ex.getMessage());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Impossible to modify classpath for ''{0}''", Freedomotic.getStackTraceInfo(ex));
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
            if (VOICE == "" || !isAvailableVoice(VOICE)) {
                LOG.log(Level.INFO, "Voice ''{0}'' not found. Using default voice ", VOICE);
                voice = defaultVoice;
            } else {
                voice = Voice.getVoice(VOICE);
                LOG.log(Level.INFO, "Voice set to ''{0}''", voice.getName());
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

    //need to do add path to Classpath with reflection since the URLClassLoader.addURL(URL url) method is protected:
    private void addPath(String s) throws Exception {
        File f = new File(s);
        URI u = f.toURI();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{u.toURL()});
    }

    private boolean isAvailableVoice(String voice) {
        Set<String> availableVoices = marytts.getAvailableVoices();
        return availableVoices.contains(voice);
    }
}

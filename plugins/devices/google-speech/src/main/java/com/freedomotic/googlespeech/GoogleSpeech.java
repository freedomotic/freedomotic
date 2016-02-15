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
package com.freedomotic.googlespeech;

import com.darkprograms.speech.gui.MainGUI;
import com.darkprograms.speech.synthesiser.Synthesiser;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.util.Info;
import java.io.*;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javazoom.jl.player.Player;

public class GoogleSpeech
        extends Protocol {

    private static final Logger LOG = Logger.getLogger(GoogleSpeech.class.getName());
    public String LANGUAGE_CODE = configuration.getStringProperty("language-code", "en-US");
    public int RECORD_TIME = configuration.getIntProperty("record-time", 3000);
    final int POLLING_WAIT;

    public GoogleSpeech() {
        //every plugin needs a name and a manifest XML file
        super("Google Speech", "/google-speech/google-speech-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000);
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
    }

    @Override
    protected void onShowGui() {
        /**
         * uncomment the line below to add a GUI to this plugin the GUI can be
         * started with a right-click on plugin list on the desktop frontend
         * (com.freedomotic.jfrontend plugin)
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            MainGUI pluginGUI = new MainGUI(this);
            pluginGUI.setVisible(true);
            pluginGUI.setLocationRelativeTo(null);
            bindGuiToPlugin(pluginGUI);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onHideGui() {
        //implement here what to do when the this plugin GUI is closed
        //for example you can change the plugin description
        setDescription("My GUI is now hidden");
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        //new SpeechDetectionTest().start();
        LOG.info("Google Speech plugin started");
    }

    @Override
    protected void onStop() {
        LOG.info("Google Speech stopped ");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        String message = c.getProperty("say");
        //new Thread(new Speaker(message)).start();
        if (c != null) {
            say(message);
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

    public void say(String message) {
        try {
            new GoogleSpeech.Speaker(message).start();
        } catch (Exception e) {
            LOG.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private class Speaker extends Thread {

        String message = "";

        private Speaker(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Synthesiser synthesiser = new Synthesiser(LANGUAGE_CODE);
            try {
                InputStream is = synthesiser.getMP3Data(message);
                //InputStreamToMP3File(is);
                Player player = new Player(is);
                player.play();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    void InputStreamToMP3File(InputStream inputStream) {

        try {
            File f = new File(Info.PATHS.PATH_DATA_FOLDER + "google.mp3");
            System.out.println(f.getAbsolutePath());
            OutputStream out = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
            System.out.println("File is created");
        } catch (IOException e) {
        }
    }
}

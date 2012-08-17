/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.tts;


import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.util.Utilities;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;

/**
 *
 * @author Enrico
 */
public class TextToSpeech extends Protocol {

    private com.sun.speech.freetts.Voice voice;

    public TextToSpeech() {
        super("Text to Speech", "/it.freedomotic.freetts/text-to-speech.xml");
    }

    @Override
    protected void onStart() {
        loadVoice();
    }
    

    @Override
    protected void onShowGui() {
        bindGuiToPlugin(new TextToSpeechGui(this));
    }

    public void loadVoice() {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            //System.setProperty("mbrola.base", "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory");
            voice = VoiceManager.getInstance().getVoice(
                    Utilities.getProperty("voice16kName", "kevin16"));
            VoiceManager voiceManager = VoiceManager.getInstance();
            Voice[] voices = voiceManager.getVoices();
            for (int i = 0; i < voices.length; i++) {
                System.out.println("Found TTS voice '" + voices[i].getName() + "' (" + voices[i].getDomain() + " domain)");
            }
            voice.allocate();
            voice.setPitch(90);
            voice.setRate(150);
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    public void say(String message) {
        try {
            new Speaker(message).start();
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
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
        public void run() {
            try {
                voice.speak(message);
            } catch (Exception e) {
            }
        }
    }
}

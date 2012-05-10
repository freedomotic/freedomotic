/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.exceptions.UnableToExecuteException;

import java.io.IOException;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.util.Utilities;
import it.freedomotic.api.Tool;
import it.freedomotic.plugins.tts.TextToSpeechGui;
import it.freedomotic.reactions.Command;

/**
 *
 * @author Enrico
 */
public class TextToSpeech extends Tool {

    private com.sun.speech.freetts.Voice voice;

    public TextToSpeech() {
        super("Text to Speech", "/it.nicoletti.media/text-to-speech.xml");
        loadVoice();
        gui = new TextToSpeechGui(this);
        start();
    }

    public void loadVoice() {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            System.setProperty("mbrola.base", "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory");
            voice = VoiceManager.getInstance().getVoice(
                    Utilities.getProperty("voice16kName", "kevin16"));
            VoiceManager voiceManager = VoiceManager.getInstance();
            Voice[] voices = voiceManager.getVoices();
            for (int i = 0; i < voices.length; i++) {
                System.out.println("    " + voices[i].getName() + " (" + voices[i].getDomain() + " domain)");
            }
            voice.allocate();
//            voice.setPitch(90);
//            voice.setRate(150);
        } catch (Exception e) {
            System.err.println("Error while initializing text to speech engine\n" + e);
        }
    }

    public void say(String message) {
//        try {
        try {
            new Speaker(message).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
//            ProcessBuilder pb = new ProcessBuilder("espeak", message);
//            Process p = pb.start();

//        } catch (IOException ex) {
//            System.err.println("Unable to text to speech the statement '" + message + "'");
//        }
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

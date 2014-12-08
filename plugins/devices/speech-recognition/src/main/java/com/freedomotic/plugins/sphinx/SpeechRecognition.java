/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */
package com.freedomotic.plugins.sphinx;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.util.Info;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple HelloWorld demo showing a simple speech application built using
 * SpeechRecognition-4. This application uses the SpeechRecognition-4
 * endpointer, which automatically segments incoming audio into utterances and
 * silences.
 *
 * More info at http://cmusphinx.sourceforge.net/wiki/tutorialsphinx4
 *
 */
public class SpeechRecognition extends Protocol {

    private LiveSpeechRecognizer recognizer;

    public SpeechRecognition() {
        super("Speech Recognition", "/speech-recognition/speech-recognition-manifest.xml");
    }

    @Override
    public void onStart() throws PluginStartupException {
        this.setPollingWait(-1); // Disable onRun loop execution
        Configuration sphinxConfiguration = new Configuration();
        // Load model from the jar
        sphinxConfiguration
                .setAcousticModelPath("resource:/edu/cmu/sphinx/models/acoustic/wsj");
        sphinxConfiguration
                .setDictionaryPath("resource:/edu/cmu/sphinx/models/acoustic/wsj/dict/cmudict.0.6d");
        sphinxConfiguration
                .setLanguageModelPath("resource:/edu/cmu/sphinx/models/language/en-us.lm.dmp");

        try {
            recognizer = new LiveSpeechRecognizer(sphinxConfiguration);
            // Start recognition process pruning previously cached data.
            recognizer.startRecognition(true);
        } catch (IOException ex) {
            throw new PluginStartupException("Cannot listen from microphone", ex);
        }

        setDescription("Ready, try saying 'turn on kitchen light'");
    }

    @Override
    public void onStop() {
        if (recognizer != null) {
            recognizer.stopRecognition();
        }
    }

    @Override
    protected void onRun() {
        while (true) {
            SpeechResult result = recognizer.getResult();
            if (result != null) {
                String resultText = result.getHypothesis();
                setDescription("You said: " + resultText);
                if (!resultText.trim().isEmpty()) {
                    Command nlpCommand = new Command();
                    nlpCommand.setName("Natuaral language command");
                    nlpCommand.setDescription("A free-form text command to be interpreded by an NLP module");
                    nlpCommand.setProperty("text", resultText);
                    nlpCommand.setReplyTimeout(2000);
                    Command reply = notifyCommand(nlpCommand);
                    if (reply != null) {
                        setDescription("No valid command similar to '" + resultText + "'");
                    }
                }
            } else {
                setDescription("I can't undestand what you said");
            }
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
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

    private String setGrammar() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#JSGF V1.0;\ngrammar hello;\npublic <command> = ( ");
        for (Iterator it = configuration.getTuples().getTuple(0).entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            buffer.append(entry.getKey() + " | ");
        }
        buffer.append(");");
        Writer output = null;
        System.out.println(buffer.toString());
        File file = new File(Info.PATHS.PATH_DATA_FOLDER + "commands.gram2");
        System.out.println(file.getAbsolutePath());
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(buffer.toString());
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(SpeechRecognition.class.getName()).log(Level.SEVERE, null, ex);
        }

        return buffer.toString();
    }
}

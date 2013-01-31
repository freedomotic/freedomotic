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
package it.freedomotic.plugins.sphinx;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.SpeechEvent;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.Info;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple HelloWorld demo showing a simple speech application built using
 * Sphinx-4. This application uses the Sphinx-4 endpointer, which automatically
 * segments incoming audio into utterances and silences.
 */
public class Sphinx extends Protocol {

    private Recognizer recognizer;

    public Sphinx() {
        super("Speech Recognition", "/it.nicoletti.sphinx4/speech-recognition-manifest.xml");
    }

    @Override
    public void onStart() {
        //setGrammar();
        ConfigurationManager cm;
        cm = new ConfigurationManager(Sphinx.class.getResource("sphinx.config.xml"));
        try {
            recognizer = (Recognizer) cm.lookup("recognizer");
            recognizer.allocate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // start the microphone or exit if the programm if this is not possible
        Microphone microphone = (Microphone) cm.lookup("microphone");
        if (microphone == null || !microphone.startRecording()) {
            System.out.println("Cannot start microphone. Check if connected.");
            recognizer.deallocate();
        }
        System.out.println("Say: (turn on | turn off) ( kitchen light | livingroom light | light one | light two )");
    }

    @Override
    public void onStop() {
        try {
            recognizer.deallocate();
        } catch (IllegalStateException illegalStateException) {
        }
    }
    
    @Override
    protected void onRun() {
        while (true) {
            Result result = recognizer.recognize();
            if (result != null) {
                String resultText = result.getBestFinalResultNoFiller();
                setDescription("You said: " + resultText);
                if (!resultText.trim().isEmpty()) {
                    SpeechEvent event = new SpeechEvent(this, resultText);
                    Freedomotic.sendEvent(event);
                }
            } else {
                setDescription("I can't hear what you said");
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
        File file = new File(Info.PATH_DATA_FOLDER + "commands.gram2");
        System.out.println(file.getAbsolutePath());
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(buffer.toString());
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(Sphinx.class.getName()).log(Level.SEVERE, null, ex);
        }

        return buffer.toString();
    }
}

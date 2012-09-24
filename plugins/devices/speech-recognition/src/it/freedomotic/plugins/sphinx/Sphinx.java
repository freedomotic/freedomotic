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
import it.freedomotic.reactions.CommandPersistence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
        ConfigurationManager cm;

        cm = new ConfigurationManager(Sphinx.class.getResource("sphinx.config.xml"));


        recognizer = (Recognizer) cm.lookup("recognizer");
        recognizer.allocate();

        // start the microphone or exit if the programm if this is not possible
        Microphone microphone = (Microphone) cm.lookup("microphone");
        if (!microphone.startRecording()) {
            System.out.println("Cannot start microphone.");
            recognizer.deallocate();
        }

        System.out.println("Say: (turn on | turn off | switch ) ( light one | light two | light ten | light seven )");
        // loop the recognition until the programm exits.
        getGrammar();
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
//                if (resultText.trim().isEmpty()){
//                    resultText="";
//                }
                System.out.println("You said: " + resultText);
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

    private String getGrammar() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#JSGF V1.0;\ngrammar hello;\npublic <command> = ( ");
        for (Command command : CommandPersistence.getUserCommands()) {
            buffer.append(command.getName().replace("-", " ") + " | ");
        }
        buffer.append(");");
        Writer output = null;
        System.out.println(buffer.toString());
        File file = new File("commands.gram2");
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

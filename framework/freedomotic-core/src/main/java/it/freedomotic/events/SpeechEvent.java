/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.core.NaturalLanguageProcessor;
import it.freedomotic.core.Resolver;

import it.freedomotic.reactions.Command;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Channel <b>app.event.sensor.speech</b> informs that a speech recognition
 * plugin have recognized a string of text. This event makes freedomotic execute
 * the most similar command.
 *
 * @author enrico
 */
public class SpeechEvent
        extends EventTemplate {

    private static final long serialVersionUID = -3856465031783898046L;

	public SpeechEvent(Object source, String phrase) {
        this.setSender(source);

        NaturalLanguageProcessor nlp = new NaturalLanguageProcessor();
        List<NaturalLanguageProcessor.Rank> mostSimilar = nlp.getMostSimilarCommand(phrase, 10);

        if (!mostSimilar.isEmpty()) {
            Command c = mostSimilar.get(0).getCommand();
            System.out.println("execute speech command: " + c.getName());

            Resolver resolver = new Resolver();
            resolver.addContext("event.",
                    getPayload());

            try {
                Command resolvedCommand = resolver.resolve(c);
                Freedomotic.sendCommand(resolvedCommand);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(SpeechEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("no available commands similar to: " + phrase);
        }
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.speech";
    }
}

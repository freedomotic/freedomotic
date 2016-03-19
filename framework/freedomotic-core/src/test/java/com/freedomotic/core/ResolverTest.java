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
package com.freedomotic.core;

import com.freedomotic.events.GenericEvent;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Trigger;
import com.thoughtworks.xstream.XStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico Nicoletti
 */
public class ResolverTest {

    /**
     *
     */
    public ResolverTest() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     *
     */
    @Test
    public void testResolve_Command() {
        System.out.println("Commands resolving a set of references mixed with text like 'temperature is @event.temperature'");
        Command c = new Command();
        c.setName("say something using TTS");
        c.setProperty("zero", "@event.temperature");
        c.setProperty("one", "temperature is @event.temperature.");
        c.setProperty("two", "temperature is @event.temperature#celsius degree.");
        c.setProperty("three", "temperature in @event.zone is @event.temperature.");
        c.setProperty("four", "temperature in @event.zone is @event.temperature celsius degree.");
        c.setProperty("five", "temperature in @event.zone is @event.temperature celsius degree. @event.zone# is hot because temperature is +@event.temperature°C.");
        c.setProperty("six", "temperature in @event.zone is managed by object @event.object.name#.");
        //testing scripting
        c.setProperty("seven", "= seven=\"Current temperature is @event.temperature celsius degrees. In fahrenheit is \" + Math.floor(((@event.temperature+40)*1.8)-40) + \" degrees.\";");
        c.setProperty("eight", "= eight=10+5;"); //this always returns a double
        c.setProperty("nine", "= nine=Math.floor(10+5).toString();"); //print the number as is to avoid conversion to double
        //c.setProperty("ten", "= if (@event.temperature<= 20) ten=2; else what=@event.temperature/10;");
        GenericEvent event = new GenericEvent(this);
        event.addProperty("zone", "Kitchen");
        event.addProperty("temperature", "25");
        event.addProperty("object.name", "Indoor Thermometer");
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        Command result = new Command();
        try {
            result = resolver.resolve(c);
        } catch (CloneNotSupportedException ex) {
            Assert.fail(ex.getMessage());
        } catch (VariableResolutionException ex) {
            Assert.fail(ex.getMessage());
        }
        assertEquals("25", result.getProperty("zero"));
        assertEquals("temperature is 25.", result.getProperty("one"));
        assertEquals("temperature is 25celsius degree.", result.getProperty("two"));
        assertEquals("temperature in Kitchen is 25.", result.getProperty("three"));
        assertEquals("temperature in Kitchen is 25 celsius degree.", result.getProperty("four"));
        assertEquals("temperature in Kitchen is 25 celsius degree. Kitchen is hot because temperature is +25°C.", result.getProperty("five"));
        assertEquals("temperature in Kitchen is managed by object Indoor Thermometer.", result.getProperty("six"));
        assertEquals("Current temperature is 25 celsius degrees. In fahrenheit is 77 degrees.", result.getProperty("seven"));
        //assertEquals("15.0", result.getProperty("eight"));
        //assertEquals("15", result.getProperty("nine"));        
        //assertEquals("15", result.getProperty("ten"));
    }

    /**
     *
     */
    @Test
    public void testResolve_Trigger() {
        System.out.println("Triggers resolving a set of references mixed with text like 'temperature is @event.temperature'");
        Trigger c = new Trigger();
        c.setName("say something using TTS");
        c.getPayload().addStatement("zero", "@event.temperature");
        c.getPayload().addStatement("one", "temperature is @event.temperature.");
        c.getPayload().addStatement("two", "temperature is @event.temperature#celsius degree.");
        c.getPayload().addStatement("three", "temperature in @event.zone is @event.temperature.");
        c.getPayload().addStatement("four", "temperature in @event.zone is @event.temperature celsius degree.");
        c.getPayload().addStatement("five", "temperature in @event.zone is @event.temperature celsius degree. @event.zone# is hot because temperature is +@event.temperature°C.");
        c.getPayload().addStatement("six", "temperature in @event.zone is managed by object @event.object.name#.");
        c.getPayload().addStatement("seven", "= seven=\"Current temperature is @event.temperature celsius degrees. In fahrenheit is \" + Math.floor(((@event.temperature+40)*1.8)-40) + \" degrees.\";");
        c.getPayload().addStatement("eight", "= eight=10+5;"); //this always returns a double
        c.getPayload().addStatement("nine", "= nine=Math.floor(10+5).toString();"); //print the number as is to avoid conversion to double
        c.getPayload().addStatement("SET", "behaviorValue", "EQUALS", "= if (@event.temperature > 20) behaviorValue=\"it's hot\"; else behaviorValue=\"it's cold\";");
        GenericEvent event = new GenericEvent(this);
        event.addProperty("zone", "Kitchen");
        event.addProperty("temperature", "25");
        event.addProperty("object.name", "Indoor Thermometer");
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        Trigger result = null;
        try {
            result = resolver.resolve(c);
        } catch (VariableResolutionException ex) {
            Assert.fail(ex.getMessage());
        }
        XStream x = new XStream();
        assertEquals("25", result.getPayload().getStatements("zero").get(0).getValue());
        assertEquals("temperature is 25.", result.getPayload().getStatements("one").get(0).getValue());
        assertEquals("temperature is 25celsius degree.", result.getPayload().getStatements("two").get(0).getValue());
        assertEquals("temperature in Kitchen is 25.", result.getPayload().getStatements("three").get(0).getValue());
        assertEquals("temperature in Kitchen is 25 celsius degree.", result.getPayload().getStatements("four").get(0).getValue());
        assertEquals("temperature in Kitchen is 25 celsius degree. Kitchen is hot because temperature is +25°C.", result.getPayload().getStatements("five").get(0).getValue());
        assertEquals("temperature in Kitchen is managed by object Indoor Thermometer.", result.getPayload().getStatements("six").get(0).getValue());
        //testing scripting
        assertEquals("Current temperature is 25 celsius degrees. In fahrenheit is 77 degrees.", result.getPayload().getStatements("seven").get(0).getValue());
        //assertEquals("15.0", result.getPayload().getStatements("eight").get(0).getValue());
        //assertEquals("15", result.getPayload().getStatements("nine").get(0).getValue());
        assertEquals("it's hot", result.getPayload().getStatements("behaviorValue").get(0).getValue());
    }
}

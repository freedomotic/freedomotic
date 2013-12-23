/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package it.freedomotic.reactions;

import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author enrico
 */
public class PayloadTest {

    public PayloadTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addStatement method, of class Payload.
     */
    @Test
    public void testAddStatement() {
        LOG.info("Try to add two copies of the same statement in a payload."
                + " Expected only one statement is inserted and no null values allowed.");
        //build the statement values
        String logical = Statement.AND;
        String attribute = "param";
        String operand = Statement.EQUALS;
        String value = "value";
        Payload payload = new Payload();
        int preSize = payload.size();
        payload.addStatement(logical, attribute, operand, value);
        payload.addStatement(logical, attribute, operand, value);
        int postSize = payload.size();
        //only a row is added
        assertEquals(preSize + 1, postSize);
        assertEquals(1, postSize);
        //attribute and value must be exacly as in function attributes
        assertEquals(payload.getStatements(attribute).get(0).getAttribute(), attribute);
        assertEquals(payload.getStatements(attribute).get(0).getValue(), value);
        //no null references
        Assert.assertNotNull(payload.getStatements(attribute).get(0));
        Assert.assertNotNull(payload.getStatements(attribute).get(0));
    }

    /**
     * Test of equals method, of class Payload.
     */
    @Test
    public void testEquals() {
        //contruct the event
        Payload event = new Payload();
        event.addStatement(Statement.AND, "number", Statement.EQUALS, "1");
        event.addStatement(Statement.AND, "text", Statement.EQUALS, "abc");

        //contruct the trigger
        Payload trigger = new Payload();
        trigger.addStatement(Statement.AND, "number", Statement.EQUALS, "1");
        trigger.addStatement(Statement.AND, "number", Statement.LESS_THAN, "2");
        trigger.addStatement(Statement.AND, "number", Statement.GREATER_THAN, "0");
        trigger.addStatement(Statement.AND, "number", Statement.LESS_EQUAL_THAN, "1");
        trigger.addStatement(Statement.AND, "number", Statement.EQUALS, Statement.ANY);

        boolean expResult = true;
        //compare trigger with events
        boolean result = trigger.equals(event);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Payload.
     */
    @Test
    public void testTriggerHasUnexistentAttribute() {
        LOG.info("Expected true if trigger has a statement with an attibute which "
                + "doesn't exists in the related event payload.");
        //contruct the event
        Payload event = new Payload();
        event.addStatement(Statement.AND, "number", Statement.EQUALS, "1");
        event.addStatement(Statement.AND, "text", Statement.EQUALS, "abc");

        //contruct the trigger
        Payload trigger = new Payload();
        trigger.addStatement(Statement.AND, "unexistent", Statement.EQUALS, "1");

        //compare trigger with events
        boolean result = trigger.equals(event);
        assertEquals(true, result);
    }

    /**
     * Test of findAttribute method, of class Payload.
     */
    @Test
    public void testFindAttribute() {
        LOG.info("Produce a list of statements searching by statement attribute name");
        Payload payload = new Payload();
        payload.addStatement("no", "value1");
        payload.addStatement("yes", "value2");
        payload.addStatement("yes", "value3");
        payload.addStatement("no", "value4");
        payload.addStatement("yes", "value5");
        payload.addStatement("yes", "value6");

        assertEquals(4, payload.getStatements("yes").size());
    }
    private static final Logger LOG = Logger.getLogger(PayloadTest.class.getName());
}

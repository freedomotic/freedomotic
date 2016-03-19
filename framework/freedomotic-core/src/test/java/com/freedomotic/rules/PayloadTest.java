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
package com.freedomotic.rules;

import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;

/**
 *
 * @author Enrico Nicoletti
 */
public class PayloadTest {

    private static final Logger LOG = LoggerFactory.getLogger(PayloadTest.class.getName());

    /**
     *
     */
    public PayloadTest() {
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
        event.addStatement(Statement.AND, "testRegex", Statement.EQUALS, "EnvObject.ElectricDevice.Light");

        //contruct the trigger
        Payload trigger = new Payload();
        trigger.addStatement(Statement.AND, "number", Statement.EQUALS, "1");
        trigger.addStatement(Statement.AND, "number", Statement.EQUALS, Statement.ANY);
        //payload eveluation is after resolve step so the trigger has elready the event.* properties in it
        trigger.addStatement(Statement.AND, "event.number", Statement.EQUALS, "123");
        trigger.addStatement("SET", "defineANewProperty", Statement.EQUALS, "123");

        trigger.addStatement(Statement.AND, "testRegex", Statement.REGEX, "^EnvObject.ElectricDevice\\.(.*)");

        boolean expResult = true;
        //compare trigger with events
        boolean result = trigger.equals(event);
        assertEquals(expResult, result);
    }

    @Test
    public void testGreaterLess() {
        //contruct the event
        Payload event = new Payload();
        event.addStatement(Statement.AND, "number", Statement.EQUALS, "1");
        event.addStatement(Statement.AND, "text", Statement.EQUALS, "abc");

        //contruct the trigger
        Payload trigger = new Payload();
        trigger.addStatement(Statement.AND, "number", Statement.LESS_THAN, "2");
        trigger.addStatement(Statement.AND, "number", Statement.GREATER_THAN, "0");
        trigger.addStatement(Statement.AND, "number", Statement.LESS_EQUAL_THAN, "1");

        //compare trigger with events
        boolean result = trigger.equals(event);
        Assert.assertTrue("1 is greater than 0", result);
    }

    @Test
    public void testBetweenTime() {
        LOG.info("Check if a give hour is inside a given hous interval");
        //contruct the event
        Payload event = new Payload();
        event.addStatement(Statement.AND, "morning", Statement.EQUALS, "10:00:00");
        event.addStatement(Statement.AND, "evening", Statement.EQUALS, "23:00:00");
        event.addStatement(Statement.AND, "midnight1", Statement.EQUALS, "00:00:00");
        event.addStatement(Statement.AND, "midnight2", Statement.EQUALS, "24:00:00");

        //contruct the trigger
        Payload trigger = new Payload();
        trigger.addStatement(Statement.AND, "morning", Statement.BETWEEN_TIME, "9:00:0-11:00:00");
        trigger.addStatement(Statement.AND, "evening", Statement.BETWEEN_TIME, "22:00:00-8:00:00");
        trigger.addStatement(Statement.AND, "midnight1", Statement.BETWEEN_TIME, "23:30:00-0:30:00");
        trigger.addStatement(Statement.AND, "midnight2", Statement.BETWEEN_TIME, "23:30:00-0:30:00");

        //compare trigger with events
        boolean result = trigger.equals(event);
        Assert.assertTrue(result);
    }

    /**
     * What if the trigger has a property which is not present in the event?
     * Expected behavior is the trigger is not consistent with the event
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
        trigger.addStatement(Statement.AND, "unexistentPropertyOne", Statement.REGEX, "*");
        trigger.addStatement(Statement.AND, "unexistentPropertyTwo", Statement.EQUALS, "1");
        //payload eveluation is after resolve step so the trigger has elready the event.* properties in it
        trigger.addStatement(Statement.AND, "event.number", Statement.EQUALS, "123");
        trigger.addStatement("SET", "defineANewProperty", Statement.EQUALS, "123");

        //compare trigger with events
        boolean result = trigger.equals(event);
        assertEquals(false, result);
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
}

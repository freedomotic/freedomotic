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
package it.freedomotic.reactions;

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
    public void testAddStatement_4args() {
        System.out.println("Try to add two copies of the same statement in a payload."
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
     * Test of addStatement method, of class Payload.
     */
    @Test
    public void testAddStatement_String_String() {
//        Payload payload = new Payload();
//        int before = payload.getStatements().size();
//        payload.addStatement("anAttribute", "aValue");
//        payload.addStatement("", "");
//        //payload.addStatement(null, null);
//        int after = payload.getStatements().size();
//        //only a row is added
//        assertEquals(before + 1, after);
//        //attribute and value must be exacly as in function attributes
//        assertEquals("anAttribute", payload.getStatements().get(0).getAttribute());
//        assertEquals("aValue", payload.getStatements().get(0).getValue());
//        //no empty rows or null references
//        assert (payload.getStatements().get(0).getAttribute() == null ? "" != null : payload.getStatements().get(0).getAttribute().length() != 0);
//        assert (payload.getStatements().get(0).getValue() == null ? "" != null : payload.getStatements().get(0).getValue().length() != 0);
//        assert (payload.getStatements().get(0)).logical.equalsIgnoreCase(Statement.AND);
//        assert (payload.getStatements().get(0)).operand.equalsIgnoreCase(Statement.EQUALS);
    }

    /**
     * Test of addStatement method, of class Payload.
     */
    @Test
    public void testAddStatement_String_int() {
//        String attribute = "anAttribute";
//        int value = 0;
//        Payload instance = new Payload();
//        int before = instance.getStatements().size();
//        instance.addStatement(attribute, value);
//        int after = instance.getStatements().size();
//        //only a row is added
//        assertEquals(before + 1, after);
//        //attribute and value must be exacly as in function attributes
//        assertEquals(attribute, instance.getStatements().get(0).getAttribute());
//        assertEquals(new Integer(value).toString().trim(), instance.getStatements().get(0).getValue());
//        //no empty rows or null references
//        assert (instance.getStatements().get(0).getAttribute() == null ? "" != null : instance.getStatements().get(0).getAttribute().length() != 0);
//        assert (instance.getStatements().get(0).getValue() == null ? "" != null : instance.getStatements().get(0).getValue().length() != 0);
//        assert (instance.getStatements().get(0)).logical.equalsIgnoreCase(Statement.AND);
//        assert (instance.getStatements().get(0)).operand.equalsIgnoreCase(Statement.EQUALS);
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
        trigger.addStatement(Statement.AND, "number", Statement.LESS_THEN, "2");
        trigger.addStatement(Statement.AND, "number", Statement.GREATER_THEN, "0");
        trigger.addStatement(Statement.AND, "number", Statement.EQUALS, Statement.ANY);

//        trigger.addStatement(Statement.AND, "number", Statement.LESS_THEN, Statement.ANY);
//        trigger.addStatement(Statement.AND, "number", Statement.GREATER_THEN, Statement.ANY);
//        trigger.addStatement(Statement.AND, "text", Statement.LESS_THEN, "baa");
//        trigger.addStatement(Statement.AND, "text", Statement.GREATER_THEN, "aaa");

//        trigger.addStatement(Statement.AND, "text", Statement.LESS_THEN, Statement.ANY);
//        trigger.addStatement(Statement.AND, "text", Statement.GREATER_THEN, Statement.ANY);
        boolean expResult = true;
        //compare trigger with events
        boolean result = trigger.equals(event);
        assertEquals(expResult, result);
    }

    /**
     * Test of findAttribute method, of class Payload.
     */
    @Test
    public void testFindAttribute() {
        Payload payload = new Payload();
        payload.addStatement("no", "no1");
        payload.addStatement("yes", "yes1");
        payload.addStatement("yes", "yes2");
        payload.addStatement("no", "no2");
        payload.addStatement("yes", "yes3");
        payload.addStatement("yes", "yes4");        

        int found=0;
        for (Statement statement : payload.getStatements("yes")) {
            found++;
        }
        assertEquals("Found all 4 statements with key='yes'", true, found==4);
    }
}

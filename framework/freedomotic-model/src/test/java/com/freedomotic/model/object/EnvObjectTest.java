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
package com.freedomotic.model.object;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico Nicoletti
 */
public class EnvObjectTest {

    /**
     *
     */
    public EnvObjectTest() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass()
            throws Exception {
    }

    /**
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass()
            throws Exception {
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

//    /**
//     * Test of getActions method, of class EnvObject.
//     */
//    @Test
//    public void testGetActions() {
//        System.out.println("getActions");
//        EnvObject instance = new EnvObject();
//        Properties expResult = null;
//        Properties result = instance.getActions();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTriggers method, of class EnvObject.
//     */
//    @Test
//    public void testGetTriggers() {
//        System.out.println("getTriggers");
//        EnvObject instance = new EnvObject();
//        Properties expResult = null;
//        Properties result = instance.getTriggers();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setName method, of class EnvObject.
//     */
//    @Test
//    public void testSetName() {
//        System.out.println("setName");
//        String name = "";
//        EnvObject instance = new EnvObject();
//        instance.setName(name);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getName method, of class EnvObject.
//     */
//    @Test
//    public void testGetName() {
//        System.out.println("getName");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getName();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getHierarchy method, of class EnvObject.
//     */
//    @Test
//    public void testGetHierarchy() {
//        System.out.println("getHierarchy");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getHierarchy();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setHierarchy method, of class EnvObject.
//     */
//    @Test
//    public void testSetHierarchy() {
//        System.out.println("setHierarchy");
//        String hierarchy = "";
//        EnvObject instance = new EnvObject();
//        instance.setHierarchy(hierarchy);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCurrentRepresentation method, of class EnvObject.
//     */
//    @Test
//    public void testSetCurrentRepresentation() {
//        System.out.println("setCurrentRepresentation");
//        int index = 0;
//        EnvObject instance = new EnvObject();
//        instance.setCurrentRepresentation(index);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCurrentRepresentation method, of class EnvObject.
//     */
//    @Test
//    public void testGetCurrentRepresentation() {
//        System.out.println("getCurrentRepresentation");
//        EnvObject instance = new EnvObject();
//        Representation expResult = null;
//        Representation result = instance.getCurrentRepresentation();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCurrentRepresentationIndex method, of class EnvObject.
//     */
//    @Test
//    public void testGetCurrentRepresentationIndex() {
//        System.out.println("getCurrentRepresentationIndex");
//        EnvObject instance = new EnvObject();
//        int expResult = 0;
//        int result = instance.getCurrentRepresentationIndex();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getRepresentations method, of class EnvObject.
//     */
//    @Test
//    public void testGetRepresentations() {
//        System.out.println("getRepresentations");
//        EnvObject instance = new EnvObject();
//        ArrayList expResult = null;
//        ArrayList result = instance.getRepresentations();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProtocol method, of class EnvObject.
//     */
//    @Test
//    public void testGetProtocol() {
//        System.out.println("getProtocol");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getProtocol();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setProtocol method, of class EnvObject.
//     */
//    @Test
//    public void testSetProtocol() {
//        System.out.println("setProtocol");
//        String protocol = "";
//        EnvObject instance = new EnvObject();
//        instance.setProtocol(protocol);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getActiveBehaviors method, of class EnvObject.
//     */
//    @Test
//    public void testGetActiveBehaviors() {
//        System.out.println("getActiveBehaviors");
//        EnvObject instance = new EnvObject();
//        ArrayList expResult = null;
//        ArrayList result = instance.getActiveBehaviors();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBehaviors method, of class EnvObject.
//     */
//    @Test
//    public void testGetBehaviors() {
//        System.out.println("getBehaviors");
//        EnvObject instance = new EnvObject();
//        ArrayList expResult = null;
//        ArrayList result = instance.getBehaviors();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBehavior method, of class EnvObject.
//     */
//    @Test
//    public void testGetBehavior() {
//        System.out.println("getBehavior");
//        String behavior = "";
//        EnvObject instance = new EnvObject();
//        Behavior expResult = null;
//        Behavior result = instance.getBehavior(behavior);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setActAs method, of class EnvObject.
//     */
//    @Test
//    public void testSetActAs() {
//        System.out.println("setActAs");
//        String actAs = "";
//        EnvObject instance = new EnvObject();
//        instance.setActAs(actAs);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getActAs method, of class EnvObject.
//     */
//    @Test
//    public void testGetActAs() {
//        System.out.println("getActAs");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getActAs();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDescription method, of class EnvObject.
//     */
//    @Test
//    public void testGetDescription() {
//        System.out.println("getDescription");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getDescription();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDescription method, of class EnvObject.
//     */
//    @Test
//    public void testSetDescription() {
//        System.out.println("setDescription");
//        String desc = "";
//        EnvObject instance = new EnvObject();
//        instance.setDescription(desc);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setType method, of class EnvObject.
//     */
//    @Test
//    public void testSetType() {
//        System.out.println("setType");
//        String type = "";
//        EnvObject instance = new EnvObject();
//        instance.setType(type);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getType method, of class EnvObject.
//     */
//    @Test
//    public void testGetType() {
//        System.out.println("getType");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getType();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPhisicalAddress method, of class EnvObject.
//     */
//    @Test
//    public void testGetPhisicalAddress() {
//        System.out.println("getPhisicalAddress");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getPhisicalAddress();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setPhisicalAddress method, of class EnvObject.
//     */
//    @Test
//    public void testSetPhisicalAddress() {
//        System.out.println("setPhisicalAddress");
//        String address = "";
//        EnvObject instance = new EnvObject();
//        instance.setPhisicalAddress(address);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getShape method, of class EnvObject.
//     */
//    @Test
//    public void testGetShape() {
//        System.out.println("getShape");
//        EnvObject instance = new EnvObject();
//        FreedomShape expResult = null;
//        FreedomShape result = instance.getShape();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExposedProperties method, of class EnvObject.
//     */
//    @Test
//    public void testGetExposedProperties() {
//        System.out.println("getExposedProperties");
//        EnvObject instance = new EnvObject();
//        HashMap expResult = null;
//        HashMap result = instance.getExposedProperties();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSimpleType method, of class EnvObject.
//     */
//    @Test
//    public void testGetSimpleType() {
//        System.out.println("getSimpleType");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.getSimpleType();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of equals method, of class EnvObject.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        EnvObject instanceA = new EnvObject();
        EnvObject instanceB = new EnvObject();
        System.out.println("Different object names and different address, expected false");
        instanceA.setName("instanceA");
        instanceA.setPhisicalAddress("addressA");
        instanceA.setProtocol("protocolA");
        instanceB.setName("instanceB");
        instanceB.setPhisicalAddress("addressB");
        instanceB.setProtocol("protocolB");

        boolean expResult = false;
        boolean result = instanceA.equals(instanceB);
        assertEquals(expResult, result);
        //same name test: expected they are equal
        System.out.println("Same object names and different address, expected true");
        instanceA.setName("instanceA");
        instanceA.setPhisicalAddress("addressA");
        instanceA.setProtocol("protocolA");
        instanceB.setName("instanceA");
        instanceB.setPhisicalAddress("addressB");
        instanceB.setProtocol("protocolB");
        expResult = true;
        result = instanceA.equals(instanceB);
        assertEquals(expResult, result);
        //same name diferent case, they must be equal
        System.out.println("Same object names in differenc cases and different address, expected true");
        instanceA.setName("INSTANCEA");
        instanceA.setPhisicalAddress("addressA");
        instanceA.setProtocol("protocolA");
        instanceB.setName("instanceA");
        instanceB.setPhisicalAddress("addressB");
        instanceB.setProtocol("protocolB");
        expResult = true;
        result = instanceA.equals(instanceB);
        assertEquals(expResult, result);
        //different name test, same address&&protocol
        //expect they are equals despite the different name
        System.out.println("Different object names and same address, expected true");
        instanceA.setName("instanceA");
        instanceA.setPhisicalAddress("addressA");
        instanceA.setProtocol("protocolA");
        instanceB.setName("instanceB");
        instanceB.setPhisicalAddress("addressA");
        instanceB.setProtocol("protocolA");
        expResult = true;
        result = instanceA.equals(instanceB);
        assertEquals(expResult, result);
        //different name test, unknown address/protocol
        //they are different because "unknown" is a placeholder
        System.out.println("Different names unknown address/protocol. Expected false because 'unknown' is a placeholder");
        instanceA.setName("instanceA");
        instanceA.setPhisicalAddress("unknown");
        instanceA.setProtocol("unknown");
        instanceB.setName("instanceB");
        instanceB.setPhisicalAddress("unknown");
        instanceB.setProtocol("unknown");
        expResult = false;
        result = instanceA.equals(instanceB);
        assertEquals(expResult, result);
        //same name test, unknown address/protocol
        System.out.println("Same names unknown address/protocol. Expected true");
        instanceA.setName("instanceA");
        instanceA.setPhisicalAddress("unknown");
        instanceA.setProtocol("unknown");
        instanceB.setName("instanceA");
        instanceB.setPhisicalAddress("unknown");
        instanceB.setProtocol("unknown");
        expResult = true;
        result = instanceA.equals(instanceB);
        assertEquals(expResult, result);
    }
//    /**
//     * Test of hashCode method, of class EnvObject.
//     */
//    @Test
//    public void testHashCode() {
//        System.out.println("hashCode");
//        EnvObject instance = new EnvObject();
//        int expResult = 0;
//        int result = instance.hashCode();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class EnvObject.
//     */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        EnvObject instance = new EnvObject();
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}

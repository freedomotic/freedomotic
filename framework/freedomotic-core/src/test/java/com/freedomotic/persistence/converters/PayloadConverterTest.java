/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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
package com.freedomotic.persistence.converters;

import com.freedomotic.persistence.PayloadConverter;
import com.freedomotic.rules.Payload;
import com.thoughtworks.xstream.XStream;
import java.util.logging.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico
 */
public class PayloadConverterTest {

    private static String xml;
    private static XStream xstream;

    /**
     *
     */
    public PayloadConverterTest() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        xml = "<payload><payload><com.freedomotic.reactions.Statement>"
                + "<logical>AND</logical><attribute>protocol</attribute>"
                + "<operand>EQUALS</operand><value>SNT084Eth8R8I</value>"
                + "</com.freedomotic.reactions.Statement>"
                + "<com.freedomotic.reactions.Statement><logical>SET</logical>"
                + "<attribute>behaviorValue</attribute><operand>EQUALS</operand>"
                + "<value>@event.isOn</value></com.freedomotic.reactions.Statement>"
                + "</payload></payload>";

        xstream = new XStream();
        xstream.registerConverter(new PayloadConverter());
        xstream.alias("payload", Payload.class);
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
    @Test
    public void testMarshal() {
        System.out.println("Testing Payload to XML");
        Payload payload = new Payload();
        payload.addStatement("protocol", "SNT084Eth8R8I");
        payload.addStatement("SET", "behaviorValue", "EQUALS", "@event.isOn");
        String marshal = xstream.toXML(payload);
        marshal = marshal.replace("\n", "");
        marshal = marshal.replace(" ", "");
        assertEquals(marshal, xml);
    }

    /**
     *
     */
    @Test
    public void testUnmarshal() {
        System.out.println("Testing XML to Payload");
        Payload payload = (Payload) xstream.fromXML(xml);
        assertEquals("protocol", payload.getStatements("protocol").get(0).getAttribute());
        assertEquals("SNT084Eth8R8I", payload.getStatements("protocol").get(0).getValue());
        assertEquals("behaviorValue", payload.getStatements("behaviorValue").get(0).getAttribute());
        assertEquals("@event.isOn", payload.getStatements("behaviorValue").get(0).getValue());                  
    }

    /**
     *
     */
    @Test
    public void testCanConvert() {
    }
    private static final Logger LOG = Logger.getLogger(PayloadConverterTest.class.getName());
}

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
package com.freedomotic.util;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.testutils.GuiceJUnitRunner;
import com.thoughtworks.xstream.XStream;
import org.slf4j.LoggerFactory;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 *
 * @author Enrico Nicoletti
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class DOMValidateDTDTest {

    private static final Logger LOG = LoggerFactory.getLogger(DOMValidateDTDTest.class.getName());

    private static XStream xstream;
    private String file
            = "<trigger>"
            + "<name>Every hour</name>"
            + "<description>schedule actions to be executed at a fixed interval of 60 minutes</description>"
            + "<channel>app.event.sensor.calendar.event.schedule</channel>"
            + "<payload>"
            + "<payload>"
            + "<statement>"
            + "<logical>AND</logical>"
            + "<attribute>anAttribute</attribute>"
            + "<operand>EQUALS</operand>"
            + "<value>ANY</value>"
            + "</statement>"
            + "</payload>"
            + "</payload>"
            + "<suspensionTime>1000</suspensionTime>"
            + "<hardwareLevel>false</hardwareLevel>"
            + "<persistence>false</persistence>"
            + "<delay>0</delay>"
            + "<priority>0</priority>"
            + "<maxExecutions>0</maxExecutions>"
            + "<numberOfExecutions>0</numberOfExecutions>"
            + "</trigger>";
    private static Trigger base = new Trigger();

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        xstream = FreedomXStream.getXstream();
        base.setName("Every hour");
        base.setDescription("schedule actions to be executed at a fixed interval of 60 minutes");
        base.setChannel("app.event.sensor.calendar.event.schedule");
        base.setSuspensionTime(1000);
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
     * @throws Exception
     */
    @Test
    public void testValidate() throws Exception {
//        String xml = DOMValidateDTD.validate(
//                new File(Info.getApplicationPath() + "/test/com.freedomotic/util/trigger-test.xtrg"), 
//                Info.getApplicationPath() + "/config/validator/trigger.dtd");
        Trigger trigger = (Trigger) xstream.fromXML(file);
        //trigger.setSuspensionTime(1000);
        //System.out.println(xstream.toXML(trigger));
        Trigger trigger2 = null;
        String tmp = xstream.toXML(trigger);
        assertEquals("Serialization", file.replaceAll("\n", "").replaceAll(" ", ""), tmp.replaceAll("\n", "").replaceAll(" ", ""));
        trigger2 = (Trigger) xstream.fromXML(tmp);
//            System.out.println(xstream.toXML(trigger2));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //Assert.assertEquals("Payload size", 0, trigger.getPayload().size());
        assertEquals("Suspension time", 1000, trigger.getSuspensionTime());
        assertEquals("Suspension time", 1000, trigger2.getSuspensionTime());
    }
}

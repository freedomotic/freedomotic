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
package it.freedomotic.util;

import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.reactions.Trigger;
import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 *
 * @author Enrico
 */
public class DOMValidateDTDTest {

    private static XStream xstream;
    private String file =
            "<trigger>"
            + "<name>Every hour</name>"
            + "<description>schedule actions to be executed at a fixed interval of 60 minutes</description>"
            + "<channel>app.event.sensor.calendar.event.schedule</channel>"
            + "<payload>"
            + "<payload>"
            + "<it.freedomotic.reactions.Statement>"
            + "<logical>AND</logical>"
            + "<attribute>anAttribute</attribute>"
            + "<operand>EQUALS</operand>"
            + "<value>ANY</value>"
            + "</it.freedomotic.reactions.Statement>"
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        xstream = FreedomXStream.getXstream();
        base.setName("Every hour");
        base.setDescription("schedule actions to be executed at a fixed interval of 60 minutes");
        base.setChannel("app.event.sensor.calendar.event.schedule");
        base.setSuspensionTime(1000);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testValidate() throws Exception {
//        String xml = DOMValidateDTD.validate(
//                new File(Info.getApplicationPath() + "/test/it/freedomotic/util/trigger-test.xtrg"), 
//                Info.getApplicationPath() + "/config/validator/trigger.dtd");
        Trigger trigger = (Trigger) xstream.fromXML(file);
        //trigger.setSuspensionTime(1000);
        //System.out.println(xstream.toXML(trigger));
        Trigger trigger2 = null;
        String tmp = xstream.toXML(trigger);
        Assert.assertEquals("Serialization", file.replaceAll("\n", "").replaceAll(" ", ""), tmp.replaceAll("\n", "").replaceAll(" ", ""));
        System.out.println(tmp);
        trigger2 = (Trigger) xstream.fromXML(tmp);
//            System.out.println(xstream.toXML(trigger2));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //Assert.assertEquals("Payload size", 0, trigger.getPayload().size());
        System.out.println(trigger.getSuspensionTime());
        Assert.assertEquals("Suspension time", 1000, trigger.getSuspensionTime());
        Assert.assertEquals("Suspension time", 1000, trigger2.getSuspensionTime());
    }
}

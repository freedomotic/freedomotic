/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://freedomotic.com
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
package com.freedomotic.plugins.devices.persistence;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.freedomotic.plugins.devices.persistence.util.PersistenceUtility;
import com.freedomotic.reactions.Command;

public class PersistenceUtilityTest {

    @Before
    public void setup() {

    }

    @Test
    public void testTimestampExistence() {
        Command testCommand = new Command();
        testCommand.setProperty("event.date.year", "2017");
        testCommand.setProperty("event.date.month", "1");
        testCommand.setProperty("event.date.day", "14");
        testCommand.setProperty("event.time.hour", "11");
        testCommand.setProperty("event.time.minute", "31");
        testCommand.setProperty("event.time.second", "00");
        Assert.assertEquals(true, PersistenceUtility.isTimestampExistingOnEventProperties(testCommand));
        Assert.assertEquals(false, PersistenceUtility.isTimestampNotExistingOnEventProperties(testCommand));
    }

    @Test
    public void testTimestampNotExistence() {
        Command testCommand = new Command();
        testCommand.setProperty("event.date.year", "2017");
        testCommand.setProperty("event.date.month", "1");
        //testCommand.setProperty("event.date.day", "14");
        testCommand.setProperty("event.time.hour", "11");
        //testCommand.setProperty("event.date.minute", "31");
        testCommand.setProperty("event.time.second", "00");
        Assert.assertEquals(false, PersistenceUtility.isTimestampExistingOnEventProperties(testCommand));
        Assert.assertEquals(true, PersistenceUtility.isTimestampNotExistingOnEventProperties(testCommand));
    }

    @Test
    public void testTimestampValue() {
        Command testCommand = new Command();
        testCommand.setProperty("event.date.year", "2017");	//14th January 2017, 11:31:00
        testCommand.setProperty("event.date.month", "1");
        testCommand.setProperty("event.date.day", "14");
        testCommand.setProperty("event.time.hour", "11");
        testCommand.setProperty("event.time.minute", "31");
        testCommand.setProperty("event.time.second", "00");
        Assert.assertEquals(true, PersistenceUtility.isTimestampExistingOnEventProperties(testCommand));
        Assert.assertEquals(1484389860000L, PersistenceUtility.generateCalendarInMillis(testCommand).longValue());
    }

    @After
    public void teardown() {

    }

}

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

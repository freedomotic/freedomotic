/**
 *
 * Copyright (c) 2009-2018 Freedomotic team http://freedomotic.com
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

package com.freedomotic.plugins;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Assert;
import org.junit.Test;

import com.freedomotic.plugins.CronSchedule.SingleTimeValue;
import com.freedomotic.plugins.CronSchedule.TimeSteps;
import com.freedomotic.plugins.CronSchedule.TimeRange;

/**
 * Tests for the {@link CronSchedule}.
 * 
 * @author alex
 */
public final class CronScheduleTest {

	/** Every hour, minute 0. */
	private static final String EVERY_HOUR = "0 * * * *";
	
	/** Every 15 minutes. */
	private static final String EVERY_15_MINUTES = "*/15 * * * *";
	
	/** Every monday. */
	private static final String EVERY_MONDAY = "0 0 * * 1";
	
	/** Every xmas. */
	private static final String EVERY_XMAS = "0 0 25 12 *";
	
	/**
	 * Tests a schedule that runs every hour.
	 */
	@Test
	public final void testEveryHour() {
		final CronSchedule schedule = new CronSchedule(EVERY_HOUR);
		
		// Make an instant, see if it matches.
		final LocalDateTime match1 = LocalDateTime.of(2017, Month.DECEMBER, 31, 17, 0);
		Assert.assertTrue(schedule.matches(match1));
		
		final LocalDateTime noMatch1 = LocalDateTime.of(2017, Month.DECEMBER, 31, 17, 2);
		Assert.assertFalse(schedule.matches(noMatch1));
	}
	
	/**
	 * Tests a schedule that runs every 15 minutes.
	 */
	@Test
	public final void testEvery15Minutes() {
		final CronSchedule schedule = new CronSchedule(EVERY_15_MINUTES);
		
		// Make an instant, see if it matches.
		final LocalDateTime match1 = LocalDateTime.of(2017, Month.DECEMBER, 31, 17, 0);
		Assert.assertTrue(schedule.matches(match1));
		
		final LocalDateTime noMatch1 = LocalDateTime.of(2017, Month.DECEMBER, 31, 17, 2);
		Assert.assertFalse(schedule.matches(noMatch1));
		
		// Make an instant, see if it matches.
		final LocalDateTime match2 = LocalDateTime.of(2017, Month.DECEMBER, 31, 17, 45);
		Assert.assertTrue(schedule.matches(match2));
	}
	
	/**
	 * Tests a schedule that runs every monday.
	 */
	@Test
	public final void testEveryMonday() {
		final CronSchedule schedule = new CronSchedule(EVERY_MONDAY);
		
		// Make an instant, see if it matches.
		final LocalDateTime match1 = LocalDateTime.of(2017, Month.OCTOBER, 9, 0, 0);
		Assert.assertTrue(schedule.matches(match1));
		
		final LocalDateTime noMatch1 = LocalDateTime.of(2017, Month.OCTOBER, 10, 17, 0);
		Assert.assertFalse(schedule.matches(noMatch1));	
	}
	
	/**
	 * Tests a schedule that runs every monday.
	 */
	@Test
	public final void testXmas() {
		final CronSchedule schedule = new CronSchedule(EVERY_XMAS);
		
		// Make an instant, see if it matches.
		final LocalDateTime match1 = LocalDateTime.of(2017, Month.DECEMBER, 25, 0, 0);
		Assert.assertTrue(schedule.matches(match1));
		
		final LocalDateTime noMatch1 = LocalDateTime.of(2017, Month.DECEMBER, 26, 0, 0);
		Assert.assertFalse(schedule.matches(noMatch1));	
	}

	@Test
	public void matchesInputNegativeOutputTrue() {
  
	  // Arrange
	  final TimeRange objectUnderTest = new TimeRange(-2_147_483_647, 16_777_217);
	  final int timeValue = -2_130_706_432;
  
	  // Act
	  final boolean retval = objectUnderTest.matches(timeValue);
  
	  // Assert result
	  Assert.assertEquals(true, retval);
	}

	@Test
	public void matchesInputZeroOutputFalse() {
  
	  // Arrange
	  final SingleTimeValue cronSchedule$SingleTimeValue = new SingleTimeValue(1);
	  final TimeSteps objectUnderTest = new TimeSteps(cronSchedule$SingleTimeValue, 0);
	  final int timeValue = 0;
  
	  // Act
	  final boolean retval = objectUnderTest.matches(timeValue);
  
	  // Assert result
	  Assert.assertEquals(false, retval);
	}

	@Test
	public void matchesInputZeroOutputFalse2() {
  
	  // Arrange
	  final TimeRange objectUnderTest = new TimeRange(0, -2_147_483_647);
	  final int timeValue = 0;
  
	  // Act
	  final boolean retval = objectUnderTest.matches(timeValue);
  
	  // Assert result
	  Assert.assertEquals(false, retval);
	}

	@Test
	public void matchesInputZeroOutputFalse3() {
  
	  // Arrange
	  final SingleTimeValue objectUnderTest = new SingleTimeValue(1);
	  final int timeValue = 0;
  
	  // Act
	  final boolean retval = objectUnderTest.matches(timeValue);
  
	  // Assert result
	  Assert.assertEquals(false, retval);
	}

	@Test
	public void matchesInputZeroOutputTrue() {
  
	  // Arrange
	  final SingleTimeValue cronSchedule$SingleTimeValue = new SingleTimeValue(0);
	  final TimeSteps objectUnderTest = new TimeSteps(cronSchedule$SingleTimeValue, -1_073_741_824);
	  final int timeValue = 0;
  
	  // Act
	  final boolean retval = objectUnderTest.matches(timeValue);
  
	  // Assert result
	  Assert.assertEquals(true, retval);
	}

}


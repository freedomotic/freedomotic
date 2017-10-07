package com.freedomotic.plugins;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Assert;
import org.junit.Test;

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
}

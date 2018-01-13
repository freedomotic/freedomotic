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
package com.freedomotic.plugins.devices.persistence.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.reactions.Command;

/**
 * Utility class for Persistence plugin
 * 
 * @author P3trur0, https://flatmap.it
 *
 */
public class PersistenceUtility {
	
	private final static Logger LOG = LoggerFactory.getLogger(PersistenceUtility.class.getName());
	
	/*
	 * Inner enumeration representing the event properties to model event timestamps
	 */
	enum DateProperty {
		YEAR("event.date.year"),
		MONTH("event.date.month"),
		DAY("event.date.day"),
		HOUR("event.time.hour"),
		MINUTE("event.time.minute"),
		SECOND("event.time.second");
		
		private String key;
		
		private DateProperty(String key) {
			this.key = key;
		}	
	}

	/**
	 * Given a command, it generates a calendar instance representing the event
	 * timestamp <br>
	 * <b>In case of errors it throws an unchecked exception</b>
	 * 
	 * @param command
	 * @return a calendar instance
	 */
	public static Calendar generateCalendar(Command command) {
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.YEAR, Integer.parseInt(command.getProperty(DateProperty.YEAR.key)));
		calendar.set(Calendar.MONTH, Integer.parseInt(command.getProperty(DateProperty.MONTH.key)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(command.getProperty(DateProperty.DAY.key)));
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(command.getProperty(DateProperty.HOUR.key)));
		calendar.set(Calendar.MINUTE, Integer.parseInt(command.getProperty(DateProperty.MINUTE.key)));
		calendar.set(Calendar.SECOND, Integer.parseInt(command.getProperty(DateProperty.SECOND.key)));
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	public static boolean isTimestampExistingOnEventProperties(Command command) {
		boolean timestampExistence = true;
		for(DateProperty property: DateProperty.values()) {
			if(command.getProperty(property.key) == null) {
				LOG.warn("Timestamp {} property does not exist for this event!", property.name());
				timestampExistence = false;
			}
				
		}
		
		return timestampExistence;
	}
	
	public static boolean isTimestampNotExistingOnEventProperties(Command command) {
		return !isTimestampExistingOnEventProperties(command);
	}

	/**
	 * Given a command, it generates a calendar instance representing the event
	 * timestamp in milliseconds <br>
	 * <b>In case of errors it throws an unchecked exception</b>
	 * 
	 * @param command
	 * @return a long representing the event timestamp with milliseconds
	 *         precision set to zero
	 */
	public static Long generateCalendarInMillis(Command command) {
		return generateCalendar(command).getTimeInMillis();
	}
}

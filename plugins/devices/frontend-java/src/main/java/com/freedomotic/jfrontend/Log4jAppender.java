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
package com.freedomotic.jfrontend;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.freedomotic.i18n.I18n;

/**
 * Custom Log4j Appender to write log entries to a graphical plugin.
 * {@link com.freedomotic.jfrontend.LogWindow}
 *
 * @author P3trur0 (https://flatmap.it)
 * 
 * @see org.apache.log4j.AppenderSkeleton
 */
public class Log4jAppender extends AppenderSkeleton {
	
	/** Reference to graphical plugin Swing Frame. */
	public LogWindow window = null;
	
	/** Reference to log4j Logger. */
	private Logger logger = null;
	
	/*Singleton*/
	private static Log4jAppender appender = null;

	/**
	 * Instantiates thes log4j appender.
	 *
	 * @param i18n the i18n
	 * @param logger the logger
	 */
	private Log4jAppender(I18n i18n, Logger logger) {
		this.logger = logger;
		this.logger.setLevel(Level.ALL);
		this.logger.addAppender(this);
		if (window == null) {
			window = new LogWindow(i18n, this.logger);
		}
	}

	/**
	 * Gets the single instance of Log4jAppender.
	 *
	 * @param i18n the i18n
	 * @param logger the logger
	 * @return single instance of Log4jAppender
	 */
	public static Log4jAppender getInstance(I18n i18n, Logger logger) {
		if (appender == null) {
			appender = new Log4jAppender(i18n, logger);
		}

		return appender;
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	protected void append(LoggingEvent event) {
		if(window!=null) {
			Level windowLevel = window.getPrintableLogger().getLevel();
			if(event.getLevel().isGreaterOrEqual(windowLevel))
				window.append(new Object[] {windowLevel, event.getMessage().toString()});
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#close()
	 */
	public void close() {
		// No implementation
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#requiresLayout()
	 */
	public boolean requiresLayout() {
		return false;
	}
}

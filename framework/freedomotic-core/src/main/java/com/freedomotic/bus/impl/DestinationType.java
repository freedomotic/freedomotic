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
package com.freedomotic.bus.impl;

/**
 * Destination types enumeration
 * 
 * @author Freedomotic Team
 * 
 */
public enum DestinationType {

	QUEUE(0), TEMP_QUEUE(1), TOPIC(2);

	private int code;

	private DestinationType(int code) {

		this.code = code;
	}

	/**
	 * Getter method for code
	 * 
	 * @return the code (as int value)
	 */
	public int getCode() {

		return code;
	}
}

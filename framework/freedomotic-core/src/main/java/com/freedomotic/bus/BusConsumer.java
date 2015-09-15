/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
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
package com.freedomotic.bus;

import javax.jms.ObjectMessage;

/**
 * Interface for bus messages consumers implementations
 * 
 * @author Freedomotic Team
 *
 */
public interface BusConsumer {
	
	/**
	 * Passes an {@link ObjectMessage} to the listener
	 * 
	 * @param message
	 */
    public void onMessage(ObjectMessage message);
}

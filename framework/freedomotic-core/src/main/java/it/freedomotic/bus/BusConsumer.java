/*Copyright 2009 Enrico Nicoletti
    eMail: enrico.nicoletti84@gmail.com

    This file is part of Freedomotic.

    Freedomotic is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    any later version.

    Freedomotic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EventEngine; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package it.freedomotic.bus;

import javax.jms.ObjectMessage;

/**
 *
 * @author Enrico
 */
public interface BusConsumer {
    public void onMessage(ObjectMessage message);
}

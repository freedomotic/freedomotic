/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.plugins.devices.x10;

import java.io.IOException;

public interface X10AbstractGateway {

    /**
     * Starts a connection to send data
     */
    void connect();

    /**
     * Parses the raw string readed from hardware to retrieve x10 housecode,
     * address and function. For example parseReaded("$>9000LW A01A01
     * AONAON0E#") will return "A01AON" for gateway PMIX35
     *
     * @param readed
     * @return
     */
    String parseReaded(String readed);

    /**
     * Write data using the connection previously created
     *
     * @param message
     * @return
     * @throws IOException
     */
    String send(String message) throws IOException;

    void read() throws IOException;

    /**
     * Performs the conversion from simple x10 string like "A01AON" to the
     * harware counterpart. For example composeMessage("A", "01", "ON"); will
     * return "$>9000LW A01A01 AONAON0E#" for gateway PMIX35
     *
     * @param housecode
     * @param address
     * @param command
     * @return
     */
    public String composeMessage(String housecode, String address, String command);

    /**
     * The gateway readable name (eg: CM15)
     *
     * @return
     */
    public String getName();

    public void disconnect();
}

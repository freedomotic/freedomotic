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
package com.freedomotic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates an unique ID as a progressive int. Used to mark events and command
 * with a numeric unique value
 *
 * @author Enrico Nicoletti
 */
public class UidGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(UidGenerator.class.getName());

    private static int lastId = 0;

    /**
     *
     * @return
     */
    public static int getNextUid() {
        lastId++;

        return lastId;
    }

    /**
     *
     * @return
     */
    public static String getNextStringUid() {
        lastId++;

        return Integer.valueOf(lastId).toString();
    }

    private UidGenerator() {
    }
}

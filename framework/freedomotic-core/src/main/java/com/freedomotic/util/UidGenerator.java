/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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

/**
 * Generates an unique ID as a progressive int. Used to mark events and command
 * with a numeric unique value.
 *
 * @author Enrico Nicoletti
 */
public class UidGenerator {

    private static int lastId = 0;

    /**
     * Returns next ID as int.
     * 
     * @return next ID as int
     */
    public static int getNextUid() {
        lastId++;
        return lastId;
    }

    /**
     * Returns next ID as String.
     * 
     * @return next ID as String
     */
    public static String getNextStringUid() {
        lastId++;
        return Integer.toString(lastId);
    }

    private UidGenerator() {
    }
}

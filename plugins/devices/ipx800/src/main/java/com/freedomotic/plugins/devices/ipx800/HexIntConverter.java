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

package com.freedomotic.plugins.devices.ipx800;

public class HexIntConverter {

    /**
     * Takes in input an int and returns a String representing its hex value Eg:
     * input= 42 returns "2a". Alpha characters are lower case
     *
     * @param integer
     * @return its corresponding int
     */
    public static String convert(int integer) {
        String hex = null;
        try {
            hex = Integer.toHexString(integer);
        } catch (Exception e) {
            return null;
        }
        return hex;
    }

    /**
     * Takes in input an hex string eg: "2A" or "2a" and returns the int 42. If
     * the String in input is not a valid hex a NumberFormatException is
     * throwed.
     *
     * @param hex
     * @return the input value converted to int
     */
    public static int convert(String hex) {
        int intValue;
        try {
            intValue = Integer.parseInt(hex, 16);
        } catch (NumberFormatException numberFormatException) {
            return -1;
        }
        return intValue;
    }

    private HexIntConverter() {
    }
}

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
package it.freedomotic.util;

/**
 * Collected methods which allow easy implementation of
 * <code>equals</code>.
 *
 * Example use case in a class called Car:
 * <pre>
 * public boolean equals(Object aThat){
 * if ( this == aThat ) return true;
 * if ( !(aThat instanceof Car) ) return false;
 * Car that = (Car)aThat;
 * return
 * EqualsUtil.areEqual(this.fName, that.fName) &&
 * EqualsUtil.areEqual(this.fNumDoors, that.fNumDoors) &&
 * EqualsUtil.areEqual(this.fGasMileage, that.fGasMileage) &&
 * EqualsUtil.areEqual(this.fColor, that.fColor) &&
 * Arrays.equals(this.fMaintenanceChecks, that.fMaintenanceChecks); //array!
 * }
 * </pre>
 *
 * <em>Arrays are not handled by this class</em>. This is because the
 * <code>Arrays.equals</code> methods should be used for array fields.
 */
public class EqualsUtil {

    static public boolean areEqual(boolean aThis, boolean aThat) {
        //Freedomotic.logger.info("boolean");
        return aThis == aThat;
    }

    static public boolean areEqual(char aThis, char aThat) {
        //Freedomotic.logger.info("char");
        return aThis == aThat;
    }

    static public boolean areEqual(long aThis, long aThat) {
        /*
         * Implementation Note
         * Note that byte, short, and int are handled by this method, through
         * implicit conversion.
         */

        //Freedomotic.logger.info("long");
        return aThis == aThat;
    }

    static public boolean areEqual(float aThis, float aThat) {
        //Freedomotic.logger.info("float");
        return Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat);
    }

    static public boolean areEqual(double aThis, double aThat) {
        //Freedomotic.logger.info("double");
        return Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat);
    }

    /**
     * Possibly-null object field.
     *
     * Includes type-safe enumerations and collections, but does not include
     * arrays. See class comment.
     */
    static public boolean areEqual(Object aThis, Object aThat) {
        //Freedomotic.logger.info("Object");
        return (aThis == null) ? (aThat == null) : aThis.equals(aThat);
    }

    private EqualsUtil() {
    }
}

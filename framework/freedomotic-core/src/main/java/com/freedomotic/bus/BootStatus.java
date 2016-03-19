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
package com.freedomotic.bus;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bus boot statuses enumeration
 *
 * @author Freedomotic Team
 *
 */
public enum BootStatus {

    /**
     *
     */
    STOPPED(0),
    /**
     *
     */
    BOOTING(1),
    /**
     *
     */
    FAILED(2),
    /**
     *
     */
    STARTED(3),
    /**
     *
     */
    STOPPING(4);

    private static final Logger LOG = LoggerFactory.getLogger(BootStatus.class.getName());

    private static final Map<Integer, BootStatus> lookup = new HashMap<Integer, BootStatus>();
    private static final EnumSet<BootStatus> initStatuses = EnumSet.of(STOPPED, BOOTING);
    private static final EnumSet<BootStatus> destroyStatuses = EnumSet.of(STOPPED, STOPPING);

    private static BootStatus currentStatus = STOPPED; // initial status
    private static Throwable throwable;

    static {

        for (BootStatus s : EnumSet.allOf(BootStatus.class)) {
            lookup.put(s.getCode(), s);
        }
    }

    private int code;

    private BootStatus(int code) {

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

    /**
     * Translates an int code into his {@link BootStatus} enumeration
     *
     * @param code the int value of code
     * @return the enumerated object
     */
    public static BootStatus get(int code) {

        return lookup.get(code);
    }

    /**
     * Setter method for this current status holder
     *
     * @param status the status to set
     */
    public static void setCurrentStatus(BootStatus status) {

        currentStatus = status;
    }

    /**
     * Getter method for this current status holder
     *
     * @return the current status
     */
    public static BootStatus getCurrentStatus() {

        return currentStatus;
    }

    /**
     * Getter method for the fault status of this holder
     *
     * @return <code>null</code> or a {@link Throwable}
     */
    // TODO Java8 Return an Optional
    public static Throwable getThrowable() {
        return throwable;
    }

    /**
     * Setter method for the fault status of this holder
     *
     * @param t The throwable
     */
    public static void setThrowable(Throwable t) {

        BootStatus.throwable = t;
        BootStatus.currentStatus = BootStatus.FAILED;

        // FIXME LCG stop bootstrap
        LOG.error("Error while initializing bus service", t);
    }

    private static boolean isAnInitialStatus(BootStatus status) {

        return initStatuses.contains(status);
    }

    /**
     * Holder current status is... method.
     *
     * @return <code>true</code> if current status is an ... status.
     * <code>false</code> otherwise.
     */
    public static boolean isStarting() {

        return isAnInitialStatus(currentStatus);
    }

    private static boolean isAnDestroyStatus(BootStatus status) {

        return destroyStatuses.contains(status);
    }

    /**
     * Holder current status is... method.
     *
     * @return <code>true</code> if current status is an ... status.
     * <code>false</code> otherwise.
     */
    public static boolean isStopping() {

        return isAnDestroyStatus(currentStatus);
    }
}

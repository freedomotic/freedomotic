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

/**
 * Abstract life cycle for managed objects.
 * <p>
 * Executes start and stop methods if and only if the {@link BootStatus} is the
 * corresponding one.
 * <p>
 * Implements a fault barrier pattern for start and stop methods signaling
 * exceptions on {@link BootStatus}
 *
 * @author Freedomotic Team
 *
 */
abstract class LifeCycle {

    /**
     *
     */
    public LifeCycle() {
        super();
    }

    /**
     * Initializes this life cycle managed object
     */
    protected void init() {
        if (BootStatus.isStarting()) {
            try {
                start();
            } catch (Exception e) {
                BootStatus.setThrowable(e);
            }
        }
    }

    /**
     * Prepares for destroy this life cycle managed object
     */
    protected void destroy() {
        if (BootStatus.isStopping()) {
            try {
                stop();
            } catch (Exception e) {
                BootStatus.setThrowable(e);
            }
        }
    }

    /**
     * Starts this managed object
     *
     * @throws Exception
     */
    protected abstract void start() throws Exception;

    /**
     * Stops this managed object
     *
     * @throws Exception
     */
    protected abstract void stop() throws Exception;

}

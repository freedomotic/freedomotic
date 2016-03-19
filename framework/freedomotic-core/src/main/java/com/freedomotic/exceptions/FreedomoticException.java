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
package com.freedomotic.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class FreedomoticException
        extends Exception {

    private static final Logger LOG = LoggerFactory.getLogger(FreedomoticException.class.getName());

    /**
     * Creates a new instance of <code>FreedomoticException</code> without
     * detail message.
     */
    public FreedomoticException() {
    }

    /**
     * Constructs an instance of <code>FreedomoticException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public FreedomoticException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public FreedomoticException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public FreedomoticException(Throwable cause) {
        super(cause);
    }
}

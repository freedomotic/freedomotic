/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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

import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class RepositoryException
        extends FreedomoticException {

    /**
     * Creates a new instance of
     * <code>DaoLayerException</code> without detail message.
     */
    public RepositoryException() {
    }

    /**
     * Constructs an instance of
     * <code>DaoLayerException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public RepositoryException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public RepositoryException(Throwable cause) {
        super(cause);
    }
    private static final Logger LOG = Logger.getLogger(RepositoryException.class.getName());
}

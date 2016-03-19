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

/**
 *
 * @author Enrico Nicoletti
 */
public class PluginLoadingException extends FreedomoticException {

    String pluginName;

    /**
     * Creates a new instance of <code>PluginLoadingException</code> without
     * detail message.
     */
    public PluginLoadingException() {
    }

    /**
     *
     * @return
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     *
     * @param pluginName
     */
    public final void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Constructs an instance of <code>PluginLoadingException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PluginLoadingException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public PluginLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param message
     * @param pluginName
     * @param cause
     */
    public PluginLoadingException(String message, String pluginName, Throwable cause) {
        super(message, cause);
        setPluginName(pluginName);
    }

    /**
     *
     * @param cause
     */
    public PluginLoadingException(Throwable cause) {
        super(cause);
    }
}

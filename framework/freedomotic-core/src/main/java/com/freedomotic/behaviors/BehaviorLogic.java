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
package com.freedomotic.behaviors;

import com.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico Nicoletti
 */
public interface BehaviorLogic {

    /**
     *
     */
    String VALUE_OPPOSITE = "opposite";

    /**
     *
     */
    String VALUE_PREVIOUS = "previous";

    /**
     *
     */
    String VALUE_NEXT = "next";

    /**
     *
     * @param params
     * @param fireCommand
     */
    void filterParams(final Config params, boolean fireCommand);

    /**
     *
     * @return
     */
    String getName();

    /**
     *
     * @return
     */
    String getDescription();

    /**
     *
     * @return
     */
    boolean isChanged();

    /**
     *
     * @param value
     */
    void setChanged(boolean value);

    /**
     *
     * @return
     */
    boolean isActive();

    /**
     *
     * @return
     */
    boolean isReadOnly();

    /**
     *
     * @param value
     */
    void setReadOnly(boolean value);

    /**
     *
     * @return
     */
    String getValueAsString();

}

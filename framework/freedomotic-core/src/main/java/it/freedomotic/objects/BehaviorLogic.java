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
package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico
 */
public interface BehaviorLogic {

    /**
     *
     * @param params
     * @param fireCommand
     */
    public void filterParams(final Config params, boolean fireCommand);

    public String getName();

    public boolean isChanged();

    public void setChanged(boolean value);

    public boolean isActive();

    public boolean isReadOnly();

    public String getValueAsString();
    public final String VALUE_OPPOSITE = "opposite";
    public final String VALUE_PREVIOUS = "previous";
    public final String VALUE_NEXT = "next";
}

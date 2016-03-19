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
package com.freedomotic.i18n;

import java.util.Locale;

/**
 *
 * @author Matteo Mazzoni
 */
public class ComboLanguage implements Comparable {

    private String descr;
    private String value;
    private Locale loc;

    public ComboLanguage(String descr, String value, Locale loc) {
        this.descr = descr;
        this.value = value;
        this.loc = loc;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return descr;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(Object o) {
        return this.descr.compareTo(o.toString());
    }
}

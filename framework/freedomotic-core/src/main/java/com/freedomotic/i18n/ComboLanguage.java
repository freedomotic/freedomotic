/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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
import java.util.Objects;

/**
 *
 * @author Matteo Mazzoni
 */
public class ComboLanguage implements Comparable<ComboLanguage> {

    private String description;
    private String value;
    private Locale locale;

    public ComboLanguage(String description, String value, Locale locale) {
        this.description = description;
        this.value = value;
        this.locale = locale;
    }

    /**
     * @return description - a string representation of the object
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return locale
     */
    public Locale getLocale() {
        return locale;
    }

    @Override
    public int compareTo(ComboLanguage other) {
        return description.compareTo(other.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        final ComboLanguage other = (ComboLanguage) obj;
        return (description != null ? description.equals(other.toString()) : other.toString() == null);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.description);
        return hash;
    }

}

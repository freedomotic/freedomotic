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
package com.freedomotic.model.object;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies a behavior to choose a specified value from a list of potential values.
 *
 * @author Enrico Nicoletti
 */
public class ListBehavior
        extends Behavior {

    private static final long serialVersionUID = 8375501744412227268L;

    private int selected;
    private ArrayList<String> list = new ArrayList<>();

    /**
     *
     * @param key
     */
    public void add(String key) {
        list.add(key);
    }

    /**
     *
     * @param key
     */
    public void remove(String key) {
        list.remove(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return list.contains(key);
    }

    /**
     *
     * @return
     */
    public String getSelected() {
        return list.get(selected);
    }

    /**
     *
     * @return
     */
    public List<String> getList() {
        return list;
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean setSelected(String key) {
        if (list.contains(key)) {
            selected = list.indexOf(key);
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public int getItemsNumber() {
        return list.size();
    }

    /**
     *
     * @param key
     * @return
     */
    public int indexOf(String key) {
        return list.indexOf(key);
    }

    /**
     *
     * @return
     */
    public int indexOfSelection() {
        return selected;
    }

    /**
     *
     * @param index
     * @return
     */
    public String get(int index) {
        return list.get(index);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return list.get(selected);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ListBehavior that = (ListBehavior) o;

        return selected == that.selected && list.equals(that.list);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + selected;
        result = 31 * result + list.hashCode();
        return result;
    }
}

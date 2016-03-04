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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class MultiselectionListBehavior
        extends Behavior {

    private static final long serialVersionUID = -7839150128393354068L;

    private final ArrayList<String> list = new ArrayList<String>();
    private final List<String> selected = new ArrayList<String>();

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
     * @param key
     * @return
     */
    public boolean isSelected(String key) {
        return selected.contains(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean isEnlisted(String key) {
        return list.contains(key);
    }

    /**
     *
     * @return
     */
    public List<String> getSelected() {
        List<String> tmp = new ArrayList<String>();

        for (String item : list) {
            if (selected.contains(item)) {
                //is selected
                tmp.add(item);
            }
        }

        return tmp;
    }

    /**
     *
     * @param key
     * @return key selected (boolean)
     */
    public boolean setSelected(String key) {
        if (list.contains(key) && !selected.contains(key)) {
            selected.add(key);
            return true;
        }
        return false;
    }

    /**
     *
     * @param key
     * @return key unselected (boolean)
     */
    public boolean setUnselected(String key) {
        if (list.contains(key) && selected.contains(key)) {
            selected.remove(key);
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public int indexOfSelection() {
        int selection = -1;

        if (selected.get(0) != null) {
            selection = list.indexOf(selected.get(0));
        }

        return Math.max(0, selection);
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
        return list.size() + " items (" + selected.size() + " selected)";
    }

    /**
     *
     * @return
     */
    public List<String> getList() {
        return list;
    }
}

/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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
package com.freedomotic.behaviors;

import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.MultiselectionListBehavior;
import java.util.List;
import java.util.logging.Logger;

/**
 * This behavior accepts a string which is an element of the list or "next" or
 * "previous" as input params. The selectedChanged is called only if the request
 * is valid (new selection is a value in the list) and if is not the current
 * selected value.
 *
 * @author Enrico
 */
public class TaxonomyBehaviorLogic
        implements BehaviorLogic {

    private MultiselectionListBehavior data;
    private TaxonomyBehaviorLogic.Listener listener;
    private boolean changed;

    /**
     *
     */
    public interface Listener {

        /**
         *
         * @param params
         * @param fireCommand
         */
        public void onSelection(final Config params, boolean fireCommand);

        /**
         *
         * @param params
         * @param fireCommand
         */
        public void onUnselection(final Config params, boolean fireCommand);

        /**
         *
         * @param params
         * @param fireCommand
         */
        public void onAdd(final Config params, boolean fireCommand);

        /**
         *
         * @param params
         * @param fireCommand
         */
        public void onRemove(final Config params, boolean fireCommand);
    }

    /**
     *
     * @param pojo
     */
    public TaxonomyBehaviorLogic(MultiselectionListBehavior pojo) {
        this.data = pojo;
    }

    @Override
    public synchronized final void filterParams(Config params, boolean fireCommand) {
        //value contains the sting used in user level commands like object="tv" behavior="inputs" value="hdmi1"
        //we have to check if value is a suitable choice according to a list of possibilities (check if it exists)
        String item = params.getProperty("item").trim();
        String value = params.getProperty("value").trim();

        if (value.equalsIgnoreCase("add") && !data.getSelected().contains(item)) {
            //add the element id not already here
            listener.onAdd(params, fireCommand);
        } else {
            if (value.equalsIgnoreCase("remove")) {
                //data.setUnselected(item);
                listener.onRemove(params, fireCommand);
            } else {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("selected") || value.equals("1")) {
                    //data.setSelected(item);
                    listener.onSelection(params, fireCommand);
                } else {
                    if (value.equalsIgnoreCase("false")
                            || value.equalsIgnoreCase("unselected")
                            || value.equals("0")) {
                        //data.setUnselected(item);
                        listener.onUnselection(params, fireCommand);
                    }
                }
            }
        }
    }

    /**
     *
     * @param item
     */
    protected void addElement(String item) {
        if (!data.contains(item)) {
            data.add(item);
            setChanged(true);
        }
    }

    /**
     *
     * @param item
     */
    protected void removeElement(String item) {
        if (data.contains(item)) {
            data.remove(item);
            setChanged(true);
        }
    }

    /**
     *
     * @param item
     */
    protected void setSelected(String item) {
        if (!data.getSelected().equals(item)) {
            data.setSelected(item);
            setChanged(true);
        }
    }

    /**
     *
     * @param item
     */
    protected void setUnselected(String item) {
        if (data.getSelected().equals(item)) {
            data.setUnselected(item);
            setChanged(true);
        }
    }

    /**
     *
     * @return
     */
    public List<String> getSelected() {
        return data.getSelected();
    }

    /**
     *
     * @return
     */
    public List<String> getList() {
        return data.getList();
    }

    /**
     *
     * @param listBehaviorListener
     */
    public void addListener(TaxonomyBehaviorLogic.Listener listBehaviorListener) {
        listener = listBehaviorListener;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return data.getName();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isActive() {
        return data.isActive();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isReadOnly() {
        return data.isReadOnly();
    }

    /**
     *
     * @return
     */
    @Override
    public String getValueAsString() {
        return data.toString();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isChanged() {
        return changed;
    }

    /**
     *
     * @param value
     */
    @Override
    public void setChanged(boolean value) {
        changed = value;
    }
    private static final Logger LOG = Logger.getLogger(TaxonomyBehaviorLogic.class.getName());
}

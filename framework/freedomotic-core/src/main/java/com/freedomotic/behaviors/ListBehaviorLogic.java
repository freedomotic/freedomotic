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
import com.freedomotic.model.object.ListBehavior;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This behavior accepts a string which is an element of the list or "next" or
 * "previous" as input params. The selectedChanged is called only if the request
 * is valid (new selection is a value in the list) and if is not the current
 * selected value.
 *
 * @author Enrico
 */
public class ListBehaviorLogic
        implements BehaviorLogic {

    private ListBehavior data;
    private Listener listener;
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
        public void selectedChanged(final Config params, boolean fireCommand);
    }

    /**
     *
     * @param pojo
     */
    public ListBehaviorLogic(ListBehavior pojo) {
        this.data = pojo;
    }

    @Override
    public synchronized final void filterParams(Config params, boolean fireCommand) {
        //value contains the sting used in user level commands like object="tv" behavior="inputs" value="hdmi1"
        //we have to check if value is a suitable choice according to a list of possibilities (check if it exists)
        String parsed = params.getProperty("value").trim();

        if (parsed.equalsIgnoreCase("next")) {
            next(params, fireCommand);
        }

        if (parsed.equalsIgnoreCase("previous")) {
            previous(params, fireCommand);
        }

        if (!parsed.equalsIgnoreCase(data.getSelected())) {
            if (data.contains(parsed)) {
                //notify the user wants to use another value from the list
                listener.selectedChanged(params, fireCommand);
            }
        }
    }

    private void next(Config params, boolean fireCommand) {
        int index = (data.indexOfSelection() + 1) % data.getItemsNumber();
        params.setProperty("value",
                data.get(index));
        listener.selectedChanged(params, fireCommand);
    }

    private void previous(Config params, boolean fireCommand) {
        int index = data.indexOfSelection() - 1;

        if (index < 0) {
            //index is negative for sure so we have to add it not substract
            index = data.getItemsNumber() + index;
        }

        params.setProperty("value",
                data.get(index));
        listener.selectedChanged(params, fireCommand);
    }

    /**
     *
     * @param listBehaviorListener
     */
    public void addListener(Listener listBehaviorListener) {
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
    public String getSelected() {
        return data.getSelected();
    }

    /**
     *
     * @param key
     */
    public void setSelected(String key) {
        if (!data.getSelected().equalsIgnoreCase(key)) {
            data.setSelected(key);
            setChanged(true);
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getValuesList() {
        return data.getList();
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
    private static final Logger LOG = Logger.getLogger(ListBehaviorLogic.class.getName());
}

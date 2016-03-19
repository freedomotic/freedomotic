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
import com.freedomotic.model.object.BooleanBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class BooleanBehaviorLogic implements BehaviorLogic {

    private static final Logger LOG = LoggerFactory.getLogger(BooleanBehaviorLogic.class.getName());
    private final BooleanBehavior data;
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
        public void onTrue(Config params, boolean fireCommand);

        /**
         *
         * @param params
         * @param fireCommand
         */
        public void onFalse(Config params, boolean fireCommand);
    }

    /**
     *
     * @param pojo
     */
    public BooleanBehaviorLogic(BooleanBehavior pojo) {
        this.data = pojo;
    }

    @Override
    public synchronized final void filterParams(final Config params, boolean fireCommand) {
        //filter accepted values
        String value = params.getProperty("value").trim();

        if (value.equalsIgnoreCase("false") || value.equals("0")) {
            if (this.getValue() != false) { //if is really changed
                listener.onFalse(params, fireCommand);
            }
        }

        if (value.equalsIgnoreCase("true") || value.equals("1")) {
            if (this.getValue() != true) { //if is really changed
                listener.onTrue(params, fireCommand);
            }
        }

        if (value.equalsIgnoreCase(VALUE_OPPOSITE)) {
            opposite(params, fireCommand);
        }

        if (value.equalsIgnoreCase(VALUE_NEXT)) {
            opposite(params, fireCommand);
        }

        if (value.equalsIgnoreCase(VALUE_PREVIOUS)) {
            opposite(params, fireCommand);
        }
    }

    private void opposite(Config params, boolean fireCommand) {
        if (data.getValue() == true) {
            if (this.getValue() != false) { //if is really changed
                listener.onFalse(params, fireCommand);
            }
        } else {
            if (this.getValue() != true) { //if is really changed
                listener.onTrue(params, fireCommand);
            }
        }
    }

    /**
     *
     * @param booleanBehaviorListener
     */
    public void addListener(Listener booleanBehaviorListener) {
        listener = booleanBehaviorListener;
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
    public String getDescription() {
        return data.getDescription();
    }

    /**
     *
     * @param b
     */
    public void setValue(boolean b) {
        if (data.getValue() != b) {
            data.setValue(b);
            setChanged(true);
        }
    }

    /**
     *
     * @return
     */
    public boolean getValue() {
        return data.getValue();
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final BooleanBehaviorLogic other = (BooleanBehaviorLogic) obj;

        if ((this.data != other.data) && ((this.data == null) || !this.data.equals(other.data))) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = (71 * hash) + ((this.data != null) ? this.data.hashCode() : 0);

        return hash;
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

    @Override
    public void setReadOnly(boolean value) {
        data.setReadOnly(value);
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
}

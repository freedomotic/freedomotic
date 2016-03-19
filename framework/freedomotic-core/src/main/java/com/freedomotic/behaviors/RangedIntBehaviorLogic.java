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
import com.freedomotic.model.object.RangedIntBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class RangedIntBehaviorLogic
        implements BehaviorLogic {

    private static final Logger LOG = LoggerFactory.getLogger(RangedIntBehaviorLogic.class.getName());
    private final RangedIntBehavior data;
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
        public void onLowerBoundValue(Config params, boolean fireCommand);

        /**
         *
         * @param params
         * @param fireCommand
         */
        public void onUpperBoundValue(Config params, boolean fireCommand);

        /**
         *
         * @param rangeValue
         * @param params
         * @param fireCommand
         */
        public void onRangeValue(int rangeValue, Config params, boolean fireCommand);
    }

    /**
     *
     * @param pojo
     */
    public RangedIntBehaviorLogic(RangedIntBehavior pojo) {
        this.data = pojo;
    }

    /**
     *
     * @param listener
     */
    public void addListener(Listener listener) {
        this.listener = listener;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return getName() + ": " + getValue();
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
     * @return
     */
    public int getValue() {
        return data.getValue();
    }

    /**
     *
     * @return
     */
    public int getStep() {
        return data.getStep();
    }

    /**
     *
     * @return
     */
    public int getMax() {
        return data.getMax();
    }

    /**
     *
     * @return
     */
    public int getMin() {
        return data.getMin();
    }

    /**
     *
     * @return
     */
    public int getScale() {
        return data.getScale();
    }

    @Override
    public synchronized final void filterParams(final Config params, boolean fireCommand) {
        //from dim to dim
        String input = params.getProperty("value").trim();
        int parsed = getMin();

        try {
            if (input.startsWith("++")) {
                parsed = getValue() + Integer.parseInt(input.replace("++", "")); //eliminate the + and sum the new value
            } else {
                if (input.startsWith("--")) {
                    parsed = getValue() - Integer.parseInt(input.replace("--", "")); //eliminate the - and subtact the new value
                } else {
                    parsed = (int) Double.parseDouble(input); //takes doubles and integers. Doubles are truncated to standard int values
                }
            }
        } catch (NumberFormatException numberFormatException) {
            LOG.warn("Paramenter 'value = " + params.getProperty("value").trim() + "' in "
                    + this.getName() + " behavior is not an integer.");
        }

        if (input.equalsIgnoreCase("next")) {
            parsed = getValue() + getStep();
        }

        if (input.equalsIgnoreCase("previous")) {
            parsed = getValue() - getStep();
        }

        if (input.equalsIgnoreCase("opposite")) {
            //opposite value not allowed for this behavior. Inform the user.
        }

        performValueChange(parsed, params, fireCommand);
    }

    private void performValueChange(int tmpValue, Config params, boolean fireCommand) {
        if (getValue() != tmpValue) {
            if (tmpValue <= getMin()) {
                params.setProperty("value",
                        Integer.valueOf(getMin()).toString());
                params.setProperty("value.original",
                        Integer.valueOf(tmpValue).toString());
                listener.onLowerBoundValue(params, fireCommand);
            } else {
                if (tmpValue >= getMax()) {
                    params.setProperty("value",
                            String.valueOf(getMax()));
                    params.setProperty("value.original",
                            Integer.valueOf(tmpValue).toString());
                    listener.onUpperBoundValue(params, fireCommand);
                } else {
                    listener.onRangeValue(tmpValue, params, fireCommand);
                }
            }
        }
    }

    /**
     *
     * @param inputValue
     */
    public void setValue(int inputValue) {
        if (data.getValue() != inputValue) {
            data.setValue(inputValue);
            setChanged(true);
        }
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

        final RangedIntBehaviorLogic other = (RangedIntBehaviorLogic) obj;

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
        int hash = 7;
        hash = (23 * hash) + ((this.data != null) ? this.data.hashCode() : 0);

        return hash;
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

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
import com.freedomotic.model.object.PropertiesBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class PropertiesBehaviorLogic
        implements BehaviorLogic {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesBehaviorLogic.class.getName());
    private PropertiesBehavior data;
    private PropertiesBehaviorLogic.Listener listener;
    private boolean changed;

    /**
     *
     */
    public interface Listener {

        /**
         *
         * @param key
         * @param value
         * @param params
         * @param fireCommand
         */
        public void propertyChanged(String key, String value, Config params, boolean fireCommand);
    }

    /**
     *
     * @param pojo
     */
    public PropertiesBehaviorLogic(PropertiesBehavior pojo) {
        this.data = pojo;
    }

    @Override
    public synchronized final void filterParams(final Config params, boolean fireCommand) {

        String[] parsed = params.getProperty("value").trim().split("=");
        String key = parsed[0].trim();
        String value = parsed[1].trim();

        if (key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
            String currentValue = data.getProperty(key);
            if (currentValue != null) {
                if (!currentValue.equalsIgnoreCase(value)) {
                    //notify the user wants to change a property in the list
                    params.setProperty(key, value);
                    listener.propertyChanged(key, value, params, fireCommand);
                }
            }
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
     * @return
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }

//    public String getProperty(String key) {
//        return data.getProperty(key);
//    }
//
//    public void setProperty(String key, String value) {
//        data.setProperty(key, value);
//    }
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
    public PropertiesBehavior getPojo() {
        return data;
    }

    /**
     *
     * @param propertiesBehaviorListener
     */
    public void addListener(PropertiesBehaviorLogic.Listener propertiesBehaviorListener) {
        listener = propertiesBehaviorListener;
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

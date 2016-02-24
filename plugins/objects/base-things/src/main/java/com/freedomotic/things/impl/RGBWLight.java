/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
package com.freedomotic.things.impl;

import com.freedomotic.behaviors.BooleanBehaviorLogic;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.reactions.Command;

/**
 *
 * @author Mauro Cicolella
 */
public class RGBWLight extends RGBLight {

    private BooleanBehaviorLogic whiteMode;
    protected final static String BEHAVIOR_WHITE_MODE = "white-mode";

    @Override
    public void init() {
        //linking this property with the behavior defined in the XML
        whiteMode = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior(BEHAVIOR_WHITE_MODE));
        whiteMode.addListener(new BooleanBehaviorLogic.Listener() {

            @Override
            public void onTrue(Config params, boolean fireCommand) {
                setWhiteMode(params);
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                unsetWhiteMode(params);
            }

        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(whiteMode);

        super.init();
    }

    @Override
    public void executePowerOff(Config params) {

        /*
         * executeCommand the body of the super implementation The super call
         * must be the last call as it executes setChanged(true)
         */
        super.executePowerOff(params);
    }

    @Override
    public void executePowerOn(Config params) {
        /*
         * Not called the setBrightness because this method executeCommand a
         * command here we want only to mantain the system coerent. If we call
         * setBrightness(100, params) the light recalls the onUpperBoundValue.
         * Only ONE command execution per situation
         */
        //executeCommand the body of the super implementation
        super.executePowerOn(params);
    }

    protected void setWhiteMode(Config params) {
        boolean executed = executeCommand("set white mode", params);

        if (executed) {
            whiteMode.setValue(true);
            setChanged(true);
        }
    }

    protected void unsetWhiteMode(Config params) {
        boolean executed = executeCommand("set white mode", params);

        if (executed) {
            whiteMode.setValue(false);
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {

        super.createCommands();

    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}

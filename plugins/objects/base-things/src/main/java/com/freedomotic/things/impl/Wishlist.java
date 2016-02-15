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
package com.freedomotic.things.impl;

import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.MultiselectionListBehavior;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.behaviors.TaxonomyBehaviorLogic;

/**
 *
 * @author enrico
 */
public class Wishlist
        extends EnvObjectLogic {

    private TaxonomyBehaviorLogic list;
    private static final String BEHAVIOR_TAXONOMY = "taxonomy";

    @Override
    public void init() {
        list = new TaxonomyBehaviorLogic((MultiselectionListBehavior) getPojo().getBehavior(BEHAVIOR_TAXONOMY));
        System.out.println(getPojo().getBehavior(BEHAVIOR_TAXONOMY));
        list.addListener(new TaxonomyBehaviorLogic.Listener() {
            @Override
            public void onSelection(Config params, boolean fireCommand) {
                setChanged(true);
            }

            @Override
            public void onUnselection(Config params, boolean fireCommand) {
                setChanged(true);
            }

            @Override
            public void onAdd(Config params, boolean fireCommand) {
                if (!list.getList().contains(params.getProperty("item"))) {
                    list.getList().add(params.getProperty("item"));
                    setChanged(true);
                }
                
            }

            @Override
            public void onRemove(Config params, boolean fireCommand) {
                 if (!list.getSelected().contains(params.getProperty("item"))) {
                     list.getList().remove(params.getProperty("item"));
                     setChanged(true);
                 }   
            }
        });
        //register this behavior to the superclass to make it visible to it
        registerBehavior(list);
        super.init();
    }

    @Override
    protected void createCommands() {
        //no commands for this kind of objects
    }

    @Override
    protected void createTriggers() {
    }
}

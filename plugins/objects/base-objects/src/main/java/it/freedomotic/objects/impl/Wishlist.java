/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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

package it.freedomotic.objects.impl;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.MultiselectionListBehavior;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.TaxonomyBehaviorLogic;

/**
 *
 * @author enrico
 */

public class Wishlist
        extends EnvObjectLogic {

    private TaxonomyBehaviorLogic list;

    @Override
    public void init() {
        list = new TaxonomyBehaviorLogic((MultiselectionListBehavior) getPojo().getBehaviors().get(0));
        System.out.println(getPojo().getBehaviors().get(0));
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
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onRemove(Config params, boolean fireCommand) {
                throw new UnsupportedOperationException("Not supported yet.");
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

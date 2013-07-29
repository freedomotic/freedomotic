/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

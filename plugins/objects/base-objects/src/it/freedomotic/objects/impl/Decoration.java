package it.freedomotic.objects.impl;

import it.freedomotic.core.EnvObjectLogic;

/**
 *
 * @author Enrico
 */
public class Decoration extends EnvObjectLogic {

    @Override
    public void init() {
        //decoration is a static object. It does nothing
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

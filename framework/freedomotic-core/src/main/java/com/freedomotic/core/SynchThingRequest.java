/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.model.object.EnvObject;

/**
 * This class is reserver for internal use, Never use it outside the core!
 * @author enrico
 */
public class SynchThingRequest extends EventTemplate {
    
    private final EnvObject thing;

    public SynchThingRequest(SynchAction action, EnvObject thing) {
        this.thing = thing;
        this.addProperty(SynchAction.KEY_SYNCH_ACTION, action.name());
    }

    public EnvObject getThing() {
        return thing;
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
        //do nothing
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return SynchManager.LISTEN_CHANNEL;
    }

}

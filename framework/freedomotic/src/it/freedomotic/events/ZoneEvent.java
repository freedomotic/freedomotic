/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;

/**
 *
 * @author enrico
 */
class ZoneEvent extends EventTemplate{

    @Override
    protected void generateEventPayload() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDefaultDestination() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

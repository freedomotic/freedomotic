/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.isy99i;

import it.freedomotic.api.Protocol;
import it.freedomotic.events.ProtocolRead;

/**
 *
 * @author mauro
 */
public class AuxClass {

    Protocol reference = null;

//constructor
    public AuxClass(Protocol reference) {
        this.reference = reference;
    }

//event notification
    public void notifyIsyEvent(ProtocolRead event) {
        reference.notifyEvent(event);
    }
}

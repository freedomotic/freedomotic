/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.x10;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;

/**
 *
 * @author enrico
 */
public class X10Event {

    private String address;
    private String function;

    public X10Event() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        //System.out.println("event address: " + address);
        this.address = address;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        //System.out.println("event function: " + function);
        this.function = function;
    }

    public boolean isEventComplete() {
        if (!address.isEmpty() && !function.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void send() {
        ProtocolRead event = new ProtocolRead(this, "X10", getAddress());
        //A01AOFF
        event.addProperty("X10.event", getAddress() + getFunction());
        //A
        event.addProperty("X10.housecode", getAddress().substring(0, 1));
        //01
        event.addProperty("X10.address", getAddress().substring(1, 3));
        //OFF
        event.addProperty("X10.function", getFunction().substring(1, getFunction().length()));
        Freedomotic.sendEvent(event);
        resetEvent();
    }

    private void resetEvent() {
        setAddress("");
        setFunction("");
    }

    @Override
    public String toString() {
        return getAddress() + getFunction();
    }
}

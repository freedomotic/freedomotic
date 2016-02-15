/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.plugins.devices.x10;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;

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

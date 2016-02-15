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

package com.freedomotic.plugins;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Successful extends Protocol {

    private Boolean powered = true;

    /**
     *
     */
    public Successful() {
        super("Successful Test", "/essential/successful.xml");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Successful.class.getName()).log(Level.SEVERE, null, ex);
        }

        c.setExecuted(true);
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        //DISABLED: sends a fake sensor read event. Used for testing
//        ProtocolRead event = new ProtocolRead(this, "test", "test");
//        event.getPayload().addStatement("object.class", "Light");
//        event.getPayload().addStatement("object.name", "myLight");
//        event.getPayload().addStatement("value",
//                powered.toString());
//        //invert the value for the next round
//        notifyEvent(event);
//
//        if (powered) {
//            powered = false;
//        } else {
//            powered = true;
//        }
//
//        //wait two seconds before sending another event
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(VariousSensors.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

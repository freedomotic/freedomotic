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

package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;

import it.freedomotic.exceptions.UnableToExecuteException;

import it.freedomotic.reactions.Command;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Delayer
        extends Protocol {

    public Delayer() {
        super("Delayer", "/test/delayer.xml");
        setDescription("Delayed commands in automations");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        reminder(c,
                Long.parseLong(c.getProperty("delay")));
    }

    public void reminder(Command c, long ms) {
        Timer timer = new Timer();
        timer.schedule(new RemindTask(c, timer),
                ms);
    }

    class RemindTask
            extends TimerTask {

        Command c;
        Timer t;

        private RemindTask(Command c, Timer t) {
            this.c = c;
            this.t = t;
        }

        public void run() {
            t.cancel(); //Terminate the timer thread
            c.setExecuted(true);
            reply(c);
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }
}

/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins;

import com.freedomotic.api.Client;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class PluginRemoteController extends Protocol {

    private static final Logger LOG = Logger.getLogger(PluginRemoteController.class.getName());

    /**
     *
     */
    public PluginRemoteController() {
        super("Plugins Remote Controller", "/essential/plugins-remote-controller.xml");
        setPollingWait(-1); //disable threaded onRun()
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        Client plugin = getApi().getClientStorage().get(c.getProperty("plugin"));
        String action = c.getProperty("action");

        if (plugin != null) {
            if (action.equalsIgnoreCase("SHOW")) {
                plugin.showGui();
            }

            if (action.equalsIgnoreCase("HIDE")) {
                plugin.hideGui();
            }
            if (action.equalsIgnoreCase("STOP")) {
                if (plugin != this && getApi().getAuth().isPermitted("sys:plugins:stop")) {
                    plugin.stop();
                }
            }
            if (action.equalsIgnoreCase("START")) {
                if (plugin != this && getApi().getAuth().isPermitted("sys:plugins:start")) {
                    plugin.start();
                }
            }
            if (action.equalsIgnoreCase("RESTART")) {
                if (plugin != this && getApi().getAuth().isPermitted("sys:plugins:stop") && getApi().getAuth().isPermitted("sys:plugins:start")) {
                    plugin.stop();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                    plugin.start();
                }
            }

        } else {
            LOG.log(Level.WARNING, "Impossible to act on plugin {0}", c.getProperty("plugin"));
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        //do nothing
        return true;
    }

    @Override
    protected void onRun() {
        //do nothing
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //do nothing
    }
}

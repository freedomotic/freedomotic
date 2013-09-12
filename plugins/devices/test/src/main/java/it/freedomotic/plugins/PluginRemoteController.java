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

import com.google.inject.Inject;
import it.freedomotic.api.Actuator;
import it.freedomotic.api.Client;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class PluginRemoteController
        extends Actuator {

    private static final Logger LOG = Logger.getLogger(PluginRemoteController.class.getName()); 
    
    public PluginRemoteController() {
        super("Plugins Remote Controller", "/test/plugins-remote-controller.xml");
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
        } else {
            LOG.warning("Impossible to act on plugin " + c.getProperty("plugin"));
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

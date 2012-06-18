/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Actuator;
import it.freedomotic.api.Client;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.AddonManager;
import it.freedomotic.reactions.Command;
import java.io.IOException;

/**
 *
 * @author Enrico
 */
public class PluginRemoteController extends Actuator {

    public PluginRemoteController() {
        super("Plugins Remote Controller", "/it.nicoletti.test/plugins-remote-controller.xml");
        start();
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        Client plugin = AddonManager.getPluginByName(c.getProperty("plugin"));
        String action = c.getProperty("action");
        if (plugin != null) {
            if (action.equalsIgnoreCase("SHOW")) {
                plugin.showGui();
            }
            if (action.equalsIgnoreCase("HIDE")) {
                plugin.hideGui();
            }
        } else {
            Freedomotic.logger.warning("Impossible to act on plugin " + c.getProperty("plugin"));
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.model.ds.Config;
import it.freedomotic.plugins.ClientStorage;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author enrico
 */
@Singleton
public class JoinPlugin
        implements BusConsumer {

    private static final CommandChannel channel = new CommandChannel();
    //dependency
    private ClientStorage clientStorage;

    static String getMessagingChannel() {
        return "app.plugin.create";
    }

    @Inject
    private JoinPlugin(ClientStorage clientStorage) {
        this.clientStorage = clientStorage;
        register();
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
        channel.setHandler(this);
        channel.consumeFrom(getMessagingChannel());
    }

    @Override
    public void onMessage(ObjectMessage message) {
        try {
            //here a plugin manifest is expected
            Config manifest = (Config) message.getObject();
            Plugin plugin = new Plugin(manifest.getProperty("name"),
                    manifest);
            clientStorage.add(plugin);
            Freedomotic.logger.info("Enqueued remote plugin " + plugin.getName());
        } catch (JMSException ex) {
            Freedomotic.logger.severe("Join Plugin receives a not valid plugin manifest");
        }
    }
}

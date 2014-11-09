/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.reactions.Command;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author enrico
 */
@Singleton
public final class JoinDevice
        implements BusConsumer {

    private static final String MESSAGING_CHANNEL = "app.objects.create";

    private static BusMessagesListener listener;
    private static final Logger LOG = Logger.getLogger(JoinDevice.class.getName());

    //dependencies
    private final EnvironmentRepository environmentPersistence;
    private BusService busService;

    @Inject
    private JoinDevice(EnvironmentRepository environmentPersistence, BusService busService) {
        this.environmentPersistence = environmentPersistence;
        this.busService = busService;
        register();
    }

    static String getMessagingChannel() {
        return MESSAGING_CHANNEL;
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
        listener = new BusMessagesListener(this, busService);
        listener.consumeCommandFrom(getMessagingChannel());
    }

    @Override
    public void onMessage(ObjectMessage message) {
        try {
            Object jmsObject = message.getObject();

            if (jmsObject instanceof Command) {
                Command command = (Command) jmsObject;
                String name = command.getProperty("object.name");
                String protocol = command.getProperty("object.protocol");
                String address = command.getProperty("object.address");
                String clazz = command.getProperty("object.class");

                if (EnvObjectPersistence.getObjectByAddress(protocol, address).isEmpty()) {
                    environmentPersistence.join(clazz, name, protocol, address);
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(JoinDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

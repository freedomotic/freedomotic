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
package com.freedomotic.core;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.model.ds.Config;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingFactory;
import com.freedomotic.things.ThingRepository;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author Enrico Nicoletti
 */
public class SynchManager implements BusConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyManager.class.getName());
    public static final String LISTEN_CHANNEL = "app.event.sensor.object.behavior.change";
    private BusMessagesListener listener;
    private final BusService busService;
    private final ThingRepository thingsRepository;
    public static final String KEY_PROVENANCE = "provenance";
    private final ThingFactory thingFactory;

    @Inject
    SynchManager(BusService busService, ThingRepository thingsRepository, ThingFactory thingFactory) {
        this.busService = busService;
        this.thingsRepository = thingsRepository;
        this.thingFactory = thingFactory;
        listener = new BusMessagesListener(this, this.busService);
        // It register the GLOBAL event channel, this mean it is using
        // standard JMS Topics not the activemq Virtual Topics
        listener.subscribeCrossInstanceEvents(LISTEN_CHANNEL);
    }

    @Override
    public void onMessage(ObjectMessage message) {
        try {
            // Skip if the message comes from the same freedomotic instance
            // We don't want to notify changes to ourself
            if (!message.getStringProperty(KEY_PROVENANCE).equals(Freedomotic.INSTANCE_ID)) {
                Object jmsObject = message.getObject();
                if (jmsObject instanceof ObjectHasChangedBehavior) {
                    synchronizeLocalThing((ObjectHasChangedBehavior) jmsObject);
                } else if (jmsObject instanceof SynchThingRequest) {
                    SynchThingRequest synchThingRequest = (SynchThingRequest) jmsObject;
                    EnvObjectLogic thing = null;
                    try {
                        thing = thingFactory.create(synchThingRequest.getThing());
                    } catch (RepositoryException ex) {
                        LOG.error(ex.getMessage());
                    }
                    // A new thing was created in another instance
                    if (synchThingRequest.getProperty(SynchAction.KEY_SYNCH_ACTION)
                            .equalsIgnoreCase(SynchAction.CREATED.name())) {
                        thingsRepository.create(thing);
                    }
                    // A thing was deleted in another instance
                    if (synchThingRequest.getProperty(SynchAction.KEY_SYNCH_ACTION)
                            .equalsIgnoreCase(SynchAction.DELETED.name())) {
                        thingsRepository.delete(thing);
                    }
                }
            }
        } catch (JMSException ex) {
            LOG.error(ex.getMessage());
        }

    }

    private void synchronizeLocalThing(ObjectHasChangedBehavior event) {
        // Synchronize changed behaviors
        EnvObjectLogic obj = thingsRepository.findOne(event.getProperty("object.uuid"));
        for (BehaviorLogic b : obj.getBehaviors()) {
            String value = event.getProperty("object.behavior." + b.getName());
            if (value != null && !value.isEmpty()) {
                Config conf = new Config();
                conf.setProperty("value", value);
                LOG.info("Synch thing {} behavior {} to {} notified by {}",
                        new Object[]{obj.getPojo().getName(), b.getName(), value});
                obj.getBehavior(b.getName()).filterParams(conf, false);
            }
        }
        // Synchronize the thing location (if possible)
        // Must be done after synchornizing behaviors, because it changes the current thing representation pointer
        try {
            int locationX = Integer.parseInt(event.getProperty("object.location.x"));
            int locationY = Integer.parseInt(event.getProperty("object.location.y"));
            LOG.info("Synch thing {} location to {},{}",
                    new Object[]{obj.getPojo().getName(), locationX, locationY});
            obj.synchLocation(locationX, locationY);
        } catch (NumberFormatException numberFormatException) {
            LOG.warn("Synch thing location is not possible because notified location it's not a valid number");
        }
    }
}

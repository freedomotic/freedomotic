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

import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.events.LocationEvent;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.things.GenericPerson;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.util.TopologyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Listens to topology related events and manages them, eventually throwing more
 * specific events like "PersonEnterZone" if it's the case.
 *
 * @author Enrico Nicoletti
 */
public class TopologyManager implements BusConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyManager.class.getName());
    private static final String LISTEN_CHANNEL = "app.event.sensor.person.movement.detected";
    private static BusMessagesListener listener;

    // Dependencies
    private final EnvironmentRepository environmentRepository;
    private final BusService busService;
    private final ThingRepository thingsRepository;

    @Inject
    TopologyManager(BusService busService, ThingRepository thingsRepository, EnvironmentRepository environmentRepository) {
        this.busService = busService;
        this.environmentRepository = environmentRepository;
        this.thingsRepository = thingsRepository;
        listener = new BusMessagesListener(this, busService);
        listener.consumeEventFrom(LISTEN_CHANNEL);
    }

    @Override
    public void onMessage(ObjectMessage message) {
        Object jmsObject = null;
        try {
            jmsObject = message.getObject();
        } catch (JMSException ex) {
            LOG.error(ex.getMessage());
        }

        if (jmsObject instanceof LocationEvent) {
            LocationEvent event = (LocationEvent) jmsObject;
            GenericPerson person = (GenericPerson) thingsRepository.findOne(event.getUuid());
            //apply the new position
            person.setLocation(event.getX(), event.getY());
            //check if this person is entering/exiting an evironment zone
            fireEnterExitEvents(person, event);
        }
    }

    /**
     * Fires PersonEntersZone or PersonExitsZone events after checking current
     * and old person's location in the environment.
     *
     * @param person the person entering/exiting the zone 
     * @param event the person's coordinates
     */
    private void fireEnterExitEvents(GenericPerson person, LocationEvent event) {
        for (ZoneLogic zone : environmentRepository.findAll().get(0).getZones()) {
            // are the new Person's coordinates inside the current zone?
            boolean isZoneAffected = TopologyUtils.contains(zone.getPojo().getShape(), new FreedomPoint(event.getX(), event.getY()));
            if (isZoneAffected) {
                if (!zone.isInside(person)) {
                    // received coordinates are inside this zone but previously 
                    // the person was not inside this zone
                    zone.enter(person); //update zone occupiers
                }
            } else {
                if (zone.isInside(person)) {
                    //this person is no more inside this (previously occupied) zone
                    zone.exit(person); //update zone occupiers
                }
            }
        }
    }
}

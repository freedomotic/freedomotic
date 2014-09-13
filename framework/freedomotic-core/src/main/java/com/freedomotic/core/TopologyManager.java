/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import static com.freedomotic.core.BehaviorManager.getMessagingChannel;
import com.freedomotic.environment.EnvironmentPersistence;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.events.LocationEvent;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.objects.impl.Person;
import com.freedomotic.util.TopologyUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Listen to topology related events and manages them, eventually throwing more
 * specific events like "PersonEnterZone" if it's the case.
 *
 * @author nicoletti
 */
public class TopologyManager implements BusConsumer {

    private static final Logger LOG = Logger.getLogger(TopologyManager.class.getName());
    private static BusMessagesListener listener;
    private static final String LISTEN_CHANNEL = "app.event.sensor.person.movement.detected";
    private final BusService busService;

    public TopologyManager() {
        busService = Freedomotic.INJECTOR.getInstance(BusService.class);
        listener = new BusMessagesListener(this);
        listener.consumeCommandFrom(LISTEN_CHANNEL);
    }

    @Override
    public void onMessage(ObjectMessage message) {
        Object jmsObject = null;
        try {
            jmsObject = message.getObject();
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        if (jmsObject instanceof LocationEvent) {
            LocationEvent event = (LocationEvent) jmsObject;
            Person person = (Person) EnvObjectPersistence.getObjectByUUID(event.getUuid());
            //apply the new position
            person.setLocation(event.getX(), event.getY());
            //check if this person is entering/exiting an evironment zone
            fireEnterExitEvents(person, event);
        }
    }

    /**
     * Fires PersonEntersZone or PersonExitsZone events after checking current
     * and old location in the environment.
     *
     * @param event
     */
    private void fireEnterExitEvents(Person person, LocationEvent event) {
        for (ZoneLogic zone : EnvironmentPersistence.getEnvironments().get(0).getZones()) {
            // are the new Person coordinates inside the current zone?
            boolean isZoneAffected = TopologyUtils.contains(zone.getPojo().getShape(), new FreedomPoint(event.getX(), event.getY()));
            if (isZoneAffected) {
                if (!zone.isInside(person)) {
                    // received coordinates are inside this zone but proviously 
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

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
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.model.ds.Config;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingFactory;
import com.freedomotic.things.ThingRepository;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author nicoletti
 */
public class SynchManager implements BusConsumer {

    private static final Logger LOG = Logger.getLogger(TopologyManager.class.getName());
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
                        Logger.getLogger(SynchManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // A new thing was created in another instance
                    if (synchThingRequest.getProperty(SynchThingRequest.KEY_SYNCH_ACTION)
                            .equalsIgnoreCase(SynchThingRequest.SynchAction.CREATED.name())) {
                        thingsRepository.create(thing);
                    }
                    // A thing was deleted in another instance
                    if (synchThingRequest.getProperty(SynchThingRequest.KEY_SYNCH_ACTION)
                            .equalsIgnoreCase(SynchThingRequest.SynchAction.DELETED.name())) {
                        thingsRepository.delete(thing);
                    }
                }
            }
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
                LOG.log(Level.CONFIG, "Synch thing {0} behavior {1} to {2} notified by {3}",
                        new Object[]{obj.getPojo().getName(), b.getName(), value});
                obj.getBehavior(b.getName()).filterParams(conf, false);
            }
        }
        // Synchronize the thing location (if possible)
        // Must be done after synchornizing behaviors, because it changes the current thing representation pointer
        try {
            int locationX = Integer.parseInt(event.getProperty("object.location.x"));
            int locationY = Integer.parseInt(event.getProperty("object.location.y"));
            LOG.log(Level.CONFIG, "Synch thing {0} location to {1},{2}",
                    new Object[]{obj.getPojo().getName(), locationX, locationY});
            obj.synchLocation(locationX, locationY);
        } catch (NumberFormatException numberFormatException) {
            LOG.log(Level.WARNING, "Synch thing location is not possible because notified location it's not a valid number");
        }

    }

}

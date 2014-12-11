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
import com.freedomotic.things.EnvObjectLogic;
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
    private static final String LISTEN_CHANNEL = "app.event.sensor.object.behavior.change";
    private BusMessagesListener listener;
    private final BusService busService;
    private final ThingRepository thingsRepository;

    @Inject
    SynchManager(BusService busService, ThingRepository thingsRepository) {
        this.busService = busService;
        this.thingsRepository = thingsRepository;
        listener = new BusMessagesListener(this, busService);
        // It register the GLOBAL event channel, this mean it is using
        // standard JMS Topics not the activemq Virtual Topics
        listener.subscribeEventFrom(LISTEN_CHANNEL);
    }

    @Override
    public void onMessage(ObjectMessage message) {
        try {
            // Skip if the message comes from the same freedomotic instance
            // We don't want to notify changes to ourself
            if (!message.getStringProperty("provenance").equals(Freedomotic.INSTANCE_ID)) {
                Object jmsObject = message.getObject();
                if (jmsObject instanceof ObjectHasChangedBehavior) {
                    synchronizeLocalThing((ObjectHasChangedBehavior) jmsObject);
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
                LOG.log(Level.INFO, "Synch thing {0} behavior {1} to {2}", new Object[]{obj.getPojo().getName(), b.getName(), value});
                obj.getBehavior(b.getName()).filterParams(conf, false);
            }
        }
        // Synchronize the thing location (if possible)
        // Must be done after synchornizing behaviors, because it changes the current thing representation pointer
        try {
            int locationX = Integer.parseInt(event.getProperty("object.location.x"));
            int locationY = Integer.parseInt(event.getProperty("object.location.y"));
            LOG.log(Level.INFO, "Synch thing {0} location to {1},{2}", new Object[]{obj.getPojo().getName(), locationX, locationY});
            obj.synchLocation(locationX, locationY);
        } catch (NumberFormatException numberFormatException) {
            LOG.log(Level.CONFIG, "Synch thing location is not possible because notified location it's not a valid number");
        }

    }

}

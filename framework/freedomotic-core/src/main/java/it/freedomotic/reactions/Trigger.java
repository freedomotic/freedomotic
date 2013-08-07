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
package it.freedomotic.reactions;

import com.google.inject.Inject;

import it.freedomotic.api.EventTemplate;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.app.Profiler;

import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.EventChannel;

import it.freedomotic.core.Resolver;
import it.freedomotic.core.TriggerCheck;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author enrico
 */
public final class Trigger
        implements BusConsumer,
        Cloneable {

    private String name;
    private String description;
    private String uuid;
    //private EventTemplate action;
    //TODO: the action is the queue getted from the default queue of the event
    //we need also the possibility to point to the channel with a string
    private String channel;
    private Payload payload = new Payload();
    private long suspensionTime;
    //TODO: change name to "mappable" or something like that
    private boolean hardwareLevel;
    private boolean persistence;
    private int delay;
    private int priority;
    private long maxExecutions;
    private long numberOfExecutions;
    private long suspensionStart;
    private EventChannel busChannel;
    //dependencies
    @Inject
    private TriggerCheck checker;

    public Trigger() {
    }

    public void register() {
        busChannel = new EventChannel();
        busChannel.setHandler(this);
        LOG.config("Registering the trigger named '" + getName() + "'");
        busChannel.consumeFrom(channel);
        numberOfExecutions = 0;
        suspensionStart = System.currentTimeMillis();
        Freedomotic.INJECTOR.injectMembers(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHardwareLevel() {
        return hardwareLevel;
    }

    public void setIsHardwareLevel(boolean hardwareLevel) {
        this.hardwareLevel = hardwareLevel;
    }

    public void setDescription(String sender) {
        this.description = sender;
    }

    public void setChannel(EventTemplate event) {
        this.channel = event.getDefaultDestination();
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isConsistentWith(EventTemplate event) {
        if (getPayload().equals(event.getPayload())) {
            return true;
        } else {
            return false;
        }
    }

    public long getMaxExecutions() {
        if (maxExecutions <= 0) {
            maxExecutions = -1; //unlimited
        }

        return maxExecutions;
    }

    public void setMaxExecutions(long maxExecutions) {
        this.maxExecutions = maxExecutions;
    }

    public long getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(long numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    public long getSuspensionTime() {
//        if (suspensionTime <= 0) {
//            suspensionTime = 100; //a minimal default suspension to control flooding
//        }
        return suspensionTime;
    }

    public void setSuspensionTime(long suspensionTime) {
        this.suspensionTime = suspensionTime;
    }

    public void setPayload(Payload p) {
        this.payload = p;
    }

    //can be moved to a stategy pattern
    public boolean canFire() {
        //num of executions < max executions
        if (getMaxExecutions() > -1) { //not unlimited

            if (getNumberOfExecutions() >= getMaxExecutions()) {
                return false;
            }
        }

        if (getNumberOfExecutions() > 0) { //if is not the first time it executes

            long wakeup = suspensionStart + getSuspensionTime();
            long now = System.currentTimeMillis();

            if (now < wakeup) {
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss.SSS");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(wakeup);
                LOG.config("Trigger " + getName() + " is suspended until "
                        + formatter.format(calendar.getTime()));

                //it is currently suspended
                return false;
            }
        }

        //can fire
        return true;
    }

    public synchronized void setExecuted() {
        suspensionStart = System.currentTimeMillis();
        numberOfExecutions++;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        if ((description == null) || (description.isEmpty())) {
            description = name;
        }

        return description;
    }

    public String getChannel() {
        return channel;
    }

    public Payload getPayload() {
        return payload;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Trigger other = (Trigger) obj;

        if ((this.name == null) ? (other.name != null) : (!this.name.equalsIgnoreCase(other.name))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (53 * hash) + ((this.name != null) ? this.name.hashCode() : 0);

        return hash;
    }

    /*
     * Performs the Trigger check comparing the received event with itself (this
     * trigger)
     */
    @Override
    public void onMessage(ObjectMessage message) {
        long start = System.currentTimeMillis();
        Object payload = null;

        try {
            payload = message.getObject();
        } catch (JMSException ex) {
            Logger.getLogger(Trigger.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (payload instanceof EventTemplate) {
            EventTemplate event = (EventTemplate) payload;
            LOG.fine("Trigger '" + this.getName() + "' filters event '" + event.getEventName()
                    + "' on channel " + this.getChannel());

            checker.check(event, this);
            long end = System.currentTimeMillis();
            Profiler.appendTriggerCheckingTime(end - start);
        }
    }

    @Override
    public Trigger clone() {
        Trigger clone = new Trigger();
        clone.setName(getName());
        clone.setDescription(getDescription());

        Payload clonePayload = new Payload();
        Iterator it = getPayload().iterator();

        while (it.hasNext()) {
            Statement original = (Statement) it.next();
            clonePayload.addStatement(original.getLogical(),
                    original.getAttribute(),
                    original.getOperand(),
                    original.getValue());
        }

        clone.setPayload(clonePayload);
        clone.setIsHardwareLevel(isHardwareLevel());
        clone.setMaxExecutions(getMaxExecutions());
        clone.setNumberOfExecutions(getNumberOfExecutions());
        clone.setSuspensionTime(getSuspensionTime());
        clone.suspensionStart = this.suspensionStart;
        clone.setPriority(0);

        return clone;
    }

    public void unregister() {
        if (busChannel != null) {
            busChannel.unsubscribe();
        }
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setPersistence(boolean persist) {
        this.persistence = persist;
    }

    public boolean isToPersist() {
        return persistence;
    }

    @Inject
    private void setTriggerCheck(TriggerCheck checker) {
        this.checker = checker;
    }
    private static final Logger LOG = Logger.getLogger(Trigger.class.getName());
}

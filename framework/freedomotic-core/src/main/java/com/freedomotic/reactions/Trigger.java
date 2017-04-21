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
package com.freedomotic.reactions;

import com.freedomotic.rules.Statement;
import com.freedomotic.rules.Payload;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.app.Profiler;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.core.TriggerCheck;
import com.google.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Trigger implements BusConsumer, Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger(Trigger.class.getName());
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
    @XmlTransient
    private BusMessagesListener listener;
    //dependencies
    @Inject
    @XmlTransient
    private TriggerCheck checker;
    @Inject
    @XmlTransient
    private BusService busService;

    /**
     *
     */
    public Trigger() {
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     *
     */
    public void register() {
        Freedomotic.INJECTOR.injectMembers(this);
        LOG.info("Registering the trigger named '" + getName() + "'");
        listener = new BusMessagesListener(this, busService);
        listener.consumeEventFrom(channel);
        numberOfExecutions = 0;
        suspensionStart = System.currentTimeMillis();
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     *
     * @return
     */
    public boolean isHardwareLevel() {
        return hardwareLevel;
    }

    /**
     *
     * @param hardwareLevel
     */
    public void setIsHardwareLevel(boolean hardwareLevel) {
        this.hardwareLevel = hardwareLevel;
    }

    /**
     *
     * @param sender
     */
    public void setDescription(String sender) {
        this.description = sender;
    }

    /**
     *
     * @param event
     */
    public void setChannel(EventTemplate event) {
        this.channel = event.getDefaultDestination();
    }

    /**
     *
     * @param channel
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     *
     * @param event
     * @return
     */
    public boolean isConsistentWith(EventTemplate event) {
        if (getPayload().equals(event.getPayload())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public long getMaxExecutions() {
        if (maxExecutions <= 0) {
            maxExecutions = -1; //unlimited
        }

        return maxExecutions;
    }

    /**
     *
     * @param maxExecutions
     */
    public void setMaxExecutions(long maxExecutions) {
        this.maxExecutions = maxExecutions;
    }

    /**
     *
     * @return
     */
    public long getNumberOfExecutions() {
        return numberOfExecutions;
    }

    /**
     *
     * @param numberOfExecutions
     */
    public void setNumberOfExecutions(long numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    /**
     *
     * @return
     */
    public long getSuspensionTime() {
//        if (suspensionTime <= 0) {
//            suspensionTime = 100; //a minimal default suspension to control flooding
//        }
        return suspensionTime;
    }

    /**
     *
     * @param suspensionTime
     */
    public void setSuspensionTime(long suspensionTime) {
        this.suspensionTime = suspensionTime;
    }

    /**
     *
     * @param p
     */
    public void setPayload(Payload p) {
        this.payload = p;
    }

    //can be moved to a stategy pattern
    /**
     *
     * @return
     */
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
                LOG.info("Trigger " + getName() + " is suspended until "
                        + formatter.format(calendar.getTime()));

                //it is currently suspended
                return false;
            }
        }

        //can fire
        return true;
    }

    /**
     *
     */
    public synchronized void setExecuted() {
        suspensionStart = System.currentTimeMillis();
        numberOfExecutions++;
    }

    /**
     *
     * @param delay
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     *
     * @return
     */
    public int getPriority() {
        return priority;
    }

    /**
     *
     * @param priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        if ((description == null) || (description.isEmpty())) {
            description = name;
        }

        return description;
    }

    /**
     *
     * @return
     */
    public String getChannel() {
        return channel;
    }

    /**
     *
     * @return
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     *
     * @return
     */
    public int getDelay() {
        return delay;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     *
     * @param obj
     * @return
     */
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

    /**
     *
     * @return
     */
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
            LOG.error(ex.getMessage());
        }

        if (payload instanceof EventTemplate) {
            EventTemplate event = (EventTemplate) payload;
            LOG.debug("Trigger '" + this.getName() + "' filters event '" + event.getEventName()
                    + "' on channel " + this.getChannel());

            checker.check(event, this);
            long end = System.currentTimeMillis();
            Profiler.appendTriggerCheckingTime(end - start);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Trigger clone() {
        Trigger clone = new Trigger();
        clone.setName(getName());
        clone.setDescription(getDescription());

        Payload clonePayload = new Payload();
        Iterator<Statement> it = getPayload().iterator();

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

    /**
     *
     */
    public void unregister() {
        if (listener != null) {
            listener.destroy();
        }
    }

    /**
     *
     * @return
     */
    public String getUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    /**
     *
     * @param uuid
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     *
     * @param persist
     */
    public void setPersistence(boolean persist) {
        this.persistence = persist;
    }

    /**
     *
     * @return
     */
    public boolean isToPersist() {
        return persistence;
    }

    @Inject
    private void setTriggerCheck(TriggerCheck checker) {
        this.checker = checker;
    }
}

/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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

import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.core.TriggerCheck;
import com.freedomotic.exceptions.FreedomoticRuntimeException;
import com.freedomotic.rules.Payload;
import com.freedomotic.rules.Statement;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;

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
    //TODO: the action is the queue got from the default queue of the event
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
     * Default constructor.
     */
    public Trigger() {
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Registers the trigger
     */
    public void register() {
        Freedomotic.INJECTOR.injectMembers(this);
        LOG.info("Registering the trigger named \"{}\"", getName());
        listener = new BusMessagesListener(this, busService);
        listener.consumeEventFrom(channel);
        numberOfExecutions = 0;
        suspensionStart = System.currentTimeMillis();
    }

    /**
     * Sets name of the trigger.
     *
     * @param name name to trim and set.
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * Gets hardware level of the trigger.
     *
     * @return hardware level.
     */
    public boolean isHardwareLevel() {
        return hardwareLevel;
    }

    /**
     * Sets hardware level of the trigger.
     *
     * @param hardwareLevel hardware level to set
     */
    public void setIsHardwareLevel(boolean hardwareLevel) {
        this.hardwareLevel = hardwareLevel;
    }

    /**
     * Sets the description of the trigger.
     *
     * @param description description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the channel based on event default destination.
     *
     * @param event provider of channel.
     */
    public void setChannel(EventTemplate event) {
        this.channel = event.getDefaultDestination();
    }

    /**
     * Sets the channel of the trigger.
     *
     * @param channel channel to set.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Checks if payload is consistent with given event's payload.
     *
     * @param event given event with payload.
     * @return true if payload is consistent with given event's payload.
     */
    public boolean isConsistentWith(EventTemplate event) {
        return getPayload().equals(event.getPayload());
    }

    /**
     * Gets max number of trigger executions.
     *
     * @return max number of trigger executions.
     */
    public long getMaxExecutions() {
        if (maxExecutions <= 0) {
            maxExecutions = -1; //unlimited
        }

        return maxExecutions;
    }

    /**
     * Sets max number of trigger executions.
     *
     * @param maxExecutions max number of executions.
     */
    public void setMaxExecutions(long maxExecutions) {
        this.maxExecutions = maxExecutions;
    }

    /**
     * Gets number of executions of the trigger.
     *
     * @return number of executions of the trigger.
     */
    public long getNumberOfExecutions() {
        return numberOfExecutions;
    }

    /**
     * Sets number of trigger executions.
     *
     * @param numberOfExecutions number of trigger executions.
     */
    public void setNumberOfExecutions(long numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    /**
     * Gets suspension time of the trigger.
     *
     * @return suspension time.
     */
    public long getSuspensionTime() {
        return suspensionTime;
    }

    /**
     * Sets suspension time of the trigger.
     *
     * @param suspensionTime suspension time to set.
     */
    public void setSuspensionTime(long suspensionTime) {
        this.suspensionTime = suspensionTime;
    }

    /**
     * Sets trigger payload.
     *
     * @param payload trigger payload to set.
     */
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    //can be moved to a stategy pattern
    /**
     * Checks whether the trigger can fire.
     *
     * @return true if trigger can fire.
     */
    public boolean canFire() {
        //num of executions < max executions
        if (getMaxExecutions() > -1 && getNumberOfExecutions() >= getMaxExecutions()) { //not unlimited
            return false;
        }

        if (getNumberOfExecutions() > 0) { //if is not the first time it executes

            long wakeup = suspensionStart + getSuspensionTime();
            long now = System.currentTimeMillis();

            if (now < wakeup) {
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss.SSS");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(wakeup);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Trigger \"" + getName() + "\" is suspended until "
                            + formatter.format(calendar.getTime()));
                }
                //it is currently suspended
                return false;
            }
        }

        //can fire
        return true;
    }

    /**
     * Increments the number of trigger executions and updates the
     * suspensionStart.
     */
    public synchronized void setExecuted() {
        suspensionStart = System.currentTimeMillis();
        numberOfExecutions++;
    }

    /**
     * Sets delay of the trigger.
     *
     * @param delay delay to set.
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Gets trigger priority.
     *
     * @return trigger priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets trigger priority.
     *
     * @param priority trigger priority to set.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets name of the trigger.
     *
     * @return name of the trigger.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets trigger description.
     *
     * @return trigger description.
     */
    public String getDescription() {
        if ((description == null) || (description.isEmpty())) {
            description = name;
        }
        return description;
    }

    /**
     * Gets trigger channel.
     *
     * @return trigger channel.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Gets trigger payload.
     *
     * @return trigger payload.
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * Gets delay of the trigger.
     *
     * @return delay of the trigger.
     */
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

        return (this.name == null) ? (other.name == null) : (this.name.equalsIgnoreCase(other.name));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (53 * hash) + ((this.name != null) ? this.name.hashCode() : 0);

        return hash;
    }

    /*
     * Performs the trigger check comparing the received event with itself (this
     * trigger)
     */
    @Override
    public void onMessage(ObjectMessage message) {
        Object msgPayload = null;

        try {
            msgPayload = message.getObject();
        } catch (JMSException ex) {
            LOG.error(ex.getMessage());
        }

        if (msgPayload instanceof EventTemplate) {
            EventTemplate event = (EventTemplate) msgPayload;
            LOG.debug("Trigger \"" + this.getName() + "\" filters event \"" + event.getEventName()
                    + "\" on channel \"" + this.getChannel() + "\"");
            checker.check(event, this);
        }
    }

    /**
     * Clones the trigger.
     *
     * @return the cloned trigger.
     */
    @Override
    public Trigger clone() {
        Trigger clone;

        try {
            clone = (Trigger) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new FreedomoticRuntimeException("Clone failed.");
        }

        clone.setUUID(UUID.randomUUID().toString());

        Payload clonePayload = new Payload();
        Iterator<Statement> it = getPayload().iterator();

        while (it.hasNext()) {
            Statement original = it.next();
            clonePayload.addStatement(original.getLogical(),
                    original.getAttribute(),
                    original.getOperand(),
                    original.getValue());
        }

        clone.setPayload(clonePayload);
        clone.setPriority(0);

        return clone;
    }

    /**
     * Unregisters the trigger.
     */
    public void unregister() {
        if (listener != null) {
            listener.destroy();
        }
    }

    /**
     * Gets the UUID of the trigger.
     *
     * @return UUID of the trigger.
     */
    public String getUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    /**
     * Sets the UUID of the trigger.
     *
     * @param uuid UUID of the trigger.
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Sets whether trigger should be persisted.
     *
     * @param persist true if trigger should be persisted.
     */
    public void setPersistence(boolean persist) {
        this.persistence = persist;
    }

    /**
     * Gets if the trigger should be persisted.
     *
     * @return true if trigger should be persisted, false otherwise
     */
    public boolean isToPersist() {
        return persistence;
    }

    @Inject
    private void setTriggerCheck(TriggerCheck checker) {
        this.checker = checker;
    }
}

/*Copyright 2009 Enrico Nicoletti
eMail: enrico.nicoletti84@gmail.com

This file is part of Freedomotic.

Freedomotic is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
any later version.

Freedomotic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with EventEngine; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.api;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.reactions.Payload;
import it.freedomotic.util.UidGenerator;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import javax.jms.Destination;

/**
 *
 * @author enrico
 */
public abstract class EventTemplate implements Serializable {

    protected String eventName;
    protected String sender;
    protected Payload payload = new Payload();
    protected boolean isValid;
    private int uid;
    private boolean executed;
    private boolean isExecutable;
    private long creation;
    private int priority;
    //TODO: change destination to simple String
    private Destination replyTo = null;

    protected abstract void generateEventPayload();

    public abstract String getDefaultDestination();

    public EventTemplate() {
        fillPayloadWithDefaults();
    }

    public EventTemplate(Object source) {
        sender = source.getClass().getSimpleName();
        fillPayloadWithDefaults();
    }

    public String getEventName() {
        return eventName;
    }

    public long getCreation() {
        return creation;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void addProperty(String key, String value) {
        payload.addStatement(key, value);
    }

    public int getUid() {
        return uid;
    }

    private void setUid() {
        uid = UidGenerator.getNextUid();
    }

    private void init() {
        eventName = this.getClass().getSimpleName();
        isValid = true;
        setUid();
        executed = true; //an event starts as executed as default value if an actuator don't deny it
        isExecutable = true;
        creation = System.currentTimeMillis();
    }

    public void fillPayloadWithDefaults() {
        init();
        try {
            Calendar rightNow = Calendar.getInstance();
            //adding date and time data
            payload.addStatement("date.dayname", rightNow.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK));
            payload.addStatement("date.day", rightNow.get(Calendar.DAY_OF_MONTH));
            payload.addStatement("date.month", rightNow.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK));
            payload.addStatement("date.year", rightNow.get(Calendar.YEAR));
            payload.addStatement("time.hour", rightNow.get(Calendar.HOUR_OF_DAY));
            payload.addStatement("time.minute", rightNow.get(Calendar.MINUTE));
            payload.addStatement("time.second", rightNow.get(Calendar.SECOND));
            //adding event.sender to event payload. So it can be used by trigger
            payload.addStatement("sender", getSender());
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    public String getSender() {
        try {
            if (sender != null) {
                return sender;
            } else {
                return "UnknownSender";
            }
        } catch (Exception e) {
            return "UnknownSenderException";
        }
    }

    public void setSender(Object source) {
        if (source != null) {
            sender = source.getClass().getSimpleName();
        } else {
            sender = "UnknownSender";
        }
    }

    public Payload getPayload() {
        return payload;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean value) {
        executed = value;
    }

    public boolean isExecutable() {
        return isExecutable;
    }

    public void setExecutable(boolean can) {
        isExecutable = can;
    }

    public Destination getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Destination to) {
        replyTo = to;
    }

    public boolean isValid() {
        return isValid;
    }

    protected void invalidate() {
        isValid = false;
    }
}

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
import it.freedomotic.reactions.Statement;
import it.freedomotic.util.UidGenerator;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author enrico
 */
public class EventTemplate implements Serializable {

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
    //private Destination replyTo = null;

    protected void generateEventPayload() {
    }

    public String getDefaultDestination() {
        return "app.event.sensor";
    }

    public EventTemplate() {
        fillPayloadWithDefaults();
    }

    public EventTemplate(Object source) {
        setSender(source);
        fillPayloadWithDefaults();
    }

    public String getEventName() {
        return eventName;
    }

    public long getCreation() {
        return creation;
    }

    public void addProperty(String key, String value) {
        payload.addStatement(key, value);
    }

    public String getProperty(String key) {
        synchronized (payload) {
            List<Statement> statements = payload.getStatements(key);
            if (statements.isEmpty()) {
                return "";
            } else {
                return statements.get(0).getValue();
            }
        }
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

    private final void fillPayloadWithDefaults() {
        init();
        try {
            Calendar rightNow = Calendar.getInstance();
            //adding date and time data
            payload.addStatement("date.day.name", rightNow.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK));
            payload.addStatement("date.day", rightNow.get(Calendar.DAY_OF_MONTH));
            payload.addStatement("date.month.name", rightNow.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK));
            payload.addStatement("date.month", rightNow.get(Calendar.MONTH) + 1);
            payload.addStatement("date.year", rightNow.get(Calendar.YEAR));
            payload.addStatement("time.hour", rightNow.get(Calendar.HOUR_OF_DAY));
            payload.addStatement("time.minute", rightNow.get(Calendar.MINUTE));
            payload.addStatement("time.second", rightNow.get(Calendar.SECOND));
            DateFormat datefmt = new SimpleDateFormat("yyyyMMdd");
            DateFormat timefmt = new SimpleDateFormat("HHmmss");
            payload.addStatement("time", timefmt.format(rightNow.getTime()));
            payload.addStatement("date", datefmt.format(rightNow.getTime()));
            //adding event.sender to event payload. So it can be used by trigger
            payload.addStatement("sender", getSender());
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private String getSender() {
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

    protected final void setSender(Object source) {
        if (source != null) {
            sender = source.getClass().getSimpleName();
        } else {
            sender = "UnknownSender";
        }
    }

    public Payload getPayload() {
        return payload;
    }
}

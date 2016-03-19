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
package com.freedomotic.api;

import com.freedomotic.rules.Payload;
import com.freedomotic.rules.Statement;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class EventTemplate implements Serializable {

    private static final long serialVersionUID = -6726283450243677665L;
    protected String eventName;
    protected String sender;
    protected Payload payload = new Payload();
    protected boolean isValid;
    private long creation;

    @XStreamOmitField
    private static final Logger LOG = LoggerFactory.getLogger(EventTemplate.class.getName());

    protected void generateEventPayload() {
    }

    /**
     *
     * @return
     */
    public String getDefaultDestination() {
        return "app.event.sensor";
    }

    /**
     *
     */
    public EventTemplate() {
        fillPayloadWithDefaults();
    }

    /**
     *
     * @param source
     */
    public EventTemplate(Object source) {
        setSender(source);
        fillPayloadWithDefaults();
    }

    /**
     *
     * @return
     */
    public String getEventName() {
        return eventName;
    }

    /**
     *
     * @return
     */
    public long getCreation() {
        return creation;
    }

    /**
     *
     * @param key
     * @param value
     */
    public void addProperty(String key, String value) {
        payload.addStatement(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
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

//    private void setUid() {
//        uid = UidGenerator.getNextUid();
//    }
    private void init() {
        eventName = this.getClass().getSimpleName();
        isValid = true;
        // setUid();
        //executed = true; //an event starts as executed as default value if an actuator don't deny it
        // isExecutable = true;
        creation = System.currentTimeMillis();
    }

    private final void fillPayloadWithDefaults() {
        init();

        try {
            Calendar rightNow = Calendar.getInstance();
            //adding date and time data
            payload.addStatement("date.day.name",
                    rightNow.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK));
            payload.addStatement("date.day",
                    rightNow.get(Calendar.DAY_OF_MONTH));
            payload.addStatement("date.month.name",
                    rightNow.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK));
            payload.addStatement("date.month", rightNow.get(Calendar.MONTH) + 1);
            payload.addStatement("date.year",
                    rightNow.get(Calendar.YEAR));
            payload.addStatement("date.dow",
                    rightNow.get(Calendar.DAY_OF_WEEK));
            payload.addStatement("time.hour",
                    rightNow.get(Calendar.HOUR_OF_DAY));
            payload.addStatement("time.minute",
                    rightNow.get(Calendar.MINUTE));
            payload.addStatement("time.second",
                    rightNow.get(Calendar.SECOND));

            DateFormat datefmt = new SimpleDateFormat("yyyyMMdd");
            DateFormat timefmt = new SimpleDateFormat("HHmmss");
            payload.addStatement("time",
                    timefmt.format(rightNow.getTime()));
            payload.addStatement("date",
                    datefmt.format(rightNow.getTime()));
            //adding event.sender to event payload. So it can be used by trigger
            payload.addStatement("sender",
                    getSender());
        } catch (Exception e) {
            LOG.error("Error while generating default data for event", e);
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

    /**
     *
     * @param source
     */
    protected final void setSender(Object source) {
        if (source != null) {
            sender = source.getClass().getSimpleName();
        } else {
            sender = "UnknownSender";
        }
    }

    /**
     *
     * @return
     */
    public Payload getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return getEventName();
    }
}

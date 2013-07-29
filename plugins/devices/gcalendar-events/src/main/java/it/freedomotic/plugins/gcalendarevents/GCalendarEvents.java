/*
 Copyright FILE Enrico Nicoletti, 2012-2013

 This file is part of FREEDOMOTIC.

 FREEDOMOTIC is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 FREEDOMOTIC is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Freedomotic.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.freedomotic.plugins.gcalendarevents;

import com.google.gdata.client.calendar.*;
import com.google.gdata.data.DateTime;

import com.google.gdata.data.calendar.*;
import com.google.gdata.util.*;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.TriggerPersistence;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class GCalendarEvents extends Protocol {

    public GCalendarEvents() {
        super("Google Calendar UI", "/it.freedomotic.gcalendarevents/gcalendarevents-manifest.xml");
    }

    @Override
    public void onStop() {
        setDescription(configuration.getStringProperty("description", ""));
    }

    @Override
    protected void onRun() {
        try {
            String calendarID = configuration.getStringProperty("calendar-id", "");
            URL feedUrl = new URL("https://www.google.com/calendar/feeds/" + calendarID + "/public/full");
            Freedomotic.logger.info("Synch today events from google calendar " + feedUrl.toString());

            CalendarQuery myQuery = new CalendarQuery(feedUrl);
            Date startToday = new Date();
            Date endToday = new Date();
            endToday.setHours(23);
            endToday.setMinutes(59);
            endToday.setSeconds(59);

            //query for today events
            DateFormat dfGoogle = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
            myQuery.setMinimumStartTime(DateTime.parseDateTime(dfGoogle.format(startToday)));
            myQuery.setMaximumStartTime(DateTime.parseDateTime(dfGoogle.format(endToday)));

            CalendarService gcalendar = new CalendarService("freedomotic");

            // Send the request and receive the response:
            CalendarEventFeed resultFeed = gcalendar.query(myQuery, CalendarEventFeed.class);
            for (CalendarEventEntry entry : resultFeed.getEntries()) {
                Freedomotic.logger.info(
                        "Readed gcalendar event '" + entry.getTitle().getPlainText() + "' @"
                        + entry.getTimes().get(0).getStartTime().toUiString());
                Trigger t = new Trigger();
                t.setName(entry.getTitle().getPlainText());
                t.setDescription("A time based trigger readed from google calendar id " + calendarID);
                t.setChannel("app.event.sensor.calendar.event.schedule");
                Date date = new Date(entry.getTimes().get(0).getStartTime().getValue());
                Calendar cal = new GregorianCalendar();
                cal.setTime(date);
                t.getPayload().addStatement("time.hour", cal.get(Calendar.HOUR_OF_DAY));
                t.getPayload().addStatement("time.minute", cal.get(Calendar.MINUTE));
                t.getPayload().addStatement("time.second", "0"); //only at the first second of a minute
                t.getPayload().addStatement("date.day", cal.get(Calendar.DAY_OF_MONTH));
                t.getPayload().addStatement("date.month", cal.get(Calendar.MONTH) + 1);
                t.getPayload().addStatement("date.year", cal.get(Calendar.YEAR));
                t.setPersistence(true);
                TriggerPersistence.addAndRegister(t);
            }
            setDescription(resultFeed.getEntries().size() + " events synchronized with your online account");

        } catch (IOException ex) {
            Logger.getLogger(GCalendarEvents.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(GCalendarEvents.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

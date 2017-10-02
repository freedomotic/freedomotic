/**
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 * <p>
 * This file is part of Freedomotic
 * <p>
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * <p>
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.rules;

<<<<<<< HEAD
import java.time.*;
import java.time.format.DateTimeFormatter;
=======
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
>>>>>>> refs/remotes/freedomotic/master

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Enrico Nicoletti
 */
public class BetweenTime extends BinaryExpression {

    private static final String OPERAND = Statement.BETWEEN_TIME;
    private static final Logger LOG = LoggerFactory.getLogger(BetweenTime.class.getName());
<<<<<<< HEAD
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
=======
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private Date todaysEnd;
    private Date tomorrowStart;
>>>>>>> refs/remotes/freedomotic/master

    @Override
    public String getOperand() {
        return OPERAND;
    }

    // left is an hour in form HH:MM::SS
    //right is a time interval in form HH:MM::SS-HH:MM::SS
    //this class checks if left is inside the right interval
    public BetweenTime(String left, String right) {
        super(left, right);
    }

    @Override
    public Boolean evaluate() {

        LocalTime time;
        // Parse the date which is supposed to be in between of the interval
        try {
<<<<<<< HEAD
            time = LocalTime.parse(this.getLeft(), TIME_FORMAT);
        } catch (DateTimeException ex) {
=======
            time = TIME_FORMAT.parse(this.getLeft());
            if (Objects.isNull(time)) {
                return false;
            }
        } catch (ParseException ex) {
>>>>>>> refs/remotes/freedomotic/master
            LOG.warn("Cannot parse hours " + getLeft() + ", valid format is HH:mm:ss", ex);
            return false;
        }

        // Parse the hour interval HH:mm:ss-HH:mm:ss
        String[] interval = getRight().split("-");
        LocalTime intervalStart;
        LocalTime intervalEnd;
        try {
            intervalStart = LocalTime.parse(interval[0]);
            intervalEnd = LocalTime.parse(interval[1]);
        } catch (DateTimeException ex) {
            LOG.warn("Cannot parse hours interval " + getRight() + ", valid hour interval format is HH:mm:ss-HH:mm:ss", ex);
            return false;
        }

        // Check if the provider time is inside the provided interval
        if (intervalStart.isBefore(intervalEnd)) {
            //if the  time interval do not cross the day boundaries
            return time.isAfter(intervalStart) && time.isBefore(intervalEnd);
        } else {
            // the time interval is crossing days boundaries
            return time.isAfter(intervalStart) || time.isBefore(intervalEnd);
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nicoletti
 */
public class BetweenTime extends BinaryExpression {

    private static final String OPERAND = Statement.BETWEEN_TIME;
    private static final Logger LOG = Logger.getLogger(BetweenTime.class.getName());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    Date todaysEnd;
    Date tomorrowStart;

    @Override
    public String getOperand() {
        return OPERAND;
    }

    // left is an hour in form HH:MM::SS
    //right is a time interval in form HH:MM::SS-HH:MM::SS
    //this class checks if left is inside the right interval
    public BetweenTime(String left, String right) {
        super(left, right);
        try {
            todaysEnd = TIME_FORMAT.parse("24:00:00");
            tomorrowStart = TIME_FORMAT.parse("00:00:00");
        } catch (ParseException parseException) {
            throw new IllegalStateException("Should never reach this point");
        }
    }

    @Override
    public Boolean evaluate() {

        Date time = null;
        // Parse the date which is supposed to be in between of the interval
        try {
            time = TIME_FORMAT.parse(this.getLeft());
        } catch (ParseException ex) {
            LOG.log(Level.WARNING, "Cannot parse hours " + getLeft() + ", valid format is HH:mm:ss", ex);
        }

        // Parse the hour interval HH:mm:ss-HH:mm:ss
        String[] interval = getRight().split("-");
        Date rightDate = null;
        Date leftDate = null;
        try {
            leftDate = TIME_FORMAT.parse(interval[0]);
            rightDate = TIME_FORMAT.parse(interval[1]);
        } catch (ParseException ex) {
            LOG.log(Level.WARNING, "Cannot parse hours interval " + getRight() + ", valid hour interval format is HH:mm:ss-HH:mm:ss", ex);
        }

        Calendar timeCalendar = GregorianCalendar.getInstance();
        timeCalendar.setTime(time);
        Calendar intervalStart = GregorianCalendar.getInstance();
        intervalStart.setTime(leftDate);
        Calendar intervalEnd = GregorianCalendar.getInstance();
        intervalEnd.setTime(rightDate);

        // Check if the provider time is inside the provided interval
        if (intervalStart.before(intervalEnd)) {
            //if the  time interval do not cross the day boundaries
            return time.after(intervalStart.getTime()) && time.before(intervalEnd.getTime());
        } else {
            // the time interval is crossing days boundaries           
            return (((time.compareTo(intervalStart.getTime()) >= 0) && (time.compareTo(todaysEnd) <= 0))
                    || ((time.compareTo(tomorrowStart) >= 0) && (time.compareTo(intervalEnd.getTime()) <= 0)));

        }
    }

}

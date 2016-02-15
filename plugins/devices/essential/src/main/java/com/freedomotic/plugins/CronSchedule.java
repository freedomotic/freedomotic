/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.plugins;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Provides cron-like scheduling information. This class implements cron-like
 * definition of scheduling information. Various methods can be used to check
 * whether a timestamp matches the schedule or not. However, there is a slight
 * difference between cron and this class. Cron describes a match when either
 * the day of month and month or the day of week are met. This class requires
 * both to be met for a match. Also note that Calendar defines Sunday through
 * Saturday with 1 through 7 respectively
 *
 * @author RalphSchuster
 * http://techblog.ralph-schuster.eu/2008/02/01/handling-unix-cron-like-information-in-java/
 */
public class CronSchedule {

    /**
     * Types being used. This array defines the types and their indices.
     */
    protected static int[] TYPES =
            new int[]{Calendar.MINUTE, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.DAY_OF_WEEK};
    private AbstractTimeValue[][] timeValues = new AbstractTimeValue[TYPES.length][];

    /**
     * Default constructor Constructor with all terms set to "*".
     */
    public CronSchedule() {
        this("*", "*", "*", "*", "*");
    }

    /**
     * Constructor with cron-style string initialization. The cron style is:
     * $minute $hour $dayOfMonth $month $dayOfWeek
     *
     * @param schedule
     */
    public CronSchedule(String schedule) {
        set(schedule);
    }

    /**
     * Constructor with separate initialization values.
     *
     * @param min - minute definition
     * @param hour - hour definition
     * @param dom - day of month definition
     * @param mon - month definition
     * @param dow - day of week definition
     */
    public CronSchedule(String min, String hour, String dom, String mon, String dow) {
        set(Calendar.MINUTE, min);
        set(Calendar.HOUR_OF_DAY, hour);
        set(Calendar.DAY_OF_MONTH, dom);
        set(Calendar.MONTH, mon);
        set(Calendar.DAY_OF_WEEK, dow);
    }

    /**
     * Sets the cron schedule. The cron style is: $minute $hour $dayOfMonth
     * $month $dayOfWeek The function will return any characters that follow the
     * cron definition
     *
     * @param schedule - cron-like schedule definition
     * @return characters following the cron definition.
     */
    public String set(String schedule) {
        String[] parts = schedule.split(" ", TYPES.length + 1);

        if (parts.length < TYPES.length) {
            throw new IllegalArgumentException("Invalid cron format: " + schedule);
        }

        for (int i = 0; i < TYPES.length; i++) {
            set(getType(i),
                    parts[i]);
        }

        return (parts.length > TYPES.length) ? parts[TYPES.length] : null;
    }

    /**
     * Sets the time values accordingly
     *
     * @param type - Calendar constant to define what values will be set
     * @param values - comma-separated list of definitions for that type
     */
    public void set(int type, String values) {
        // Split the values
        String[] parts = values.split(",");
        AbstractTimeValue[] result = new AbstractTimeValue[parts.length];

        // Iterate over entries
        for (int i = 0; i < parts.length; i++) {
            // Decide what time value is set and create it
            if (parts[i].indexOf("/") > 0) {
                result[i] = new TimeSteps(parts[i]);
            } else if (parts[i].indexOf("-") > 0) {
                result[i] = new TimeRange(parts[i]);
            } else if (parts[i].equals("*")) {
                result[i] = new TimeAll();
            } else {
                result[i] = new SingleTimeValue(parts[i]);
            }
        }

        // Save the array
        set(type, result);
    }

    /**
     * Sets the values for a specific type
     *
     * @param type - Calendar constant defining the time type
     * @param values - values to be set
     */
    protected void set(int type, AbstractTimeValue[] values) {
        timeValues[getIndex(type)] = values;
    }

    /**
     * Returns the values for a specific time type
     *
     * @param type - Calendar constant defining the type
     * @return time value definitions
     */
    protected AbstractTimeValue[] getValues(int type) {
        return timeValues[getIndex(type)];
    }

    /**
     * Returns the cron-like definition string for the given time value
     *
     * @param type - Calendar constant defining time type
     * @return cron-like definition
     */
    public String get(int type) {
        AbstractTimeValue[] values = getValues(type);
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            buff.append(",").append(values[i].toString());
        }

        return buff.substring(1);
    }

    /**
     * Returns the cron-like definition of the schedule.
     * @return 
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < TYPES.length; i++) {
            buff.append(" ").append(get(getType(i)));
        }

        return buff.toString().trim();
    }

    /**
     * Checks whether given timestamp matches with defined schedule. This is
     * default check method. All criteria must be met including seconds to be 0.
     *
     * @param timeStamp - time in ms since Epoch time
     * @return true when schedule matches
     */
    public boolean matches(long timeStamp) {
        return matches(getCalendar(timeStamp));
    }

    /**
     * Checks whether given timestamp matches with defined schedule. This is
     * default check method. All criteria must be met including seconds to be 0.
     *
     * @param cal - calendar date
     * @return true when schedule matches
     */
    public boolean matches(Calendar cal) {
        return isMinute(cal) && (cal.get(Calendar.SECOND) == 0);
    }

    /**
     * Checks whether given timestamp matches with defined schedule. This method
     * can be used when seconds are not relevant for matching. This is default
     * check method.
     *
     * @param timeStamp - time in ms since Epoch time
     * @return true when schedule matches
     */
    public boolean isMinute(long timeStamp) {
        return isMinute(getCalendar(timeStamp));
    }

    /**
     * Checks whether given calendar date matches with defined schedule. This
     * method can be used when seconds are not relevant for matching.
     *
     * @param cal - calendar date
     * @return true when schedule matches
     */
    public boolean isMinute(Calendar cal) {
        return matches(Calendar.MINUTE, cal) && isHour(cal);
    }

    /**
     * Checks whether given timestamp matches with defined hour schedule. This
     * method can be used when minute definition is not relevant for matching.
     *
     * @param timeStamp
     * @param timestamp - time in ms since Epoch time
     * @return true when schedule matches
     */
    public boolean isHour(long timeStamp) {
        return isHour(getCalendar(timeStamp));
    }

    /**
     * Checks whether given calendar date matches with defined hour schedule.
     * This method can be used when minute definition is not relevant for
     * matching.
     *
     * @param cal - calendar date
     * @return true when schedule matches
     */
    public boolean isHour(Calendar cal) {
        return matches(Calendar.HOUR_OF_DAY, cal) && isDay(cal);
    }

    /**
     * Checks whether given timestamp matches with defined day schedule. This
     * method can be used when minute and hour definitions are not relevant for
     * matching.
     *
     * @param timeStamp
     * @param timestamp - time in ms since Epoch time
     * @return true when schedule matches
     */
    public boolean isDay(long timeStamp) {
        return isDay(getCalendar(timeStamp));
    }

    /**
     * Checks whether given calendar date matches with defined day schedule.
     * This method can be used when minute and hour definitions are not relevant
     * for matching.
     *
     * @param cal - calendar date
     * @return true when schedule matches
     */
    public boolean isDay(Calendar cal) {
        return matches(Calendar.DAY_OF_WEEK, cal) && matches(Calendar.DAY_OF_MONTH, cal)
                && matches(Calendar.MONTH, cal);
    }

    /**
     * Checks whether specific schedule definition matches against the given
     * calendar date.
     *
     * @param type - Calendar constant defining time type to check for
     * @param calendar - calendar representing the date to check
     * @return true when definition matches
     */
    protected boolean matches(int type, Calendar calendar) {
        // get the definitions and the comparison value
        AbstractTimeValue[] defs = timeValues[getIndex(type)];
        int value = calendar.get(type);

        // Any of the criteria must be met
        for (int i = 0; i < defs.length; i++) {
            if (defs[i].matches(value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates the calendar for a timestamp.
     *
     * @param timeStamp - timestamp
     * @return calendar
     */
    protected Calendar getCalendar(long timeStamp) {
        Calendar rc = new GregorianCalendar();
        rc.setTimeInMillis(timeStamp);

        return rc;
    }

    /**
     * Returns the type at the specified index
     *
     * @param index - index
     * @return Calendar constant of type
     */
    protected static int getType(int index) {
        return TYPES[index];
    }

    /**
     * Returns the index for the specified Calendar type.
     *
     * @param type - Calendar constant for type
     * @return internal index
     */
    protected static int getIndex(int type) {
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i] == type) {
                return i;
            }
        }

        throw new IllegalArgumentException("No such time type: " + type);
    }

    /**
     * Base class for timing values.
     *
     * @author RalphSchuster
     */
    public static abstract class AbstractTimeValue {

        /**
         * Returns true when given time value matches defined time.
         *
         * @param timeValue - time value to evaluate
         * @return true when time matches
         */
        public abstract boolean matches(int timeValue);
    }

    /**
     * Represents a single time value, e.g. 9
     *
     * @author RalphSchuster
     */
    public static class SingleTimeValue
            extends AbstractTimeValue {

        private int value;

        /**
         *
         * @param value
         */
        public SingleTimeValue(int value) {
            setValue(value);
        }

        /**
         *
         * @param value
         */
        public SingleTimeValue(String value) {
            setValue(Integer.parseInt(value));
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(int value) {
            this.value = value;
        }

        /**
         * Returns true when given time value matches defined value.
         *
         * @param timeValue - time value to evaluate
         * @return true when time matches
         */
        public boolean matches(int timeValue) {
            return timeValue == getValue();
        }

        /**
         * Returns cron-like string of this definition.
         * @return 
         */
        public String toString() {
            return "" + getValue();
        }
    }

    /**
     * Represents a time range, e.g. 5-9
     *
     * @author RalphSchuster
     */
    public static class TimeRange
            extends AbstractTimeValue {

        private int startValue;
        private int endValue;

        /**
         *
         * @param startValue
         * @param endValue
         */
        public TimeRange(int startValue, int endValue) {
            setStartValue(startValue);
            setEndValue(endValue);
        }

        /**
         *
         * @param range
         */
        public TimeRange(String range) {
            int dashPos = range.indexOf("-");
            setStartValue(Integer.parseInt(range.substring(0, dashPos)));
            setEndValue(Integer.parseInt(range.substring(dashPos + 1)));
        }

        /**
         * @return the endValue
         */
        public int getEndValue() {
            return endValue;
        }

        /**
         * @param endValue the endValue to set
         */
        public void setEndValue(int endValue) {
            this.endValue = endValue;
        }

        /**
         * @return the startValue
         */
        public int getStartValue() {
            return startValue;
        }

        /**
         * @param startValue the startValue to set
         */
        public void setStartValue(int startValue) {
            this.startValue = startValue;
        }

        /**
         * Returns true when given time value falls in range.
         *
         * @param timeValue - time value to evaluate
         * @return true when time falls in range
         */
        public boolean matches(int timeValue) {
            return (getStartValue() <= timeValue) && (timeValue <= getEndValue());
        }

        /**
         * Returns cron-like string of this definition.
         * @return 
         */
        public String toString() {
            return getStartValue() + "-" + getEndValue();
        }
    }

    /**
     * Represents a time interval, e.g. 0-4/10
     *
     * @author RalphSchuster
     */
    public static class TimeSteps
            extends AbstractTimeValue {

        private AbstractTimeValue range;
        private int steps;

        /**
         *
         * @param range
         * @param steps
         */
        public TimeSteps(AbstractTimeValue range, int steps) {
            setRange(range);
            setSteps(steps);
        }

        /**
         *
         * @param def
         */
        public TimeSteps(String def) {
            int divPos = def.indexOf("/");
            String r = def.substring(0, divPos);

            if (r.equals("*")) {
                setRange(new TimeAll());
            } else if (r.indexOf("-") > 0) {
                setRange(new TimeRange(r));
            } else {
                throw new IllegalArgumentException("Invalid range: " + def);
            }

            setSteps(Integer.parseInt(def.substring(divPos + 1)));
        }

        /**
         * Returns true when given time value matches the interval.
         *
         * @param timeValue - time value to evaluate
         * @return true when time matches the interval
         */
        public boolean matches(int timeValue) {
            boolean rc = getRange().matches(timeValue);

            if (rc) {
                rc = (timeValue % getSteps()) == 0;
            }

            return rc;
        }

        /**
         * @return the range
         */
        public AbstractTimeValue getRange() {
            return range;
        }

        /**
         * @param range the range to set
         */
        public void setRange(AbstractTimeValue range) {
            this.range = range;
        }

        /**
         * @return the steps
         */
        public int getSteps() {
            return steps;
        }

        /**
         * @param steps the steps to set
         */
        public void setSteps(int steps) {
            this.steps = steps;
        }

        /**
         * Returns cron-like string of this definition.
         * @return 
         */
        public String toString() {
            return getRange() + "/" + getSteps();
        }
    }

    /**
     * Represents the ALL time, *.
     *
     * @author RalphSchuster
     */
    public static class TimeAll
            extends AbstractTimeValue {

        /**
         *
         */
        public TimeAll() {
        }

        /**
         * Returns always true.
         *
         * @param timeValue - time value to evaluate
         * @return true
         */
        public boolean matches(int timeValue) {
            return true;
        }

        /**
         * Returns cron-like string of this definition.
         * @return 
         */
        public String toString() {
            return "*";
        }
    }
}

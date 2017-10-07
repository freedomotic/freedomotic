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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

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
	
	/** Maps the values to field types. */
	private static final ChronoField[] VALUE_TYPES = new ChronoField[] {
		ChronoField.MINUTE_OF_HOUR,
		ChronoField.HOUR_OF_DAY,
		ChronoField.DAY_OF_MONTH,
		ChronoField.MONTH_OF_YEAR,
		ChronoField.DAY_OF_WEEK
	};
	
	/** Values in the expression. */
	private final AbstractTimeValue[][] values = new AbstractTimeValue[VALUE_TYPES.length][];

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
        set(ChronoField.MINUTE_OF_HOUR, min);
        set(ChronoField.HOUR_OF_DAY, hour);
        set(ChronoField.DAY_OF_MONTH, dom);
        set(ChronoField.MONTH_OF_YEAR, mon);
        set(ChronoField.DAY_OF_WEEK, dow);
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
        final String[] parts = schedule.split(" ", VALUE_TYPES.length + 1);

        if (parts.length < VALUE_TYPES.length) {
            throw new IllegalArgumentException("Invalid cron format: " + schedule);
        }

        for (int i = 0; i < VALUE_TYPES.length; i++) {
            set(VALUE_TYPES[i],
                    parts[i]);
        }

        return (parts.length > VALUE_TYPES.length) ? parts[VALUE_TYPES.length] : null;
    }

    /**
     * Sets the time values accordingly
     *
     * @param field - {@link ChronoField} to define what values will be set
     * @param values - comma-separated list of definitions for that type
     */
    public void set(final ChronoField field, final String values) {
        // Split the values
        final String[] parts = values.split(",");
        final AbstractTimeValue[] result = new AbstractTimeValue[parts.length];

        // Iterate over entries
        for (int i = 0; i < parts.length; i++) {
            // Decide what time value is set and create it
            if (parts[i].indexOf('/') > 0) {
                result[i] = new TimeSteps(parts[i]);
            } else if (parts[i].indexOf('-') > 0) {
                result[i] = new TimeRange(parts[i]);
            } else if (parts[i].equals("*")) {
                result[i] = new TimeAll();
            } else {
                result[i] = new SingleTimeValue(parts[i]);
            }
        }

        // Save the array
        set(field, result);
    }

    /**
     * Sets the values for a specific type
     *
     * @param type - Calendar constant defining the time type
     * @param values - values to be set
     */
    private final void set(final ChronoField field, AbstractTimeValue[] values) {
        this.values[getIndex(field)] = values;
    }

    /**
     * Returns the values for a specific time type
     *
     * @param type - Calendar constant defining the type
     * @return time value definitions
     */
    protected final AbstractTimeValue[] getValues(final ChronoField type) {
        return this.values[getIndex(type)];
    }

    /**
     * Returns the cron-like definition string for the given time value
     *
     * @param type - Calendar constant defining time type
     * @return cron-like definition
     */
    public final String get(final ChronoField type) {
        AbstractTimeValue[] values = getValues(type);
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            buff.append(",").append(values[i].toString());
        }

        return buff.substring(1);
    }

    /**
     * Returns the cron-like definition of the schedule.
     * 
     * @return 	The {@link String} representation of this {@link CronSchedule}.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < VALUE_TYPES.length; i++) {
        	final ChronoField field = VALUE_TYPES[i];
            buff.append(" ").append(get(field));
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
    public final boolean matches(final long timeStamp) {
        return matches(LocalDateTime.from(Instant.ofEpochMilli(timeStamp)));
    }

    /**
     * Checks whether given timestamp matches with defined schedule. This is
     * default check method. All criteria must be met including seconds to be 0.
     *
     * @param instant - The {@link Instant}.
     * @return true when schedule matches
     */
    public final boolean matches(final LocalDateTime instant) {
        return isMinute(instant) && (instant.get(ChronoField.SECOND_OF_MINUTE) == 0);
    }

    /**
     * Checks whether given timestamp matches with defined schedule. This method
     * can be used when seconds are not relevant for matching. This is default
     * check method.
     *
     * @param timeStamp - time in ms since Epoch time
     * @return true when schedule matches
     */
    public final boolean isMinute(final long timeStamp) {
        return isMinute(LocalDateTime.from(Instant.ofEpochMilli(timeStamp)));
    }

    /**
     * Checks whether given calendar date matches with defined schedule. This
     * method can be used when seconds are not relevant for matching.
     *
     * @param instant - calendar date
     * @return true when schedule matches
     */
    public final boolean isMinute(final LocalDateTime instant) {
        return matches(ChronoField.MINUTE_OF_HOUR, instant) && isHour(instant);
    }

    /**
     * Checks whether given timestamp matches with defined hour schedule. This
     * method can be used when minute definition is not relevant for matching.
     *
     * @param timeStamp - time in ms since Epoch time
     * @return true when schedule matches
     */
    public final boolean isHour(long timeStamp) {
        return isHour(LocalDateTime.from(Instant.ofEpochMilli(timeStamp)));
    }

    /**
     * Checks whether given calendar date matches with defined hour schedule.
     * This method can be used when minute definition is not relevant for
     * matching.
     *
     * @param instant - calendar date
     * @return true when schedule matches
     */
    public final boolean isHour(final LocalDateTime instant) {
        return matches(ChronoField.HOUR_OF_DAY, instant) && isDay(instant);
    }

    /**
     * Checks whether given timestamp matches with defined day schedule. This
     * method can be used when minute and hour definitions are not relevant for
     * matching.
     *
     * @param timeStamp - time in ms since Epoch time
     * @return true when schedule matches
     */
    public final boolean isDay(final long timeStamp) {
        return isDay(LocalDateTime.from(Instant.ofEpochMilli(timeStamp)));
    }

    /**
     * Checks whether given calendar date matches with defined day schedule.
     * This method can be used when minute and hour definitions are not relevant
     * for matching.
     *
     * @param instant 		The {@link Instant}.
     * @return true when schedule matches
     */
    public final boolean isDay(final LocalDateTime instant) {
        return matches(ChronoField.DAY_OF_WEEK, instant) 
        	   && matches(ChronoField.DAY_OF_MONTH, instant)
               && matches(ChronoField.MONTH_OF_YEAR, instant);
    }

    /**
     * Checks if this expression matches a particular field of the given instant.
     * 
     * @param 	field		The {@link TemporalField}.
     * @param 	instant		The {@link LocalDateTime} to check against.
     * 
     * @return	<code>true</code> if the given {@link TemporalField} of the {@link LocalDateTime} matches this expression.
     */
    private final boolean matches(final TemporalField field, final LocalDateTime instant) {
        // get the definitions and the comparison value
        AbstractTimeValue[] defs = this.values[getIndex(field)];
        int value = instant.get(field);

        // Any of the criteria must be met
        for (int i = 0; i < defs.length; i++) {
            if (defs[i].matches(value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the index for the specified Calendar type.
     *
     * @param type - Calendar constant for type
     * @return internal index
     */
    private static final int getIndex(final TemporalField field) {
        for (int i = 0; i < VALUE_TYPES.length; i++) {
            if (VALUE_TYPES[i] == field) {
                return i;
            }
        }

        throw new IllegalArgumentException("No such time field: " + field);
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
            return this.value;
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
         * 
         * @return 	The {@link String} representation.
         */
        public String toString() {
            return Integer.toString(getValue());
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
            int dashPos = range.indexOf('-');
            setStartValue(Integer.parseInt(range.substring(0, dashPos)));
            setEndValue(Integer.parseInt(range.substring(dashPos + 1)));
        }

        /**
         * @return the endValue
         */
        public int getEndValue() {
            return this.endValue;
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
            return this.startValue;
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
         * 
         * @return 	The {@link String} representation.
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
            int divPos = def.indexOf('/');
            String r = def.substring(0, divPos);

            if (r.equals("*")) {
                setRange(new TimeAll());
            } else if (r.indexOf('-') > 0) {
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
            return this.range;
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
            return this.steps;
        }

        /**
         * @param steps the steps to set
         */
        public void setSteps(int steps) {
            this.steps = steps;
        }

        /**
         * Returns cron-like string of this definition.
         * 
         * @return 	The {@link String} representation.
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
         * 
         * @return 	The {@link String} representation.
         */
        public String toString() {
            return "*";
        }
    }
}

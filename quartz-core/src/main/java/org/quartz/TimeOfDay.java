/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */
package org.quartz;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents a time in hour, minute and second of any given day.
 * 
 * <p>The hour is in 24-hour convention, meaning values are from 0 to 23.</p> 
 * 
 * @see DailyTimeIntervalScheduleBuilder
 * 
 * @since 2.0.3
 * 
 * @author James House
 * @author Zemian Deng &lt;saltnlight5@gmail.com&gt;
 */
public class TimeOfDay implements Serializable {

    private static final long serialVersionUID = 2964774315889061771L;

    private final int hour;
    private final int minute;
    private final int second;
    
    /**
     * Create a TimeOfDay instance for the given hour, minute and second.
     * 
     * @param hour The hour of day, between 0 and 23.
     * @param minute The minute of the hour, between 0 and 59.
     * @param second The second of the minute, between 0 and 59.
     * @throws IllegalArgumentException if one or more of the input values is out of their valid range.
     */
    public TimeOfDay(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        validate();
    }
    
    /**
     * Create a TimeOfDay instance for the given hour and minute (at the zero second of the minute).
     * 
     * @param hour The hour of day, between 0 and 23.
     * @param minute The minute of the hour, between 0 and 59.
     * @throws IllegalArgumentException if one or more of the input values is out of their valid range.
     */
    public TimeOfDay(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.second = 0;
        validate();
    }
    
    private void validate() {
        if(hour < 0 || hour > 23)
            throw new IllegalArgumentException("Hour must be from 0 to 23");
        if(minute < 0 || minute > 59)
            throw new IllegalArgumentException("Minute must be from 0 to 59");
        if(second < 0 || second > 59)
            throw new IllegalArgumentException("Second must be from 0 to 59");
    }

    /**
     * Create a TimeOfDay instance for the given hour, minute and second.
     * 
     * @param hour The hour of day, between 0 and 23.
     * @param minute The minute of the hour, between 0 and 59.
     * @param second The second of the minute, between 0 and 59.
     * @return the time of day
     * @throws IllegalArgumentException if one or more of the input values is out of their valid range.
     */
    public static TimeOfDay hourMinuteAndSecondOfDay(int hour, int minute, int second) {
        return new TimeOfDay(hour, minute, second);
    }

    /**
     * Create a TimeOfDay instance for the given hour and minute (at the zero second of the minute).
     * 
     * @param hour The hour of day, between 0 and 23.
     * @param minute The minute of the hour, between 0 and 59.
     * @return the time of day
     * @throws IllegalArgumentException if one or more of the input values is out of their valid range.
     */
    public static TimeOfDay hourAndMinuteOfDay(int hour, int minute) {
        return new TimeOfDay(hour, minute);
    }
    
    /**
     * The hour of the day (between 0 and 23).
     * 
     * @return The hour of the day (between 0 and 23).
     */
    public int getHour() {
        return hour;
    }

    /**
     * The minute of the hour.
     * 
     * @return The minute of the hour (between 0 and 59).
     */
    public int getMinute() {
        return minute;
    }

    /**
     * The second of the minute.
     * 
     * @return The second of the minute (between 0 and 59).
     */
    public int getSecond() {
        return second;
    }

    /**
     * Determine if this time of day is before the given time of day.
     *
     * @param timeOfDay then time of day to test
     * @return true this time of day is before the given time of day.
     */
    public boolean before(TimeOfDay timeOfDay) {
        
        if(timeOfDay.hour > hour)
            return true;
        if(timeOfDay.hour < hour)
            return false;

        if(timeOfDay.minute > minute)
            return true;
        if(timeOfDay.minute < minute)
            return false;

        if(timeOfDay.second > second)
            return true;
        if(timeOfDay.second < second)
            return false;
        
        return false; // must be equal...
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof TimeOfDay))
            return false;
        
        TimeOfDay other = (TimeOfDay)obj;
        
        return (other.hour == hour && other.minute == minute && other.second == second);
    }

    @Override
    public int hashCode() {
        return (hour + 1) ^ (minute + 1) ^ (second + 1);
    }
    
    /**
     * Return a date with time of day reset to this object values. The millisecond value will be zero.
     *
     * @param dateTime the time to modify using this instance's values
     * @return the time modified by this instance's values
     */
    public Date getTimeOfDayForDate(Date dateTime) {
        if (dateTime == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.clear(Calendar.MILLISECOND);
        return cal.getTime();
    }
    
    /**
     * Create a TimeOfDay from the given date, in the system default TimeZone.
     * 
     * @param dateTime The java.util.Date from which to extract Hour, Minute and Second.
     * @return time of day
     */
    public static TimeOfDay hourAndMinuteAndSecondFromDate(Date dateTime) {
        return hourAndMinuteAndSecondFromDate(dateTime, null);
    }
    
    /**
     * Create a TimeOfDay from the given date, in the given TimeZone.
     * 
     * @param dateTime The java.util.Date from which to extract Hour, Minute and Second.
     * @param tz The TimeZone from which relate Hour, Minute and Second for the given date.  If null, system default
     * TimeZone will be used.
     * @return time of day
     */
    public static TimeOfDay hourAndMinuteAndSecondFromDate(Date dateTime, TimeZone tz) {
        if (dateTime == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        if(tz != null)
            cal.setTimeZone(tz);
        
        return new TimeOfDay(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    }
    
    /**
     * Create a TimeOfDay from the given date (at the zero-second), in the system default TimeZone.
     * 
     * @param dateTime The java.util.Date from which to extract Hour and Minute.
     * @return time of day
     */
    public static TimeOfDay hourAndMinuteFromDate(Date dateTime) {
        return hourAndMinuteFromDate(dateTime, null);
    }
    
    /**
     * Create a TimeOfDay from the given date (at the zero-second), in the system default TimeZone.
     * 
     * @param dateTime The java.util.Date from which to extract Hour and Minute.
     * @param tz The TimeZone from which relate Hour and Minute for the given date.  If null, system default
     * TimeZone will be used.
     * @return time of day
     */
    public static TimeOfDay hourAndMinuteFromDate(Date dateTime, TimeZone tz) {
        if (dateTime == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        if(tz != null)
            cal.setTimeZone(tz);
        
        return new TimeOfDay(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }
    
    @Override
    public String toString() {
        return "TimeOfDay[" + hour + ":" + minute + ":" + second + "]";
    }
}

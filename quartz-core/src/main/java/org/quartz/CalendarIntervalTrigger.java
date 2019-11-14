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

import java.util.Calendar;
import java.util.TimeZone;

import org.quartz.DateBuilder.IntervalUnit;

/**
 * A concrete <code>{@link Trigger}</code> that is used to fire a <code>{@link org.quartz.JobDetail}</code>
 * based upon repeating calendar time intervals.
 * 
 * <p>The trigger will fire every N (see {@link #getRepeatInterval()} ) units of calendar time
 * (see {@link #getRepeatIntervalUnit()}) as specified in the trigger's definition.
 * This trigger can achieve schedules that are not possible with {@link SimpleTrigger} (e.g 
 * because months are not a fixed number of seconds) or {@link CronTrigger} (e.g. because
 * "every 5 months" is not an even divisor of 12).</p>
 * 
 * <p>If you use an interval unit of <code>MONTH</code> then care should be taken when setting
 * a <code>startTime</code> value that is on a day near the end of the month.  For example,
 * if you choose a start time that occurs on January 31st, and have a trigger with unit
 * <code>MONTH</code> and interval <code>1</code>, then the next fire time will be February 28th, 
 * and the next time after that will be March 28th - and essentially each subsequent firing will 
 * occur on the 28th of the month, even if a 31st day exists.  If you want a trigger that always
 * fires on the last day of the month - regardless of the number of days in the month, 
 * you should use <code>CronTrigger</code>.</p> 
 * 
 * @see TriggerBuilder
 * @see CalendarIntervalScheduleBuilder
 * @see SimpleScheduleBuilder
 * @see CronScheduleBuilder
 * 
 * @author James House
 */
public interface CalendarIntervalTrigger extends Trigger {

    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CalendarIntervalTrigger}</code> wants to be 
     * fired now by <code>Scheduler</code>.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = 1;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CalendarIntervalTrigger}</code> wants to have it's
     * next-fire-time updated to the next time in the schedule after the
     * current time (taking into account any associated <code>{@link Calendar}</code>,
     * but it does not want to be fired now.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_DO_NOTHING = 2;

    /**
     * <p>Get the interval unit - the time unit on with the interval applies.</p>
     *
     * @return the repeat interval unit
     */
    public IntervalUnit getRepeatIntervalUnit();

    /**
     * <p>
     * Get the the time interval that will be added to the <code>DateIntervalTrigger</code>'s
     * fire time (in the set repeat interval unit) in order to calculate the time of the 
     * next trigger repeat.
     * </p>
     *
     * @return the repeat interval
     */
    public int getRepeatInterval();

    /**
     * <p>
     * Get the number of times the <code>DateIntervalTrigger</code> has already
     * fired.
     * </p>
     *
     * @return the times triggered
     */
    public int getTimesTriggered();

    /**
     * <p>
     * Gets the time zone within which time calculations related to this 
     * trigger will be performed.
     * </p>
     * 
     * <p>
     * If null, the system default TimeZone will be used.
     * </p>
     *
     * @return the time zone
     */
    public TimeZone getTimeZone();
    
    
    /**
     * If intervals are a day or greater, this property (set to true) will 
     * cause the firing of the trigger to always occur at the same time of day,
     * (the time of day of the startTime) regardless of daylight saving time 
     * transitions.  Default value is false.
     * 
     * <p>
     * For example, without the property set, your trigger may have a start 
     * time of 9:00 am on March 1st, and a repeat interval of 2 days.  But 
     * after the daylight saving transition occurs, the trigger may start 
     * firing at 8:00 am every other day.
     * </p>
     * 
     * <p>
     * If however, the time of day does not exist on a given day to fire
     * (e.g. 2:00 am in the United States on the days of daylight saving
     * transition), the trigger will go ahead and fire one hour off on 
     * that day, and then resume the normal hour on other days.  If
     * you wish for the trigger to never fire at the "wrong" hour, then
     * you should set the property skipDayIfHourDoesNotExist.
     * </p>
     *
     * @return if hour is preserved across daylight savings
     *
     * @see #isSkipDayIfHourDoesNotExist()
     * @see #getStartTime()
     * @see #getTimeZone()
     */
    public boolean isPreserveHourOfDayAcrossDaylightSavings();
    
    /**
     * If intervals are a day or greater, and 
     * preserveHourOfDayAcrossDaylightSavings property is set to true, and the
     * hour of the day does not exist on a given day for which the trigger 
     * would fire, the day will be skipped and the trigger advanced a second
     * interval if this property is set to true.  Defaults to false.
     * 
     * <p>
     * <b>CAUTION!</b>  If you enable this property, and your hour of day happens 
     * to be that of daylight savings transition (e.g. 2:00 am in the United 
     * States) and the trigger's interval would have had the trigger fire on
     * that day, then you may actually completely miss a firing on the day of 
     * transition if that hour of day does not exist on that day!  In such a 
     * case the next fire time of the trigger will be computed as double (if 
     * the interval is 2 days, then a span of 4 days between firings will 
     * occur).
     * </p>
     *
     * @return if day is skipped if hour does not exist
     *
     * @see #isPreserveHourOfDayAcrossDaylightSavings()
     */
    public boolean isSkipDayIfHourDoesNotExist();
    
    
    TriggerBuilder<CalendarIntervalTrigger> getTriggerBuilder();
}

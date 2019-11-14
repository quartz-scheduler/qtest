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

/**
 * The public interface for inspecting settings specific to a CronTrigger, .
 * which is used to fire a <code>{@link org.quartz.Job}</code>
 * at given moments in time, defined with Unix 'cron-like' schedule definitions.
 * 
 * <p>
 * For those unfamiliar with "cron", this means being able to create a firing
 * schedule such as: "At 8:00am every Monday through Friday" or "At 1:30am
 * every last Friday of the month".
 * </p>
 * 
 * <p>
 * The format of a "Cron-Expression" string is documented on the 
 * {@link org.quartz.CronExpression} class.
 * </p>
 * 
 * <p>
 * Here are some full examples:
 * </p>
 *
 * <table cellspacing="8" summary="">
 * <tr>
 *   <th align="left">Expression</th>
 *   <th align="left">&nbsp;</th>
 *   <th align="left">Meaning</th>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 0 12 * * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 12pm (noon) every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 ? * *"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 * * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 * * ? *"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 * * ? 2005"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am every day during the year 2005</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 * 14 * * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire every minute starting at 2pm and ending at 2:59pm, every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 0/5 14 * * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire every 5 minutes starting at 2pm and ending at 2:55pm, every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 0/5 14,18 * * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire every 5 minutes starting at 2pm and ending at 2:55pm, AND fire every 5 minutes starting at 6pm and ending at 6:55pm, every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 0-5 14 * * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire every minute starting at 2pm and ending at 2:05pm, every day</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 10,44 14 ? 3 WED"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 2:10pm and at 2:44pm every Wednesday in the month of March.</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 ? * MON-FRI"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am every Monday, Tuesday, Wednesday, Thursday and Friday</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 15 * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am on the 15th day of every month</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 L * ?"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am on the last day of every month</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 ? * 6L"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am on the last Friday of every month</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 ? * 6L"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am on the last Friday of every month</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 ? * 6L 2002-2005"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am on every last Friday of every month during the years 2002, 2003, 2004 and 2005</code></td>
 * </tr>
 * <tr>
 *   <td align="left"><code>"0 15 10 ? * 6#3"</code></td>
 *   <td align="left">&nbsp;</td>
 *   <td align="left"><code>Fire at 10:15am on the third Friday of every month</code></td>
 * </tr>
 * </table>
 *
 * <p>
 * Pay attention to the effects of '?' and '*' in the day-of-week and
 * day-of-month fields!
 * </p>
 * 
 * <p>
 * <b>NOTES:</b>
 * </p>
 * <ul>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in on of these fields).
 * </li>
 * <li>Be careful when setting fire times between mid-night and 1:00 AM -
 * "daylight savings" can cause a skip or a repeat depending on whether the
 * time moves back or jumps forward.</li>
 * </ul>
 *
 * @see CronScheduleBuilder
 * @see TriggerBuilder
 * 
 * @author jhouse
 * @author Contributions from Mads Henderson
 */
public interface CronTrigger extends Trigger {

    public static final long serialVersionUID = -8644953146451592766L;
    
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CronTrigger}</code> wants to be fired now
     * by <code>Scheduler</code>.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = 1;
    
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CronTrigger}</code> wants to have it's
     * next-fire-time updated to the next time in the schedule after the
     * current time (taking into account any associated <code>{@link Calendar}</code>,
     * but it does not want to be fired now.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_DO_NOTHING = 2;

    public String getCronExpression();

    /**
     * <p>
     * Returns the time zone for which the <code>cronExpression</code> of
     * this <code>CronTrigger</code> will be resolved.
     * </p>
     *
     * @return this trigger's time zone
     */
    public TimeZone getTimeZone();

    public String getExpressionSummary();

    TriggerBuilder<CronTrigger> getTriggerBuilder();
}

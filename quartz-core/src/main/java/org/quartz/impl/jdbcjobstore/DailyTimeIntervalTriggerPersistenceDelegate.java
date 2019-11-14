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
package org.quartz.impl.jdbcjobstore;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.DailyTimeIntervalTrigger;
import org.quartz.TimeOfDay;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl;
import org.quartz.spi.OperableTrigger;

/**
 * Persist a DailyTimeIntervalTrigger by converting internal fields to and from
 * SimplePropertiesTriggerProperties.
 * 
 * @see DailyTimeIntervalScheduleBuilder
 * @see DailyTimeIntervalTrigger
 * 
 * @since 2.1.0
 * 
 * @author Zemian Deng &lt;saltnlight5@gmail.com&gt;
 */
public class DailyTimeIntervalTriggerPersistenceDelegate extends SimplePropertiesTriggerPersistenceDelegateSupport {

    public boolean canHandleTriggerType(OperableTrigger trigger) {
        return ((trigger instanceof DailyTimeIntervalTrigger) && !((DailyTimeIntervalTriggerImpl)trigger).hasAdditionalProperties());
    }

    public String getHandledTriggerTypeDiscriminator() {
        return TTYPE_DAILY_TIME_INT;
    }

    @Override
    protected SimplePropertiesTriggerProperties getTriggerProperties(OperableTrigger trigger) {
        DailyTimeIntervalTriggerImpl dailyTrigger = (DailyTimeIntervalTriggerImpl)trigger;
        SimplePropertiesTriggerProperties props = new SimplePropertiesTriggerProperties();
        
        props.setInt1(dailyTrigger.getRepeatInterval());
        props.setString1(dailyTrigger.getRepeatIntervalUnit().name());
        props.setInt2(dailyTrigger.getTimesTriggered());
        
        Set<Integer> days = dailyTrigger.getDaysOfWeek();
        String daysStr = join(days, ",");
        props.setString2(daysStr);

        StringBuilder timeOfDayBuffer = new StringBuilder();
        TimeOfDay startTimeOfDay = dailyTrigger.getStartTimeOfDay();
        if (startTimeOfDay != null) {
            timeOfDayBuffer.append(startTimeOfDay.getHour()).append(",");
            timeOfDayBuffer.append(startTimeOfDay.getMinute()).append(",");
            timeOfDayBuffer.append(startTimeOfDay.getSecond()).append(",");
        } else {
            timeOfDayBuffer.append(",,,");
        }
        TimeOfDay endTimeOfDay = dailyTrigger.getEndTimeOfDay();
        if (endTimeOfDay != null) {
            timeOfDayBuffer.append(endTimeOfDay.getHour()).append(",");
            timeOfDayBuffer.append(endTimeOfDay.getMinute()).append(",");
            timeOfDayBuffer.append(endTimeOfDay.getSecond());
        } else {
            timeOfDayBuffer.append(",,,");
        }
        props.setString3(timeOfDayBuffer.toString());
        
        props.setLong1(dailyTrigger.getRepeatCount());
        
        return props;
    }

    private String join(Set<Integer> days, String sep) {
        StringBuilder sb = new StringBuilder();
        if (days == null || days.size() <= 0)
            return "";
        
        Iterator<Integer> itr = days.iterator();
        sb.append(itr.next());
        while(itr.hasNext()) {
            sb.append(sep).append(itr.next());
        }
        return sb.toString();
    }

    @Override
    protected TriggerPropertyBundle getTriggerPropertyBundle(SimplePropertiesTriggerProperties props) {
        int repeatCount = (int)props.getLong1();
        int interval = props.getInt1();
        String intervalUnitStr = props.getString1();
        String daysOfWeekStr = props.getString2();
        String timeOfDayStr = props.getString3();

        IntervalUnit intervalUnit = IntervalUnit.valueOf(intervalUnitStr);
        DailyTimeIntervalScheduleBuilder scheduleBuilder = DailyTimeIntervalScheduleBuilder
                .dailyTimeIntervalSchedule()
                .withInterval(interval, intervalUnit)
                .withRepeatCount(repeatCount);
                
        if (daysOfWeekStr != null) {
            Set<Integer> daysOfWeek = new HashSet<Integer>();
            String[] nums = daysOfWeekStr.split(",");
            if (nums.length > 0) {
                for (String num : nums) {
                    daysOfWeek.add(Integer.parseInt(num));
                }
                scheduleBuilder.onDaysOfTheWeek(daysOfWeek);
            }
        } else {
            scheduleBuilder.onDaysOfTheWeek(DailyTimeIntervalScheduleBuilder.ALL_DAYS_OF_THE_WEEK);
        }
        
        if (timeOfDayStr != null) {
            String[] nums = timeOfDayStr.split(",");
            TimeOfDay startTimeOfDay;
            if (nums.length >= 3) {
                int hour = Integer.parseInt(nums[0]);
                int min = Integer.parseInt(nums[1]);
                int sec = Integer.parseInt(nums[2]);
                startTimeOfDay = new TimeOfDay(hour, min, sec);
            } else {
                startTimeOfDay = TimeOfDay.hourMinuteAndSecondOfDay(0, 0, 0);
            }
            scheduleBuilder.startingDailyAt(startTimeOfDay);

            TimeOfDay endTimeOfDay;
            if (nums.length >= 6) {
                int hour = Integer.parseInt(nums[3]);
                int min = Integer.parseInt(nums[4]);
                int sec = Integer.parseInt(nums[5]);
                endTimeOfDay = new TimeOfDay(hour, min, sec);
            } else {
                endTimeOfDay = TimeOfDay.hourMinuteAndSecondOfDay(23, 59, 59);
            }
            scheduleBuilder.endingDailyAt(endTimeOfDay);
        } else {
            scheduleBuilder.startingDailyAt(TimeOfDay.hourMinuteAndSecondOfDay(0, 0, 0));
            scheduleBuilder.endingDailyAt(TimeOfDay.hourMinuteAndSecondOfDay(23, 59, 59));
        }
        
        int timesTriggered = props.getInt2();
        String[] statePropertyNames = { "timesTriggered" };
        Object[] statePropertyValues = { timesTriggered };

        return new TriggerPropertyBundle(scheduleBuilder, statePropertyNames, statePropertyValues);
    }
}

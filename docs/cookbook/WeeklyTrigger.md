---
title: Cookbook
visible_title: "Quartz Cookbook"
active_sub_menu_id: site_mnu_docs_cookbook
---
<div class="secNavPanel"><a href=".">Contents</a> | <a href="BiDailyTrigger.md">&lsaquo;&nbsp;Prev</a> | <a href="BiWeeklyTrigger.md">Next&nbsp;&rsaquo;</a></div>





# How-To: Trigger That Executes Every Week


If you want a trigger that always fires at a certain time of day, use CronTrigger or CalendarIntervalTrigger because
they can preserve the fire time's time of day across daylight savings time changes.


### Using CronTrigger

Create a CronTrigger. that executes every Wednesday at 3:00PM:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startNow()
    .withSchedule(weeklyOnDayAndHourAndMinute(DateBuilder.WEDNESDAY, 15, 0)) // fire every wednesday at 15:00
    .build();

</code></pre>


OR -


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startNow()
    .withSchedule(cronSchedule("0 0 15 ? * WED")) // fire every wednesday at 15:00
    .build();

</code></pre>



### Using SimpleTrigger

Create a SimpleTrigger that executes 3:00PM tomorrow, and then every 7 * 24 hours (which may not always be at 3:00 PM -
because adding 24 hours on days where daylight savings time shifts may result in 2:00 PM or 4:00 PM depending upon
whether the 3:00 PM time was started during DST or standard time):


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(tomorrowAt(15, 0, 0)  // first fire time 15:00:00 tomorrow
    .withSchedule(simpleSchedule()
            .withIntervalInHours(7 * 24) // interval is actually set at 7 * 24 hours' worth of milliseconds
            .repeatForever())
    .build();

</code></pre>


### Using CalendarIntervalTrigger


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(tomorrowAt(15, 0, 0)  // 15:00:00 tomorrow
    .withSchedule(calendarIntervalSchedule()
            .withIntervalInWeeks(1)) // interval is set in calendar weeks
    .build();

</code></pre>

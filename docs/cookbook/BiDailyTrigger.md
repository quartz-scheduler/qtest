---
title: Cookbook
visible_title: "Quartz Cookbook"
active_sub_menu_id: site_mnu_docs_cookbook
---
<div class="secNavPanel"><a href=".">Contents</a> | <a href="DailyTrigger.md">&lsaquo;&nbsp;Prev</a> | <a href="WeeklyTrigger.md">Next&nbsp;&rsaquo;</a></div>





# How-To: Trigger That Executes Every 2 Days

At first glance, you may be tempted to use a CronTrigger. However, if this is truly to be every two days, CronTrigger won't work. To illustrate this, simply think of how many days are in a typical month (28-31). A cron expression like "0 0 5 2/2 * ?" would give us a trigger that would restart its count at the beginning of every month. This means that we would would get subsequent firings on July 30 and August 2, which is an interval of three days, not two.

Likewise, an expression like "0 0 5 1/2 * ?" would end up firing on July 31 and August 1, just one day apart.

Therefore, for this schedule, using SimpleTrigger or CalendarIntervalTrigger makes sense:

### Using SimpleTrigger

Create a SimpleTrigger that executes 3:00PM tomorrow, and then every 48 hours (which may not always be at 3:00 PM -
because adding 24 hours on days where daylight savings time shifts may result in 2:00 PM or 4:00 PM depending upon
whether the 3:00 PM time was started during DST or standard time):


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(tomorrowAt(15, 0, 0)  // first fire time 15:00:00 tomorrow
    .withSchedule(simpleSchedule()
            .withIntervalInHours(2 * 24) // interval is actually set at 48 hours' worth of milliseconds
            .repeatForever())
    .build();
</code></pre>


### Using CalendarIntervalTrigger


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(tomorrowAt(15, 0, 0)  // 15:00:00 tomorrow
    .withSchedule(calendarIntervalSchedule()
            .withIntervalInDays(2)) // interval is set in calendar days
    .build();
</code></pre>

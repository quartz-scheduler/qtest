---
title: Cookbook
visible_title: "Quartz Cookbook"
active_sub_menu_id: site_mnu_docs_cookbook
---
<div class="secNavPanel"><a href=".">Contents</a> | <a href="WeeklyTrigger.md">&lsaquo;&nbsp;Prev</a> | <a href="MonthlyTrigger.md">Next&nbsp;&rsaquo;</a></div>





# How-To: Trigger That Executes Every 2 Weeks

As with a trigger meant to fire every two days, CronTrigger won't work for this schedule. For more details, see <a href="BiDailyTrigger.md">Trigger That Fires Every 2 Days</a>. We'll need to use a SimpleTrigger or CalendarIntervalTrigger:


### Using SimpleTrigger

Create a SimpleTrigger that executes 3:00PM tomorrow, and then every 48 hours (which may not always be at 3:00 PM -
because adding 24 hours on days where daylight savings time shifts may result in 2:00 PM or 4:00 PM depending upon
whether the 3:00 PM time was started during DST or standard time):


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(tomorrowAt(15, 0, 0)  // first fire time 15:00:00 tomorrow
    .withSchedule(simpleSchedule()
            .withIntervalInHours(14 * 24) // interval is actually set at 14 * 24 hours' worth of milliseconds
            .repeatForever())
    .build();
</code></pre>


### Using CalendarIntervalTrigger


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(tomorrowAt(15, 0, 0)  // 15:00:00 tomorrow
    .withSchedule(calendarIntervalSchedule()
            .withIntervalInWeeks(2)) // interval is set in calendar weeks
    .build();
</code></pre>

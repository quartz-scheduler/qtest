---
title: Cookbook
visible_title: "Quartz Cookbook"
active_sub_menu_id: site_mnu_docs_cookbook
---
<div class="secNavPanel"><a href=".">Contents</a> | <a href="BiWeeklyTrigger.md">&lsaquo;&nbsp;Prev</a> </div>





# How-To: Trigger That Executes Every Month

### Using CronTrigger

Create a CronTrigger. that executes every Wednesday at 3:00PM:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startNow()
    .withSchedule(monthlyOnDayAndHourAndMinute(5, 15, 0)) // fire on the 5th day of every month at 15:00
    .build();
</code></pre>


OR -


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startNow()
    .withSchedule(cronSchedule("0 0 15 5 * ?")) // fire on the 5th day of every month at 15:00
    .build();
</code></pre>


OR -


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startNow()
    .withSchedule(cronSchedule("0 0 15 L * ?")) // fire on the last day of every month at 15:00
    .build();
</code></pre>


OR -


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startNow()
    .withSchedule(cronSchedule("0 0 15 LW * ?")) // fire on the last weekday day of every month at 15:00
    .build();
</code></pre>


OR -


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startNow()
    .withSchedule(cronSchedule("0 0 15 L-3 * ?")) // fire on the third to last day of every month at 15:00
    .build();
</code></pre>


There are other possible combinations as well, which are more fully covered in the API documentation. All of these options were made by simply changing the day-of-month field. Imagine what you can do if you leverage the other fields as well!

### Using CalendarIntervalTrigger


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(tomorrowAt(15, 0, 0)  // 15:00:00 tomorrow
    .withSchedule(calendarIntervalSchedule()
            .withIntervalInMonths(1)) // interval is set in calendar months
    .build();
</code></pre>

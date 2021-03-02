---
title: Tutorial 4
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-03.md" title="Go to Lesson 3">&lsaquo;&nbsp;Lesson 3</a> |
          <a href="tutorial-lesson-05.md" title="Go to Lesson 5">Lesson 5&nbsp;&rsaquo;</a>
</div>

## Lesson 4: More About Triggers

Like jobs, triggers are quite easy to work with, but do contain a variety of customizable options that you
need to be aware of and understand before you can make full use of Quartz. Also, as noted earlier, there are different
types of triggers that you can select from to meet different scheduling needs.

You will learn about the two most common types of triggers in <a href="tutorial-lesson-05.md"
    title="Tutorial Lesson 5">Lesson 5: Simple Triggers</a> and <a href="tutorial-lesson-06.md" title="Tutorial Lesson 6">Lesson 6: Cron Triggers</a>.

### [Common Trigger Attributes](#TutorialLesson4-CommonAttrs)

Aside from the fact that all trigger types have TriggerKey properties for tracking their identities, there are
a number of other properties that are common to all trigger types.  These common properties are set using the
TriggerBuilder when you are building the trigger definition (examples of that will follow).

Here is a listing of properties common to all trigger types:


+ The "jobKey" property indicates the identity of the job that should be executed when the trigger fires.
+ The "startTime" property indicates when the trigger's schedule first comes into affect.  The value is a
*java.util.Date* object that defines a moment in time on a given calendar date. For some trigger types, the
trigger will actually fire at the start time, for others it simply marks the time that the schedule should start being
followed.  This means you can store a trigger with a schedule such as "every 5th day of the month" during January,
and if the startTime property is set to April 1st, it will be a few months before the first firing.
+ The "endTime" property indicates when the trigger's schedule should no longer be in effect.  In other words, a
trigger with a schedule of "every 5th day of the month" and with an end time of July 1st will fire for it's last time
on June 5th.


Other properties, which take a bit more explanation are discussed in the following sub-sections.

### [Priority](#TutorialLesson4-Priority)

Sometimes, when you have many Triggers (or few worker threads in your Quartz thread pool), Quartz may not have
enough resources to immediately fire all of the Triggers that are scheduled to fire at the same time. In this
case, you may want to control which of your Triggers get first crack at the available Quartz worker threads. For
this purpose, you can set the *priority* property on a Trigger. If N Triggers are to fire at the same time,
but there are only Z worker threads currently available, then the first Z Triggers with the *highest* priority
will be executed first. If you do not set a priority on a Trigger, then it will use the default priority of 5.
Any integer value is allowed for priority, positive or negative.

**Note:** Priorities are only compared when triggers have the same fire time.  A trigger scheduled to fire at
10:59 will always fire before one scheduled to fire at 11:00.

**Note:** When a trigger's job is detected to require recovery, its recovery is scheduled with the same priority
as the original trigger.

### [Misfire Instructions](#TutorialLesson4-MisfireInstructions)

Another important property of a Trigger is its "misfire instruction". A misfire occurs if a persistent trigger
"misses" its firing time because of the scheduler being shutdown, or because there are no available threads in Quartz's
thread pool for executing the job. The different trigger types have different misfire instructions available to them. By
default they use a 'smart policy' instruction - which has dynamic behavior based on trigger type and configuration. When
the scheduler starts, it searches for any persistent triggers that have misfired, and it then updates each of them based
on their individually configured misfire instructions. When you start using Quartz in your own projects, you should make
yourself familiar with the misfire instructions that are defined on the given trigger types, and explained in their
JavaDoc. More specific information about misfire instructions will be given within the tutorial lessons specific to each
trigger type.

### [Calendars](#TutorialLesson4-Calendars)

Quartz ***Calendar*** objects (not java.util.Calendar objects) can be associated with triggers at the
time the trigger is defined and stored in the scheduler. Calendars are useful for excluding blocks of time from the the
trigger's firing schedule. For instance, you could create a trigger that fires a job every weekday at 9:30 am, but then
add a Calendar that excludes all of the business's holidays.

Calendar's can be any serializable objects that implement the Calendar interface, which looks like this:

**The Calendar Interface**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
package org.quartz;

public interface Calendar {

  public boolean isTimeIncluded(long timeStamp);

  public long getNextIncludedTime(long timeStamp);

}
</code></pre>

Notice that the parameters to these methods are of the long type. As you may guess, they are timestamps in
millisecond format. This means that calendars can 'block out' sections of time as narrow as a millisecond. Most likely,
you'll be interested in 'blocking-out' entire days. As a convenience, Quartz includes the class
org.quartz.impl.HolidayCalendar, which does just that.

Calendars must be instantiated and registered with the scheduler via the addCalendar(..) method. If you use
HolidayCalendar, after instantiating it, you should use its addExcludedDate(Date date) method in order to populate it
with the days you wish to have excluded from scheduling. The same calendar instance can be used with multiple triggers
such as this:

**Calendar Example**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
HolidayCalendar cal = new HolidayCalendar();
cal.addExcludedDate( someDate );
cal.addExcludedDate( someOtherDate );

sched.addCalendar("myHolidays", cal, false);


Trigger t = newTrigger()
    .withIdentity("myTrigger")
    .forJob("myJob")
    .withSchedule(dailyAtHourAndMinute(9, 30)) // execute job daily at 9:30
    .modifiedByCalendar("myHolidays") // but not on holidays
    .build();

// .. schedule job with trigger

Trigger t2 = newTrigger()
    .withIdentity("myTrigger2")
    .forJob("myJob2")
    .withSchedule(dailyAtHourAndMinute(11, 30)) // execute job daily at 11:30
    .modifiedByCalendar("myHolidays") // but not on holidays
    .build();

// .. schedule job with trigger2
</code></pre>


The details of the construction/building of triggers will be given in the next couple lessons. For
now, just believe that the code above creates two triggers, each scheduled to fire daily. However, any of the firings
that would have occurred during the period excluded by the calendar will be skipped.

See the *org.quartz.impl.calendar* package for a number of Calendar implementations that may suit your
needs.

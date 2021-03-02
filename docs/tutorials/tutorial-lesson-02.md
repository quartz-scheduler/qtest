---
title: Tutorial 2
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-01.md" title="Go to Lesson 1">&lsaquo;&nbsp;Lesson 1</a> |
          <a href="tutorial-lesson-03.md" title="Go to Lesson 3">Lesson 3&nbsp;&rsaquo;</a> |
</div>

## Lesson 2: The Quartz API, Jobs And Triggers

### [The Quartz API](#TutorialLesson2-QuartzAPI)

The key interfaces of the Quartz API are:

+ Scheduler - the main API for interacting with the scheduler.
+ Job - an interface to be implemented by components that you wish to have executed by the scheduler.
+ JobDetail - used to define instances of Jobs.
+ Trigger - a component that defines the schedule upon which a given Job will be executed.
+ JobBuilder - used to define/build JobDetail instances, which define instances of Jobs.
+ TriggerBuilder - used to define/build Trigger instances.


A **Scheduler**'s life-cycle is bounded by it's creation, via a **SchedulerFactory** and
a call to its *shutdown()* method.  Once created the Scheduler interface can be used add, remove, and list
Jobs and Triggers, and perform other scheduling-related operations (such as pausing a trigger).  However, the
Scheduler will not actually act on any triggers (execute jobs) until it has been started with the *start()*
method, as shown in <a href="tutorial-lesson-01.html" title="Go to Lesson 1">Lesson 1</a>.       

Quartz provides "builder" classes that define a Domain Specific Language (or DSL, also sometimes referred to as
a "fluent interface"). In the previous lesson you saw an example of it, which we present a portion of here again:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  // define the job and tie it to our HelloJob class
  JobDetail job = newJob(HelloJob.class)
      .withIdentity("myJob", "group1") // name "myJob", group "group1"
      .build();

  // Trigger the job to run now, and then every 40 seconds
  Trigger trigger = newTrigger()
      .withIdentity("myTrigger", "group1")
      .startNow()
      .withSchedule(simpleSchedule()
          .withIntervalInSeconds(40)
          .repeatForever())            
      .build();

  // Tell quartz to schedule the job using our trigger
  sched.scheduleJob(job, trigger);
</code></pre>


The block of code that builds the job definition is using methods that were statically imported from the
***JobBuilder*** class.  Likewise, the block of code that builds the trigger is using methods imported
from the ***TriggerBuilder*** class - as well as from the ***SimpleScheduleBuilder*** class.

The static imports of the DSL can be achieved through import statements such as these:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;
</code></pre>


The various "*ScheduleBuilder*" classes have methods relating to defining different types of schedules.

The *DateBuilder* class contains various methods for easily constructing *java.util.Date* instances for
particular points in time (such as a date that represents the next even hour - or in other words 10:00:00 if it is
currently 9:43:27).


### [Jobs and Triggers](#TutorialLesson2-JobsAndTriggers)

A Job is a class that implements the ***Job*** interface, which has only one simple method:

**The Job Interface**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  package org.quartz;

  public interface Job {

    public void execute(JobExecutionContext context)
      throws JobExecutionException;
  }
</code></pre>


When the Job's trigger fires (more on that in a moment), the execute(..) method is invoked by one of the scheduler's
worker threads.  The *JobExecutionContext* object that is passed to this method provides the job
instance with information about its "run-time" environment - a handle to the Scheduler that executed it, a handle to the
Trigger that triggered the execution, the job's JobDetail object, and a few other items.

The *JobDetail* object is created by the Quartz client (your program) at the time the Job is added
to the scheduler. It contains various property settings for the Job, as well as a *JobDataMap*, which can
be used to store state information for a given instance of your job class.  It is essentially the definition of the job
instance, and is discussed in further detail in the next lesson.

*Trigger* objects are used to trigger the execution (or 'firing') of jobs. When you wish to
schedule a job, you instantiate a trigger and 'tune' its properties to provide the scheduling you wish to have. Triggers
may also have a JobDataMap associated with them - this is useful to passing parameters to a Job that are specific to the
firings of the trigger. Quartz ships with a handful of different trigger types, but the most commonly used types are
SimpleTrigger and CronTrigger.

SimpleTrigger is handy if you need 'one-shot' execution (just single execution of a job at a given moment in
time), or if you need to fire a job at a given time, and have it repeat N times, with a delay of T between executions.
CronTrigger is useful if you wish to have triggering based on calendar-like schedules - such as "every Friday, at noon"
or "at 10:15 on the 10th day of every month."

Why Jobs AND Triggers? Many job schedulers do not have separate notions of jobs and triggers. Some define a 'job'
as simply an execution time (or schedule) along with some small job identifier. Others are much like the union of
Quartz's job and trigger objects. While developing Quartz, we decided that it made sense to create a separation between
the schedule and the work to be performed on that schedule. This has (in our opinion) many benefits.

For example, Jobs can be created and stored in the job scheduler independent of a trigger, and many triggers can
be associated with the same job. Another benefit of this loose-coupling is the ability to configure jobs that remain in
the scheduler after their associated triggers have expired, so that that it can be rescheduled later, without having to
re-define it. It also allows you to modify or replace a trigger without having to re-define its associated job.

### [Identities](#TutorialLesson2-Identities)

Jobs and Triggers are given identifying keys as they are registered with the Quartz scheduler. The keys of Jobs and
Triggers (JobKey and TriggerKey) allow them to be placed into 'groups' which can be useful for organizing your jobs and
triggers into categories such as "reporting jobs" and "maintenance jobs". The name portion of the key of a job or
trigger must be unique within the group - or in other words, the complete key (or identifier) of a job or trigger is the
compound of the name and group.


You now have a general idea about what Jobs and Triggers are, you can learn more about them in <a
    href="tutorial-lesson-03.md" title="Tutorial Lesson 3">Lesson 3: More About Jobs &amp; JobDetails</a> and <a
    href="tutorial-lesson-04.md" title="Tutorial Lesson 4">Lesson 4: More About Triggers</a>.

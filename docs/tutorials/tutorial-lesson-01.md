---
title: Tutorial 1
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-02.md" title="Go to Lesson 2">Lesson 2&nbsp;&rsaquo;</a>
</div>

## Lesson 1: Using Quartz

Before you can use the scheduler, it needs to be instantiated (who'd have guessed?). To do this, you use a
SchedulerFactory. Some users of Quartz may keep an instance of a factory in a JNDI store, others may find it
just as easy (or easier) to instantiate and use a factory instance directly (such as in the example below).

Once a scheduler is instantiated, it can be started, placed in stand-by mode, and shutdown. Note that once a
scheduler is shutdown, it cannot be restarted without being re-instantiated. Triggers do not fire (jobs do not execute)
until the scheduler has been started, nor while it is in the paused state.

Here's a quick snippet of code, that instantiates and starts a scheduler, and schedules a job for execution:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

  Scheduler sched = schedFact.getScheduler();

  sched.start();

  // define the job and tie it to our HelloJob class
  JobDetail job = newJob(HelloJob.class)
      .withIdentity("myJob", "group1")
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


As you can see, working with quartz is rather simple. In <a href="./tutorial-lesson-02.md"
title="Tutorial Lesson 2">Lesson 2</a> we'll give a quick overview of Jobs and Triggers, and Quartz's API so that
you can more fully understand this example.

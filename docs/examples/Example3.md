---
title: Examples
visible_title: "Quartz Examples"
active_sub_menu_id: site_mnu_docs_examples
---
<div class="secNavPanel">
          <a href=".">Contents</a> |
	  <a href="Example1.md">&lsaquo;&nbsp;Prev</a> |
          <a href="Example4.md">Next&nbsp;&rsaquo;</a>
</div>

## Example 3 - Cron-based Triggers

This example is designed to demonstrate how you can use Cron Triggers to schedule jobs.   This example will fire off several simple jobs that say "Hello World" and display the date and time that the job was executed.

The program will perform the following actions:

+ Start up the Quartz Scheduler
+ Schedule several jobs using various features of CronTrigger
+ Wait for 300 seconds (5 minutes) to give Quartz a chance to run the jobs
+ Shut down the Scheduler



Note:  Refer to the Quartz javadoc for a thorough explanation of CronTrigger.

## [Running the Example](#Example3-RunningtheExample)
This example can be executed from the **examples/example3** directory.   There are two out-of-the-box methods for running this example


+ **example3.sh** - A UNIX/Linux shell script
+ **example3.bat** - A Windows Batch file



## [The Code](#Example3-TheCode)

The code for this example resides in the package **org.quartz.examples.example3**.   

The code in this example is made up of the following classes:

<table><thead>
<tr>
<th> Class Name </th>
<th> Description</th>
</tr>
</thead>

<tbody>
<tr>
<td> CronTriggerExample </td>
<td> The main program</td>
</tr>
<tr>
<td> SimpleJob </td>
<td> A simple job that says Hello World and displays the date/time</td>
</tr>
</tbody></table>

### [SimpleJob](#Example3-SimpleJob)
SimpleJob is a simple job that implements the *Job* interface and logs a nice message to the log (by default, this will simply go to the screen).   The current date and time is printed in the job so that you can see exactly when the job is run.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">public void execute(JobExecutionContext context) throws JobExecutionException {
    JobKey jobKey = context.getJobDetail().getKey();
    _log.info("SimpleJob says: " + jobKey + " executing at " + new Date());
}
</code></pre>


### [CronTriggerExample](#Example3-CronTriggerExample)
The program starts by getting an instance of the Scheduler.  This is done by creating a *StdSchedulerFactory* and then using it to create a scheduler.   This will create a simple, RAM-based scheduler.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">SchedulerFactory sf = new StdSchedulerFactory();
Scheduler sched = sf.getScheduler();
</code></pre>


Job #1 is scheduled to run every 20 seconds

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">JobDetail job = newJob(SimpleJob.class)
    .withIdentity("job1", "group1")
    .build();

CronTrigger trigger = newTrigger()
    .withIdentity("trigger1", "group1")
    .withSchedule(cronSchedule("0/20 * * * * ?"))
    .build();

sched.scheduleJob(job, trigger);
</code></pre>


Job #2 is scheduled to run every other minute, starting at 15 seconds past the minute.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(SimpleJob.class)
    .withIdentity("job2", "group1")
    .build();

trigger = newTrigger()
    .withIdentity("trigger2", "group1")
    .withSchedule(cronSchedule("15 0/2 * * * ?"))
    .build();

sched.scheduleJob(job, trigger);
</code></pre>


Job #3 is scheduled to every other minute, between 8am and 5pm (17 o'clock).

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(SimpleJob.class)
    .withIdentity("job3", "group1")
    .build();

trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .withSchedule(cronSchedule("0 0/2 8-17 * * ?"))
    .build();

sched.scheduleJob(job, trigger);
</code></pre>


Job #4 is scheduled to run every three minutes but only between 5pm and 11pm

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(SimpleJob.class)
    .withIdentity("job4", "group1")
    .build();

trigger = newTrigger()
    .withIdentity("trigger4", "group1")
    .withSchedule(cronSchedule("0 0/3 17-23 * * ?"))
    .build();

sched.scheduleJob(job, trigger);
</code></pre>


Job #5 is scheduled to run at 10am on the 1st and 15th days of the month

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(SimpleJob.class)
    .withIdentity("job5", "group1")
    .build();

trigger = newTrigger()
    .withIdentity("trigger5", "group1")
    .withSchedule(cronSchedule("0 0 10am 1,15 * ?"))
    .build();

sched.scheduleJob(job, trigger);
</code></pre>


Job #6 is scheduled to run every 30 seconds on Weekdays (Monday through Friday)

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(SimpleJob.class)
    .withIdentity("job6", "group1")
    .build();

trigger = newTrigger()
    .withIdentity("trigger6", "group1")
    .withSchedule(cronSchedule("0,30 * * ? * MON-FRI"))
    .build();

sched.scheduleJob(job, trigger);
</code></pre>


Job #7 is scheduled to run every 30 seconds on Weekends (Saturday and Sunday)

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(SimpleJob.class)
    .withIdentity("job7", "group1")
    .build();

trigger = newTrigger()
    .withIdentity("trigger7", "group1")
    .withSchedule(cronSchedule("0,30 * * ? * SAT,SUN"))
    .build();

sched.scheduleJob(job, trigger);
</code></pre>


The scheduler is then started (it also would have been fine to start it before scheduling the jobs).


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.start();
</code></pre>


To let the program have an opportunity to run the job, we then sleep for five minutes (300 seconds).  The scheduler is running in the background and should fire off several jobs during that time.

Note:  Because many of the jobs have hourly and daily restrictions on them, not all of the jobs will run in this example.   For example:   Job #6 only runs on Weekdays while Job #7 only runs on Weekends.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">Thread.sleep(300L * 1000L);
</code></pre>


Finally, we will gracefully shutdown the scheduler:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.shutdown(true);
</code></pre>

Note:  passing *true* into the *shutdown* message tells the Quartz Scheduler to wait until all jobs have completed running before returning from the method call.

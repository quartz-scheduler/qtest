---
title: Examples
visible_title: "Quartz Examples"
active_sub_menu_id: site_mnu_docs_examples
---
<div class="secNavPanel">
          <a href=".">Contents</a> |
	  <a href="Example6.md">&lsaquo;&nbsp;Prev</a>
</div>

## Example 14 - Trigger Priorities

This example will demonstrate how Trigger priorities can be used to manage firing order for Triggers with the same fire time.

The program will perform the following actions:

+ Create a Scheduler with a single worker thread
+ Schedule three Triggers with different priorities that fire the first time at the same time, and a second time at staggered intervals
+ Start up the Quartz Scheduler
+ Wait for 30 seconds to give Quartz a chance to fire the Triggers
+ Shut down the Scheduler



## [Running the Example](#Example14-RunningtheExample)

This example can be executed from the **examples/example14** directory.   There are two out-of-the-box methods for running this example


+ **example14.sh** &#45; A UNIX/Linux shell script
+ **example14.bat** &#45; A Windows Batch file



## [Expected Results](#Example14-ExpectedResults)

Each of the three Triggers should fire twice. Once in order of priority as they all start at the same time, and a second time in order of their staggered firing times. You should see something like this in the log or on the console:

<pre>
INFO 15 Aug 12:15:51.345 PM PriorityExampleScheduler_Worker-0 org.quartz.examples.example14.TriggerEchoJob
TRIGGER: Priority10Trigger15SecondRepeat
INFO 15 Aug 12:15:51.345 PM PriorityExampleScheduler_Worker-0 org.quartz.examples.example14.TriggerEchoJob
TRIGGER: Priority5Trigger10SecondRepeat
INFO 15 Aug 12:15:51.345 PM PriorityExampleScheduler_Worker-0 org.quartz.examples.example14.TriggerEchoJob
TRIGGER: PriorityNeg5Trigger5SecondRepeat
INFO 15 Aug 12:15:56.220 PM PriorityExampleScheduler_Worker-0 org.quartz.examples.example14.TriggerEchoJob
TRIGGER: PriorityNeg5Trigger5SecondRepeat
INFO 15 Aug 12:16:01.220 PM PriorityExampleScheduler_Worker-0 org.quartz.examples.example14.TriggerEchoJob
TRIGGER: Priority5Trigger10SecondRepeat
INFO 15 Aug 12:16:06.220 PM PriorityExampleScheduler_Worker-0 org.quartz.examples.example14.TriggerEchoJob
TRIGGER: Priority10Trigger15SecondRepeat
</pre>



## [The Code](#Example14-TheCode)

The code for this example resides in the package **org.quartz.examples.example14**.

The code in this example is made up of the following classes:

<table><tbody>
<tr>
<th> Class Name </th>
<th> Description </th>
</tr>
<tr>
<td> PriorityExample </td>
<td> The main program </td>
</tr>

<tr>
<td> TriggerEchoJob </td>
<td> A simple job that echos the name if the Trigger that fired it </td>
</tr>
</tbody></table>

### [TriggerEchoJob](#Example14-TriggerEchoJob)

TriggerEchoJob is a simple job that implements the *Job* interface and logs the name of the *Trigger* that fired it to the log (by default, this will simply go to the screen):


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.info("TRIGGER: " + context.getTrigger().getKey());
}
</code></pre>


### [PriorityExample](#Example14-PriorityExample)

The program starts by getting an instance of the Scheduler.  This is done by creating a *StdSchedulerFactory* and then using it to create a scheduler.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">SchedulerFactory sf = new StdSchedulerFactory(
    "org/quartz/examples/example14/quartz_priority.properties");
Scheduler sched = sf.getScheduler();
</code></pre>


We pass a specific Quartz properties file to the StdSchedulerFactory to configure our new Scheduler instance.   These properties will create a simple, RAM-based scheduler with only one worker thread so that we can see priorities act as the tie breaker when Triggers compete for the single thread, *quartz_priority.properties*:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">org.quartz.scheduler.instanceName=PriorityExampleScheduler
# Set thread count to 1 to force Triggers scheduled for the same time to
# to be ordered by priority.
org.quartz.threadPool.threadCount=1
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool

org.quartz.jobStore.class=org.quartz.simpl.RAMJobStore
</code></pre>


The TriggerEchoJob is defined as a Job to Quartz using the *JobDetail* class.  It passes **null** for its group, so it will use the default group:JobDetail job = new JobDetail("TriggerEchoJob", null, TriggerEchoJob.class);

We create three *SimpleTrigger*s that will all fire the first time five seconds from now but with different priorities, and then fire a second time at staggered five second intervals:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">// Calculate the start time of all triggers as 5 seconds from now
Date startTime = futureDate(5, IntervalUnit.SECOND);

// First trigger has priority of 1, and will repeat after 5 seconds
Trigger trigger1 = newTrigger()
    .withIdentity("PriorityNeg5Trigger5SecondRepeat")
    .startAt(startTime)
    .withSchedule(simpleSchedule().withRepeatCount(1).withIntervalInSeconds(5))
    .withPriority(1)
    .forJob(job)
    .build();

// Second trigger has default priority of 5 (default), and will repeat after 10 seconds
Trigger trigger2 = newTrigger()
    .withIdentity("Priority5Trigger10SecondRepeat")
    .startAt(startTime)
    .withSchedule(simpleSchedule().withRepeatCount(1).withIntervalInSeconds(10))
    .forJob(job)
    .build();

// Third trigger has priority 10, and will repeat after 15 secondsTrigger trigger3 = newTrigger()
    .withIdentity("Priority10Trigger15SecondRepeat")
    .startAt(startTime)
    .withSchedule(simpleSchedule().withRepeatCount(1).withIntervalInSeconds(15))
    .withPriority(10)
    .forJob(job)
    .build();
</code></pre>


We now associate the three Triggers with our Job in the scheduler. The first time we need to also add the job itself to the scheduler:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">// Tell quartz to schedule the job using our trigger
sched.scheduleJob(job, trigger1);
sched.scheduleJob(trigger2);
sched.scheduleJob(trigger3);
</code></pre>


At this point, the triggers have been scheduled to run. However, the scheduler is not yet running. So, we must tell the scheduler to start up&#33;


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.start();
</code></pre>


To let the program have an opportunity to run the job, we then sleep for 30 seconds. The scheduler is running in the background and should fire off the job six times during those 30 seconds.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">Thread.sleep(30L * 1000L);
</code></pre>


Finally, we will gracefully shutdown the scheduler:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.shutdown(true);
</code></pre>


Note:  passing *true* into the *shutdown* message tells the Quartz Scheduler to wait until all jobs have completed running before returning from the method call.

---
title: Examples
visible_title: "Quartz Examples"
active_sub_menu_id: site_mnu_docs_examples
---
<div class="secNavPanel">
          <a href=".">Contents</a> |
	  <a href="Example5.html">&lsaquo;&nbsp;Prev</a> |
          <a href="Example14.html">Next&nbsp;&rsaquo;</a>
</div>

## Example 6 - Dealing with Job Exceptions

This example is designed to demonstrate how can deal with job execution exceptions.   Jobs in Quartz are permitted
to throw a *JobExecutionExceptions*.   When this exception is thrown, you can instruct quartz what action to take.

The program will perform the following actions:


+ Start up the Quartz Scheduler
+ Schedule two jobs, each job will execute the every three seconds, indefintely
+ The jobs will throw an exception, and quartz will take appropriate action
+ The program will wait 60 seconds so that the two jobs have plenty of time to run
+ Shut down the Scheduler



## [Running the Example](#Example6-RunningtheExample)
This example can be executed from the **examples/example6** directory.   There are two out-of-the-box methods for running this example


+ **example6.sh** - A UNIX/Linux shell script
+ **example6.bat** - A Windows Batch file



## [The Code](#Example6-TheCode)

The code for this example resides in the package **org.quartz.examples.example6**.   

The code in this example is made up of the following classes:

<table><thead>
<tr>
<th> Class Name </th>
<th> Description</th>
</tr>
</thead>

<tbody>
<tr>
<td> JobExceptionExample </td>
<td> The main program</td>
</tr>
<tr>
<td> BadJob1 </td>
<td> A simple job that will throw an exception and instruct quartz to refire its trigger immediately</td>
</tr>
<tr>

<td> BadJob2 </td>
<td> A simple job that will throw an exception and instruct quartz to never schedule the job again</td>
</tr>
</tbody></table>

### [BadJob1](#Example6-BadJob1)

BadJob1 is a simple job that simply creates an artificial exception (divide by zero).   When this exception is caught, a *JobExecutionException* is thrown and set to refire the job immediatly.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">        try {
            int zero = 0;
            int calculation = 4815 / zero;
        }
        catch (Exception e) {
        	_log.info("--- Error in job!");
        	JobExecutionException e2 =
        		new JobExecutionException(e);
        	// this job will refire immediately
        	e2.setRefireImmediately(true);
        	throw e2;
        }
</code></pre>


This will force quartz to run this job over and over and over and over again.

### [BadJob2](#Example6-BadJob2)

BadJob2 is a simple job that simply creates an artificial exception (divide by zero).   When this exception is caught, a *JobExecutionException* is thrown and set to ensure that quartz never runs the job again.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">        try {
            int zero = 0;
            int calculation = 4815 / zero;
        }
        catch (Exception e) {
        	_log.info("--- Error in job!");
        	JobExecutionException e2 =
        		new JobExecutionException(e);
        	// Quartz will automatically unschedule
        	// all triggers associated with this job
        	// so that it does not run again
        	e2.setUnscheduleAllTriggers(true);
        	throw e2;
        }
</code></pre>


This will force quartz to shutdown this job so that it does not run again.

### [JobExceptionExample](#Example6-JobExceptionExample)

The program starts by getting an instance of the Scheduler.  This is done by creating a *StdSchedulerFactory* and then using it to create a scheduler.   This will create a simple, RAM-based scheduler.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">SchedulerFactory sf = new StdSchedulerFactory();
Scheduler sched = sf.getScheduler();
</code></pre>


Job #1 is scheduled to run every 3 seconds indefinitely.   This job will fire *BadJob1*.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">JobDetail job = newJob(BadJob1.class)
    .withIdentity("badJob1", "group1")
    .build();

SimpleTrigger trigger = newTrigger()
    .withIdentity("trigger1", "group1")
    .startAt(startTime)
    .withSchedule(simpleSchedule()
            .withIntervalInSeconds(3)
            .repeatForever())
    .build();

Date ft = sched.scheduleJob(job, trigger);
</code></pre>


Job #2 is scheduled to run every 3 seconds indefinitely.   This job will fire *BadJob2*.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(BadJob2.class)
    .withIdentity("badJob2", "group1")
    .build();

trigger = newTrigger()
    .withIdentity("trigger2", "group1")
    .startAt(startTime)
    .withSchedule(simpleSchedule()
            .withIntervalInSeconds(3)
            .repeatForever())
    .build();

ft = sched.scheduleJob(job, trigger);
</code></pre>



The scheduler is then started.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.start();
</code></pre>


To let the program have an opportunity to run the job, we then sleep for 1 minute (60 seconds)

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">Thread.sleep(60L * 1000L);
</code></pre>


This scheduler will run both jobs (BadJob1 and BadJob2).   Both jobs will throw an exception.   Job 1 should attempt to refire immediately.  Job 2 should never run again.

Finally, we will gracefully shutdown the scheduler:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.shutdown(true);
</code></pre>


Note:  passing *true* into the *shutdown* message tells the Quartz Scheduler to wait until all jobs have completed running before returning from the method call.

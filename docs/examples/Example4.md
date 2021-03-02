---
title: Examples
visible_title: "Quartz Examples"
active_sub_menu_id: site_mnu_docs_examples
---
<div class="secNavPanel">
          <a href=".">Contents</a> |
	  <a href="Example3.md">&lsaquo;&nbsp;Prev</a> |
          <a href="Example5.md">Next&nbsp;&rsaquo;</a>
</div>

## Example 4 - Job Parameters and Job State

This example is designed to demonstrate how you can pass run-time parameters into quartz jobs and how you can maintain state in a job.

The program will perform the following actions:


+ Start up the Quartz Scheduler
+ Schedule two jobs, each job will execute the every ten seconds for a total of times
+ The scheduler will pass a run-time job parameter of "Green" to the first job instance
+ The scheduler will pass a run-time job parameter of "Red" to the second job instance
+ The program will wait 60 seconds so that the two jobs have plenty of time to run
+ Shut down the Scheduler




## [Running the Example](#Example4-RunningtheExample)
This example can be executed from the **examples/example4** directory.   There are two out-of-the-box methods for running this example


+ **example4.sh** - A UNIX/Linux shell script
+ **example4.bat** - A Windows Batch file



## [The Code](#Example4-TheCode)

The code for this example resides in the package **org.quartz.examples.example4**.   

The code in this example is made up of the following classes:

<table><thead>
<tr>
<th> Class Name </th>
<th> Description</th>
</tr>
</thead>

<tbody>
<tr>
<td> JobStateExample </td>
<td> The main program</td>
</tr>
<tr>
<td> ColorJob </td>
<td> A simple job that prints a favorite color (passed in as a run-time parameter) and displays its execution count.</td>
</tr>
</tbody></table>

### [ColorJob](#Example4-ColorJob)

ColorJob is a simple class that implement the Job interface, and is annotated as such:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ColorJob implements Job {
</code></pre>


The annotations cause behavior just as their names describe - multiple instances of the job will not be allowed to
run concurrently (consider a case where a job has code in its execute() method that takes 34 seconds to run, but it is
scheduled with a trigger that repeats every 30 seconds), and will have its JobDataMap contents re-persisted in the
scheduler's JobStore after each execution.  For the purposes of this example, only *@PersistJobDataAfterExecution*
annotation is truly relevant, but it's always wise to use the *@DisallowConcurrentExecution* annotation with
it, to prevent race-conditions on saved data.

ColorJob logs the following information when the job is executed:


+ The job's identification key (name and group) and time/date of execution
+ The job's favorite color (which is passed in as a run-time parameter)
+ The job's execution count calculated from a member variable
+ The job's execution count maintained as a job map parameter




<pre class="prettyprint highlight"><code class="language-java" data-lang="java">_log.info("ColorJob: " + jobKey + " executing at " + new Date() + "\n" +
    "  favorite color is " + favoriteColor + "\n" +
    "  execution count (from job map) is " + count + "\n" +
    "  execution count (from job member variable) is " + _counter);
</code></pre>


The variable *favoriteColor* is passed in as a job parameter.  It is retrieved as follows from the *JobDataMap*:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">JobDataMap data = context.getJobDetail().getJobDataMap();
String favoriteColor = data.getString(FAVORITE_COLOR);
</code></pre>


The variable *count* is stored in the job data map as well:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">JobDataMap data = context.getJobDetail().getJobDataMap();
int count = data.getInt(EXECUTION_COUNT);
</code></pre>


The variable is later incremented and stored back into the job data map so that job state can be preserved:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">count++;
data.put(EXECUTION_COUNT, count);
</code></pre>


There is also a member variable named *counter*.   This variable is defined as a member variable to the class:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">private int _counter = 1;
</code></pre>


This variable is also incremented and displayed.  However, its count will always be displayed as "1" because Quartz will always instantiate a new instance of the class during each execution - which prevents member variables from being used to maintain state.

### [JobStateExample ](#Example4-JobStateExample)
The program starts by getting an instance of the Scheduler.  This is done by creating a *StdSchedulerFactory* and then using it to create a scheduler.   This will create a simple, RAM-based scheduler.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">SchedulerFactory sf = new StdSchedulerFactory();
Scheduler sched = sf.getScheduler();
</code></pre>


Job #1 is scheduled to run every 10 seconds, for a total of five times:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">JobDetail job1 = newJob(ColorJob.class)
    .withIdentity("job1", "group1")
    .build();

SimpleTrigger trigger1 = newTrigger()
    .withIdentity("trigger1", "group1")
    .startAt(startTime)
    .withSchedule(simpleSchedule()
            .withIntervalInSeconds(10)
            .withRepeatCount(4))
    .build();
</code></pre>


Job #1 is passed in two job parameters.   One is a favorite color, with a value of "Green".  The other is an execution count, which is initialized with a value of 1.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job1.getJobDataMap().put(ColorJob.FAVORITE_COLOR, "Green");
job1.getJobDataMap().put(ColorJob.EXECUTION_COUNT, 1);
</code></pre>


Job #2 is also scheduled to run every 10 seconds, for a total of five times:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">JobDetail job2 = newJob(ColorJob.class)
    .withIdentity("job2", "group1")
    .build();

SimpleTrigger trigger2 = newTrigger()
    .withIdentity("trigger2", "group1")
    .startAt(startTime)
    .withSchedule(simpleSchedule()
            .withIntervalInSeconds(10)
            .withRepeatCount(4))
    .build();
</code></pre>


Job #2 is also passed in two job parameters.   One is a favorite color, with a value of "Red".  The other is an execution count, which is initialized with a value of 1.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job2.getJobDataMap().put(ColorJob.FAVORITE_COLOR, "Red");
job2.getJobDataMap().put(ColorJob.EXECUTION_COUNT, 1);
</code></pre>



The scheduler is then started.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.start();
</code></pre>


To let the program have an opportunity to run the job, we then sleep for one minute (60 seconds)

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">Thread.sleep(60L * 1000L);
</code></pre>


Finally, we will gracefully shutdown the scheduler:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.shutdown(true);
</code></pre>


Note:  passing *true* into the *shutdown* message tells the Quartz Scheduler to wait until all jobs have completed running before returning from the method call.

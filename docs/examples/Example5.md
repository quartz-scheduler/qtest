---
title: Examples
visible_title: "Quartz Examples"
active_sub_menu_id: site_mnu_docs_examples
---
<div class="secNavPanel">
          <a href=".">Contents</a> |
	  <a href="Example4.md">&lsaquo;&nbsp;Prev</a> |
          <a href="Example6.md">Next&nbsp;&rsaquo;</a>
</div>

## Example 5 - Job Misfires

This example is designed to demonstrate concepts related to trigger misfires.

The program will perform the following actions:


+ Start up the Quartz Scheduler
+ Schedule two jobs, each job will execute the every three seconds, indefinitely
+ The jobs will take ten seconds to run (preventing the execution trigger from firing every three seconds)
+ Each job has different misfire instructions
+ The program will wait 10 minutes so that the two jobs have plenty of time to run
+ Shut down the Scheduler




## [Running the Example](#Example5-RunningtheExample)
This example can be executed from the **examples/example5** directory.   There are two out-of-the-box methods for running this example


+ **example5.sh** - A UNIX/Linux shell script
+ **example5.bat** - A Windows Batch file



## [The Code](#Example5-TheCode)

The code for this example resides in the package **org.quartz.examples.example5**.   

The code in this example is made up of the following classes:

<table><thead>
<tr>
<th> Class Name </th>
<th> Description</th>
</tr>
</thead>

<tbody>
<tr>
<td> MisfireExample </td>
<td> The main program</td>
</tr>
<tr>
<td> StatefulDumbJob </td>
<td> A simple job class who's execute method takes 10 seconds to run</td>
</tr>
</tbody></table>

### [StatefulDumbJob ](#Example5-StatefulDumbJob)

StatefulDumbJob is a simple job that prints its execution time and then will wait for a period of time before completing.  
The amount of wait time is defined by the job parameter EXECUTION_DELAY.  If this job parameter is not passed in, the
job will default to a wait time of 5 seconds.  The job is also keep its own count of how many times it has executed
using a value in its JobDataMap called NUM_EXECUTIONS.  Because the class has the *PersistJobDataAfterExecution*
annotation, the execution count is preserved between each execution.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class StatefulDumbJob implements Job {

    public static final String NUM_EXECUTIONS = "NumExecutions";
    public static final String EXECUTION_DELAY = "ExecutionDelay";

    public StatefulDumbJob() {
    }
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        System.err.println("---" + context.getJobDetail().getKey()
                + " executing.[" + new Date() + "]");

        JobDataMap map = context.getJobDetail().getJobDataMap();

        int executeCount = 0;
        if (map.containsKey(NUM_EXECUTIONS)) {
            executeCount = map.getInt(NUM_EXECUTIONS);
        }

        executeCount++;
        map.put(NUM_EXECUTIONS, executeCount);

        long delay = 5000l;
        if (map.containsKey(EXECUTION_DELAY)) {
            delay = map.getLong(EXECUTION_DELAY);
        }

        try {
            Thread.sleep(delay);
        } catch (Exception ignore) {
        }

        System.err.println("  -" + context.getJobDetail().getKey()
                + " complete (" + executeCount + ").");
    }
}
</code></pre>



### [MisfireExample](#Example5-MisfireExample)

The program starts by getting an instance of the Scheduler.  This is done by creating a *StdSchedulerFactory*
and then using it to create a scheduler.  This will create a simple, RAM-based scheduler because no specific
quartz.properties config file telling it to do otherwise is provided.

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">SchedulerFactory sf = new StdSchedulerFactory();
Scheduler sched = sf.getScheduler();
</code></pre>


Job #1 is scheduled to run every 3 seconds indefinitely.  An execution delay of 10 seconds is passed into the job:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">JobDetail job = newJob(StatefulDumbJob.class)
    .withIdentity("statefulJob1", "group1")
    .usingJobData(StatefulDumbJob.EXECUTION_DELAY, 10000L)
    .build();

SimpleTrigger trigger = newTrigger()
    .withIdentity("trigger1", "group1")
    .startAt(startTime)
    .withSchedule(simpleSchedule()
            .withIntervalInSeconds(3)
            .repeatForever())
    .build();

sched.scheduleJob(job, trigger);
</code></pre>



Job #2 is scheduled to run every 3 seconds indefinitely.  An execution delay of 10 seconds is passed into the job:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">job = newJob(StatefulDumbJob.class)
            .withIdentity("statefulJob2", "group1")
            .usingJobData(StatefulDumbJob.EXECUTION_DELAY, 10000L)
            .build();

        trigger = newTrigger()
            .withIdentity("trigger2", "group1")
            .startAt(startTime)
            .withSchedule(simpleSchedule()
                    .withIntervalInSeconds(3)
                    .repeatForever()
                    .withMisfireHandlingInstructionNowWithExistingCount()) // set misfire instruction
            .build();
</code></pre>

Note: The trigger for job #2 is set with a misfire instruction that will cause it to reschedule with the existing
repeat count.   This policy forces quartz to refire the trigger as soon as possible.   Job #1 uses the default
"smart" misfire policy for simple triggers, which causes the trigger to fire at it's next normal execution time.


The scheduler is then started.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.start();
</code></pre>


To let the program have an opportunity to run the job, we then sleep for ten minutes (600 seconds)

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">Thread.sleep(600L * 1000L);
</code></pre>


Finally, we will gracefully shutdown the scheduler:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">sched.shutdown(true);
</code></pre>


Note:  passing *true* into the *shutdown* message tells the Quartz Scheduler to wait until all jobs have completed running before returning from the method call.

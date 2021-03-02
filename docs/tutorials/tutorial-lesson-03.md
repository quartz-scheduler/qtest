---
title: Tutorial 3
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-02.md" title="Go to Lesson 2">&lsaquo;&nbsp;Lesson 2</a> |
          <a href="tutorial-lesson-04.md" title="Go to Lesson 4">Lesson 4&nbsp;&rsaquo;</a>
</div>

## Lesson 3: More About Jobs and Job Details

As you saw in Lesson 2, Jobs are rather easy to implement, having just a single 'execute' method in the interface.
There are just a few more things that you need to understand about the nature of jobs, about the execute(..) method of
the Job interface, and about JobDetails.

While a job class that you implement has the code that knows how to do the actual work of the particular type of
job, Quartz needs to be informed about various attributes that you may wish an instance of that job to have.
This is done via the JobDetail class, which was mentioned briefly in the previous section.

JobDetail instances are built using the JobBuilder class.  You will typically want to use a static import of
all of its methods, in order to have the DSL-feel within your code.


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
import static org.quartz.JobBuilder.*;
</code></pre>


Let's take a moment now to discuss a bit about the 'nature' of Jobs and the life-cycle of job instances within
Quartz. First lets take a look back at some of that snippet of code we saw in Lesson 1:

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

Now consider the job class "HelloJob" defined as such:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  public class HelloJob implements Job {

    public HelloJob() {
    }

    public void execute(JobExecutionContext context)
      throws JobExecutionException
    {
      System.err.println("Hello!  HelloJob is executing.");
    }
  }
</code></pre>


Notice that we give the scheduler a JobDetail instance, and that it knows the type of job to be executed by simply
providing the job's class as we build the JobDetail. Each (and every) time the scheduler executes the job, it creates a
new instance of the class before calling its execute(..) method.  When the execution is complete, references to the
job class instance are dropped, and the instance is then garbage collected. One of the ramifications of this behavior is
the fact that jobs must have a no-argument constructor (when using the default JobFactory implementation). Another
ramification is that it does not make sense to have state data-fields defined on the job class - as their values would
not be preserved between job executions.

You may now be wanting to ask "how can I provide properties/configuration for a Job instance?" and "how can I
keep track of a job's state between executions?" The answer to these questions are the same: the key is the JobDataMap,
which is part of the JobDetail object.

### [JobDataMap](#TutorialLesson3-JobDataMap)

The JobDataMap can be used to hold any amount of (serializable) data objects which you wish to have made available to
the job instance when it executes. JobDataMap is an implementation of the Java Map interface, and has some added
convenience methods for storing and retrieving data of primitive types.

Here's some quick snippets of putting data into the JobDataMap while defining/building the JobDetail,  prior to
adding the job to the scheduler:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  // define the job and tie it to our DumbJob class
  JobDetail job = newJob(DumbJob.class)
      .withIdentity("myJob", "group1") // name "myJob", group "group1"
      .usingJobData("jobSays", "Hello World!")
      .usingJobData("myFloatValue", 3.141f)
      .build();
</code></pre>

Here's a quick example of getting data from the JobDataMap during the job's execution:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
public class DumbJob implements Job {

    public DumbJob() {
    }

    public void execute(JobExecutionContext context)
      throws JobExecutionException
    {
      JobKey key = context.getJobDetail().getKey();

      JobDataMap dataMap = context.getJobDetail().getJobDataMap();

      String jobSays = dataMap.getString("jobSays");
      float myFloatValue = dataMap.getFloat("myFloatValue");

      System.err.println("Instance " + key + " of DumbJob says: " + jobSays + ", and val is: " + myFloatValue);
    }
  }
</code></pre>


If you use a persistent JobStore (discussed in the JobStore section of this tutorial) you should use some care in
deciding what you place in the JobDataMap, because the object in it will be serialized, and they therefore become prone
to class-versioning problems. Obviously standard Java types should be very safe, but beyond that, any time someone
changes the definition of a class for which you have serialized instances, care has to be taken not to break
compatibility.
Optionally, you can put JDBC-JobStore and JobDataMap into a mode where only primitives and strings are allowed to be
stored in the map, thus eliminating any possibility of later serialization problems.

If you add setter methods to your job class that correspond to the names of keys in the JobDataMap (such as a
*setJobSays(String val)* method for the data in the example above), then Quartz's default JobFactory
implementation will automatically call those setters when the job is instantiated, thus preventing the need to
explicitly get the values out of the map within your execute method.

Triggers can also have JobDataMaps associated with them. This can be useful in the case where you have a Job that
is stored in the scheduler for regular/repeated use by multiple Triggers, yet with each independent triggering, you want
to supply the Job with different data inputs.

The JobDataMap that is found on the JobExecutionContext during Job execution serves as a convenience. It is a
merge of the JobDataMap found on the JobDetail and the one found on the Trigger, with the values in the latter
overriding any same-named values in the former.

Here's a quick example of getting data from the JobExecutionContext's merged JobDataMap during the job's
execution:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
public class DumbJob implements Job {

    public DumbJob() {
    }

    public void execute(JobExecutionContext context)
      throws JobExecutionException
    {
      JobKey key = context.getJobDetail().getKey();

      JobDataMap dataMap = context.getMergedJobDataMap();  // Note the difference from the previous example

      String jobSays = dataMap.getString("jobSays");
      float myFloatValue = dataMap.getFloat("myFloatValue");
      ArrayList state = (ArrayList)dataMap.get("myStateData");
      state.add(new Date());

      System.err.println("Instance " + key + " of DumbJob says: " + jobSays + ", and val is: " + myFloatValue);
    }
  }
</code></pre>

Or if you wish to rely on the JobFactory "injecting" the data map values onto your class, it might look like this
instead:

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  public class DumbJob implements Job {


    String jobSays;
    float myFloatValue;
    ArrayList state;

    public DumbJob() {
    }

    public void execute(JobExecutionContext context)
      throws JobExecutionException
    {
      JobKey key = context.getJobDetail().getKey();

      JobDataMap dataMap = context.getMergedJobDataMap();  // Note the difference from the previous example

      state.add(new Date());

      System.err.println("Instance " + key + " of DumbJob says: " + jobSays + ", and val is: " + myFloatValue);
    }

    public void setJobSays(String jobSays) {
      this.jobSays = jobSays;
    }

    public void setMyFloatValue(float myFloatValue) {
      myFloatValue = myFloatValue;
    }

    public void setState(ArrayList state) {
      state = state;
    }

  }
</code></pre>


You'll notice that the overall code of the class is longer, but the code in the execute() method is cleaner. One
could also argue that although the code is longer, that it actually took less coding, if the programmer's IDE was
used to auto-generate the setter methods, rather than having to hand-code the individual calls to retrieve the
values from the JobDataMap.   The choice is yours.

### <a name="TutorialLesson3-JobInstances"></a>Job "Instances"

Many users spend time being confused about what exactly constitutes a "job instance". We'll try to clear
that up here and in the section below about job state and concurrency.

You can create a single job class, and store
many 'instance definitions' of it within the scheduler by creating multiple instances of JobDetails - each with its own
set of properties and JobDataMap - and adding them all to the scheduler.

For example, you can create a class that implements the Job interface called
"SalesReportJob". The job might be coded to expect parameters sent to it (via the JobDataMap) to specify the
name of the sales person that the sales report should be based on.  They may then create multiple definitions
(JobDetails) of the job, such as "SalesReportForJoe" and "SalesReportForMike" which have "joe" and "mike" specified
in the corresponding JobDataMaps as input to the respective jobs.

When a trigger fires, the JobDetail (instance definition) it is associated to is loaded, and the job class it
refers to is instantiated via the JobFactory configured on the Scheduler.  The default JobFactory simply calls
newInstance() on the job class, then attempts to call setter methods on the class that match the names of keys within
the JobDataMap. You may want to create your own implementation of JobFactory to accomplish things such as having your
application's IoC or DI container produce/initialize the job instance.

In "Quartz speak", we refer to each stored JobDetail as a "job definition" or "JobDetail instance", and we refer
to a each executing job as a "job instance" or "instance of a job definition".  Usually if we just use the word
"job" we are referring to a named definition, or JobDetail.  When we are referring to the class implementing the job
interface, we usually use the term "job class".

### [Job State and Concurrency](#TutorialLesson3-StatefulJob)

Now, some additional notes about a job's state data (aka JobDataMap) and concurrency.  There are a couple annotations
that can be added to your Job class that affect Quartz's behavior with respect to these aspects.

***@DisallowConcurrentExecution*** is an annotation that can be added to the Job class that tells Quartz
not to execute multiple instances of a given job definition (that refers to the given job class) concurrently.  
Notice the wording there, as it was chosen very carefully.  In the example from the previous section, if
"SalesReportJob" has this annotation, then only one instance of "SalesReportForJoe" can execute at a given time, but
it *can* execute concurrently with an instance of "SalesReportForMike".  The constraint is based upon an instance
definition (JobDetail), not on instances of the job class.  However, it was decided (during the design of Quartz) to
have the annotation carried on the class itself, because it does often make a difference to how the class is coded.

***@PersistJobDataAfterExecution*** is an annotation that can be added to the Job class that tells Quartz
to update the stored copy of the JobDetail's JobDataMap after the execute() method completes successfully (without
throwing an exception), such that the next execution of the same job (JobDetail) receives the updated values rather
than the originally stored values. Like the *@DisallowConcurrentExecution* annotation, this applies to a job definition instance, not
a job class instance, though it was decided to have the job class carry the attribute because it does often make a
difference to how the class is coded (e.g. the 'statefulness' will need to be explicitly 'understood' by the code
within the execute method).

If you use the *@PersistJobDataAfterExecution* annotation, you should strongly consider also using the
*@DisallowConcurrentExecution* annotation, in order to avoid possible confusion (race conditions) of what data was
left stored when two instances of the same job (JobDetail) executed concurrently.

### [Other Attributes Of Jobs](#TutorialLesson3-OtherAttributesOfJobs)

Here's a quick summary of the other properties which can be defined for a job instance via the JobDetail object:

+ Durability - if a job is non-durable, it is automatically deleted from the scheduler once there are no
    longer any active triggers associated with it. In other words, non-durable jobs have a life span bounded by
    the existence of its triggers.
+ RequestsRecovery - if a job "requests recovery", and it is executing during the time of a 'hard shutdown'
    of the scheduler (i.e. the process it is running within crashes, or the machine is shut off), then it is re-executed
    when the scheduler is started again. In this case, the JobExecutionContext.isRecovering() method will return true.


### [JobExecutionException](#TutorialLesson3-JobExecutionException)

Finally, we need to inform you of a few details of the `Job.execute(..)` method. The only type of exception
(including RuntimeExceptions) that you are allowed to throw from the execute method is the JobExecutionException.
Because of this, you should generally wrap the entire contents of the execute method with a 'try-catch' block. You
should also spend some time looking at the documentation for the JobExecutionException, as your job can use it to
provide the scheduler various directives as to how you want the exception to be handled.

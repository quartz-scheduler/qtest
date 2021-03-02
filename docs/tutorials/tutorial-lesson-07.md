---
title: Tutorial 7
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-06.md" title="Go to Lesson 6">&lsaquo;&nbsp;Lesson 6</a> |
          <a href="tutorial-lesson-08.md" title="Go to Lesson 8">Lesson 8&nbsp;&rsaquo;</a>
</div>

## Lesson 7: TriggerListeners and JobListeners


Listeners are objects that you create to perform actions based on events occurring within the scheduler. As you
can probably guess, ***TriggerListeners*** receive events related to triggers, and ***JobListeners***
receive events related to jobs.

Trigger-related events include: trigger firings, trigger mis-firings (discussed in the "Triggers" section of this
document), and trigger completions (the jobs fired off by the trigger is finished).

**The org.quartz.TriggerListener Interface**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
public interface TriggerListener {

    public String getName();

    public void triggerFired(Trigger trigger, JobExecutionContext context);

    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context);

    public void triggerMisfired(Trigger trigger);

    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            int triggerInstructionCode);
}
</code></pre>


Job-related events include: a notification that the job is about to be executed, and a notification when the job
has completed execution.

**The org.quartz.JobListener Interface**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
public interface JobListener {

    public String getName();

    public void jobToBeExecuted(JobExecutionContext context);

    public void jobExecutionVetoed(JobExecutionContext context);

    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jobException);

}
</code></pre>


### [Using Your Own Listeners](#TutorialLesson7-UsingYourOwnListeners)

To create a listener, simply create an object that implements the org.quartz.TriggerListener and/or
org.quartz.JobListener interface. Listeners are then registered with the scheduler during run time, and must be given a
name (or rather, they must advertise their own name via their getName() method).

For your convenience, rather than implementing those interfaces, your class could also extend the class
JobListenerSupport or TriggerListenerSupport and simply override the events you're interested in.

Listeners are registered with the scheduler's ListenerManager along with a Matcher that describes which Jobs/Triggers
the listener wants to receive events for.

<blockquote>
        Listeners are registered with the scheduler during run time, and are NOT stored in the JobStore along with the
        jobs and triggers. This is because listeners are typically an integration point with your application.  
        Hence, each time your application runs, the listeners need to be re-registered with the scheduler.
</blockquote>



**Adding a JobListener that is interested in a particular job:**

<pre>
scheduler.getListenerManager().addJobListener(myJobListener, KeyMatcher.jobKeyEquals(new JobKey("myJobName", "myJobGroup")));
</pre>


You may want to use static imports for the matcher and key classes, which will make your defining the matchers cleaner:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
import static org.quartz.JobKey.*;
import static org.quartz.impl.matchers.KeyMatcher.*;
import static org.quartz.impl.matchers.GroupMatcher.*;
import static org.quartz.impl.matchers.AndMatcher.*;
import static org.quartz.impl.matchers.OrMatcher.*;
import static org.quartz.impl.matchers.EverythingMatcher.*;
...etc.
</code></pre>


Which turns the above example into this:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
scheduler.getListenerManager().addJobListener(myJobListener, jobKeyEquals(jobKey("myJobName", "myJobGroup")));
</code></pre>


**Adding a JobListener that is interested in all jobs of a particular group:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
scheduler.getListenerManager().addJobListener(myJobListener, jobGroupEquals("myJobGroup"));
</code></pre>


**Adding a JobListener that is interested in all jobs of two particular groups:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
scheduler.getListenerManager().addJobListener(myJobListener, or(jobGroupEquals("myJobGroup"), jobGroupEquals("yourGroup")));
</code></pre>


**Adding a JobListener that is interested in all jobs:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
scheduler.getListenerManager().addJobListener(myJobListener, allJobs());
</code></pre>



...Registering TriggerListeners works in just the same way.

Listeners are not used by most users of Quartz, but are handy when application requirements create the need for
the notification of events, without the Job itself having to explicitly notify the application.

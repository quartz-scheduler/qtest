---
title: Tutorial 7
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-07.md" title="Go to Lesson 7">&lsaquo;&nbsp;Lesson 7</a> |
          <a href="tutorial-lesson-09.md" title="Go to Lesson 9">Lesson 9&nbsp;&rsaquo;</a>
</div>

## Lesson 8: SchedulerListeners

***SchedulerListeners*** are much like TriggerListeners and JobListeners, except they receive
notification of events within the Scheduler itself - not necessarily events related to a specific trigger or job.

Scheduler-related events include: the addition of a job/trigger, the removal of a job/trigger, a serious error
within the scheduler, notification of the scheduler being shutdown, and others.

**The org.quartz.SchedulerListener Interface**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
public interface SchedulerListener {

    public void jobScheduled(Trigger trigger);

    public void jobUnscheduled(String triggerName, String triggerGroup);

    public void triggerFinalized(Trigger trigger);

    public void triggersPaused(String triggerName, String triggerGroup);

    public void triggersResumed(String triggerName, String triggerGroup);

    public void jobsPaused(String jobName, String jobGroup);

    public void jobsResumed(String jobName, String jobGroup);

    public void schedulerError(String msg, SchedulerException cause);

    public void schedulerStarted();

    public void schedulerInStandbyMode();

    public void schedulerShutdown();

    public void schedulingDataCleared();
}
</code></pre>


SchedulerListeners are registered with the scheduler's ListenerManager. SchedulerListeners can be virtually any
object that implements the org.quartz.SchedulerListener interface.

**Adding a SchedulerListener:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
scheduler.getListenerManager().addSchedulerListener(mySchedListener);
</code></pre>


**Removing a SchedulerListener:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
scheduler.getListenerManager().removeSchedulerListener(mySchedListener);
</code></pre>

---
title: Tutorial 10
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-09.md" title="Go to Lesson 9">&lsaquo;&nbsp;Lesson 9</a> |
          <a href="tutorial-lesson-11.md" title="Go to Lesson 11">Lesson 11&nbsp;&rsaquo;</a>
</div>

## Lesson 10: Configuration, Resource Usage and SchedulerFactory

The architecture of Quartz is modular, and therefore to get it running several components need to be "snapped"
together. Fortunately, some helpers exist for making this happen.

The major components that need to be configured before Quartz can do its work are:


+ ThreadPool
+ JobStore
+ DataSources (if necessary)
+ The Scheduler itself



The ***ThreadPool*** provides a set of Threads for Quartz to use when executing Jobs. The more threads
in the pool, the greater number of Jobs that can run concurrently. However, too many threads may bog-down your system.
Most Quartz users find that 5 or so threads are plenty- because they have fewer than 100 jobs at any given time, the
jobs are not generally scheduled to run at the same time, and the jobs are short-lived (complete quickly). Other users
find that they need 10, 15, 50 or even 100 threads - because they have tens-of-thousands of triggers with various
schedules - which end up having an average of between 10 and 100 jobs trying to execute at any given moment. Finding the
right size for your scheduler's pool is completely dependent on what you're using the scheduler for. There are no real
rules, other than to keep the number of threads as small as possible (for the sake of your machine's resources) - but
make sure you have enough for your Jobs to fire on time. Note that if a trigger's time to fire arrives, and there isn't
an available thread, Quartz will block (pause) until a thread comes available, then the Job will execute - some number
of milliseconds later than it should have. This may even cause the thread to misfire - if there is no available thread
for the duration of the scheduler's configured "misfire threshold".

A ThreadPool interface is defined in the org.quartz.spi package, and you can create a ThreadPool implementation
in any way you like. Quartz ships with a simple (but very satisfactory) thread pool named
org.quartz.simpl.SimpleThreadPool. This ThreadPool simply maintains a fixed set of threads in its pool - never grows,
never shrinks. But it is otherwise quite robust and is very well tested - as nearly everyone using Quartz uses this
pool.

***JobStores*** and ***DataSources*** were discussed in <a href="./tutorial-lesson-09.md"
    title="Tutorial Lesson 9">Lesson 9</a> of this tutorial. Worth noting here, is the fact that all JobStores implement
the org.quartz.spi.JobStore interface - and that if one of the bundled JobStores does not fit your needs, then you can
make your own.

Finally, you need to create your ***Scheduler*** instance. The Scheduler itself needs to be given a
name, told its RMI settings, and handed instances of a JobStore and ThreadPool. The RMI settings include whether the
Scheduler should create itself as a server object for RMI (make itself available to remote connections), what host and port
to use, etc.. StdSchedulerFactory (discussed below) can also produce Scheduler instances that are actually proxies (RMI
stubs) to Schedulers created in remote processes.

### [StdSchedulerFactory](#TutorialLesson10-StdSchedulerFactory)

StdSchedulerFactory is an implementation of the org.quartz.SchedulerFactory interface. It uses a set of
properties (java.util.Properties) to create and initialize a Quartz Scheduler. The properties are generally stored in
and loaded from a file, but can also be created by your program and handed directly to the factory. Simply calling
getScheduler() on the factory will produce the scheduler, initialize it (and its ThreadPool, JobStore and DataSources),
and return a handle to its public interface.

There are some sample configurations (including descriptions of the properties) in the "docs/config" directory of
the Quartz distribution. You can find complete documentation in the "Configuration" manual under the "Reference" section
of the Quartz documentation.

### [DirectSchedulerFactory](#TutorialLesson10-DirectSchedulerFactory)

DirectSchedulerFactory is another SchedulerFactory implementation. It is useful to those wishing to create their
Scheduler instance in a more programmatic way. Its use is generally discouraged for the following reasons: (1) it
requires the user to have a greater understanding of what they're doing, and (2) it does not allow for declarative
configuration - or in other words, you end up hard-coding all of the scheduler's settings.

### [Logging](#TutorialLesson10-Logging)
Quartz uses the SLF4J framework for all of its logging needs.  In order to "tune" the logging settings
(such as the amount of output, and where the output goes), you need to understand the SLF4J framework, which is
beyond the scope of this document.

If you want to capture extra information about trigger firings and job executions, you may be interested
in enabling the *org.quartz.plugins.history.LoggingJobHistoryPlugin* and/or
*org.quartz.plugins.history.LoggingTriggerHistoryPlugin*.

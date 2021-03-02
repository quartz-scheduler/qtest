---
title: Tutorial 12
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-11.md" title="Go to Lesson 11">&lsaquo;&nbsp;Lesson 11</a>
</div>

## Lesson 12: Miscellaneous Features of Quartz

### [Plug-Ins](#TutorialLesson12-PlugIns)

Quartz provides an interface (org.quartz.spi.SchedulerPlugin) for plugging-in additional functionality.

Plugins that ship with Quartz to provide various utility capabilities can be found documented in the ***org.quartz.plugins***
package. They provide functionality such as auto-scheduling of jobs upon scheduler startup, logging a history of job and
trigger events, and ensuring that the scheduler shuts down cleanly when the JVM exits.


### [JobFactory](#TutorialLesson12-JobFactory)

When a trigger fires, the Job it is associated to is instantiated via the JobFactory configured on the Scheduler.
The default JobFactory simply calls newInstance() on the job class. You may want to create your own implementation of
JobFactory to accomplish things such as having your application's IoC or DI container produce/initialize the job
instance.

See the **org.quartz.spi.JobFactory** interface, and the associated **Scheduler.setJobFactory(fact)**
method.


### '[Factory-Shipped' Jobs](#TutorialLesson12-FactoryShippedJobs)

Quartz also provides a number of utility Jobs that you can use in your application for doing things like sending
e-mails and invoking EJBs. These out-of-the-box Jobs can be found documented in the ***org.quartz.jobs***
package.

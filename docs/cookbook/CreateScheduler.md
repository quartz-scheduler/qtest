---
title: Cookbook
visible_title: "Quartz Cookbook"
active_sub_menu_id: site_mnu_docs_cookbook
---
<div class="secNavPanel"><a href=".">Contents</a> | <a href="SchedulerStandby.md">Next&nbsp;&rsaquo;</a></div>





# How-To: Instantiating a Scheduler

### Instantiating the Default Scheduler

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
// the 'default' scheduler is defined in "quartz.properties" found
// in the current working directory, in the classpath, or
// resorts to a fall-back default that is in the quartz.jar

SchedulerFactory sf = new StdSchedulerFactory();
Scheduler scheduler = sf.getScheduler();

// Scheduler will not execute jobs until it has been started (though they can be scheduled before start())
scheduler.start();
</code></pre>


### Instantiating A Specific Scheduler From Specific Properties

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
StdSchedulerFactory sf = new StdSchedulerFactory();

sf.initialize(schedulerProperties);

Scheduler scheduler = sf.getScheduler();

// Scheduler will not execute jobs until it has been started (though they can be scheduled before start())
scheduler.start();
</code></pre>


### Instantiating A Specific Scheduler From A Specific Property File

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
StdSchedulerFactory sf = new StdSchedulerFactory();

sf.initialize(fileName);

Scheduler scheduler = sf.getScheduler();

// Scheduler will not execute jobs until it has been started (though they can be scheduled before start())
scheduler.start();
</code></pre>

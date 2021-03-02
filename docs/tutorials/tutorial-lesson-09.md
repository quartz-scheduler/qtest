---
title: Tutorial 9
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-08.md" title="Go to Lesson 8">&lsaquo;&nbsp;Lesson 8</a> |
          <a href="tutorial-lesson-10.md" title="Go to Lesson 10">Lesson 10&nbsp;&rsaquo;</a>
</div>

## Lesson 9: Job Stores


JobStore's are responsible for keeping track of all the "work data" that you give to the scheduler: jobs,
triggers, calendars, etc. Selecting the appropriate JobStore for your Quartz scheduler instance is an important step.
Luckily, the choice should be a very easy one once you understand the differences between them. You declare which
JobStore your scheduler should use (and it's configuration settings) in the properties file (or object) that you provide
to the SchedulerFactory that you use to produce your scheduler instance.
<blockquote>
        Never use a JobStore instance directly in your code. For some reason many people attempt to do this. The
        JobStore is for behind-the-scenes use of Quartz itself. You have to tell Quartz (through configuration) which
        JobStore to use, but then you should only work with the Scheduler interface in your code.
</blockquote>

### [RAMJobStore](#TutorialLesson9-RAMJobStore)

RAMJobStore is the simplest JobStore to use, it is also the most performant (in terms of CPU time). RAMJobStore
gets its name in the obvious way: it keeps all of its data in RAM. This is why it's lightning-fast, and also why it's so
simple to configure. The drawback is that when your application ends (or crashes) all of the scheduling information is
lost - this means RAMJobStore cannot honor the setting of "non-volatility" on jobs and triggers. For some applications
this is acceptable - or even the desired behavior, but for other applications, this may be disastrous.

To use RAMJobStore (and assuming you're using StdSchedulerFactory) simply specify the class name
org.quartz.simpl.RAMJobStore as the JobStore class property that you use to configure quartz:

**Configuring Quartz to use RAMJobStore**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
org.quartz.jobStore.class = org.quartz.simpl.RAMJobStore
</code></pre>


There are no other settings you need to worry about.

### [JDBCJobStore](#TutorialLesson9-JDBCJobStore)

JDBCJobStore is also aptly named - it keeps all of its data in a database via JDBC. Because of this it is a bit
more complicated to configure than RAMJobStore, and it also is not as fast. However, the performance draw-back is not
terribly bad, especially if you build the database tables with indexes on the primary keys. On fairly modern set
of machines with a decent LAN (between the scheduler and database) the time to retrieve and update a firing trigger
will typically be less than 10 milliseconds.

JDBCJobStore works with nearly any database, it has been used widely with Oracle, PostgreSQL, MySQL, MS SQLServer,
HSQLDB, and DB2. To use JDBCJobStore, you must first create a set of database tables for Quartz to use. You
can find table-creation SQL scripts in the "docs/dbTables" directory of the Quartz distribution. If there is not already
a script for your database type, just look at one of the existing ones, and modify it in any way necessary for your DB.
One thing to note is that in these scripts, all the the tables start with the prefix "QRTZ_" (such as the tables
"QRTZ_TRIGGERS", and "QRTZ_JOB_DETAIL"). This prefix can actually be anything you'd like, as long as you inform
JDBCJobStore what the prefix is (in your Quartz properties). Using different prefixes may be useful for creating
multiple sets of tables, for multiple scheduler instances, within the same database.

Once you've got the tables created, you have one more major decision to make before configuring and firing up
JDBCJobStore. You need to decide what type of transactions your application needs. If you don't need to tie your
scheduling commands (such as adding and removing triggers) to other transactions, then you can let Quartz manage the
transaction by using ***JobStoreTX*** as your JobStore (this is the most common selection).

If you need Quartz to work along with other transactions (i.e. within a J2EE application server), then you should
use ***JobStoreCMT*** &#45; in which case Quartz will let the app server container manage the transactions.

The last piece of the puzzle is setting up a DataSource from which JDBCJobStore can get connections to your
database. DataSources are defined in your Quartz properties using one of a few different approaches. One approach is to
have Quartz create and manage the DataSource itself - by providing all of the connection information for the database.
Another approach is to have Quartz use a DataSource that is managed by an application server that Quartz is running
inside of - by providing JDBCJobStore the JNDI name of the DataSource. For details on the properties, consult the
example config files in the "docs/config" folder.

To use JDBCJobStore (and assuming you're using StdSchedulerFactory) you first need to set the JobStore class
property of your Quartz configuration to be either org.quartz.impl.jdbcjobstore.JobStoreTX or
org.quartz.impl.jdbcjobstore.JobStoreCMT - depending on the selection you made based on the explanations in the above
few paragraphs.

**Configuring Quartz to use JobStoreTx**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
org.quartz.jobStore.class = org.quartz.impl.jdbcjobstore.JobStoreTX
</code></pre>


Next, you need to select a DriverDelegate for the JobStore to use. The DriverDelegate is responsible for doing
any JDBC work that may be needed for your specific database. StdJDBCDelegate is a delegate that uses "vanilla" JDBC code
(and SQL statements) to do its work. If there isn't another delegate made specifically for your database, try using this
delegate - we've only made database-specific delegates for databases that we've found problems using StdJDBCDelegate with
(which seems to be most!). Other delegates can be found in the "org.quartz.impl.jdbcjobstore" package, or in its
sub-packages. Other delegates include DB2v6Delegate (for DB2 version 6 and earlier), HSQLDBDelegate (for HSQLDB),
MSSQLDelegate (for Microsoft SQLServer), PostgreSQLDelegate (for PostgreSQL), WeblogicDelegate (for using JDBC
drivers made by Weblogic), OracleDelegate (for using Oracle), and others.

Once you've selected your delegate, set its class name as the delegate for JDBCJobStore to use.

**Configuring JDBCJobStore to use a DriverDelegate**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.StdJDBCDelegate
</code></pre>


Next, you need to inform the JobStore what table prefix (discussed above) you are using.

**Configuring JDBCJobStore with the Table Prefix**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
org.quartz.jobStore.tablePrefix = QRTZ_
</code></pre>


And finally, you need to set which DataSource should be used by the JobStore. The named DataSource must also be
defined in your Quartz properties. In this case, we're specifying that Quartz should use the DataSource name "myDS"
(that is defined elsewhere in the configuration properties).

**Configuring JDBCJobStore with the name of the DataSource to use**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
org.quartz.jobStore.dataSource = myDS
</code></pre>
<blockquote>
        If your Scheduler is busy (i.e. nearly always executing the same number of jobs as the size of the
        thread pool, then you should probably set the number of connections in the DataSource to be the about the size
        of the thread pool + 2.
</blockquote>
<blockquote>
        The "org.quartz.jobStore.useProperties" config parameter can be set to "true" (defaults to false) in
        order to instruct JDBCJobStore that all values in JobDataMaps will be Strings, and therefore can be stored as
        name-value pairs, rather than storing more complex objects in their serialized form in the BLOB column. This is
        much safer in the long term, as you avoid the class versioning issues that there are with serializing your
        non-String classes into a BLOB.
</blockquote>

### [TerracottaJobStore](#TutorialLesson9-TerracottaJobStore)

TerracottaJobStore provides a means for scaling and robustness without the use of a database.  This means your database
can be kept free of load from Quartz, and can instead have all of its resources saved for the rest of your application.

TerracottaJobStore can be ran clustered or non-clustered, and in either case provides a storage medium for your
job data that is persistent between application restarts, because the data is stored in the Terracotta server.  It's
performance is much better than using a database via JDBCJobStore (about an order of magnitude better), but fairly
slower than RAMJobStore.

To use TerracottaJobStore (and assuming you're using StdSchedulerFactory) simply specify the class name
org.quartz.jobStore.class = org.terracotta.quartz.TerracottaJobStore as the JobStore class property that you use
to configure quartz, and add one extra line of configuration to specify the location of the Terracotta server:

**Configuring Quartz to use TerracottaJobStore**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
org.quartz.jobStore.class = org.terracotta.quartz.TerracottaJobStore
org.quartz.jobStore.tcConfigUrl = localhost:9510
</code></pre>


More information about this JobStore and Terracotta can be found at
<a href="http://www.terracotta.org/quartz">http://www.terracotta.org/quartz</a>

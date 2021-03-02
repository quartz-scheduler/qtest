---
title: Tutorial 5
visible_title: "Quartz Tutorials"
active_sub_menu_id: site_mnu_docs_tutorials
---
<div class="secNavPanel">
          <a href="./" title="Go to Tutorial Table of Contents">Table of Contents</a> |
          <a href="tutorial-lesson-04.md" title="Go to Lesson 4">&lsaquo;&nbsp;Lesson 4</a> |
          <a href="tutorial-lesson-06.md" title="Go to Lesson 6">Lesson 6&nbsp;&rsaquo;</a>
</div>

## Lesson 5: SimpleTrigger

***SimpleTrigger*** should meet your scheduling needs if you need to have a job execute exactly once at
a specific moment in time, or at a specific moment in time  followed by repeats at a specific interval.
For example, if you want the trigger to fire at exactly 11:23:54 AM on January 13, 2015, or if you want it to fire at
that time, and then fire five more times, every ten seconds.

With this description, you may not find it surprising to find that the properties of a SimpleTrigger include: a
start-time, and end-time, a repeat count, and a repeat interval. All of these properties are exactly what you'd expect
them to be, with only a couple special notes related to the end-time property.

The repeat count can be zero, a positive integer, or the constant value SimpleTrigger.REPEAT_INDEFINITELY. The
repeat interval property must be zero, or a positive long value, and represents a number of milliseconds. Note that a
repeat interval of zero will cause 'repeat count' firings of the trigger to happen concurrently (or as close to
concurrently as the scheduler can manage).

If you're not already familiar with Quartz's DateBuilder class, you may find it helpful for computing your
trigger fire-times, depending on the ***startTime*** (or endTime) that you're trying to create.

The ***endTime*** property (if it is specified) overrides the repeat count property. This can be useful
if you wish to create a trigger such as one that fires every 10 seconds until a given moment in time - rather than
having to compute the number of times it would repeat between the start-time and the end-time, you can simply specify
the end-time and then use a repeat count of REPEAT_INDEFINITELY (you could even specify a repeat count of some huge
number that is sure to be more than the number of times the trigger will actually fire before the end-time arrives).

SimpleTrigger instances are built using TriggerBuilder (for the trigger's main properties) and SimpleScheduleBuilder
(for the SimpleTrigger-specific properties).  To use these builders in a DSL-style, use static imports:


<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.DateBuilder.*:
</code></pre>


Here are various examples of defining triggers with simple schedules, read through them all, as they each show
at least one new/different point:

**Build a trigger for a specific moment in time, with no repeats:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  SimpleTrigger trigger = (SimpleTrigger) newTrigger()
    .withIdentity("trigger1", "group1")
    .startAt(myStartTime) // some Date
    .forJob("job1", "group1") // identify job with name, group strings
    .build();
</code></pre>

**Build a trigger for a specific moment in time, then repeating every ten seconds ten times:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(myTimeToStartFiring)  // if a start time is not given (if this line were omitted), "now" is implied
    .withSchedule(simpleSchedule()
        .withIntervalInSeconds(10)
        .withRepeatCount(10)) // note that 10 repeats will give a total of 11 firings
    .forJob(myJob) // identify job with handle to its JobDetail itself                   
    .build();
</code></pre>


**Build a trigger that will fire once, five minutes in the future:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  trigger = (SimpleTrigger) newTrigger()
    .withIdentity("trigger5", "group1")
    .startAt(futureDate(5, IntervalUnit.MINUTE)) // use DateBuilder to create a date in the future
    .forJob(myJobKey) // identify job with its JobKey
    .build();
</code></pre>


**Build a trigger that will fire now, then repeat every five minutes, until the hour 22:00:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  trigger = newTrigger()
    .withIdentity("trigger7", "group1")
    .withSchedule(simpleSchedule()
        .withIntervalInMinutes(5)
        .repeatForever())
    .endAt(dateOf(22, 0, 0))
    .build();
</code></pre>


**Build a trigger that will fire at the top of the next hour, then repeat every 2 hours, forever:**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  trigger = newTrigger()
    .withIdentity("trigger8") // because group is not specified, "trigger8" will be in the default group
    .startAt(evenHourDate(null)) // get the next even-hour (minutes and seconds zero ("00:00"))
    .withSchedule(simpleSchedule()
        .withIntervalInHours(2)
        .repeatForever())
    // note that in this example, 'forJob(..)' is not called
    //  - which is valid if the trigger is passed to the scheduler along with the job  
    .build();

    scheduler.scheduleJob(trigger, job);
</code></pre>


Spend some time looking at all of the available methods in the language defined by TriggerBuilder and
SimpleScheduleBuilder so that you can be familiar with options available to you that may not have been demonstrated
in the examples above.
<blockquote>
    Note that TriggerBuilder (and Quartz's other builders) will generally choose a reasonable value for properties
    that you do not explicitly set.  For examples: if you don't call one of the *withIdentity(..)* methods, then
    TriggerBuilder will generate a random name for your trigger; if you don't call *startAt(..)* then the current
    time (immediately) is assumed.
</blockquote>


### [SimpleTrigger Misfire Instructions](#TutorialLesson5-SimpleTriggerMisfireInstructions)

SimpleTrigger has several instructions that can be used to inform Quartz what it should do when a misfire occurs.
(Misfire situations were introduced in "Lesson 4: More About Triggers"). These instructions are defined
as constants on SimpleTrigger itself (including JavaDoc describing their behavior). The instructions include:

**Misfire Instruction Constants of SimpleTrigger**

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
MISFIRE_INSTRUCTION_FIRE_NOW
MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT
MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT
MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT
</code></pre>


You should recall from the earlier lessons that all triggers have the *Trigger.MISFIRE_INSTRUCTION_SMART_POLICY*
instruction available for use, and this instruction is also the default for all trigger types.

If the 'smart policy' instruction is used, SimpleTrigger dynamically chooses between its various MISFIRE
instructions, based on the configuration and state of the given SimpleTrigger instance. The JavaDoc for the
SimpleTrigger.updateAfterMisfire() method explains the exact details of this dynamic behavior.

When building SimpleTriggers, you specify the misfire instruction as part of the simple schedule
(via SimpleSchedulerBuilder):

<pre class="prettyprint highlight"><code class="language-java" data-lang="java">
  trigger = newTrigger()
    .withIdentity("trigger7", "group1")
    .withSchedule(simpleSchedule()
        .withIntervalInMinutes(5)
        .repeatForever()
        .withMisfireHandlingInstructionNextWithExistingCount())
    .build();
</code></pre>

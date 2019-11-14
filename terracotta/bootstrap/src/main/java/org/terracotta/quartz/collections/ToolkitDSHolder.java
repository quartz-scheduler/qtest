/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 */

package org.terracotta.quartz.collections;

import java.util.HashMap;
import java.util.Map;
import org.quartz.Calendar;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.terracotta.quartz.wrappers.FiredTrigger;
import org.terracotta.quartz.wrappers.JobWrapper;
import org.terracotta.quartz.wrappers.TriggerWrapper;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.concurrent.locks.ToolkitLock;
import org.terracotta.toolkit.internal.ToolkitInternal;
import org.terracotta.toolkit.internal.concurrent.locks.ToolkitLockTypeInternal;
import org.terracotta.toolkit.store.ToolkitConfigFields.Consistency;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.terracotta.toolkit.builder.ToolkitStoreConfigBuilder;
import org.terracotta.toolkit.collections.ToolkitSet;
import org.terracotta.toolkit.store.ToolkitConfigFields;
import org.terracotta.toolkit.store.ToolkitStore;

/**
 * How JOBS mappings will look? <br>
 * JobKey(name, groupname) &rarr; JobWrapper <br>
 * groupName &rarr; List&lt;String&gt; <br>
 * List &rarr; allGroupNames<br>
 */
public class ToolkitDSHolder {
  private static final String                                                       JOBS_MAP_PREFIX                     = "_tc_quartz_jobs";
  private static final String                                                       ALL_JOBS_GROUP_NAMES_SET_PREFIX     = "_tc_quartz_grp_names";
  private static final String                                                       PAUSED_GROUPS_SET_PREFIX            = "_tc_quartz_grp_paused_names";
  private static final String                                                       BLOCKED_JOBS_SET_PREFIX             = "_tc_quartz_blocked_jobs";
  private static final String                                                       JOBS_GROUP_MAP_PREFIX               = "_tc_quartz_grp_jobs_";

  private static final String                                                       TRIGGERS_MAP_PREFIX                 = "_tc_quartz_triggers";
  private static final String                                                       TRIGGERS_GROUP_MAP_PREFIX           = "_tc_quartz_grp_triggers_";
  private static final String                                                       ALL_TRIGGERS_GROUP_NAMES_SET_PREFIX = "_tc_quartz_grp_names_triggers";
  private static final String                                                       PAUSED_TRIGGER_GROUPS_SET_PREFIX    = "_tc_quartz_grp_paused_trogger_names";
  private static final String                                                       TIME_TRIGGER_SORTED_SET_PREFIX      = "_tc_time_trigger_sorted_set";
  private static final String                                                       FIRED_TRIGGER_MAP_PREFIX            = "_tc_quartz_fired_trigger";
  private static final String                                                       CALENDAR_WRAPPER_MAP_PREFIX         = "_tc_quartz_calendar_wrapper";
  private static final String                                                       SINGLE_LOCK_NAME_PREFIX             = "_tc_quartz_single_lock";

  private static final String                                                       DELIMETER                           = "|";
  private final String                                                              jobStoreName;
  protected final Toolkit                                                           toolkit;

  private final AtomicReference<SerializedToolkitStore<JobKey, JobWrapper>>         jobsMapReference                    = new AtomicReference<SerializedToolkitStore<JobKey, JobWrapper>>();
  private final AtomicReference<SerializedToolkitStore<TriggerKey, TriggerWrapper>> triggersMapReference                = new AtomicReference<SerializedToolkitStore<TriggerKey, TriggerWrapper>>();

  private final AtomicReference<ToolkitSet<String>>                                 allGroupsReference                  = new AtomicReference<ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<String>>                                 allTriggersGroupsReference          = new AtomicReference<ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<String>>                                 pausedGroupsReference               = new AtomicReference<ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<JobKey>>                                 blockedJobsReference                = new AtomicReference<ToolkitSet<JobKey>>();
  private final Map<String, ToolkitSet<String>>                                     jobsGroupSet                        = new HashMap<String, ToolkitSet<String>>();
  private final Map<String, ToolkitSet<String>>                                     triggersGroupSet                    = new HashMap<String, ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<String>>                                 pausedTriggerGroupsReference        = new AtomicReference<ToolkitSet<String>>();

  private final AtomicReference<ToolkitStore<String, FiredTrigger>>                 firedTriggersMapReference           = new AtomicReference<ToolkitStore<String, FiredTrigger>>();
  private final AtomicReference<ToolkitStore<String, Calendar>>                     calendarWrapperMapReference         = new AtomicReference<ToolkitStore<String, Calendar>>();
  private final AtomicReference<TimeTriggerSet>                                     timeTriggerSetReference             = new AtomicReference<TimeTriggerSet>();

  private final Map<String, ToolkitStore<?, ?>>                                     toolkitMaps                         = new HashMap<String, ToolkitStore<?, ?>>();
  
  public ToolkitDSHolder(String jobStoreName, Toolkit toolkit) {
    this.jobStoreName = jobStoreName;
    this.toolkit = toolkit;
  }

  protected final String generateName(String prefix) {
    return prefix + DELIMETER + jobStoreName;
  }

  public SerializedToolkitStore<JobKey, JobWrapper> getOrCreateJobsMap() {
    String jobsMapName = generateName(JOBS_MAP_PREFIX);
    SerializedToolkitStore<JobKey, JobWrapper> temp = new SerializedToolkitStore<JobKey, JobWrapper>(createStore(jobsMapName));
    jobsMapReference.compareAndSet(null, temp);
    return jobsMapReference.get();
  }

  protected ToolkitStore<?, ?> toolkitMap(String nameOfMap) {
    ToolkitStore<?, ?> map = toolkitMaps.get(nameOfMap);
    if (map != null && !map.isDestroyed()) {
      return map;
    } else {
      map = createStore(nameOfMap);
      toolkitMaps.put(nameOfMap, map);
      return map;
    }
  }

  private ToolkitStore createStore(String nameOfMap) {
    ToolkitStoreConfigBuilder builder = new ToolkitStoreConfigBuilder();
    return toolkit.getStore(nameOfMap, builder.consistency(Consistency.STRONG).concurrency(1).build(), null);
  }

  public SerializedToolkitStore<TriggerKey, TriggerWrapper> getOrCreateTriggersMap() {
    String triggersMapName = generateName(TRIGGERS_MAP_PREFIX);
    SerializedToolkitStore<TriggerKey, TriggerWrapper> temp = new SerializedToolkitStore<TriggerKey, TriggerWrapper>(
                                                                                                                     createStore(triggersMapName));
    triggersMapReference.compareAndSet(null, temp);
    return triggersMapReference.get();
  }

  public ToolkitStore<String, FiredTrigger> getOrCreateFiredTriggersMap() {
    String firedTriggerMapName = generateName(FIRED_TRIGGER_MAP_PREFIX);
    ToolkitStore<String, FiredTrigger> temp = createStore(firedTriggerMapName);
    firedTriggersMapReference.compareAndSet(null, temp);
    return firedTriggersMapReference.get();
  }

  public ToolkitStore<String, Calendar> getOrCreateCalendarWrapperMap() {
    String calendarWrapperName = generateName(CALENDAR_WRAPPER_MAP_PREFIX);
    ToolkitStore<String, Calendar> temp = createStore(calendarWrapperName);
    calendarWrapperMapReference.compareAndSet(null, temp);
    return calendarWrapperMapReference.get();
  }

  public Set<String> getOrCreateAllGroupsSet() {
    String allGrpSetNames = generateName(ALL_JOBS_GROUP_NAMES_SET_PREFIX);
    ToolkitSet<String> temp = toolkit.getSet(allGrpSetNames, String.class);
    allGroupsReference.compareAndSet(null, temp);

    return allGroupsReference.get();
  }

  public Set<JobKey> getOrCreateBlockedJobsSet() {
    String blockedJobsSetName = generateName(BLOCKED_JOBS_SET_PREFIX);
    ToolkitSet<JobKey> temp = toolkit.getSet(blockedJobsSetName, JobKey.class);
    blockedJobsReference.compareAndSet(null, temp);

    return blockedJobsReference.get();
  }

  public Set<String> getOrCreatePausedGroupsSet() {
    String pausedGrpsSetName = generateName(PAUSED_GROUPS_SET_PREFIX);
    ToolkitSet<String> temp = toolkit.getSet(pausedGrpsSetName, String.class);
    pausedGroupsReference.compareAndSet(null, temp);

    return pausedGroupsReference.get();
  }

  public Set<String> getOrCreatePausedTriggerGroupsSet() {
    String pausedGrpsSetName = generateName(PAUSED_TRIGGER_GROUPS_SET_PREFIX);
    ToolkitSet<String> temp = toolkit.getSet(pausedGrpsSetName, String.class);
    pausedTriggerGroupsReference.compareAndSet(null, temp);

    return pausedTriggerGroupsReference.get();
  }

  public Set<String> getOrCreateJobsGroupMap(String name) {
    ToolkitSet<String> set = jobsGroupSet.get(name);

    if (set != null && !set.isDestroyed()) {
      return set;
    } else {
      String nameForMap = generateName(JOBS_GROUP_MAP_PREFIX + name);
      set = toolkit.getSet(nameForMap, String.class);
      jobsGroupSet.put(name, set);
      return set;
    }
  }

  public void removeJobsGroupMap(String name) {
    ToolkitSet<String> set = jobsGroupSet.remove(name);
    if (set != null) {
      set.destroy();
    }
  }

  public Set<String> getOrCreateTriggersGroupMap(String name) {
    ToolkitSet<String> set = triggersGroupSet.get(name);

    if (set != null && !set.isDestroyed()) {
      return set;
    } else {
      String nameForMap = generateName(TRIGGERS_GROUP_MAP_PREFIX + name);
      set = toolkit.getSet(nameForMap, String.class);
      triggersGroupSet.put(name, set);
      return set;
    }
  }

  public void removeTriggersGroupMap(String name) {
    ToolkitSet<String> set = triggersGroupSet.remove(name);
    if (set != null) {
      set.destroy();
    }
  }

  public Set<String> getOrCreateAllTriggersGroupsSet() {
    String allTriggersGrpsName = generateName(ALL_TRIGGERS_GROUP_NAMES_SET_PREFIX);
    ToolkitSet<String> temp = toolkit.getSet(allTriggersGrpsName, String.class);
    allTriggersGroupsReference.compareAndSet(null, temp);

    return allTriggersGroupsReference.get();
  }

  public TimeTriggerSet getOrCreateTimeTriggerSet() {
    String triggerSetName = generateName(TIME_TRIGGER_SORTED_SET_PREFIX);
    TimeTriggerSet set = new TimeTriggerSet(toolkit.getSortedSet(triggerSetName, TimeTrigger.class));
    timeTriggerSetReference.compareAndSet(null, set);

    return timeTriggerSetReference.get();
  }

  public ToolkitLock getLock(ToolkitLockTypeInternal lockType) {
    String lockName = generateName(SINGLE_LOCK_NAME_PREFIX);
    return ((ToolkitInternal) toolkit).getLock(lockName, lockType);
  }
}

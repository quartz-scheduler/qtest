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
package org.terracotta.quartz;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.StringMatcher;
import org.quartz.impl.matchers.StringMatcher.StringOperatorName;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.TriggerFiredResult;
import org.terracotta.quartz.TriggerWrapper.TriggerState;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.cluster.ClusterEvent;
import org.terracotta.toolkit.cluster.ClusterInfo;
import org.terracotta.toolkit.cluster.ClusterNode;
import org.terracotta.toolkit.concurrent.locks.ToolkitLock;
import org.terracotta.toolkit.concurrent.locks.ToolkitLockType;
import org.terracotta.toolkit.internal.ToolkitInternal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class implements a <code>{@link org.quartz.spi.JobStore}</code> that utilizes Terracotta as its storage device.
 * </p>
 * <p>
 * This code is largely a cut-n-paste of RamJobStore and the world would be a better place if the duplicate code could
 * be refactored
 * </p>
 */

class DefaultClusteredJobStore implements ClusteredJobStore {
  private final ClusteredQuartzToolkitDSHolder                            toolkitDSHolder;
  private final Toolkit                                                   toolkit;

  private final Map<JobKey, JobWrapper>                                   jobsByFQN;
  private final Set<String>                                               allJobsGroupNames;
  private final Set<String>                                               pausedJobGroups;
  private final Set<JobKey>                                               blockedJobs;

  private final Map<TriggerKey, TriggerWrapper>                           triggersByFQN;
  private final Set<String>                                               allTriggersGroupNames;
  private final Set<String>                                               pausedTriggerGroups;
  private final TimeTriggerSet                                            timeTriggers;
  private final Map<String, FiredTrigger>                                 firedTriggers;

  private final Map<String, Calendar>                                     calendarsByName;
  private long                                                            misfireThreshold                        = 60000L;

  private final ToolkitLockType                                           lockType;
  private transient final ToolkitLock                                     lock;

  private final Serializer                                                serializer                              = new Serializer();
  private final ClusterInfo                                               dsoCluster;

  private transient long                                                  ftrCtr;
  private transient volatile SchedulerSignaler                            signaler;
  private transient volatile LoggerWrapper                                logger;
  private transient volatile String                                       terracottaClientId;
  private transient long                                                  estimatedTimeToReleaseAndAcquireTrigger = 15L;
  private transient volatile LocalLockState                               localStateLock;
  private transient volatile TriggerRemovedFromCandidateFiringListHandler triggerRemovedFromCandidateFiringListHandler;

  // This is a hack to prevent certain objects from ever being flushed. "this" should never be flushed (at least not
  // until the scheduler is shutdown) since it is referenced from the scheduler (which is not a shared object)
  // private transient Set<Object> hardRefs = new HashSet<Object>();

  public DefaultClusteredJobStore(boolean synchWrite, Toolkit toolkit, String jobStoreName) {
    this.toolkit = toolkit;
    this.dsoCluster = toolkit.getClusterInfo();
    this.toolkitDSHolder = new ClusteredQuartzToolkitDSHolder(jobStoreName, toolkit, serializer);
    this.jobsByFQN = toolkitDSHolder.getOrCreateJobsMap();
    this.allJobsGroupNames = toolkitDSHolder.getOrCreateAllGroupsSet();
    this.pausedJobGroups = toolkitDSHolder.getOrCreatePausedGroupsSet();
    this.blockedJobs = toolkitDSHolder.getOrCreateBlockedJobsSet();

    this.triggersByFQN = toolkitDSHolder.getOrCreateTriggersMap();
    this.allTriggersGroupNames = toolkitDSHolder.getOrCreateAllTriggersGroupsSet();
    this.pausedTriggerGroups = toolkitDSHolder.getOrCreatePausedTriggerGroupsSet();

    this.timeTriggers = toolkitDSHolder.getOrCreateTimeTriggerSet();
    this.firedTriggers = toolkitDSHolder.getOrCreateFiredTriggersMap();
    this.calendarsByName = toolkitDSHolder.getOrCreateCalendarWrapperMap();

    this.lockType = synchWrite ? ToolkitLockType.SYNCHRONOUS_WRITE : ToolkitLockType.WRITE;
    this.lock = toolkitDSHolder.getLock(lockType);

    getLog().info("Synchronous write locking is [" + synchWrite + "]");
  }

  // private synchronized boolean hardRef(Object obj) {
  // if (hardRefs == null) {
  // hardRefs = new HashSet<Object>();
  // }
  //
  // return hardRefs.add(obj);
  // }

  private LoggerWrapper getLog() {
    if (logger == null) {
      this.logger = LogWrapperFactory.getLogger(getClass());
    }
    return logger;
  }

  private void disable() {
    try {
      getLocalLockState().disableLocking();
    } catch (InterruptedException e) {
      getLog().error("failed to disable the job store", e);
    }
  }

  private LocalLockState getLocalLockState() {
    LocalLockState rv = localStateLock;
    if (rv != null) return rv;

    synchronized (DefaultClusteredJobStore.class) {
      if (localStateLock == null) {
        localStateLock = new LocalLockState();
      }
      return localStateLock;
    }
  }

  void lock() throws JobPersistenceException {
    getLocalLockState().attemptAcquireBegin();
    lock.lock();
  }

  void unlock() {
    lock.unlock();
    getLocalLockState().release();
  }

  Serializer getSerializer() {
    return serializer;
  }

  /**
   * <p>
   * Called by the QuartzScheduler before the <code>JobStore</code> is used, in order to give the it a chance to
   * initialize.
   * </p>
   */

  // XXX: remove this suppression
  @SuppressWarnings("unchecked")
  public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler schedulerSignaler) {
    this.terracottaClientId = dsoCluster.waitUntilNodeJoinsCluster().getId();
    this.ftrCtr = System.currentTimeMillis();

    // this MUST happen before initializing the trigger set (otherwise we might receive an update which get an NPE)
    // this.serializer.setClassLoadHelper(loadHelper);

    this.signaler = schedulerSignaler;

    getLog().info(getClass().getSimpleName() + " initialized.");

    ((ToolkitInternal) toolkit).registerBeforeShutdownHook(new ShutdownHook(this));
  }

  public void schedulerStarted() throws SchedulerException {
    dsoCluster.addClusterListener(this);

    Collection<ClusterNode> nodes = dsoCluster.getClusterTopology().getNodes();

    Set<String> activeClientIDs = new HashSet<String>();
    for (ClusterNode node : nodes) {
      boolean added = activeClientIDs.add(node.getId());
      if (!added) {
        getLog().error("DUPLICATE node ID detected: " + node);
      }
    }

    lock();
    try {
      List<TriggerWrapper> toEval = new ArrayList<TriggerWrapper>();

      // scan for orphaned triggers
      for (TriggerKey triggerKey : triggersByFQN.keySet()) {
        TriggerWrapper tw = triggersByFQN.get(triggerKey);
        String lastTerracotaClientId = tw.getLastTerracotaClientId();
        if (lastTerracotaClientId == null) {
          continue;
        }

        if (!activeClientIDs.contains(lastTerracotaClientId) || tw.getState() == TriggerState.ERROR) {
          toEval.add(tw);
        }
      }

      for (TriggerWrapper tw : toEval) {
        evalOrphanedTrigger(tw, true);
      }

      // scan firedTriggers
      for (Iterator<FiredTrigger> iter = firedTriggers.values().iterator(); iter.hasNext();) {
        FiredTrigger ft = iter.next();
        if (!activeClientIDs.contains(ft.getClientId())) {
          getLog().info("Found non-complete fired trigger: " + ft);
          iter.remove();

          TriggerWrapper tw = triggersByFQN.get(ft.getTriggerKey());
          if (tw == null) {
            getLog().error("no trigger found for executing trigger: " + ft.getTriggerKey());
            continue;
          }

          scheduleRecoveryIfNeeded(tw, ft.getFireTime());
        }
      }
    } finally {
      unlock();
    }
  }

  public void schedulerPaused() {
    // do nothing
  }

  public void schedulerResumed() {
    // do nothing
  }

  private void evalOrphanedTrigger(TriggerWrapper tw, boolean newNode) {
    getLog().info("Evaluating orphaned trigger " + tw);

    JobWrapper jobWrapper = jobsByFQN.get(tw.getJobKey());

    if (jobWrapper == null) {
      getLog().error("No job found for orphaned trigger: " + tw);
      return;
    }

    if (newNode && tw.getState() == TriggerState.ERROR) {
      tw.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);
      timeTriggers.add(tw);
    }

    if (tw.getState() == TriggerState.BLOCKED) {
      tw.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);
      timeTriggers.add(tw);
    } else if (tw.getState() == TriggerState.PAUSED_BLOCKED) {
      tw.setState(TriggerState.PAUSED, terracottaClientId, triggersByFQN);
    }

    if (tw.getState() == TriggerState.ACQUIRED) {
      tw.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);
      timeTriggers.add(tw);
    }

    if (!tw.mayFireAgain() && !jobWrapper.requestsRecovery()) {
      try {
        removeTrigger(tw.getKey());
      } catch (JobPersistenceException e) {
        getLog().error("Can't remove completed trigger (and related job) " + tw, e);
      }
    }

    if (jobWrapper.isConcurrentExectionDisallowed()) {
      List<TriggerWrapper> triggersForJob = getTriggerWrappersForJob(jobWrapper.getKey());

      for (TriggerWrapper trigger : triggersForJob) {
        if (trigger.getState() == TriggerState.BLOCKED) {
          trigger.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);
          timeTriggers.add(trigger);
        } else if (trigger.getState() == TriggerState.PAUSED_BLOCKED) {
          trigger.setState(TriggerState.PAUSED, terracottaClientId, triggersByFQN);
        }
      }
    }
  }

  private void scheduleRecoveryIfNeeded(TriggerWrapper tw, long origFireTime) {
    JobWrapper jobWrapper = jobsByFQN.get(tw.getJobKey());

    if (jobWrapper == null) {
      getLog().error("No job found for orphaned trigger: " + tw);
      return;
    }

    if (jobWrapper.requestsRecovery()) {
      SimpleTriggerImpl recoveryTrigger = new SimpleTriggerImpl("recover_" + terracottaClientId + "_" + ftrCtr++,
                                                                Scheduler.DEFAULT_RECOVERY_GROUP, new Date());

      recoveryTrigger.setJobName(jobWrapper.getKey().getName());
      recoveryTrigger.setJobGroup(jobWrapper.getKey().getGroup());
      recoveryTrigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
      recoveryTrigger.setPriority(tw.getPriority());
      JobDataMap jd = jobWrapper.getJobDataMapClone();
      jd.put(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_NAME, tw.getKey().getName());
      jd.put(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_GROUP, tw.getKey().getGroup());
      jd.put(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_FIRETIME_IN_MILLISECONDS, String.valueOf(origFireTime));

      recoveryTrigger.setJobDataMap(jd);
      jobWrapper.setJobDataMap(jd, jobsByFQN);

      recoveryTrigger.computeFirstFireTime(null);

      try {
        storeTrigger(recoveryTrigger, false);
        if (!tw.mayFireAgain()) {
          removeTrigger(tw.getKey());
        }
        getLog().info("Recovered job " + jobWrapper + " for trigger " + tw);
      } catch (JobPersistenceException e) {
        getLog().error("Can't recover job " + jobWrapper + " for trigger " + tw, e);
      }
    }
  }

  private long getMisfireThreshold() {
    return misfireThreshold;
  }

  /**
   * The number of milliseconds by which a trigger must have missed its next-fire-time, in order for it to be considered
   * "misfired" and thus have its misfire instruction applied.
   * 
   * @param misfireThreshold
   */
  public void setMisfireThreshold(long misfireThreshold) {
    if (misfireThreshold < 1) { throw new IllegalArgumentException("Misfirethreashold must be larger than 0"); }

    try {
      lock();
    } catch (JobPersistenceException e) {
      getLog().error("Not setting misfireThreshold to " + misfireThreshold, e);
      return;
    }

    try {
      this.misfireThreshold = misfireThreshold;
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Called by the QuartzScheduler to inform the <code>JobStore</code> that it should free up all of it's resources
   * because the scheduler is shutting down.
   * </p>
   */
  public void shutdown() {
    // nothing to do
  }

  public boolean supportsPersistence() {
    // We throw an assertion here since this method should never be called directly on this instance.
    throw new AssertionError();
  }

  /**
   * <p>
   * Store the given <code>{@link org.quartz.JobDetail}</code> and <code>{@link org.quartz.Trigger}</code>.
   * </p>
   * 
   * @param newJob The <code>JobDetail</code> to be stored.
   * @param newTrigger The <code>Trigger</code> to be stored.
   * @throws ObjectAlreadyExistsException if a <code>Job</code> with the same name/group already exists.
   */
  public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws JobPersistenceException {
    storeJob(newJob, false);
    storeTrigger(newTrigger, false);
  }

  /**
   * <p>
   * Store the given <code>{@link org.quartz.Job}</code>.
   * </p>
   * 
   * @param newJob The <code>Job</code> to be stored.
   * @param replaceExisting If <code>true</code>, any <code>Job</code> existing in the <code>JobStore</code> with the
   *        same name & group should be over-written.
   * @throws ObjectAlreadyExistsException if a <code>Job</code> with the same name/group already exists, and
   *         replaceExisting is set to false.
   */
  public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException,
      JobPersistenceException {
    JobDetail clone = (JobDetail) newJob.clone();

    lock();
    try {
      // wrapper construction must be done in lock since serializer is unlocked
      JobWrapper jw = new JobWrapper(clone);

      if (jobsByFQN.containsKey(jw.getKey())) {
        if (!replaceExisting) { throw new ObjectAlreadyExistsException(newJob); }
      } else {
        // get job group
        Set<String> grpSet = toolkitDSHolder.getOrCreateJobsGroupMap(newJob.getKey().getGroup());
        // add to jobs by group
        grpSet.add(jw.getKey().getName());

        if (!allJobsGroupNames.contains(jw.getKey().getGroup())) {
          allJobsGroupNames.add(jw.getKey().getGroup());
        }
      }

      // add/update jobs FQN map
      jobsByFQN.put(jw.getKey(), jw);
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Remove (delete) the <code>{@link org.quartz.Job}</code> with the given name, and any
   * <code>{@link org.quartz.Trigger}</code> s that reference it.
   * </p>
   * 
   * @param jobKey The key of the <code>Job</code> to be removed.
   * @return <code>true</code> if a <code>Job</code> with the given name & group was found and removed from the store.
   */
  public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
    boolean found = false;
    lock();
    try {
      List<OperableTrigger> trigger = getTriggersForJob(jobKey);
      for (OperableTrigger trig : trigger) {
        this.removeTrigger(trig.getKey());
        found = true;
      }

      found = (jobsByFQN.remove(jobKey) != null) | found;
      if (found) {
        Set<String> grpSet = toolkitDSHolder.getOrCreateJobsGroupMap(jobKey.getGroup());
        grpSet.remove(jobKey.getName());
        if (grpSet.isEmpty()) {
          toolkitDSHolder.removeJobsGroupMap(jobKey.getGroup());
          allJobsGroupNames.remove(jobKey.getGroup());
        }
      }
    } finally {
      unlock();
    }

    return found;
  }

  public boolean removeJobs(List<JobKey> jobKeys) throws JobPersistenceException {
    boolean allFound = true;

    lock();
    try {
      for (JobKey key : jobKeys)
        allFound = removeJob(key) && allFound;
    } finally {
      unlock();
    }

    return allFound;
  }

  public boolean removeTriggers(List<TriggerKey> triggerKeys) throws JobPersistenceException {
    boolean allFound = true;

    lock();
    try {
      for (TriggerKey key : triggerKeys)
        allFound = removeTrigger(key) && allFound;
    } finally {
      unlock();
    }

    return allFound;
  }

  public void storeJobsAndTriggers(Map<JobDetail, List<Trigger>> triggersAndJobs, boolean replace)
      throws ObjectAlreadyExistsException, JobPersistenceException {

    lock();
    try {
      // make sure there are no collisions...
      if (!replace) {
        for (JobDetail job : triggersAndJobs.keySet()) {
          if (checkExists(job.getKey())) throw new ObjectAlreadyExistsException(job);
          for (Trigger trigger : triggersAndJobs.get(job)) {
            if (checkExists(trigger.getKey())) throw new ObjectAlreadyExistsException(trigger);
          }
        }
      }
      // do bulk add...
      for (JobDetail job : triggersAndJobs.keySet()) {
        storeJob(job, true);
        for (Trigger trigger : triggersAndJobs.get(job)) {
          storeTrigger((OperableTrigger) trigger, true);
        }
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Store the given <code>{@link org.quartz.Trigger}</code>.
   * </p>
   * 
   * @param newTrigger The <code>Trigger</code> to be stored.
   * @param replaceExisting If <code>true</code>, any <code>Trigger</code> existing in the <code>JobStore</code> with
   *        the same name & group should be over-written.
   * @throws ObjectAlreadyExistsException if a <code>Trigger</code> with the same name/group already exists, and
   *         replaceExisting is set to false.
   * @see #pauseTriggers(org.quartz.impl.matchers.GroupMatcher)
   */
  public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws JobPersistenceException {
    OperableTrigger clone = (OperableTrigger) newTrigger.clone();

    lock();
    try {
      JobDetail job = retrieveJob(newTrigger.getJobKey());
      if (job == null) {
        //
        throw new JobPersistenceException("The job (" + newTrigger.getJobKey()
                                          + ") referenced by the trigger does not exist.");
      }

      // wrapper construction must be done in lock since serializer is unlocked
      TriggerWrapper tw = new TriggerWrapper(clone, job.isConcurrentExectionDisallowed(), serializer);

      if (triggersByFQN.containsKey(tw.getKey())) {
        if (!replaceExisting) { throw new ObjectAlreadyExistsException(newTrigger); }

        removeTrigger(newTrigger.getKey(), false);
      }

      // add to triggers by group
      Set<String> grpSet = toolkitDSHolder.getOrCreateTriggersGroupMap(newTrigger.getKey().getGroup());
      grpSet.add(newTrigger.getKey().getName());
      if (!allTriggersGroupNames.contains(newTrigger.getKey().getGroup())) {
        allTriggersGroupNames.add(newTrigger.getKey().getGroup());
      }

      if (pausedTriggerGroups.contains(newTrigger.getKey().getGroup())
          || pausedJobGroups.contains(newTrigger.getJobKey().getGroup())) {
        tw.setState(TriggerState.PAUSED, terracottaClientId, triggersByFQN);
        if (blockedJobs.contains(tw.getJobKey())) {
          tw.setState(TriggerState.PAUSED_BLOCKED, terracottaClientId, triggersByFQN);
        }
      } else if (blockedJobs.contains(tw.getJobKey())) {
        tw.setState(TriggerState.BLOCKED, terracottaClientId, triggersByFQN);
      } else {
        timeTriggers.add(tw);
      }

      // add to triggers by FQN map
      triggersByFQN.put(tw.getKey(), tw);
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Remove (delete) the <code>{@link org.quartz.Trigger}</code> with the given name.
   * </p>
   * 
   * @param triggerKey The key of the <code>Trigger</code> to be removed.
   * @return <code>true</code> if a <code>Trigger</code> with the given name & group was found and removed from the
   *         store.
   */
  public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    return removeTrigger(triggerKey, true);
  }

  private boolean removeTrigger(TriggerKey triggerKey, boolean removeOrphanedJob) throws JobPersistenceException {

    lock();
    TriggerWrapper tw = null;
    try {
      // remove from triggers by FQN map
      tw = triggersByFQN.remove(triggerKey);

      if (tw != null) {
        // remove from triggers by group
        Set<String> grpSet = toolkitDSHolder.getOrCreateTriggersGroupMap(triggerKey.getGroup());
        grpSet.remove(triggerKey.getName());
        if (grpSet.size() == 0) {
          toolkitDSHolder.removeTriggersGroupMap(triggerKey.getGroup());
          allTriggersGroupNames.remove(triggerKey.getGroup());
        }
        // remove from triggers array
        timeTriggers.remove(tw);

        if (removeOrphanedJob) {
          JobWrapper jw = jobsByFQN.get(tw.getJobKey());
          List<OperableTrigger> trigs = getTriggersForJob(tw.getJobKey());
          if ((trigs == null || trigs.size() == 0) && !jw.isDurable()) {
            JobKey jobKey = tw.getJobKey();
            if (removeJob(jobKey)) {
              signaler.notifySchedulerListenersJobDeleted(jobKey);
            }
          }
        }
      }
    } finally {
      unlock();
    }

    return tw != null;
  }

  /**
   * @see org.quartz.spi.JobStore#replaceTrigger
   */
  public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
    boolean found = false;

    lock();
    try {
      // remove from triggers by FQN map
      TriggerWrapper tw = triggersByFQN.remove(triggerKey);
      found = tw != null;

      if (tw != null) {
        if (!tw.getJobKey().equals(newTrigger.getJobKey())) { throw new JobPersistenceException(
                                                                                                "New trigger is not related to the same job as the old trigger."); }
        // remove from triggers by group
        Set<String> grpSet = toolkitDSHolder.getOrCreateTriggersGroupMap(triggerKey.getGroup());
        grpSet.remove(triggerKey.getName());
        if (grpSet.size() == 0) {
          toolkitDSHolder.removeTriggersGroupMap(triggerKey.getGroup());
          allTriggersGroupNames.remove(triggerKey.getGroup());
        }
        timeTriggers.remove(tw);

        try {
          storeTrigger(newTrigger, false);
        } catch (JobPersistenceException jpe) {
          storeTrigger(tw.getTriggerClone(), false); // put previous trigger back...
          throw jpe;
        }
      }
    } finally {
      unlock();
    }

    return found;
  }

  /**
   * <p>
   * Retrieve the <code>{@link org.quartz.JobDetail}</code> for the given <code>{@link org.quartz.Job}</code>.
   * </p>
   * 
   * @param jobKey The key of the <code>Job</code> to be retrieved.
   * @return The desired <code>Job</code>, or null if there is no match.
   */
  public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
    JobWrapper jobWrapper = getJob(jobKey);
    return jobWrapper == null ? null : (JobDetail) jobWrapper.getJobDetailClone();
  }

  JobWrapper getJob(final JobKey key) throws JobPersistenceException {
    lock();
    try {
      return jobsByFQN.get(key);
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
   * </p>
   * 
   * @param triggerKey The key of the <code>Trigger</code> to be retrieved.
   * @return The desired <code>Trigger</code>, or null if there is no match.
   */
  public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    lock();
    try {
      TriggerWrapper tw = triggersByFQN.get(triggerKey);
      return (tw != null) ? (OperableTrigger) tw.getTriggerClone() : null;
    } finally {
      unlock();
    }
  }

  public boolean checkExists(final JobKey jobKey) {
    return jobsByFQN.containsKey(jobKey);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws JobPersistenceException
   */
  public boolean checkExists(final TriggerKey triggerKey) throws JobPersistenceException {
    return triggersByFQN.containsKey(triggerKey);
  }

  public void clearAllSchedulingData() throws JobPersistenceException {
    lock();
    try {
      // unschedule jobs (delete triggers)
      List<String> lst = getTriggerGroupNames();
      for (String group : lst) {
        Set<TriggerKey> keys = getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
        for (TriggerKey key : keys) {
          removeTrigger(key);
        }
      }
      // delete jobs
      lst = getJobGroupNames();
      for (String group : lst) {
        Set<JobKey> keys = getJobKeys(GroupMatcher.jobGroupEquals(group));
        for (JobKey key : keys) {
          removeJob(key);
        }
      }
      // delete calendars
      lst = getCalendarNames();
      for (String name : lst) {
        removeCalendar(name);
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the current state of the identified <code>{@link Trigger}</code>.
   * </p>
   * 
   * @see Trigger.TriggerState
   */
  public Trigger.TriggerState getTriggerState(org.quartz.TriggerKey key) throws JobPersistenceException {

    TriggerWrapper tw;
    lock();
    try {
      tw = triggersByFQN.get(key);
    } finally {
      unlock();
    }

    if (tw == null) { return Trigger.TriggerState.NONE; }

    if (tw.getState() == TriggerState.COMPLETE) { return Trigger.TriggerState.COMPLETE; }

    if (tw.getState() == TriggerState.PAUSED) { return Trigger.TriggerState.PAUSED; }

    if (tw.getState() == TriggerState.PAUSED_BLOCKED) { return Trigger.TriggerState.PAUSED; }

    if (tw.getState() == TriggerState.BLOCKED) { return Trigger.TriggerState.BLOCKED; }

    if (tw.getState() == TriggerState.ERROR) { return Trigger.TriggerState.ERROR; }

    return Trigger.TriggerState.NORMAL;
  }

  /**
   * <p>
   * Store the given <code>{@link org.quartz.Calendar}</code>.
   * </p>
   * 
   * @param calendar The <code>Calendar</code> to be stored.
   * @param replaceExisting If <code>true</code>, any <code>Calendar</code> existing in the <code>JobStore</code> with
   *        the same name & group should be over-written.
   * @param updateTriggers If <code>true</code>, any <code>Trigger</code>s existing in the <code>JobStore</code> that
   *        reference an existing Calendar with the same name with have their next fire time re-computed with the new
   *        <code>Calendar</code>.
   * @throws ObjectAlreadyExistsException if a <code>Calendar</code> with the same name already exists, and
   *         replaceExisting is set to false.
   */
  public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers)
      throws ObjectAlreadyExistsException, JobPersistenceException {

    Calendar clone = (Calendar) calendar.clone();

    lock();
    try {
      Calendar cal = calendarsByName.get(name);

      if (cal != null && replaceExisting == false) {
        throw new ObjectAlreadyExistsException("Calendar with name '" + name + "' already exists.");
      } else if (cal != null) {
        calendarsByName.remove(name);
      }

      Calendar cw = clone;
      calendarsByName.put(name, cw);

      if (cal != null && updateTriggers) {
        for (TriggerWrapper tw : getTriggerWrappersForCalendar(name)) {
          boolean removed = timeTriggers.remove(tw);

          tw.updateWithNewCalendar(clone, getMisfireThreshold(), triggersByFQN);

          if (removed) {
            timeTriggers.add(tw);
          }
        }
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Remove (delete) the <code>{@link org.quartz.Calendar}</code> with the given name.
   * </p>
   * <p>
   * If removal of the <code>Calendar</code> would result in <code.Trigger</code>s pointing to non-existent calendars,
   * then a <code>JobPersistenceException</code> will be thrown.
   * </p>
   * *
   * 
   * @param calName The name of the <code>Calendar</code> to be removed.
   * @return <code>true</code> if a <code>Calendar</code> with the given name was found and removed from the store.
   */
  public boolean removeCalendar(String calName) throws JobPersistenceException {
    int numRefs = 0;

    lock();
    try {
      for (TriggerKey triggerKey : triggersByFQN.keySet()) {
        TriggerWrapper tw = triggersByFQN.get(triggerKey);
        if (tw.getCalendarName() != null && tw.getCalendarName().equals(calName)) {
          numRefs++;
        }
      }

      if (numRefs > 0) { throw new JobPersistenceException("Calender cannot be removed if it referenced by a Trigger!"); }

      return (calendarsByName.remove(calName) != null);
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
   * </p>
   * 
   * @param calName The name of the <code>Calendar</code> to be retrieved.
   * @return The desired <code>Calendar</code>, or null if there is no match.
   */
  public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
    lock();
    try {
      Calendar cw = calendarsByName.get(calName);
      return (Calendar) (cw == null ? null : cw.clone());
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the number of <code>{@link org.quartz.JobDetail}</code> s that are stored in the <code>JobsStore</code>.
   * </p>
   */
  public int getNumberOfJobs() throws JobPersistenceException {
    lock();
    try {
      return jobsByFQN.size();
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the number of <code>{@link org.quartz.Trigger}</code> s that are stored in the <code>JobsStore</code>.
   * </p>
   */
  public int getNumberOfTriggers() throws JobPersistenceException {
    lock();
    try {
      return triggersByFQN.size();
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the number of <code>{@link org.quartz.Calendar}</code> s that are stored in the <code>JobsStore</code>.
   * </p>
   */
  public int getNumberOfCalendars() throws JobPersistenceException {
    lock();
    try {
      return calendarsByName.size();
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the names of all of the <code>{@link org.quartz.Job}</code> s that have the given group name.
   * </p>
   */
  public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    lock();
    try {
      Set<String> matchingGroups = new HashSet<String>();
      switch (matcher.getCompareWithOperator()) {
        case EQUALS:
          matchingGroups.add(matcher.getCompareToValue());
          break;
        default:
          for (String group : allJobsGroupNames) {
            if (matcher.getCompareWithOperator().evaluate(group, matcher.getCompareToValue())) {
              matchingGroups.add(group);
            }
          }
      }

      Set<JobKey> out = new HashSet<JobKey>();
      for (String matchingGroup : matchingGroups) {

        Set<String> grpJobNames = toolkitDSHolder.getOrCreateJobsGroupMap(matchingGroup);
        for (String jobName : grpJobNames) {
          JobKey jobKey = new JobKey(jobName, matchingGroup);
          if (jobsByFQN.containsKey(jobKey)) {
            out.add(jobKey);
          }
        }
      }

      return out;
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the names of all of the <code>{@link org.quartz.Calendar}</code> s in the <code>JobStore</code>.
   * </p>
   * <p>
   * If there are no Calendars in the given group name, the result should be a zero-length array (not <code>null</code>
   * ).
   * </p>
   */
  public List<String> getCalendarNames() throws JobPersistenceException {
    lock();
    try {
      Set<String> names = calendarsByName.keySet();
      return new ArrayList<String>(names);
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the names of all of the <code>{@link org.quartz.Trigger}</code> s that have the given group name.
   * </p>
   */
  public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    lock();
    try {
      Set<String> groupNames = new HashSet<String>();
      switch (matcher.getCompareWithOperator()) {
        case EQUALS:
          groupNames.add(matcher.getCompareToValue());
          break;
        default:
          for (String group : allTriggersGroupNames) {
            if (matcher.getCompareWithOperator().evaluate(group, matcher.getCompareToValue())) {
              groupNames.add(group);
            }
          }
      }

      Set<TriggerKey> out = new HashSet<TriggerKey>();

      for (String groupName : groupNames) {
        Set<String> grpSet = toolkitDSHolder.getOrCreateTriggersGroupMap(groupName);

        for (String key : grpSet) {
          TriggerKey triggerKey = new TriggerKey(key, groupName);
          TriggerWrapper tw = triggersByFQN.get(triggerKey);
          if (tw != null) {
            out.add(triggerKey);
          }
        }
      }

      return out;
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the names of all of the <code>{@link org.quartz.Job}</code> groups.
   * </p>
   */
  public List<String> getJobGroupNames() throws JobPersistenceException {
    lock();
    try {
      return new ArrayList<String>(allJobsGroupNames);
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get the names of all of the <code>{@link org.quartz.Trigger}</code> groups.
   * </p>
   */
  public List<String> getTriggerGroupNames() throws JobPersistenceException {
    lock();
    try {
      return new ArrayList<String>(allTriggersGroupNames);
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Get all of the Triggers that are associated to the given Job.
   * </p>
   * <p>
   * If there are no matches, a zero-length array should be returned.
   * </p>
   */
  public List<OperableTrigger> getTriggersForJob(final JobKey jobKey) throws JobPersistenceException {
    List<OperableTrigger> trigList = new ArrayList<OperableTrigger>();

    lock();
    try {
      for (TriggerKey triggerKey : triggersByFQN.keySet()) {
        TriggerWrapper tw = triggersByFQN.get(triggerKey);
        if (tw.getJobKey().equals(jobKey)) {
          trigList.add(tw.getTriggerClone());
        }
      }
    } finally {
      unlock();
    }

    return trigList;
  }

  private List<TriggerWrapper> getTriggerWrappersForJob(JobKey key) {
    List<TriggerWrapper> trigList = new ArrayList<TriggerWrapper>();

    for (TriggerKey triggerKey : triggersByFQN.keySet()) {
      TriggerWrapper tw = triggersByFQN.get(triggerKey);
      if (tw.getJobKey().equals(key)) {
        trigList.add(tw);
      }
    }

    return trigList;
  }

  private List<TriggerWrapper> getTriggerWrappersForCalendar(String calName) {
    List<TriggerWrapper> trigList = new ArrayList<TriggerWrapper>();

    for (TriggerKey triggerKey : triggersByFQN.keySet()) {
      TriggerWrapper tw = triggersByFQN.get(triggerKey);
      String tcalName = tw.getCalendarName();
      if (tcalName != null && tcalName.equals(calName)) {
        trigList.add(tw);
      }
    }

    return trigList;
  }

  /**
   * <p>
   * Pause the <code>{@link Trigger}</code> with the given name.
   * </p>
   */
  public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    lock();
    try {
      TriggerWrapper tw = triggersByFQN.get(triggerKey);

      // does the trigger exist?
      if (tw == null) { return; }

      // if the trigger is "complete" pausing it does not make sense...
      if (tw.getState() == TriggerState.COMPLETE) { return; }

      if (tw.getState() == TriggerState.BLOCKED) {
        tw.setState(TriggerState.PAUSED_BLOCKED, terracottaClientId, triggersByFQN);
      } else {
        tw.setState(TriggerState.PAUSED, terracottaClientId, triggersByFQN);
      }

      timeTriggers.remove(tw);
      if (triggerRemovedFromCandidateFiringListHandler != null) {
        triggerRemovedFromCandidateFiringListHandler.removeCandidateTrigger(tw);
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Pause all of the <code>{@link Trigger}s</code> in the given group.
   * </p>
   * <p>
   * The JobStore should "remember" that the group is paused, and impose the pause on any new triggers that are added to
   * the group while the group is paused.
   * </p>
   */
  public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    HashSet<String> pausedGroups = new HashSet<String>();
    lock();
    try {
      Set<TriggerKey> triggerKeys = getTriggerKeys(matcher);
      for (TriggerKey key : triggerKeys) {
        pausedTriggerGroups.add(key.getGroup());
        pausedGroups.add(key.getGroup());
        pauseTrigger(key);
      }
      // make sure to account for an exact group match for a group that doesn't yet exist
      StringMatcher.StringOperatorName operator = matcher.getCompareWithOperator();
      if (operator.equals(StringOperatorName.EQUALS)) {
        pausedTriggerGroups.add(matcher.getCompareToValue());
        pausedGroups.add(matcher.getCompareToValue());
      }
    } finally {
      unlock();
    }
    return pausedGroups;
  }

  /**
   * <p>
   * Pause the <code>{@link org.quartz.JobDetail}</code> with the given name - by pausing all of its current
   * <code>Trigger</code>s.
   * </p>
   */
  public void pauseJob(JobKey jobKey) throws JobPersistenceException {
    lock();
    try {
      for (OperableTrigger trigger : getTriggersForJob(jobKey)) {
        pauseTrigger(trigger.getKey());
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Pause all of the <code>{@link org.quartz.JobDetail}s</code> in the given group - by pausing all of their
   * <code>Trigger</code>s.
   * </p>
   * <p>
   * The JobStore should "remember" that the group is paused, and impose the pause on any new jobs that are added to the
   * group while the group is paused.
   * </p>
   */
  public Collection<String> pauseJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    Collection<String> pausedGroups = new HashSet<String>();
    lock();
    try {

      Set<JobKey> jobKeys = getJobKeys(matcher);

      for (JobKey jobKey : jobKeys) {
        for (OperableTrigger trigger : getTriggersForJob(jobKey)) {
          pauseTrigger(trigger.getKey());
        }
        pausedGroups.add(jobKey.getGroup());
      }
      // make sure to account for an exact group match for a group that doesn't yet exist
      StringMatcher.StringOperatorName operator = matcher.getCompareWithOperator();
      if (operator.equals(StringOperatorName.EQUALS)) {
        pausedJobGroups.add(matcher.getCompareToValue());
        pausedGroups.add(matcher.getCompareToValue());
      }
    } finally {
      unlock();
    }
    return pausedGroups;
  }

  /**
   * <p>
   * Resume (un-pause) the <code>{@link Trigger}</code> with the given name.
   * </p>
   * <p>
   * If the <code>Trigger</code> missed one or more fire-times, then the <code>Trigger</code>'s misfire instruction will
   * be applied.
   * </p>
   */
  public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    lock();
    try {
      TriggerWrapper tw = triggersByFQN.get(triggerKey);

      // does the trigger exist?
      if (tw == null) { return; }

      // if the trigger is not paused resuming it does not make sense...
      if (tw.getState() != TriggerState.PAUSED && tw.getState() != TriggerState.PAUSED_BLOCKED) { return; }

      if (blockedJobs.contains(tw.getJobKey())) {
        tw.setState(TriggerState.BLOCKED, terracottaClientId, triggersByFQN);
      } else {
        tw.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);
      }

      applyMisfire(tw);

      if (tw.getState() == TriggerState.WAITING) {
        timeTriggers.add(tw);
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Resume (un-pause) all of the <code>{@link Trigger}s</code> in the given group.
   * </p>
   * <p>
   * If any <code>Trigger</code> missed one or more fire-times, then the <code>Trigger</code>'s misfire instruction will
   * be applied.
   * </p>
   */
  public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    Collection<String> groups = new HashSet<String>();
    lock();
    try {
      Set<TriggerKey> triggerKeys = getTriggerKeys(matcher);

      for (TriggerKey triggerKey : triggerKeys) {
        TriggerWrapper tw = triggersByFQN.get(triggerKey);
        if (tw != null) {
          String jobGroup = tw.getJobKey().getGroup();

          if (pausedJobGroups.contains(jobGroup)) {
            continue;
          }
          groups.add(triggerKey.getGroup());
        }
        resumeTrigger(triggerKey);
      }
      pausedTriggerGroups.removeAll(groups);
    } finally {
      unlock();
    }
    return groups;
  }

  /**
   * <p>
   * Resume (un-pause) the <code>{@link org.quartz.JobDetail}</code> with the given name.
   * </p>
   * <p>
   * If any of the <code>Job</code>'s<code>Trigger</code> s missed one or more fire-times, then the <code>Trigger</code>
   * 's misfire instruction will be applied.
   * </p>
   */
  public void resumeJob(JobKey jobKey) throws JobPersistenceException {

    lock();
    try {
      for (OperableTrigger trigger : getTriggersForJob(jobKey)) {
        resumeTrigger(trigger.getKey());
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Resume (un-pause) all of the <code>{@link org.quartz.JobDetail}s</code> in the given group.
   * </p>
   * <p>
   * If any of the <code>Job</code> s had <code>Trigger</code> s that missed one or more fire-times, then the
   * <code>Trigger</code>'s misfire instruction will be applied.
   * </p>
   */
  public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    Collection<String> groups = new HashSet<String>();
    lock();
    try {
      Set<JobKey> jobKeys = getJobKeys(matcher);

      for (JobKey jobKey : jobKeys) {
        if (groups.add(jobKey.getGroup())) {
          pausedJobGroups.remove(jobKey.getGroup());
        }
        for (OperableTrigger trigger : getTriggersForJob(jobKey)) {
          resumeTrigger(trigger.getKey());
        }
      }
    } finally {
      unlock();
    }
    return groups;
  }

  /**
   * <p>
   * Pause all triggers - equivalent of calling <code>pauseTriggerGroup(group)</code> on every group.
   * </p>
   * <p>
   * When <code>resumeAll()</code> is called (to un-pause), trigger misfire instructions WILL be applied.
   * </p>
   * 
   * @see #resumeAll()
   * @see #pauseTriggers(org.quartz.impl.matchers.GroupMatcher)
   */
  public void pauseAll() throws JobPersistenceException {

    lock();
    try {
      List<String> names = getTriggerGroupNames();

      for (String name : names) {
        pauseTriggers(GroupMatcher.triggerGroupEquals(name));
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Resume (un-pause) all triggers - equivalent of calling <code>resumeTriggerGroup(group)</code> on every group.
   * </p>
   * <p>
   * If any <code>Trigger</code> missed one or more fire-times, then the <code>Trigger</code>'s misfire instruction will
   * be applied.
   * </p>
   * 
   * @see #pauseAll()
   */
  public void resumeAll() throws JobPersistenceException {

    lock();
    try {
      pausedJobGroups.clear();
      List<String> names = getTriggerGroupNames();

      for (String name : names) {
        resumeTriggers(GroupMatcher.triggerGroupEquals(name));
      }
    } finally {
      unlock();
    }
  }

  boolean applyMisfire(TriggerWrapper tw) throws JobPersistenceException {
    long misfireTime = System.currentTimeMillis();
    if (getMisfireThreshold() > 0) {
      misfireTime -= getMisfireThreshold();
    }

    Date tnft = tw.getNextFireTime();
    if (tnft == null || tnft.getTime() > misfireTime
        || tw.getMisfireInstruction() == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) { return false; }

    Calendar cal = null;
    if (tw.getCalendarName() != null) {
      cal = retrieveCalendar(tw.getCalendarName());
    }

    signaler.notifyTriggerListenersMisfired(tw.getTriggerClone());

    tw.updateAfterMisfire(cal, triggersByFQN);

    if (tw.getNextFireTime() == null) {
      tw.setState(TriggerState.COMPLETE, terracottaClientId, triggersByFQN);
      signaler.notifySchedulerListenersFinalized(tw.getTriggerClone());
      timeTriggers.remove(tw);
    } else if (tnft.equals(tw.getNextFireTime())) { return false; }

    return true;
  }

  public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow)
      throws JobPersistenceException {

    lock();
    try {
      List<OperableTrigger> result = new ArrayList<OperableTrigger>();

      for (TriggerWrapper tw : getNextTriggerWrappers(timeTriggers, noLaterThan, maxCount, timeWindow)) {
        result.add(markAndCloneTrigger(tw));
      }
      return result;
    } finally {
      unlock();
    }
  }

  OperableTrigger markAndCloneTrigger(final TriggerWrapper tw) {
    tw.setState(TriggerState.ACQUIRED, terracottaClientId, triggersByFQN);

    String firedInstanceId = terracottaClientId + "-" + String.valueOf(ftrCtr++);
    tw.setFireInstanceId(firedInstanceId, triggersByFQN);

    return tw.getTriggerClone();
  }

  List<TriggerWrapper> getNextTriggerWrappers(final long noLaterThan, final int maxCount, final long timeWindow)
      throws JobPersistenceException {
    return getNextTriggerWrappers(timeTriggers, noLaterThan, maxCount, timeWindow);
  }

  List<TriggerWrapper> getNextTriggerWrappers(final TimeTriggerSet source, final long noLaterThan, final int maxCount,
                                              final long timeWindow) throws JobPersistenceException {

    List<TriggerWrapper> wrappers = new ArrayList<TriggerWrapper>();
    Set<JobKey> acquiredJobKeysForNoConcurrentExec = new HashSet<JobKey>();
    Set<TriggerWrapper> excludedTriggers = new HashSet<TriggerWrapper>();
    JobPersistenceException caughtJpe = null;
    long firstAcquiredTriggerFireTime = 0;

    try {
      while (true) {
        TriggerWrapper tw = null;

        try {
          TriggerKey triggerKey = source.removeFirst();
          if (triggerKey != null) {
            tw = triggersByFQN.get(triggerKey);
          }
          if (tw == null) break;
        } catch (java.util.NoSuchElementException nsee) {
          break;
        }

        if (tw.getNextFireTime() == null) {
          continue;
        }

        // it's possible that we've selected triggers way outside of the max fire ahead time for batches
        // (up to idleWaitTime + fireAheadTime) so we need to make sure not to include such triggers.
        // So we select from the first next trigger to fire up until the max fire ahead time after that...
        // which will perfectly honor the fireAheadTime window because the no firing will occur until
        // the first acquired trigger's fire time arrives.
        if (firstAcquiredTriggerFireTime > 0
            && tw.getNextFireTime().getTime() > (firstAcquiredTriggerFireTime + timeWindow)) {
          source.add(tw);
          break;
        }

        if (applyMisfire(tw)) {
          if (tw.getNextFireTime() != null) {
            source.add(tw);
          }
          continue;
        }

        if (tw.getNextFireTime().getTime() > noLaterThan + timeWindow) {
          source.add(tw);
          break;
        }
        if (tw.jobDisallowsConcurrence()) {
          if (acquiredJobKeysForNoConcurrentExec.contains(tw.getJobKey())) {
            excludedTriggers.add(tw);
            continue;
          }
          acquiredJobKeysForNoConcurrentExec.add(tw.getJobKey());
        }
        wrappers.add(tw);
        if (firstAcquiredTriggerFireTime == 0) firstAcquiredTriggerFireTime = tw.getNextFireTime().getTime();
        if (wrappers.size() == maxCount) {
          break;
        }
      }
    } catch (JobPersistenceException jpe) {
      caughtJpe = jpe; // hold the exception while we patch back up the collection ...
    }

    // If we did excluded triggers to prevent ACQUIRE state due to DisallowConcurrentExecution, we need to add them back
    // to store.
    if (excludedTriggers.size() > 0) {
      for (TriggerWrapper tw : excludedTriggers) {
        source.add(tw);
      }
    }

    // if we held and exception, now we need to put back all the TriggerWrappers that we may have removed from the
    // source set
    if (caughtJpe != null) {
      for (TriggerWrapper tw : wrappers) {
        source.add(tw);
      }
      // and now throw the exception...
      throw new JobPersistenceException("Exception encountered while trying to select triggers for firing.", caughtJpe);
    }

    return wrappers;
  }

  public void setTriggerRemovedFromCandidateFiringListHandler(final TriggerRemovedFromCandidateFiringListHandler triggerRemovedFromCandidateFiringListHandler) {
    this.triggerRemovedFromCandidateFiringListHandler = triggerRemovedFromCandidateFiringListHandler;
  }

  /**
   * <p>
   * Inform the <code>JobStore</code> that the scheduler no longer plans to fire the given <code>Trigger</code>, that it
   * had previously acquired (reserved).
   * </p>
   */
  public void releaseAcquiredTrigger(OperableTrigger trigger) throws JobPersistenceException {
    lock();
    try {
      TriggerWrapper tw = triggersByFQN.get(trigger.getKey());
      if (tw != null && tw.getState() == TriggerState.ACQUIRED) {
        tw.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);
        timeTriggers.add(tw);
      }
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Inform the <code>JobStore</code> that the scheduler is now firing the given <code>Trigger</code> (executing its
   * associated <code>Job</code>), that it had previously acquired (reserved).
   * </p>
   */
  public List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggersFired) throws JobPersistenceException {

    lock();
    try {
      List<TriggerFiredResult> results = new ArrayList<TriggerFiredResult>();

      for (OperableTrigger trigger : triggersFired) {
        TriggerWrapper tw = triggersByFQN.get(trigger.getKey());
        // was the trigger deleted since being acquired?
        if (tw == null) {
          results.add(new TriggerFiredResult((TriggerFiredBundle) null));
          continue;
        }
        // was the trigger completed, paused, blocked, etc. since being acquired?
        if (tw.getState() != TriggerState.ACQUIRED) {
          results.add(new TriggerFiredResult((TriggerFiredBundle) null));
          continue;
        }

        Calendar cal = null;
        if (tw.getCalendarName() != null) {
          cal = retrieveCalendar(tw.getCalendarName());
          if (cal == null) {
            results.add(new TriggerFiredResult((TriggerFiredBundle) null));
            continue;
          }
        }
        Date prevFireTime = trigger.getPreviousFireTime();
        // in case trigger was replaced between acquiring and firering
        timeTriggers.remove(tw);

        // call triggered on our copy, and the scheduler's copy
        tw.triggered(cal, triggersByFQN);
        trigger.triggered(cal); // calendar is already clone()'d so it is okay to pass out to trigger

        // tw.state = EXECUTING;
        tw.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);

        TriggerFiredBundle bndle = new TriggerFiredBundle(retrieveJob(trigger.getJobKey()), trigger, cal, false,
                                                          new Date(), trigger.getPreviousFireTime(), prevFireTime,
                                                          trigger.getNextFireTime());

        String fireInstanceId = trigger.getFireInstanceId();
        FiredTrigger prev = firedTriggers.put(fireInstanceId, new FiredTrigger(terracottaClientId, tw.getKey()));
        getLog().trace("Tracking " + trigger + " has fired on " + fireInstanceId);
        if (prev != null) {
          // this shouldn't happen
          throw new AssertionError("duplicate fireInstanceId detected (" + fireInstanceId + ") for " + trigger
                                   + ", previous is " + prev);
        }

        JobDetail job = bndle.getJobDetail();

        if (job.isConcurrentExectionDisallowed()) {
          List<TriggerWrapper> trigs = getTriggerWrappersForJob(job.getKey());
          for (TriggerWrapper ttw : trigs) {
            if (ttw.getState() == TriggerState.WAITING) {
              ttw.setState(TriggerState.BLOCKED, terracottaClientId, triggersByFQN);
            }
            if (ttw.getState() == TriggerState.PAUSED) {
              ttw.setState(TriggerState.PAUSED_BLOCKED, terracottaClientId, triggersByFQN);
            }
            timeTriggers.remove(ttw);
            if (triggerRemovedFromCandidateFiringListHandler != null) {
              triggerRemovedFromCandidateFiringListHandler.removeCandidateTrigger(ttw);
            }
          }
          blockedJobs.add(job.getKey());
        } else if (tw.getNextFireTime() != null) {
          timeTriggers.add(tw);
        }

        results.add(new TriggerFiredResult(bndle));
      }
      return results;
    } finally {
      unlock();
    }
  }

  /**
   * <p>
   * Inform the <code>JobStore</code> that the scheduler has completed the firing of the given <code>Trigger</code> (and
   * the execution its associated <code>Job</code>), and that the <code>{@link org.quartz.JobDataMap}</code> in the
   * given <code>JobDetail</code> should be updated if the <code>Job</code> is stateful.
   * </p>
   */
  public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail,
                                   CompletedExecutionInstruction triggerInstCode) throws JobPersistenceException {

    lock();
    try {
      String fireId = trigger.getFireInstanceId();
      FiredTrigger removed = firedTriggers.remove(fireId);
      if (removed == null) {
        getLog().warn("No fired trigger record found for " + trigger + " (" + fireId + ")");
      }

      JobKey jobKey = jobDetail.getKey();
      JobWrapper jw = jobsByFQN.get(jobKey);
      TriggerWrapper tw = triggersByFQN.get(trigger.getKey());

      // It's possible that the job is null if:
      // 1- it was deleted during execution
      // 2- RAMJobStore is being used only for volatile jobs / triggers
      // from the JDBC job store
      if (jw != null) {
        if (jw.isConcurrentExectionDisallowed()) {
          JobDataMap newData = jobDetail.getJobDataMap();
          if (newData != null) {
            newData = (JobDataMap) newData.clone();
            newData.clearDirtyFlag();
          }
          jw.setJobDataMap(newData, jobsByFQN);

          blockedJobs.remove(jw.getKey());
          List<TriggerWrapper> trigs = getTriggerWrappersForJob(jw.getKey());

          for (TriggerWrapper ttw : trigs) {
            if (ttw.getState() == TriggerState.BLOCKED) {
              ttw.setState(TriggerState.WAITING, terracottaClientId, triggersByFQN);
              timeTriggers.add(ttw);
            }
            if (ttw.getState() == TriggerState.PAUSED_BLOCKED) {
              ttw.setState(TriggerState.PAUSED, terracottaClientId, triggersByFQN);
            }
          }
          signaler.signalSchedulingChange(0L);
        }
      } else { // even if it was deleted, there may be cleanup to do
        blockedJobs.remove(jobKey);
      }

      // check for trigger deleted during execution...
      if (tw != null) {
        if (triggerInstCode == CompletedExecutionInstruction.DELETE_TRIGGER) {

          if (trigger.getNextFireTime() == null) {
            // double check for possible reschedule within job
            // execution, which would cancel the need to delete...
            if (tw.getNextFireTime() == null) {
              removeTrigger(trigger.getKey());
            }
          } else {
            removeTrigger(trigger.getKey());
            signaler.signalSchedulingChange(0L);
          }
        } else if (triggerInstCode == CompletedExecutionInstruction.SET_TRIGGER_COMPLETE) {
          tw.setState(TriggerState.COMPLETE, terracottaClientId, triggersByFQN);
          timeTriggers.remove(tw);
          signaler.signalSchedulingChange(0L);
        } else if (triggerInstCode == CompletedExecutionInstruction.SET_TRIGGER_ERROR) {
          getLog().info("Trigger " + trigger.getKey() + " set to ERROR state.");
          tw.setState(TriggerState.ERROR, terracottaClientId, triggersByFQN);
          signaler.signalSchedulingChange(0L);
        } else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR) {
          getLog().info("All triggers of Job " + trigger.getJobKey() + " set to ERROR state.");
          setAllTriggersOfJobToState(trigger.getJobKey(), TriggerState.ERROR);
          signaler.signalSchedulingChange(0L);
        } else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE) {
          setAllTriggersOfJobToState(trigger.getJobKey(), TriggerState.COMPLETE);
          signaler.signalSchedulingChange(0L);
        }
      }
    } finally {
      unlock();
    }
  }

  private void setAllTriggersOfJobToState(JobKey jobKey, TriggerState state) {
    List<TriggerWrapper> tws = getTriggerWrappersForJob(jobKey);

    for (TriggerWrapper tw : tws) {
      tw.setState(state, terracottaClientId, triggersByFQN);
      if (state != TriggerState.WAITING) {
        timeTriggers.remove(tw);
      }
    }
  }

  /**
   * @see org.quartz.spi.JobStore#getPausedTriggerGroups()
   */
  public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
    lock();
    try {
      Set<String> rv = new HashSet<String>(pausedTriggerGroups.size());
      for (String ptg : pausedTriggerGroups) {
        rv.add(ptg);
      }
      return rv;
    } finally {
      unlock();
    }
  }

  public void setInstanceId(String schedInstId) {
    //
  }

  public void setInstanceName(String schedName) {
    //
  }

  public void nodeLeft(ClusterEvent event) {
    final String nodeLeft = event.getNode().getId();

    try {
      lock();
    } catch (JobPersistenceException e) {
      getLog().info("Job store is already disabled, not processing nodeLeft() for " + nodeLeft);
      return;
    }

    try {

      List<TriggerWrapper> toEval = new ArrayList<TriggerWrapper>();

      for (TriggerKey triggerKey : triggersByFQN.keySet()) {
        TriggerWrapper tw = triggersByFQN.get(triggerKey);
        String clientId = tw.getLastTerracotaClientId();
        if (clientId != null && clientId.equals(nodeLeft)) {
          toEval.add(tw);
        }
      }

      for (TriggerWrapper tw : toEval) {
        evalOrphanedTrigger(tw, false);
      }

      for (Iterator<FiredTrigger> iter = firedTriggers.values().iterator(); iter.hasNext();) {
        FiredTrigger ft = iter.next();
        if (nodeLeft.equals(ft.getClientId())) {
          getLog().info("Found non-complete fired trigger: " + ft);
          iter.remove();

          TriggerWrapper tw = triggersByFQN.get(ft.getTriggerKey());
          if (tw == null) {
            getLog().error("no trigger found for executing trigger: " + ft.getTriggerKey());
            continue;
          }

          scheduleRecoveryIfNeeded(tw, ft.getFireTime());
        }
      }
    } finally {
      unlock();
    }

    // nudge the local scheduler. This is a lazy way to do it. This should perhaps be conditionally happening and
    // also passing a real next job time (as opposed to 0)
    signaler.signalSchedulingChange(0);
  }

  public long getEstimatedTimeToReleaseAndAcquireTrigger() {
    // right now this is a static (but configurable) value. It could be based on actual observation
    // of trigger acquire/release at runtime in the future though
    return this.estimatedTimeToReleaseAndAcquireTrigger;
  }

  public void setEstimatedTimeToReleaseAndAcquireTrigger(long estimate) {
    this.estimatedTimeToReleaseAndAcquireTrigger = estimate;
  }

  public void setThreadPoolSize(final int size) {
    //
  }

  public boolean isClustered() {
    // We throw an assertion here since this method should never be called directly on this instance.
    throw new AssertionError();
  }

  void injectTriggerWrapper(final TriggerWrapper triggerWrapper) {
    timeTriggers.add(triggerWrapper);
  }

  private static class ShutdownHook implements Runnable {
    private final DefaultClusteredJobStore store;

    ShutdownHook(DefaultClusteredJobStore store) {
      this.store = store;
    }

    public void run() {
      store.disable();
    }
  }

  private static class LocalLockState {
    private int     acquires = 0;
    private boolean disabled;

    synchronized void attemptAcquireBegin() throws JobPersistenceException {
      if (disabled) { throw new JobPersistenceException("org.terracotta.quartz.TerracottaJobStore is disabled"); }
      acquires++;
    }

    synchronized void release() {
      acquires--;
      notifyAll();
    }

    synchronized void disableLocking() throws InterruptedException {
      disabled = true;

      while (acquires > 0) {
        wait();
      }
    }
  }

  ClusterInfo getDsoCluster() {
    return dsoCluster;
  }

  interface TriggerRemovedFromCandidateFiringListHandler {
    public boolean removeCandidateTrigger(final TriggerWrapper ttw);
  }

  @Override
  public void onClusterEvent(ClusterEvent event, ClusterInfo clusterInfo) {
    switch (event.getType()) {
      case NODE_JOINED:
      case OPERATIONS_DISABLED:
      case OPERATIONS_ENABLED:
        break;
      case NODE_LEFT:
        nodeLeft(event);
        break;
    }
  }
}

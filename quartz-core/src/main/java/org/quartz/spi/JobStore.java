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

package org.quartz.spi;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * <p>
 * The interface to be implemented by classes that want to provide a <code>{@link org.quartz.Job}</code>
 * and <code>{@link org.quartz.Trigger}</code> storage mechanism for the
 * <code>{@link org.quartz.core.QuartzScheduler}</code>'s use.
 * </p>
 *
 * <p>
 * Storage of <code>Job</code> s and <code>Trigger</code> s should be keyed
 * on the combination of their name and group for uniqueness.
 * </p>
 *
 * @see org.quartz.core.QuartzScheduler
 * @see org.quartz.Trigger
 * @see org.quartz.Job
 * @see org.quartz.JobDetail
 * @see org.quartz.JobDataMap
 * @see org.quartz.Calendar
 *
 * @author James House
 * @author Eric Mueller
 */
public interface JobStore {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Interface.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Called by the QuartzScheduler before the <code>JobStore</code> is
     * used, in order to give the it a chance to initialize.
     *
     * @param loadHelper the class loader helper
     * @param signaler   the scheduler signaler
     * @throws SchedulerConfigException if any exception occurs while initializing this <code>JobStore</code>
     */
    void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) 
        throws SchedulerConfigException;

    /**
     * Called by the QuartzScheduler to inform the <code>JobStore</code> that
     * the scheduler has started.
     *
     * @throws SchedulerException if any error occurred starting the <code>Scheduler</code>
     */
    void schedulerStarted() throws SchedulerException;

    /**
     * Called by the QuartzScheduler to inform the <code>JobStore</code> that
     * the scheduler has been paused.
     */
    void schedulerPaused();

    /**
     * Called by the QuartzScheduler to inform the <code>JobStore</code> that
     * the scheduler has resumed after being paused.
     */
    void schedulerResumed();

    /**
     * Called by the QuartzScheduler to inform the <code>JobStore</code> that
     * it should free up all of it's resources because the scheduler is
     * shutting down.
     */
    void shutdown();

    /**
     * Does this <code>JobStore</code> persistently store its state in a database or elsewhere.
     *
     * @return boolean
     */
    boolean supportsPersistence();
    
    /**
     * How long (in milliseconds) the <code>JobStore</code> implementation 
     * estimates that it will take to release a trigger and acquire a new one.
     *
     * @return the number of milliseconds estimated
     */
    long getEstimatedTimeToReleaseAndAcquireTrigger();
    
    /**
     * Whether or not the <code>JobStore</code> implementation is clustered.
     *
     * @return <code>true</code> if this <code>JobStore</code> is clustered and <code>false</code> otherwise
     */
    boolean isClustered();

    /////////////////////////////////////////////////////////////////////////////
    //
    // Job & Trigger Storage methods
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Store the given <code>{@link org.quartz.JobDetail}</code> and <code>{@link org.quartz.Trigger}</code>.
     *
     * @param newJob
     *          The <code>JobDetail</code> to be stored.
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already
     *           exists.
     */
    void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) 
        throws ObjectAlreadyExistsException, JobPersistenceException;

    /**
     * Store the given <code>{@link org.quartz.JobDetail}</code>.
     *
     * @param newJob
     *          The <code>JobDetail</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Job</code> existing in the
     *          <code>JobStore</code> with the same name and group should be
     *          over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already
     *           exists, and replaceExisting is set to false.
     */
    void storeJob(JobDetail newJob, boolean replaceExisting) 
        throws ObjectAlreadyExistsException, JobPersistenceException;

    public void storeJobsAndTriggers(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace)
        throws ObjectAlreadyExistsException, JobPersistenceException;

    /**
     * Remove (delete) the <code>{@link org.quartz.Job}</code> with the given
     * key, and any <code>{@link org.quartz.Trigger}</code> s that reference
     * it.
     *
     * <p>
     * If removal of the <code>Job</code> results in an empty group, the
     * group should be removed from the <code>JobStore</code>'s list of
     * known group names.
     * </p>
     *
     * @param jobKey  the handled of the <code>Job</code>to remove from this store
     * @return <code>true</code> if a <code>Job</code> with the given name and
     *         group was found and removed from the store.
     *
     * @throws JobPersistenceException
     *         if there was any error removing the <code>Job</code> from a persistence <code>JobStore</code>
     */
    boolean removeJob(JobKey jobKey) 
        throws JobPersistenceException;

    /**
     * Remove (delete) each <code>Job</code> referenced by the specified list of <code>JobKey</code>.
     *
     * @param jobKeys the list of <code>JobKey</code>'s to remove
     * @return <code>true</code> if each of the referenced <code>Job</code>'s is successfully removed
     *         <code>false</code> otherwise
     * @throws JobPersistenceException
     *         if there was any error removing the <code>Job</code>'s from a persistence <code>JobStore</code>
     */
    public boolean removeJobs(List<JobKey> jobKeys) 
        throws JobPersistenceException;
    
    /**
     * Retrieve the <code>{@link org.quartz.JobDetail}</code> for the given
     * <code>{@link org.quartz.Job}</code>.
     *
     * @param jobKey returns the <code>JobDetail</code> for the specify <code>JobKey</code>
     * @return The desired <code>JobDetail</code>, or null if there is no match.
     * @throws JobPersistenceException
     *         if there was any error retrieving the specified <code>Job</code> from a persistence <code>JobStore</code>
     */
    JobDetail retrieveJob(JobKey jobKey) 
        throws JobPersistenceException;

    /**
     * Store the given <code>{@link org.quartz.Trigger}</code>.
     *
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Trigger</code> existing in
     *          the <code>JobStore</code> with the same name and group should
     *          be over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Trigger</code> with the same name/group already
     *           exists, and replaceExisting is set to false.
     *
     * @see #pauseTriggers(org.quartz.impl.matchers.GroupMatcher)
     */
    void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) 
        throws ObjectAlreadyExistsException, JobPersistenceException;

    /**
     * Remove (delete) the <code>{@link org.quartz.Trigger}</code> with the
     * given key.
     *
     * <p>
     * If removal of the <code>Trigger</code> results in an empty group, the
     * group should be removed from the <code>JobStore</code>'s list of
     * known group names.
     * </p>
     *
     * <p>
     * If removal of the <code>Trigger</code> results in an 'orphaned' <code>Job</code>
     * that is not 'durable', then the <code>Job</code> should be deleted
     * also.
     * </p>
     *
     * @param triggerKey the <code>TriggerKey</code> to remove
     * @return <code>true</code> if a <code>Trigger</code> with the given
     *         name and group was found and removed from the store.
     * @throws JobPersistenceException
     *         if there was any error removing the specified <code>Trigger</code> from a persistence <code>JobStore</code>
     */
    boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException;

    public boolean removeTriggers(List<TriggerKey> triggerKeys)
        throws JobPersistenceException;

    /**
     * Remove (delete) the <code>{@link org.quartz.Trigger}</code> with the
     * given key, and store the new given one - which must be associated
     * with the same job.
     *
     * @param triggerKey
     *          The <code>TriggerKey</code> to replace.
     * @param newTrigger
     *          The new <code>Trigger</code> to be stored.
     * @return <code>true</code> if a <code>Trigger</code> with the given
     *         name and group was found and removed from the store.
     * @throws JobPersistenceException
     *         if there was any error replacing the specified <code>Trigger</code> from a persistence <code>JobStore</code>
     */
    boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) 
        throws JobPersistenceException;

    /**
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     *
     * @param triggerKey
     *         The <code>TriggerKey</code> to retrieve.
     * @return The desired <code>Trigger</code>, or null if there is no
     *         match.
     * @throws JobPersistenceException
     *         if there was any error retrieving the specified <code>Trigger</code> from a persistence <code>JobStore</code>
     */
    OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException;

    /**
     * Determine whether a {@link Job} with the given identifier already 
     * exists within the scheduler.
     * 
     * @param jobKey the identifier to check for
     * @return true if a Job exists with the given identifier
     * @throws JobPersistenceException
     *         if there was any error checking the existence of the specified <code>Job</code> from a
     *         persistence <code>JobStore</code>
     */
    boolean checkExists(JobKey jobKey) throws JobPersistenceException; 
   
    /**
     * Determine whether a {@link Trigger} with the given identifier already 
     * exists within the scheduler.
     * 
     * @param triggerKey the identifier to check for
     * @return true if a Trigger exists with the given identifier
     * @throws JobPersistenceException
     *         if there was any error checking the existence of the specified <code>Trigger</code> from a
     *         persistence <code>JobStore</code>
     */
    boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException;
 
    /**
     * Clear (delete) all scheduling data - all {@link Job}s, {@link Trigger}s
     * {@link Calendar}s.
     * 
     * @throws JobPersistenceException
     *         if there was any error clearing the <code>Scheduler</code> from a persistence <code>JobStore</code>
     */
    void clearAllSchedulingData() throws JobPersistenceException;
    
    /**
     * Store the given <code>{@link org.quartz.Calendar}</code>.
     *
     * @param name
     *          The name of the <code>Calendar</code> to be stored.
     * @param calendar
     *          The <code>Calendar</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Calendar</code> existing
     *          in the <code>JobStore</code> with the same name and group
     *          should be over-written.
     * @param updateTriggers
     *          If <code>true</code>, any <code>Trigger</code>s existing
     *          in the <code>JobStore</code> that reference an existing
     *          Calendar with the same name with have their next fire time
     *          re-computed with the new <code>Calendar</code>.
     * @throws JobPersistenceException
     *           if a <code>Calendar</code> with the same name already
     *           exists, and replaceExisting is set to false.
     */
    void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers)
        throws JobPersistenceException;

    /**
     * Remove (delete) the <code>{@link org.quartz.Calendar}</code> with the
     * given name.
     *
     * <p>
     * If removal of the <code>Calendar</code> would result in
     * <code>Trigger</code>s pointing to non-existent calendars, then a
     * <code>JobPersistenceException</code> will be thrown.</p>
     *
     * @param calName The name of the <code>Calendar</code> to be removed.
     * @return <code>true</code> if a <code>Calendar</code> with the given name
     * was found and removed from the store.
     * @throws JobPersistenceException
     *         if there was any error removing the specified <code>Calendar</code> from a persistence <code>JobStore</code>
     */
    boolean removeCalendar(String calName)
        throws JobPersistenceException;

    /**
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     *
     * @param calName
     *          The name of the <code>Calendar</code> to be retrieved.
     * @return The desired <code>Calendar</code>, or null if there is no
     *         match.
     * @throws JobPersistenceException
     *         if there was any error retrieving the specified <code>Calendar</code> from a persistence <code>JobStore</code>
     */
    Calendar retrieveCalendar(String calName)
        throws JobPersistenceException;

    /////////////////////////////////////////////////////////////////////////////
    //
    // Informational methods
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Get the number of <code>{@link org.quartz.Job}</code> s that are
     * stored in the <code>JobsStore</code>.
     *
     * @return The number of jobs
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Job</code>'s from a persistence <code>JobStore</code>
     */
    int getNumberOfJobs()
        throws JobPersistenceException;

    /**
     * Get the number of <code>{@link org.quartz.Trigger}</code>'s that are
     * stored in the <code>JobsStore</code>.
     *
     * @return The number of triggers
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Trigger</code>'s from a persistence <code>JobStore</code>
     */
    int getNumberOfTriggers()
        throws JobPersistenceException;

    /**
     * Get the number of <code>{@link org.quartz.Calendar}</code> s that are
     * stored in the <code>JobsStore</code>.
     *
     * @return The number of calendars
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Calendar</code>'s from a persistence <code>JobStore</code>
     */
    int getNumberOfCalendars()
        throws JobPersistenceException;

    /**
     * Get the keys of all of the <code>{@link org.quartz.Job}</code> s that
     * have the given group name.
     *
     * <p>
     * If there are no jobs in the given group name, the result should be 
     * an empty collection (not <code>null</code>).
     * </p>
     *
     * @param matcher the specified <code>Job</code>'s
     * @return The set of job keys
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Jobs</code>'s specified by the
     *         <code>GroupMatcher</code>from a persistence <code>JobStore</code>
     */
    Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher)
        throws JobPersistenceException;

    /**
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code> s
     * that have the given group name.
     *
     * <p>
     * If there are no triggers in the given group name, the result should be a
     * zero-length array (not <code>null</code>).
     * </p>
     *
     * @param matcher the specified <code>Trigger</code>'s
     * @return The set of trigger keys
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Trigger</code>'s specified by the
     *         <code>GroupMatcher</code>from a persistence <code>JobStore</code>
     */
    Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher)
        throws JobPersistenceException;

    /**
     * Get the names of all of the <code>{@link org.quartz.Job}</code>
     * groups.
     *
     * <p>
     * If there are no known group names, the result should be a zero-length
     * array (not <code>null</code>).
     * </p>
     *
     * @return The list of job-group names
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Job</code> group names
     *         from a persistence <code>JobStore</code>
     */
    List<String> getJobGroupNames()
        throws JobPersistenceException;

    /**
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code>
     * groups.
     *
     * <p>
     * If there are no known group names, the result should be a zero-length
     * array (not <code>null</code>).
     * </p>
     *
     * @return The list of trigger-group names
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Trigger</code> group names
     *         from a persistence <code>JobStore</code>
     */
    List<String> getTriggerGroupNames()
        throws JobPersistenceException;

    /**
     * Get the names of all of the <code>{@link org.quartz.Calendar}</code> s
     * in the <code>JobStore</code>.
     *
     * <p>
     * If there are no Calendars in the given group name, the result should be
     * a zero-length array (not <code>null</code>).
     * </p>
     *
     * @return The list of calendar names
     * @throws JobPersistenceException
     *         if there was any error returning the number of <code>Calendar</code> names
     *         from a persistence <code>JobStore</code>
     */
    List<String> getCalendarNames()
        throws JobPersistenceException;

    /**
     * Get all of the Triggers that are associated to the given Job.
     *
     * <p>
     * If there are no matches, a zero-length array should be returned.
     * </p>
     *
     * @param jobKey the <code>JobKey</code> specifying the triggers to return
     * @return List of triggers for job key
     * @throws JobPersistenceException
     *         if there was any error returning the list of <code>Triggers</code>'s for the specified <code>Job</code>
     *         from a persistence <code>JobStore</code>
     */
    List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException;

    /**
     * Get the current state of the identified <code>{@link Trigger}</code>.
     *
     * @param triggerKey the specified <code>TriggerKey</code>
     * @return The trigger state
     * @throws JobPersistenceException
     *         if there was any error returning the <code>TriggerState</code> for the specified <code>Trigger</code>
     *         from a persistence <code>JobStore</code>
     *
     * @see Trigger.TriggerState
     */
    TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException;

    /**
     * Reset the current state of the identified <code>{@link Trigger}</code>
     * from {@link TriggerState#ERROR} to {@link TriggerState#NORMAL} or
     * {@link TriggerState#PAUSED} as appropriate.
     *
     * <p>Only affects triggers that are in ERROR state - if identified trigger is not
     * in that state then the result is a no-op.</p>
     *
     * <p>The result will be the trigger returning to the normal, waiting to
     * be fired state, unless the trigger's group has been paused, in which
     * case it will go into the PAUSED state.</p>
     *
     * @param triggerKey the <code>TriggerKey</code> to reset
     * @throws JobPersistenceException
     *         if there was any error resetting the specified <code>Trigger</code> from a persistence <code>JobStore</code>
     */
    void resetTriggerFromErrorState(TriggerKey triggerKey) throws JobPersistenceException;


    /////////////////////////////////////////////////////////////////////////////
    //
    // Trigger State manipulation methods
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Pause the <code>{@link org.quartz.Trigger}</code> with the given key.
     *
     * @param triggerKey the <code>TriggerKey</code> to pause
     * @throws JobPersistenceException
     *         if there was any error pausing the specified <code>Trigger</code> from a persistence <code>JobStore</code>
     *
     * @see #resumeTrigger(TriggerKey)
     */
    void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException;

    /**
     * Pause all of the <code>{@link org.quartz.Trigger}s</code> in the
     * given group.
     *
     * <p>
     * The JobStore should "remember" that the group is paused, and impose the
     * pause on any new triggers that are added to the group while the group is
     * paused.
     * </p>
     *
     * @param matcher the <code>GroupMatcher</code> specifying the tiggers to pause
     * @return The set of paused triggers matching matcher
     * @throws JobPersistenceException
     *         if there was any error pausing the specified <code>Trigger</code>'s from a persistence <code>JobStore</code>
     *
     * @see #resumeTriggers(GroupMatcher)
     */
    Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException;

    /**
     * Pause the <code>{@link org.quartz.Job}</code> with the given name - by
     * pausing all of its current <code>Trigger</code>s.
     *
     * @param jobKey the <code>JobKey</code> specifying the <code>JOb</code> to pause
     * @throws JobPersistenceException
     *         if there was any error pausing the specified <code>Job</code> from a persistence <code>JobStore</code>
     *
     * @see #resumeJob(JobKey)
     */
    void pauseJob(JobKey jobKey) throws JobPersistenceException;

    /**
     * Pause all of the <code>{@link org.quartz.Job}s</code> in the given
     * group - by pausing all of their <code>Trigger</code>s.
     *
     * <p>
     * The JobStore should "remember" that the group is paused, and impose the
     * pause on any new jobs that are added to the group while the group is
     * paused.
     * </p>
     *
     * @param groupMatcher the <code>GroupMatcher</code> specifying the <code>Job</code>'s to pause
     * @return The set of paused jobs matching matcher
     * @throws JobPersistenceException
     *         if there was any error pausing the specified <code>Job</code>'s from a persistence <code>JobStore</code>
     *
     * @see #resumeJobs(GroupMatcher)
     */
    Collection<String> pauseJobs(GroupMatcher<JobKey> groupMatcher)
        throws JobPersistenceException;

    /**
     * Resume (un-pause) the <code>{@link org.quartz.Trigger}</code> with the
     * given key.
     *
     * <p>
     * If the <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     * @param triggerKey the <code>TriggerKey</code> specifying the <code>Trigger</code> to resume
     * @throws JobPersistenceException
     *         if there was any error resuming the specified <code>Trigger</code> from a persistence <code>JobStore</code>
     *
     * @see #pauseTrigger(TriggerKey)
     */
    void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException;

    /**
     * Resume (un-pause) all of the <code>{@link org.quartz.Trigger}s</code>
     * in the given group.
     *
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     * @param matcher the <code>GroupMatcher</code> specifying the <code>Trigger</code>'s to resume
     * @throws JobPersistenceException
     *         if there was any error resuming the specified <code>Trigger</code>'s from a persistence <code>JobStore</code>
     * @return the set of resumed <code>Trigger</code> names
     * @see #pauseTriggers(GroupMatcher)
     */
    Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher)
        throws JobPersistenceException;

    /**
     * Get the set of paused trigger groups.
     *
     * @return The set of paused trigger groups
     * @throws JobPersistenceException
     *         if there was any error returning the set of paused <code>Trigger</code>'s from a
     *         persistence <code>JobStore</code>
     */
    Set<String> getPausedTriggerGroups()
        throws JobPersistenceException;

    /**
     * Resume (un-pause) the <code>{@link org.quartz.Job}</code> with the
     * given key.
     *
     * <p>
     * If any of the <code>Job</code>'s<code>Trigger</code> s missed one
     * or more fire-times, then the <code>Trigger</code>'s misfire
     * instruction will be applied.
     * </p>
     *
     * @param jobKey the <code>JobKey</code> specifying the <code>Job</code> to resume
     * @throws JobPersistenceException
     *         if there was any error resuming the specified <code>Job</code> from a
     *         persistence <code>JobStore</code>
     *
     * @see #pauseJob(JobKey)
     */
    void resumeJob(JobKey jobKey) throws JobPersistenceException;

    /**
     * Resume (un-pause) all of the <code>{@link org.quartz.Job}s</code> in
     * the given group.
     *
     * <p>
     * If any of the <code>Job</code> s had <code>Trigger</code> s that
     * missed one or more fire-times, then the <code>Trigger</code>'s
     * misfire instruction will be applied.
     * </p>
     *
     * @param matcher the <code>GroupMatcher</code> specifying the <code>Job</code>'s to resume
     * @throws JobPersistenceException
     *         if there was any error resuming the specified <code>Job</code>'s from a
     *         persistence <code>JobStore</code>
     * @return the set of resumed <code>Job</code> names
     * @see #pauseJobs(GroupMatcher)
     */
    Collection<String> resumeJobs(GroupMatcher<JobKey> matcher)
        throws JobPersistenceException;

    /**
     * Pause all triggers - equivalent of calling <code>pauseTriggerGroup(group)</code>
     * on every group.
     *
     * <p>
     * When <code>resumeAll()</code> is called (to un-pause), trigger misfire
     * instructions WILL be applied.
     * </p>
     *
     * @throws JobPersistenceException
     *         if there was any error pausing all the <code>Job</code>'s from a
     *         persistence <code>JobStore</code>
     *
     * @see #resumeAll()
     * @see #pauseTriggers(GroupMatcher)
     */
    void pauseAll() throws JobPersistenceException;

    /**
     * Resume (un-pause) all triggers - equivalent of calling <code>resumeTriggerGroup(group)</code>
     * on every group.
     *
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     * @throws JobPersistenceException
     *         if there was any error resuming all the <code>Job</code>'s from a
     *         persistence <code>JobStore</code>
     *
     * @see #pauseAll()
     */
    void resumeAll()
        throws JobPersistenceException;

    /////////////////////////////////////////////////////////////////////////////
    //
    // Trigger-Firing methods
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Get a handle to the next trigger to be fired, and mark it as 'reserved'
     * by the calling scheduler.
     *
     * @param noLaterThan If &gt; 0, the JobStore should only return a Trigger
     * that will fire no later than the time represented in this value as
     * milliseconds.
     * @param maxCount the maximum number of triggers to acquire
     * @param timeWindow the number of milliseconds in the future to consider
     * @return the list of acquired <code>Trigger</code>'s
     * @throws JobPersistenceException
     *         if there was any error acquiring the specified <code>Trigger</code>'s from a
     *         persistence <code>JobStore</code>
     *
     * @see #releaseAcquiredTrigger(OperableTrigger)
     */
    List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow)
        throws JobPersistenceException;

    /**
     * Inform the <code>JobStore</code> that the scheduler no longer plans to
     * fire the given <code>Trigger</code>, that it had previously acquired
     * (reserved).
     *
     * @param trigger the <code>Trigger</code> to no longer fire
     */
    void releaseAcquiredTrigger(OperableTrigger trigger);

    /**
     * Inform the <code>JobStore</code> that the scheduler is now firing the
     * given <code>Trigger</code> (executing its associated <code>Job</code>),
     * that it had previously acquired (reserved).
     *
     * @param triggers the list of <code>Trigger</code>'s that where fired
     * @return may return null if all the triggers or their calendars no longer exist, or
     *         if the trigger was not successfully put into the 'executing'
     *         state.  Preference is to return an empty list if none of the triggers
     *         could be fired.
     * @throws JobPersistenceException
     *         if there was any error notifying the firing of the specified <code>Trigger</code>'s from a
     *         persistence <code>JobStore</code>
     */
    List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggers) throws JobPersistenceException;

    /**
     * Inform the <code>JobStore</code> that the scheduler has completed the
     * firing of the given <code>Trigger</code> (and the execution of its
     * associated <code>Job</code> completed, threw an exception, or was vetoed),
     * and that the <code>{@link org.quartz.JobDataMap}</code>
     * in the given <code>JobDetail</code> should be updated if the <code>Job</code>
     * is stateful.
     *
     * @param trigger the <code>Trigger</code> that was completed
     * @param jobDetail the <code>JobDetail</code> of the <code>Job</code> that was completed
     * @param triggerInstCode the <code>CompletedExecutionInstruction</code> of the <code>Job</code> that was completed
     */
    void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail, CompletedExecutionInstruction triggerInstCode);

    /**
     * Inform the <code>JobStore</code> of the Scheduler instance's Id,
     * prior to initialize being invoked.
     *
     * @param schedInstId the identifier for this <code>JobStore</code>
     * @since 1.7
     */
    void setInstanceId(String schedInstId);

    /**
     * Inform the <code>JobStore</code> of the Scheduler instance's name,
     * prior to initialize being invoked.
     *
     * @param schedName the name of this <code>JobStore</code>
     * @since 1.7
     */
    void setInstanceName(String schedName);

    /**
     * Tells the JobStore the pool size used to execute jobs
     * @param poolSize amount of threads allocated for job execution
     * @since 2.0
     */
    void setThreadPoolSize(int poolSize);

    /**
     * Get the amount of time (in ms) to wait when accessing this job store
     * repeatedly fails.
     *
     * Called by the executor thread(s) when calls to
     * {@link #acquireNextTriggers} fail more than once in succession, and the
     * thread thus wants to wait a bit before trying again, to not consume
     * 100% CPU, write huge amounts of errors into logs, etc. in cases like
     * the DB being offline/restarting.
     *
     * The delay returned by implementations should be between 20 and
     * 600000 milliseconds.
     *
     * @param failureCount the number of successive failures seen so far
     * @return the time (in milliseconds) to wait before trying again
     */
    long getAcquireRetryDelay(int failureCount);
}

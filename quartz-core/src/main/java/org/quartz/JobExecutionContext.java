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

package org.quartz;

import java.util.Date;

/**
 * A context bundle containing handles to various environment information, that
 * is given to a <code>{@link org.quartz.JobDetail}</code> instance as it is
 * executed, and to a <code>{@link Trigger}</code> instance after the
 * execution completes.
 * 
 * <p>
 * The <code>JobDataMap</code> found on this object (via the 
 * <code>getMergedJobDataMap()</code> method) serves as a convenience -
 * it is a merge of the <code>JobDataMap</code> found on the 
 * <code>JobDetail</code> and the one found on the <code>Trigger</code>, with 
 * the value in the latter overriding any same-named values in the former.
 * <i>It is thus considered a 'best practice' that the execute code of a Job
 * retrieve data from the JobDataMap found on this object</i>  NOTE: Do not
 * expect value 'set' into this JobDataMap to somehow be set back onto a
 * job's own JobDataMap  - even if it has the
 * <code>@PersistJobDataAfterExecution</code> annotation.
 * </p>
 * 
 * <p>
 * <code>JobExecutionContext</code> s are also returned from the 
 * <code>Scheduler.getCurrentlyExecutingJobs()</code>
 * method. These are the same instances as those passed into the jobs that are
 * currently executing within the scheduler. The exception to this is when your
 * application is using Quartz remotely (i.e. via RMI) - in which case you get
 * a clone of the <code>JobExecutionContext</code>s, and their references to
 * the <code>Scheduler</code> and <code>Job</code> instances have been lost (a
 * clone of the <code>JobDetail</code> is still available - just not a handle
 * to the job instance that is running).
 * </p>
 * 
 * @see #getScheduler()
 * @see #getMergedJobDataMap()
 * @see #getJobDetail()
 * 
 * @see Job
 * @see Trigger
 * @see JobDataMap
 * 
 * @author James House
 */
public interface JobExecutionContext {

    /**
     * <p>
     * Get a handle to the <code>Scheduler</code> instance that fired the
     * <code>Job</code>.
     * </p>
     *
     * @return the <code>Scheduler</code> instance that fired the <code>Job</code>
     */
    public Scheduler getScheduler();

    /**
     * <p>
     * Get a handle to the <code>Trigger</code> instance that fired the
     * <code>Job</code>.
     * </p>
     *
     * @return the <code>Trigger</code> instance that fired the <code>Job</code>
     */
    public Trigger getTrigger();

    /**
     * <p>
     * Get a handle to the <code>Calendar</code> referenced by the <code>Trigger</code>
     * instance that fired the <code>Job</code>.
     * </p>
     *
     * @return the <code>Calendar</code> associated with the fired <code>Job</code>
     */
    public Calendar getCalendar();

    /**
     * <p>
     * If the <code>Job</code> is being re-executed because of a 'recovery'
     * situation, this method will return <code>true</code>.
     * </p>
     *
     * @return if the job is recovering from 'fail-over' or 'recovery'
     */
    public boolean isRecovering();

    /**
     * Return the {@code TriggerKey} of the originally scheduled and now recovering job.
     * <p>
     * When recovering a previously failed job execution this method returns the identity
     * of the originally firing trigger.  This recovering job will have been scheduled for
     * the same firing time as the original job, and so is available via the
     * {@link #getScheduledFireTime()} method.  The original firing time of the job can be
     * accessed via the {@link Scheduler#FAILED_JOB_ORIGINAL_TRIGGER_FIRETIME_IN_MILLISECONDS}
     * element of this job's {@code JobDataMap}.
     * 
     * @return the recovering trigger details
     * @throws IllegalStateException if this is not a recovering job.
     */
    public TriggerKey getRecoveringTriggerKey() throws IllegalStateException;

    public int getRefireCount();

    /**
     * <p>
     * Get the convenience <code>JobDataMap</code> of this execution context.
     * </p>
     * 
     * <p>
     * The <code>JobDataMap</code> found on this object serves as a convenience -
     * it is a merge of the <code>JobDataMap</code> found on the 
     * <code>JobDetail</code> and the one found on the <code>Trigger</code>, with 
     * the value in the latter overriding any same-named values in the former.
     * <i>It is thus considered a 'best practice' that the execute code of a Job
     * retrieve data from the JobDataMap found on this object.</i>
     * </p>
     * 
     * <p>NOTE: Do not expect value 'set' into this JobDataMap to somehow be set
     * or persisted back onto a job's own JobDataMap - even if it has the
     * <code>@PersistJobDataAfterExecution</code> annotation.
     * </p>
     * 
     * <p>
     * Attempts to change the contents of this map typically result in an 
     * <code>IllegalStateException</code>.
     * </p>
     *
     * @return the effective data map of the executed <code>Job</code>
     */
    public JobDataMap getMergedJobDataMap();

    /**
     * <p>
     * Get the <code>JobDetail</code> associated with the <code>Job</code>.
     * </p>
     *
     * @return the <code>JobDetail</code> for the executed <code>Job</code>
     */
    public JobDetail getJobDetail();

    /**
     * <p>
     * Get the instance of the <code>Job</code> that was created for this
     * execution.
     * </p>
     * 
     * <p>
     * Note: The Job instance is not available through remote scheduler
     * interfaces.
     * </p>
     *
     * @return the executed <code>Job</code>
     */
    public Job getJobInstance();

    /**
     * The actual time the trigger fired. For instance the scheduled time may
     * have been 10:00:00 but the actual fire time may have been 10:00:03 if
     * the scheduler was too busy.
     * 
     * @return Returns the fireTime.
     * @see #getScheduledFireTime()
     */
    public Date getFireTime();

    /**
     * The scheduled time the trigger fired for. For instance the scheduled
     * time may have been 10:00:00 but the actual fire time may have been
     * 10:00:03 if the scheduler was too busy.
     * 
     * @return Returns the scheduledFireTime.
     * @see #getFireTime()
     */
    public Date getScheduledFireTime();

    public Date getPreviousFireTime();

    public Date getNextFireTime();

    /**
     * Get the unique Id that identifies this particular firing instance of the
     * trigger that triggered this job execution.  It is unique to this 
     * JobExecutionContext instance as well.
     * 
     * @return the unique fire instance id
     * @see Scheduler#interrupt(String)
     */
    public String getFireInstanceId();
    
    /**
     * Returns the result (if any) that the <code>Job</code> set before its 
     * execution completed (the type of object set as the result is entirely up 
     * to the particular job).
     * 
     * <p>
     * The result itself is meaningless to Quartz, but may be informative
     * to <code>{@link JobListener}s</code> or 
     * <code>{@link TriggerListener}s</code> that are watching the job's 
     * execution.
     * </p> 
     * 
     * @return Returns the result.
     */
    public Object getResult();

    /**
     * Set the result (if any) of the <code>Job</code>'s execution (the type of 
     * object set as the result is entirely up to the particular job).
     * 
     * <p>
     * The result itself is meaningless to Quartz, but may be informative
     * to <code>{@link JobListener}s</code> or 
     * <code>{@link TriggerListener}s</code> that are watching the job's 
     * execution.
     * </p>
     *
     * @param result the user-specified, opaque <code>Job</code> execution result
     */
    public void setResult(Object result);

    /**
     * The amount of time the job ran for (in milliseconds).  The returned 
     * value will be -1 until the job has actually completed (or thrown an 
     * exception), and is therefore generally only useful to 
     * <code>JobListener</code>s and <code>TriggerListener</code>s.
     * 
     * @return Returns the jobRunTime.
     */
    public long getJobRunTime();

    /**
     * Put the specified value into the context's data map with the given key.
     * Possibly useful for sharing data between listeners and jobs.
     *
     * <p>NOTE: this data is volatile - it is lost after the job execution
     * completes, and all TriggerListeners and JobListeners have been 
     * notified.</p> 
     *  
     * @param key the key for the associated value
     * @param value the value to store
     */
    public void put(Object key, Object value);

    /**
     * Get the value with the given key from the context's data map.
     * 
     * @param key the key for the desired value
     * @return the desired value
     */
    public Object get(Object key);

}
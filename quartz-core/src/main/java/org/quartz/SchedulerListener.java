
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

/**
 * The interface to be implemented by classes that want to be informed of major
 * <code>{@link Scheduler}</code> events.
 * 
 * @see Scheduler
 * @see JobListener
 * @see TriggerListener
 * 
 * @author James House
 */
public interface SchedulerListener {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * is scheduled.
     * </p>
     *
     * @param trigger the trigger associated with the scheduled job
     */
    void jobScheduled(Trigger trigger);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * is unscheduled.
     * </p>
     *
     * @param triggerKey a reference to the trigger associated with the unscheduled job
     *
     * @see SchedulerListener#schedulingDataCleared()
     */
    void jobUnscheduled(TriggerKey triggerKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has reached the condition in which it will never fire again.
     * </p>
     *
     * @param trigger the trigger that was finalized
     */
    void triggerFinalized(Trigger trigger);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has been paused.
     * </p>
     *
     * @param triggerKey a reference to the trigger that was paused
     */
    void triggerPaused(TriggerKey triggerKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a 
     * group of <code>{@link Trigger}s</code> has been paused.
     * </p>
     * 
     * <p>If all groups were paused then triggerGroup will be null</p>
     * 
     * @param triggerGroup the paused group, or null if all were paused
     */
    void triggersPaused(String triggerGroup);
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has been un-paused.
     * </p>
     *
     * @param triggerKey a reference to the trigger that was resumed
     */
    void triggerResumed(TriggerKey triggerKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a 
     * group of <code>{@link Trigger}s</code> has been un-paused.
     * </p>
     *
     * @param triggerGroup the name of the trigger group that was resumed, or null if all were resumed
     */
    void triggersResumed(String triggerGroup);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been added.
     * </p>
     *
     * @param jobDetail the details of the job that was added
     */
    void jobAdded(JobDetail jobDetail);
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been deleted.
     * </p>
     *
     * @param jobKey a reference to the job that was deleted
     */
    void jobDeleted(JobKey jobKey);
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been paused.
     * </p>
     *
     * @param jobKey a reference to the job that was paused
     */
    void jobPaused(JobKey jobKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a 
     * group of <code>{@link org.quartz.JobDetail}s</code> has been paused.
     * </p>
     * 
     * @param jobGroup the paused group, or null if all were paused
     */
    void jobsPaused(String jobGroup);
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been un-paused.
     * </p>
     *
     * @param jobKey a reference to the job that was resumed
     */
    void jobResumed(JobKey jobKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a 
     * group of <code>{@link org.quartz.JobDetail}s</code> has been un-paused.
     * </p>
     *
     * @param jobGroup the name of the job group that was resumed, or null if all were resumed
     */
    void jobsResumed(String jobGroup);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a serious error has
     * occurred within the scheduler - such as repeated failures in the <code>JobStore</code>,
     * or the inability to instantiate a <code>Job</code> instance when its
     * <code>Trigger</code> has fired.
     * </p>
     * 
     * <p>
     * The <code>getErrorCode()</code> method of the given SchedulerException
     * can be used to determine more specific information about the type of
     * error that was encountered.
     * </p>
     *
     * @param msg a textual description of the scheduler error
     * @param cause the underlying cause of the scheduler error
     */
    void schedulerError(String msg, SchedulerException cause);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has move to standby mode.
     * </p>
     */
    void schedulerInStandbyMode();

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has started.
     * </p>
     */
    void schedulerStarted();
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it is starting.
     * </p>
     */
    void schedulerStarting();
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has shutdown.
     * </p>
     */
    void schedulerShutdown();
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has begun the shutdown sequence.
     * </p>
     */
    void schedulerShuttingdown();

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that all jobs, triggers and calendars were deleted.
     */
    void schedulingDataCleared();
}

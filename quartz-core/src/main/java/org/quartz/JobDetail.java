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

import java.io.Serializable;

/**
 * Conveys the detail properties of a given <code>Job</code> instance. JobDetails are
 * to be created/defined with {@link JobBuilder}.
 * 
 * <p>
 * Quartz does not store an actual instance of a <code>Job</code> class, but
 * instead allows you to define an instance of one, through the use of a <code>JobDetail</code>.
 * </p>
 * 
 * <p>
 * <code>Job</code>s have a name and group associated with them, which
 * should uniquely identify them within a single <code>{@link Scheduler}</code>.
 * </p>
 * 
 * <p>
 * <code>Trigger</code>s are the 'mechanism' by which <code>Job</code>s
 * are scheduled. Many <code>Trigger</code>s can point to the same <code>Job</code>,
 * but a single <code>Trigger</code> can only point to one <code>Job</code>.
 * </p>
 * 
 * @see JobBuilder
 * @see Job
 * @see JobDataMap
 * @see Trigger
 * 
 * @author James House
 */
public interface JobDetail extends Serializable, Cloneable {

    public JobKey getKey();

    /**
     * <p>
     * Return the description given to the <code>Job</code> instance by its
     * creator (if any).
     * </p>
     * 
     * @return null if no description was set.
     */
    public String getDescription();

    /**
     * <p>
     * Get the instance of <code>Job</code> that will be executed.
     * </p>
     *
     * @return the job <code>Class</code>
     */
    public Class<? extends Job> getJobClass();

    /**
     * <p>
     * Get the <code>JobDataMap</code> that is associated with the <code>Job</code>.
     * </p>
     *
     * @return the job's data map
     */
    public JobDataMap getJobDataMap();

    /**
     * <p>
     * Whether or not the <code>Job</code> should remain stored after it is
     * orphaned (no <code>{@link Trigger}s</code> point to it).
     * </p>
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> if the Job should remain persisted after
     *         being orphaned.
     */
    public boolean isDurable();

    /**
     * @see PersistJobDataAfterExecution
     * @return whether the associated Job class carries the {@link PersistJobDataAfterExecution} annotation.
     */
    public boolean isPersistJobDataAfterExecution();

    /**
     * @see DisallowConcurrentExecution
     * @return whether the associated Job class carries the {@link DisallowConcurrentExecution} annotation.
     */
    public boolean isConcurrentExectionDisallowed();

    /**
     * <p>
     * Instructs the <code>Scheduler</code> whether or not the <code>Job</code>
     * should be re-executed if a 'recovery' or 'fail-over' situation is
     * encountered.
     * </p>
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     *
     * @return does the job want to be re-executed on 'recovery' or 'fail-over'
     *
     * @see JobExecutionContext#isRecovering()
     */
    public boolean requestsRecovery();

    public Object clone();
    
    /**
     * Get a {@link JobBuilder} that is configured to produce a 
     * <code>JobDetail</code> identical to this one.
     *
     * @return the job builder
     */
    public JobBuilder getJobBuilder();

}
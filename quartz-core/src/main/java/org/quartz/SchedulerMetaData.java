
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
 * Describes the settings and capabilities of a given <code>{@link Scheduler}</code>
 * instance.
 * 
 * @author James House
 */
public class SchedulerMetaData implements java.io.Serializable {
  
    private static final long serialVersionUID = 4203690002633917647L;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String schedName;

    private String schedInst;

    private Class<?> schedClass;

    private boolean isRemote;

    private boolean started;

    private boolean isInStandbyMode;

    private boolean shutdown;

    private Date startTime;

    private int numJobsExec;

    private Class<?> jsClass;

    private boolean jsPersistent;

    private boolean jsClustered;

    private Class<?> tpClass;

    private int tpSize;

    private String version;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public SchedulerMetaData(String schedName, String schedInst,
            Class<?> schedClass, boolean isRemote, boolean started,
            boolean isInStandbyMode, boolean shutdown, Date startTime, int numJobsExec,
            Class<?> jsClass, boolean jsPersistent, boolean jsClustered, Class<?> tpClass, int tpSize,
            String version) {
        this.schedName = schedName;
        this.schedInst = schedInst;
        this.schedClass = schedClass;
        this.isRemote = isRemote;
        this.started = started;
        this.isInStandbyMode = isInStandbyMode;
        this.shutdown = shutdown;
        this.startTime = startTime;
        this.numJobsExec = numJobsExec;
        this.jsClass = jsClass;
        this.jsPersistent = jsPersistent;
        this.jsClustered = jsClustered;
        this.tpClass = tpClass;
        this.tpSize = tpSize;
        this.version = version;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Returns the name of the <code>Scheduler</code>.
     * </p>
     *
     * @return the scheduler's name
     */
    public String getSchedulerName() {
        return schedName;
    }

    /**
     * <p>
     * Returns the instance Id of the <code>Scheduler</code>.
     * </p>
     *
     * @return the scheduler's instance id
     */
    public String getSchedulerInstanceId() {
        return schedInst;
    }

    /**
     * <p>
     * Returns the class-name of the <code>Scheduler</code> instance.
     * </p>
     *
     * @return the scheduler's class
     */
    public Class<?> getSchedulerClass() {
        return schedClass;
    }

    /**
     * <p>
     * Returns the <code>Date</code> at which the Scheduler started running.
     * </p>
     * 
     * @return null if the scheduler has not been started.
     */
    public Date getRunningSince() {
        return startTime;
    }
    
    /**
     * <p>
     * Returns the number of jobs executed since the <code>Scheduler</code>
     * started..
     * </p>
     *
     * @return number of executed jobs since start
     */
    public int getNumberOfJobsExecuted() {
        return numJobsExec;
    }

    /**
     * <p>
     * Returns whether the <code>Scheduler</code> is being used remotely (via
     * RMI).
     * </p>
     *
     * @return is scheduler remote
     */
    public boolean isSchedulerRemote() {
        return isRemote;
    }

    /**
     * <p>
     * Returns whether the scheduler has been started.
     * </p>
     * 
     * <p>
     * Note: <code>isStarted()</code> may return <code>true</code> even if
     * <code>isInStandbyMode()</code> returns <code>true</code>.
     * </p>
     *
     * @return is scheduler started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Reports whether the <code>Scheduler</code> is in standby mode.
     *
     * @return is scheduler standing by
     */
    public boolean isInStandbyMode() {
        return isInStandbyMode;
    }

    /**
     * <p>
     * Reports whether the <code>Scheduler</code> has been shutdown.
     * </p>
     *
     * @return is scheduler shutdown
     */
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * <p>
     * Returns the class-name of the <code>JobStore</code> instance that is
     * being used by the <code>Scheduler</code>.
     * </p>
     *
     * @return the job store class
     */
    public Class<?> getJobStoreClass() {
        return jsClass;
    }
    
    /**
     * <p>
     * Returns whether or not the <code>Scheduler</code>'s<code>JobStore</code>
     * instance supports persistence.
     * </p>
     *
     * @return is job store persistent
     */
    public boolean isJobStoreSupportsPersistence() {
        return jsPersistent;
    }

    /**
     * <p>
     * Returns whether or not the <code>Scheduler</code>'s<code>JobStore</code>
     * is clustered.
     * </p>
     *
     * @return is job store clustered
     */
    public boolean isJobStoreClustered() {
        return jsClustered;
    }

    /**
     * <p>
     * Returns the class-name of the <code>ThreadPool</code> instance that is
     * being used by the <code>Scheduler</code>.
     * </p>
     *
     * @return scheduler's thread pool class
     */
    public Class<?> getThreadPoolClass() {
        return tpClass;
    }

    /**
     * <p>
     * Returns the number of threads currently in the <code>Scheduler</code>'s
     * <code>ThreadPool</code>.
     * </p>
     *
     * @return scheduler's thread pool thread count
     */
    public int getThreadPoolSize() {
        return tpSize;
    }

    /**
     * <p>
     * Returns the version of Quartz that is running.
     * </p>
     *
     * @return Quartz version
     */
    public String getVersion() {
        return version;
    }

    /**
     * <p>
     * Return a simple string representation of this object.
     * </p>
     */
    @Override
    public String toString() {
        try {
            return getSummary();
        } catch (SchedulerException se) {
            return "SchedulerMetaData: undeterminable.";
        }
    }

    /**
     * <p>
     * Returns a formatted (human readable) String describing all the <code>Scheduler</code>'s
     * meta-data values.
     * </p>
     * 
     * <p>
     * The format of the String looks something like this:
     * </p>
     *
     * <pre>
     *  Quartz Scheduler 'SchedulerName' with instanceId 'SchedulerInstanceId' Scheduler class: 'org.quartz.impl.StdScheduler' - running locally. Running since: '11:33am on Jul 19, 2002' Not currently paused. Number of Triggers fired: '123' Using thread pool 'org.quartz.simpl.SimpleThreadPool' - with '8' threads Using job-store 'org.quartz.impl.JDBCJobStore' - which supports persistence.
     * </pre>
     *
     * @return scheduler summary text
     * @throws SchedulerException internal scheduler error
     */
    public String getSummary() throws SchedulerException {
        StringBuilder str = new StringBuilder("Quartz Scheduler (v");
        str.append(getVersion());
        str.append(") '");

        str.append(getSchedulerName());
        str.append("' with instanceId '");
        str.append(getSchedulerInstanceId());
        str.append("'\n");

        str.append("  Scheduler class: '");
        str.append(getSchedulerClass().getName());
        str.append("'");
        if (isSchedulerRemote()) {
            str.append(" - access via RMI.");
        } else {
            str.append(" - running locally.");
        }
        str.append("\n");

        if (!isShutdown()) {
            if (getRunningSince() != null) {
                str.append("  Running since: ");
                str.append(getRunningSince());
            } else {
                str.append("  NOT STARTED.");
            }
            str.append("\n");

            if (isInStandbyMode()) {
                str.append("  Currently in standby mode.");
            } else {
                str.append("  Not currently in standby mode.");
            }
        } else {
            str.append("  Scheduler has been SHUTDOWN.");
        }
        str.append("\n");

        str.append("  Number of jobs executed: ");
        str.append(getNumberOfJobsExecuted());
        str.append("\n");

        str.append("  Using thread pool '");
        str.append(getThreadPoolClass().getName());
        str.append("' - with ");
        str.append(getThreadPoolSize());
        str.append(" threads.");
        str.append("\n");

        str.append("  Using job-store '");
        str.append(getJobStoreClass().getName());
        str.append("' - which ");
        if (isJobStoreSupportsPersistence()) {
            str.append("supports persistence.");
        } else {
            str.append("does not support persistence.");
        }
        if (isJobStoreClustered()) {
            str.append(" and is clustered.");
        } else {
            str.append(" and is not clustered.");
        }
        str.append("\n");

        return str.toString();
    }

}

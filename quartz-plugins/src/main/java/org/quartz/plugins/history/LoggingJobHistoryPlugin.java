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

package org.quartz.plugins.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.JobListener;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

import java.text.MessageFormat;

/**
 * Logs a history of all job executions (and execution vetos) via the 
 * Jakarta Commons-Logging framework.
 * 
 * <p>
 * The logged message is customizable by setting one of the following message
 * properties to a String that conforms to the syntax of <code>java.util.MessageFormat</code>.
 * </p>
 * 
 * <p>
 * JobToBeFiredMessage - available message data are:
 *
 * <table summary="">
 * <tr>
 * <th>Element</th>
 * <th>Data Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>String</td>
 * <td>The Job's Name.</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>String</td>
 * <td>The Job's Group.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Date</td>
 * <td>The current time.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>String</td>
 * <td>The Trigger's name.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>String</td>
 * <td>The Triggers's group.</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>Date</td>
 * <td>The scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>Date</td>
 * <td>The next scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>Integer</td>
 * <td>The re-fire count from the JobExecutionContext.</td>
 * </tr>
 * </table>
 *
 * <p>
 * The default message text is <i>"Job {1}.{0} fired (by trigger {4}.{3}) at:
 * {2, date, HH:mm:ss MM/dd/yyyy}"</i>
 * </p>
 * 
 * <p>
 * JobSuccessMessage - available message data are:
 *
 * <table summary="">
 * <tr>
 *   <th>Element</th>
 *   <th>Data Type</th>
 *   <th>Description</th>
 * </tr>
 * <tr>
 *   <td>0</td>
 *   <td>String</td>
 *   <td>The Job's Name.</td>
 * </tr>
 * <tr>
 *   <td>1</td>
 *   <td>String</td>
 *   <td>The Job's Group.</td>
 * </tr>
 * <tr>
 *   <td>2</td>
 *   <td>Date</td>
 *   <td>The current time.</td>
 * </tr>
 * <tr>
 *   <td>3</td>
 *   <td>String</td>
 *   <td>The Trigger's name.</td>
 * </tr>
 * <tr>
 *   <td>4</td>
 *   <td>String</td>
 *   <td>The Triggers's group.</td>
 * </tr>
 * <tr>
 *   <td>5</td>
 *   <td>Date</td>
 *   <td>The scheduled fire time.</td>
 * </tr>
 * <tr>
 *   <td>6</td>
 *   <td>Date</td>
 *   <td>The next scheduled fire time.</td>
 * </tr>
 * <tr>
 *   <td>7</td>
 *   <td>Integer</td>
 *   <td>The re-fire count from the JobExecutionContext.</td>
 * </tr>
 * <tr>
 *   <td>8</td>
 *   <td>Object</td>
 *   <td>The string value (toString() having been called) of the result (if any)
 *      that the Job set on the JobExecutionContext, with on it.  "NULL" if no 
 *      result was set.</td>
 * </tr>
 * </table>
 *
 * <p>
 * The default message text is <i>"Job {1}.{0} execution complete at {2, date,
 * HH:mm:ss MM/dd/yyyy} and reports: {8}"</i>
 * </p>
 * 
 * <p>
 * JobFailedMessage - available message data are:
 *
 * <table summary="">
 * <tr>
 * <th>Element</th>
 * <th>Data Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>String</td>
 * <td>The Job's Name.</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>String</td>
 * <td>The Job's Group.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Date</td>
 * <td>The current time.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>String</td>
 * <td>The Trigger's name.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>String</td>
 * <td>The Triggers's group.</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>Date</td>
 * <td>The scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>Date</td>
 * <td>The next scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>Integer</td>
 * <td>The re-fire count from the JobExecutionContext.</td>
 * </tr>
 * <tr>
 * <td>8</td>
 * <td>String</td>
 * <td>The message from the thrown JobExecution Exception.
 * </td>
 * </tr>
 * </table>
 *
 * <p>
 * The default message text is <i>"Job {1}.{0} execution failed at {2, date,
 * HH:mm:ss MM/dd/yyyy} and reports: {8}"</i>
 * </p>
 * 
 * 
 * <p>
 * JobWasVetoedMessage - available message data are:
 *
 * <table summary="">
 * <tr>
 * <th>Element</th>
 * <th>Data Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>String</td>
 * <td>The Job's Name.</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>String</td>
 * <td>The Job's Group.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Date</td>
 * <td>The current time.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>String</td>
 * <td>The Trigger's name.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>String</td>
 * <td>The Triggers's group.</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>Date</td>
 * <td>The scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>Date</td>
 * <td>The next scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>Integer</td>
 * <td>The re-fire count from the JobExecutionContext.</td>
 * </tr>
 * </table>
 *
 * <p>
 * The default message text is <i>"Job {1}.{0} was vetoed.  It was to be fired 
 * (by trigger {4}.{3}) at: {2, date, HH:mm:ss MM/dd/yyyy}"</i>
 * </p>
 * 
 * 
 * @author James House
 */
public class LoggingJobHistoryPlugin implements SchedulerPlugin, JobListener {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String name;

    private String jobToBeFiredMessage = "Job {1}.{0} fired (by trigger {4}.{3}) at: {2, date, HH:mm:ss MM/dd/yyyy}";
    
    private String jobSuccessMessage = "Job {1}.{0} execution complete at {2, date, HH:mm:ss MM/dd/yyyy} and reports: {8}";

    private String jobFailedMessage = "Job {1}.{0} execution failed at {2, date, HH:mm:ss MM/dd/yyyy} and reports: {8}";

    private String jobWasVetoedMessage = "Job {1}.{0} was vetoed.  It was to be fired (by trigger {4}.{3}) at: {2, date, HH:mm:ss MM/dd/yyyy}";

    private final Logger log = LoggerFactory.getLogger(getClass());

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public LoggingJobHistoryPlugin() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected Logger getLog() {
        return log;
    }

    /**
     * Get the message that is logged when a Job successfully completes its 
     * execution.
     */
    public String getJobSuccessMessage() {
        return jobSuccessMessage;
    }

    /**
     * Get the message that is logged when a Job fails its 
     * execution.
     */
    public String getJobFailedMessage() {
        return jobFailedMessage;
    }

    /**
     * Get the message that is logged when a Job is about to execute.
     */
    public String getJobToBeFiredMessage() {
        return jobToBeFiredMessage;
    }

    /**
     * Set the message that is logged when a Job successfully completes its 
     * execution.
     * 
     * @param jobSuccessMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobSuccessMessage(String jobSuccessMessage) {
        this.jobSuccessMessage = jobSuccessMessage;
    }

    /**
     * Set the message that is logged when a Job fails its 
     * execution.
     * 
     * @param jobFailedMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobFailedMessage(String jobFailedMessage) {
        this.jobFailedMessage = jobFailedMessage;
    }

    /**
     * Set the message that is logged when a Job is about to execute.
     * 
     * @param jobToBeFiredMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobToBeFiredMessage(String jobToBeFiredMessage) {
        this.jobToBeFiredMessage = jobToBeFiredMessage;
    }

    /**
     * Get the message that is logged when a Job execution is vetoed by a
     * trigger listener.
     */
    public String getJobWasVetoedMessage() {
        return jobWasVetoedMessage;
    }

    /**
     * Set the message that is logged when a Job execution is vetoed by a
     * trigger listener.
     * 
     * @param jobWasVetoedMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobWasVetoedMessage(String jobWasVetoedMessage) {
        this.jobWasVetoedMessage = jobWasVetoedMessage;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * SchedulerPlugin Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     * 
     * @throws SchedulerException
     *           if there is an error initializing.
     */
    public void initialize(String pname, Scheduler scheduler,ClassLoadHelper classLoadHelper)
        throws SchedulerException {
        this.name = pname;
        scheduler.getListenerManager().addJobListener(this, EverythingMatcher.allJobs());
    }

    public void start() {
        // do nothing...
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    public void shutdown() {
        // nothing to do...
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * JobListener Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /*
     * Object[] arguments = { new Integer(7), new
     * Date(System.currentTimeMillis()), "a disturbance in the Force" };
     * 
     * String result = MessageFormat.format( "At {1,time} on {1,date}, there
     * was {2} on planet {0,number,integer}.", arguments);
     */

    public String getName() {
        return name;
    }

    /** 
     * @see org.quartz.JobListener#jobToBeExecuted(JobExecutionContext)
     */
    public void jobToBeExecuted(JobExecutionContext context) {
        if (!getLog().isInfoEnabled()) {
            return;
        } 
        
        Trigger trigger = context.getTrigger();

        Object[] args = {
            context.getJobDetail().getKey().getName(),
            context.getJobDetail().getKey().getGroup(), new java.util.Date(),
            trigger.getKey().getName(), trigger.getKey().getGroup(),
            trigger.getPreviousFireTime(), trigger.getNextFireTime(),
            Integer.valueOf(context.getRefireCount())
        };

        getLog().info(MessageFormat.format(getJobToBeFiredMessage(), args));
    }
    
    /** 
     * @see org.quartz.JobListener#jobWasExecuted(JobExecutionContext, JobExecutionException)
     */
    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jobException) {

        Trigger trigger = context.getTrigger();
        
        Object[] args = null;
        
        if (jobException != null) {
            if (!getLog().isWarnEnabled()) {
                return;
            } 
            
            String errMsg = jobException.getMessage();
            args = 
                new Object[] {
                    context.getJobDetail().getKey().getName(),
                    context.getJobDetail().getKey().getGroup(), new java.util.Date(),
                    trigger.getKey().getName(), trigger.getKey().getGroup(),
                    trigger.getPreviousFireTime(), trigger.getNextFireTime(),
                    Integer.valueOf(context.getRefireCount()), errMsg
                };
            
            getLog().warn(MessageFormat.format(getJobFailedMessage(), args), jobException); 
        } else {
            if (!getLog().isInfoEnabled()) {
                return;
            } 
            
            String result = String.valueOf(context.getResult());
            args =
                new Object[] {
                    context.getJobDetail().getKey().getName(),
                    context.getJobDetail().getKey().getGroup(), new java.util.Date(),
                    trigger.getKey().getName(), trigger.getKey().getGroup(),
                    trigger.getPreviousFireTime(), trigger.getNextFireTime(),
                    Integer.valueOf(context.getRefireCount()), result
                };
            
            getLog().info(MessageFormat.format(getJobSuccessMessage(), args));
        }
    }

    /** 
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
        
        if (!getLog().isInfoEnabled()) {
            return;
        } 
        
        Trigger trigger = context.getTrigger();

        Object[] args = {
            context.getJobDetail().getKey().getName(),
            context.getJobDetail().getKey().getGroup(), new java.util.Date(),
            trigger.getKey().getName(), trigger.getKey().getGroup(),
            trigger.getPreviousFireTime(), trigger.getNextFireTime(),
            Integer.valueOf(context.getRefireCount())
        };

        getLog().info(MessageFormat.format(getJobWasVetoedMessage(), args));
    }

}

// EOF

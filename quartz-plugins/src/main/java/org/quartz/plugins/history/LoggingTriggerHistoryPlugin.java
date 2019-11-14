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

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

/**
 * Logs a history of all trigger firings via the Jakarta Commons-Logging
 * framework.
 * 
 * <p>
 * The logged message is customizable by setting one of the following message
 * properties to a String that conforms to the syntax of <code>java.util.MessageFormat</code>.
 * </p>
 * 
 * <p>
 * TriggerFiredMessage - available message data are:
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
 * <td>The Trigger's Name.</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>String</td>
 * <td>The Trigger's Group.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Date</td>
 * <td>The scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>Date</td>
 * <td>The next scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>Date</td>
 * <td>The actual fire time.</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>String</td>
 * <td>The Job's name.</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>String</td>
 * <td>The Job's group.</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>Integer</td>
 * <td>The re-fire count from the JobExecutionContext.</td>
 * </tr>
 * </table>
 *
 * <p>
 * The default message text is <i>"Trigger {1}.{0} fired job {6}.{5} at: {4,
 * date, HH:mm:ss MM/dd/yyyy}"</i>
 * </p>
 * 
 * <p>
 * TriggerMisfiredMessage - available message data are:
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
 * <td>The Trigger's Name.</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>String</td>
 * <td>The Trigger's Group.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Date</td>
 * <td>The scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>Date</td>
 * <td>The next scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>Date</td>
 * <td>The actual fire time. (the time the misfire was detected/handled)</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>String</td>
 * <td>The Job's name.</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>String</td>
 * <td>The Job's group.</td>
 * </tr>
 * </table>
 *
 * <p>
 * The default message text is <i>"Trigger {1}.{0} misfired job {6}.{5} at:
 * {4, date, HH:mm:ss MM/dd/yyyy}. Should have fired at: {3, date, HH:mm:ss
 * MM/dd/yyyy}"</i>
 * </p>
 * 
 * <p>
 * TriggerCompleteMessage - available message data are:
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
 * <td>The Trigger's Name.</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>String</td>
 * <td>The Trigger's Group.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Date</td>
 * <td>The scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>Date</td>
 * <td>The next scheduled fire time.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>Date</td>
 * <td>The job completion time.</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>String</td>
 * <td>The Job's name.</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>String</td>
 * <td>The Job's group.</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>Integer</td>
 * <td>The re-fire count from the JobExecutionContext.</td>
 * </tr>
 * <tr>
 * <td>8</td>
 * <td>Integer</td>
 * <td>The trigger's resulting instruction code.</td>
 * </tr>
 * <tr>
 * <td>9</td>
 * <td>String</td>
 * <td>A human-readable translation of the trigger's resulting instruction
 * code.</td>
 * </tr>
 * </table>
 *
 * <p>
 * The default message text is <i>"Trigger {1}.{0} completed firing job
 * {6}.{5} at {4, date, HH:mm:ss MM/dd/yyyy} with resulting trigger instruction
 * code: {9}"</i>
 * </p>
 * 
 * @author James House
 */
public class LoggingTriggerHistoryPlugin implements SchedulerPlugin,
        TriggerListener {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String name;

    private String triggerFiredMessage = "Trigger {1}.{0} fired job {6}.{5} at: {4, date, HH:mm:ss MM/dd/yyyy}";

    private String triggerMisfiredMessage = "Trigger {1}.{0} misfired job {6}.{5}  at: {4, date, HH:mm:ss MM/dd/yyyy}.  Should have fired at: {3, date, HH:mm:ss MM/dd/yyyy}";

    private String triggerCompleteMessage = "Trigger {1}.{0} completed firing job {6}.{5} at {4, date, HH:mm:ss MM/dd/yyyy} with resulting trigger instruction code: {9}";

    private final Logger log = LoggerFactory.getLogger(getClass());

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public LoggingTriggerHistoryPlugin() {
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
     * Get the message that is printed upon the completion of a trigger's
     * firing.
     * 
     * @return String
     */
    public String getTriggerCompleteMessage() {
        return triggerCompleteMessage;
    }

    /**
     * Get the message that is printed upon a trigger's firing.
     * 
     * @return String
     */
    public String getTriggerFiredMessage() {
        return triggerFiredMessage;
    }

    /**
     * Get the message that is printed upon a trigger's mis-firing.
     * 
     * @return String
     */
    public String getTriggerMisfiredMessage() {
        return triggerMisfiredMessage;
    }

    /**
     * Set the message that is printed upon the completion of a trigger's
     * firing.
     * 
     * @param triggerCompleteMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setTriggerCompleteMessage(String triggerCompleteMessage) {
        this.triggerCompleteMessage = triggerCompleteMessage;
    }

    /**
     * Set the message that is printed upon a trigger's firing.
     * 
     * @param triggerFiredMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setTriggerFiredMessage(String triggerFiredMessage) {
        this.triggerFiredMessage = triggerFiredMessage;
    }

    /**
     * Set the message that is printed upon a trigger's firing.
     * 
     * @param triggerMisfiredMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setTriggerMisfiredMessage(String triggerMisfiredMessage) {
        this.triggerMisfiredMessage = triggerMisfiredMessage;
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
     * @throws org.quartz.SchedulerConfigException
     *           if there is an error initializing.
     */
    public void initialize(String pname, Scheduler scheduler, ClassLoadHelper classLoadHelper)
        throws SchedulerException {
        this.name = pname;

        scheduler.getListenerManager().addTriggerListener(this,  EverythingMatcher.allTriggers());
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
     * TriggerListener Interface.
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

    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        if (!getLog().isInfoEnabled()) {
            return;
        } 
        
        Object[] args = {
            trigger.getKey().getName(), trigger.getKey().getGroup(),
            trigger.getPreviousFireTime(), trigger.getNextFireTime(),
            new java.util.Date(), context.getJobDetail().getKey().getName(),
            context.getJobDetail().getKey().getGroup(),
            Integer.valueOf(context.getRefireCount())
        };

        getLog().info(MessageFormat.format(getTriggerFiredMessage(), args));
    }

    public void triggerMisfired(Trigger trigger) {
        if (!getLog().isInfoEnabled()) {
            return;
        } 
        
        Object[] args = {
            trigger.getKey().getName(), trigger.getKey().getGroup(),
            trigger.getPreviousFireTime(), trigger.getNextFireTime(),
            new java.util.Date(), trigger.getJobKey().getName(),
            trigger.getJobKey().getGroup()
        };

        getLog().info(MessageFormat.format(getTriggerMisfiredMessage(), args));
    }

    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        if (!getLog().isInfoEnabled()) {
            return;
        } 
        
        String instrCode = "UNKNOWN";
        if (triggerInstructionCode == CompletedExecutionInstruction.DELETE_TRIGGER) {
            instrCode = "DELETE TRIGGER";
        } else if (triggerInstructionCode == CompletedExecutionInstruction.NOOP) {
            instrCode = "DO NOTHING";
        } else if (triggerInstructionCode == CompletedExecutionInstruction.RE_EXECUTE_JOB) {
            instrCode = "RE-EXECUTE JOB";
        } else if (triggerInstructionCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE) {
            instrCode = "SET ALL OF JOB'S TRIGGERS COMPLETE";
        } else if (triggerInstructionCode == CompletedExecutionInstruction.SET_TRIGGER_COMPLETE) {
            instrCode = "SET THIS TRIGGER COMPLETE";
        }

        Object[] args = {
            trigger.getKey().getName(), trigger.getKey().getGroup(),
            trigger.getPreviousFireTime(), trigger.getNextFireTime(),
            new java.util.Date(), context.getJobDetail().getKey().getName(),
            context.getJobDetail().getKey().getGroup(),
            Integer.valueOf(context.getRefireCount()),
            triggerInstructionCode.toString(), instrCode
        };

        getLog().info(MessageFormat.format(getTriggerCompleteMessage(), args));
    }

    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

}

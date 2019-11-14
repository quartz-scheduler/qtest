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
 */
package org.quartz.plugins.interrupt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.listeners.TriggerListenerSupport;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin catches the event of job running for a long time (more than the
 * configured max time) and tells the scheduler to "try" interrupting it if
 * enabled.
 * 
 * @see org.quartz.Scheduler#interrupt(org.quartz.JobKey)
 * @see org.quartz.Scheduler#interrupt(String)
 *
 * @author Rama Chavali
 */

public class JobInterruptMonitorPlugin extends TriggerListenerSupport implements SchedulerPlugin {

    private static final String JOB_INTERRUPT_MONITOR_KEY = "JOB_INTERRUPT_MONITOR_KEY";

    private long DEFAULT_MAX_RUNTIME = 300000;
    
    private String name;

    private ScheduledExecutorService executor;

    @SuppressWarnings("rawtypes")
    private ScheduledFuture future;

    private Scheduler scheduler;

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    //Constants

    public static final String AUTO_INTERRUPTIBLE = "AutoInterruptable";

    public static final String MAX_RUN_TIME = "MaxRunTime";

    public JobInterruptMonitorPlugin() {
    }

    @Override
    public void start() {
    }

    @Override

    public void shutdown() {
        this.executor.shutdown();
    }

    protected Logger getLog() {
        return log;
    }

    @SuppressWarnings("rawtypes")
    public ScheduledFuture scheduleJobInterruptMonitor(JobKey jobkey, long delay) {
        return this.executor.schedule(new InterruptMonitor(jobkey, scheduler), delay, TimeUnit.MILLISECONDS);
    }

    // Bean Property Methods

    public long getDefaultMaxRunTime() {
        return this.DEFAULT_MAX_RUNTIME;
    }

    public void setDefaultMaxRunTime(long defaultMaxRunTime) {
        this.DEFAULT_MAX_RUNTIME = defaultMaxRunTime;
    }

    // Trigger Listener Methods
    public String getName() {
        return name;
    }

    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        // Call the scheduleJobInterruptMonitor and capture the ScheduledFuture in context
        try {
            // Schedule Monitor only if the job wants AutoInterruptable functionality
            if (context.getJobDetail().getJobDataMap().getBoolean(AUTO_INTERRUPTIBLE)) {
                JobInterruptMonitorPlugin monitorPlugin = (JobInterruptMonitorPlugin) context.getScheduler()
                        .getContext().get(JOB_INTERRUPT_MONITOR_KEY);
                // Get the MaxRuntime from Job Data if NOT available use DEFAULT_MAX_RUNTIME from Plugin Configuration
                long jobDataDelay  = DEFAULT_MAX_RUNTIME;

                if (context.getJobDetail().getJobDataMap().get(MAX_RUN_TIME) != null){
                     jobDataDelay = context.getJobDetail().getJobDataMap().getLong(MAX_RUN_TIME);
                }
                future = monitorPlugin.scheduleJobInterruptMonitor(context.getJobDetail().getKey(), jobDataDelay);
                getLog().debug("Job's Interrupt Monitor has been scheduled to interrupt with the delay :"
                        + DEFAULT_MAX_RUNTIME);
            }
        } catch (SchedulerException e) {
            getLog().info("Error scheduling interrupt monitor " + e.getMessage(), e);
        }
    }

    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        // cancel the Future if job is complete
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper helper) throws SchedulerException {

        getLog().info("Registering Job Interrupt Monitor Plugin");
        this.name = name;
        this.executor = Executors.newScheduledThreadPool(1);
        scheduler.getContext().put(JOB_INTERRUPT_MONITOR_KEY, this);
        this.scheduler = scheduler;
        // Set the trigger Listener as this class to the ListenerManager here
        this.scheduler.getListenerManager().addTriggerListener(this);

    }

    static class InterruptMonitor implements Runnable {

        private final JobKey jobKey;
        private final Scheduler scheduler;

        private final Logger log = LoggerFactory.getLogger(getClass());

        InterruptMonitor(JobKey jobKey, Scheduler scheduler) {
            this.jobKey = jobKey;
            this.scheduler = scheduler;
        }

        protected Logger getLog() {
            return log;
        }

        @Override
        public void run() {
            try {

                // Interrupt the job here - using Scheduler API that gets propagated to Job's interrupt
                getLog().info("Interrupting Job as it ran more than the configured max time. Job Details [" + jobKey.getName() + ":" + jobKey.getGroup()+"]");
                scheduler.interrupt(jobKey);
            } catch (SchedulerException x) {
                getLog().info("Error interrupting Job: " + x.getMessage(), x);
            }
        }
    }
}

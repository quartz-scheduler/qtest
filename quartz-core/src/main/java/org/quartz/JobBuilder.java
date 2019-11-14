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

import org.quartz.impl.JobDetailImpl;
import org.quartz.utils.Key;

/**
 * <code>JobBuilder</code> is used to instantiate {@link JobDetail}s.
 * 
 * <p>The builder will always try to keep itself in a valid state, with 
 * reasonable defaults set for calling build() at any point.  For instance
 * if you do not invoke <i>withIdentity(..)</i> a job name will be generated
 * for you.</p>
 *   
 * <p>Quartz provides a builder-style API for constructing scheduling-related
 * entities via a Domain-Specific Language (DSL).  The DSL can best be
 * utilized through the usage of static imports of the methods on the classes
 * <code>TriggerBuilder</code>, <code>JobBuilder</code>, 
 * <code>DateBuilder</code>, <code>JobKey</code>, <code>TriggerKey</code> 
 * and the various <code>ScheduleBuilder</code> implementations.</p>
 * 
 * <p>Client code can then use the DSL to write code such as this:</p>
 * <pre>
 *         JobDetail job = newJob(MyJob.class)
 *             .withIdentity("myJob")
 *             .build();
 *             
 *         Trigger trigger = newTrigger() 
 *             .withIdentity(triggerKey("myTrigger", "myTriggerGroup"))
 *             .withSchedule(simpleSchedule()
 *                 .withIntervalInHours(1)
 *                 .repeatForever())
 *             .startAt(futureDate(10, MINUTES))
 *             .build();
 *         
 *         scheduler.scheduleJob(job, trigger);
 * </pre>
 *  
 * @see TriggerBuilder
 * @see DateBuilder 
 * @see JobDetail
 */
public class JobBuilder {

    private JobKey key;
    private String description;
    private Class<? extends Job> jobClass;
    private boolean durability;
    private boolean shouldRecover;
    
    private JobDataMap jobDataMap = new JobDataMap();
    
    protected JobBuilder() {
    }
    
    /**
     * Create a JobBuilder with which to define a <code>JobDetail</code>.
     * 
     * @return a new JobBuilder
     */
    public static JobBuilder newJob() {
        return new JobBuilder();
    }
    
    /**
     * Create a JobBuilder with which to define a <code>JobDetail</code>,
     * and set the class name of the <code>Job</code> to be executed.
     *
     * @param jobClass the class of the job the builder will create
     * @return a new JobBuilder
     */
    public static JobBuilder newJob(Class <? extends Job> jobClass) {
        JobBuilder b = new JobBuilder();
        b.ofType(jobClass);
        return b;
    }

    /**
     * Produce the <code>JobDetail</code> instance defined by this 
     * <code>JobBuilder</code>.
     * 
     * @return the defined JobDetail.
     */
    public JobDetail build() {

        JobDetailImpl job = new JobDetailImpl();
        
        job.setJobClass(jobClass);
        job.setDescription(description);
        if(key == null)
            key = new JobKey(Key.createUniqueName(null), null);
        job.setKey(key); 
        job.setDurability(durability);
        job.setRequestsRecovery(shouldRecover);
        
        
        if(!jobDataMap.isEmpty())
            job.setJobDataMap(jobDataMap);
        
        return job;
    }
    
    /**
     * Use a <code>JobKey</code> with the given name and default group to
     * identify the JobDetail.
     * 
     * <p>If none of the 'withIdentity' methods are set on the JobBuilder,
     * then a random, unique JobKey will be generated.</p>
     * 
     * @param name the name element for the Job's JobKey
     * @return the updated JobBuilder
     * @see JobKey
     * @see JobDetail#getKey()
     */
    public JobBuilder withIdentity(String name) {
        key = new JobKey(name, null);
        return this;
    }  
    
    /**
     * Use a <code>JobKey</code> with the given name and group to
     * identify the JobDetail.
     * 
     * <p>If none of the 'withIdentity' methods are set on the JobBuilder,
     * then a random, unique JobKey will be generated.</p>
     * 
     * @param name the name element for the Job's JobKey
     * @param group the group element for the Job's JobKey
     * @return the updated JobBuilder
     * @see JobKey
     * @see JobDetail#getKey()
     */
    public JobBuilder withIdentity(String name, String group) {
        key = new JobKey(name, group);
        return this;
    }
    
    /**
     * Use a <code>JobKey</code> to identify the JobDetail.
     * 
     * <p>If none of the 'withIdentity' methods are set on the JobBuilder,
     * then a random, unique JobKey will be generated.</p>
     * 
     * @param jobKey the Job's JobKey
     * @return the updated JobBuilder
     * @see JobKey
     * @see JobDetail#getKey()
     */
    public JobBuilder withIdentity(JobKey jobKey) {
        this.key = jobKey;
        return this;
    }
    
    /**
     * Set the given (human-meaningful) description of the Job.
     * 
     * @param jobDescription the description for the Job
     * @return the updated JobBuilder
     * @see JobDetail#getDescription()
     */
    public JobBuilder withDescription(String jobDescription) {
        this.description = jobDescription;
        return this;
    }
    
    /**
     * Set the class which will be instantiated and executed when a
     * Trigger fires that is associated with this JobDetail.
     * 
     * @param jobClazz a class implementing the Job interface.
     * @return the updated JobBuilder
     * @see JobDetail#getJobClass()
     */
    public JobBuilder ofType(Class <? extends Job> jobClazz) {
        this.jobClass = jobClazz;
        return this;
    }

    /**
     * Instructs the <code>Scheduler</code> whether or not the <code>Job</code>
     * should be re-executed if a 'recovery' or 'fail-over' situation is
     * encountered.
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * - this method sets the value to <code>true</code>.
     * </p>
     * 
     * @return the updated JobBuilder
     * @see JobDetail#requestsRecovery()
     */
    public JobBuilder requestRecovery() {
        this.shouldRecover = true;
        return this;
    }

    /**
     * Instructs the <code>Scheduler</code> whether or not the <code>Job</code>
     * should be re-executed if a 'recovery' or 'fail-over' situation is
     * encountered.
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     * 
     * @param jobShouldRecover the desired setting
     * @return the updated JobBuilder
     */
    public JobBuilder requestRecovery(boolean jobShouldRecover) {
        this.shouldRecover = jobShouldRecover;
        return this;
    }

    /**
     * Whether or not the <code>Job</code> should remain stored after it is
     * orphaned (no <code>{@link Trigger}s</code> point to it).
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code> 
     * - this method sets the value to <code>true</code>.
     * </p>
     * 
     * @return the updated JobBuilder
     * @see JobDetail#isDurable()
     */
    public JobBuilder storeDurably() {
        return storeDurably(true);
    }
    
    /**
     * Whether or not the <code>Job</code> should remain stored after it is
     * orphaned (no <code>{@link Trigger}s</code> point to it).
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     * 
     * @param jobDurability the value to set for the durability property.
     * @return the updated JobBuilder
     * @see JobDetail#isDurable()
     */
    public JobBuilder storeDurably(boolean jobDurability) {
        this.durability = jobDurability;
        return this;
    }
    
    /**
     * Add the given key-value pair to the JobDetail's {@link JobDataMap}.
     *
     * @param dataKey the string key for the job's data map mapping that will be returned by the builder
     * @param value the <code>String</code> value for the job's data map mapping that will be returned by the builder
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap()
     */
    public JobBuilder usingJobData(String dataKey, String value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    
    /**
     * Add the given key-value pair to the JobDetail's {@link JobDataMap}.
     *
     * @param dataKey the string key for the job's data map mapping that will be returned by the builder
     * @param value the <code>Integer</code> value for the job's data map mapping that will be returned by the builder
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap()
     */
    public JobBuilder usingJobData(String dataKey, Integer value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    
    /**
     * Add the given key-value pair to the JobDetail's {@link JobDataMap}.
     *
     * @param dataKey the string key for the job's data map mapping that will be returned by the builder
     * @param value the <code>Long</code> value for the job's data map mapping that will be returned by the builder
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap()
     */
    public JobBuilder usingJobData(String dataKey, Long value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    
    /**
     * Add the given key-value pair to the JobDetail's {@link JobDataMap}.
     *
     * @param dataKey the string key for the job's data map mapping that will be returned by the builder
     * @param value the <code>Float</code> value for the job's data map mapping that will be returned by the builder
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap()
     */
    public JobBuilder usingJobData(String dataKey, Float value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    
    /**
     * Add the given key-value pair to the JobDetail's {@link JobDataMap}.
     *
     * @param dataKey the string key for the job's data map mapping that will be returned by the builder
     * @param value the <code>Double</code> value for the job's data map mapping that will be returned by the builder
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap()
     */
    public JobBuilder usingJobData(String dataKey, Double value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    
    /**
     * Add the given key-value pair to the JobDetail's {@link JobDataMap}.
     *
     * @param dataKey the string key for the job's data map mapping that will be returned by the builder
     * @param value the <code>Boolean</code> value for the job's data map mapping that will be returned by the builder
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap()
     */
    public JobBuilder usingJobData(String dataKey, Boolean value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    
    /**
     * Add all the data from the given {@link JobDataMap} to the
     * {@code JobDetail}'s {@code JobDataMap}.
     *
     * @param newJobDataMap the mappings to add to the builder's existing mappings
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap()
     */
    public JobBuilder usingJobData(JobDataMap newJobDataMap) {
        jobDataMap.putAll(newJobDataMap);
        return this;
    }

    /**
     * Replace the {@code JobDetail}'s {@link JobDataMap} with the
     * given {@code JobDataMap}.
     *
     * @param newJobDataMap the mappings to replace the builder's existing mappings
     * @return the updated JobBuilder
     * @see JobDetail#getJobDataMap() 
     */
    public JobBuilder setJobData(JobDataMap newJobDataMap) {
        jobDataMap = newJobDataMap;
        return this;
    }
}

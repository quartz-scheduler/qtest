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

import java.util.List;

/**
 * Client programs may be interested in the 'listener' interfaces that are
 * available from Quartz. The <code>{@link JobListener}</code> interface
 * provides notifications of <code>Job</code> executions. The 
 * <code>{@link TriggerListener}</code> interface provides notifications of 
 * <code>Trigger</code> firings. The <code>{@link SchedulerListener}</code> 
 * interface provides notifications of <code>Scheduler</code> events and 
 * errors.  Listeners can be associated with local schedulers through the 
 * {@link ListenerManager} interface.  
 * 
 * <p>Listener registration order is preserved, and hence notification of listeners
 * will be in the order in which they were registered.</p>
 * 
 * @author jhouse
 * @since 2.0 - previously listeners were managed directly on the Scheduler interface.
 */
public interface ListenerManager {

    /**
     * Add the given <code>{@link JobListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for all Jobs.
     * 
     * Because no matchers are provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param jobListener the listener to receive job-related events
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addJobListener(JobListener jobListener);

    /**
     * Add the given <code>{@link JobListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for Jobs that are matched by the
     * given Matcher.
     * 
     * If no matchers are provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param jobListener the listener to receive job-related events
     * @param matcher the matcher for filtering the jobs from which to to receive events
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addJobListener(JobListener jobListener, Matcher<JobKey> matcher);

    /**
     * Add the given <code>{@link JobListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for Jobs that are matched by ANY of the
     * given Matchers.
     * 
     * If no matchers are provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param jobListener the listener to receive job-related events
     * @param matchers the matchers for filtering the jobs from which to to receive events
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addJobListener(JobListener jobListener, Matcher<JobKey> ... matchers);

    /**
     * Add the given <code>{@link JobListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for Jobs that are matched by ANY of the
     * given Matchers.
     * 
     * If no matchers are provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param jobListener the listener to receive job-related events
     * @param matchers the list of matchers for filtering the jobs from which to to receive events
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addJobListener(JobListener jobListener, List<Matcher<JobKey>> matchers);

    /**
     * Add the given Matcher to the set of matchers for which the listener
     * will receive events if ANY of the matchers match.
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @param matcher the additional matcher to apply for selecting events
     * @return true if the identified listener was found and updated
     */
    public boolean addJobListenerMatcher(String listenerName, Matcher<JobKey> matcher);

    /**
     * Remove the given Matcher to the set of matchers for which the listener
     * will receive events if ANY of the matchers match.
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @param matcher the additional matcher to apply for selecting events
     * @return true if the given matcher was found and removed from the listener's list of matchers
     */
    public boolean removeJobListenerMatcher(String listenerName, Matcher<JobKey> matcher);

    /**
     * Set the set of Matchers for which the listener
     * will receive events if ANY of the matchers match.
     * 
     * <p>Removes any existing matchers for the identified listener!</p>
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @param matchers the matchers to apply for selecting events
     * @return true if the given matcher was found and removed from the listener's list of matchers
     */
    public boolean setJobListenerMatchers(String listenerName, List<Matcher<JobKey>> matchers);

    /**
     * Get the set of Matchers for which the listener
     * will receive events if ANY of the matchers match.
     * 
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @return the matchers registered for selecting events for the identified listener
     */
    public List<Matcher<JobKey>> getJobListenerMatchers(String listenerName);

    /**
     * Remove the identified <code>{@link JobListener}</code> from the <code>Scheduler</code>.
     *
     * @param name the name of the listener to remove
     * @return true if the identified listener was found in the list, and
     *         removed.
     */
    public boolean removeJobListener(String name);

    /**
     * Get a List containing all of the <code>{@link JobListener}</code>s in
     * the <code>Scheduler</code>, in the order in which they were registered.
     *
     * @return the list of all job listeners currently registered
     */
    public List<JobListener> getJobListeners();

    /**
     * Get the <code>{@link JobListener}</code> that has the given name.
     *
     * @param name the name of the job listener to return
     * @return the named job listener
     */
    public JobListener getJobListener(String name);

    /**
     * Add the given <code>{@link TriggerListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for all Triggers.
     * 
     * Because no matcher is provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param triggerListener the trigger listener to register for all trigger events
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addTriggerListener(TriggerListener triggerListener);
    
    /**
     * Add the given <code>{@link TriggerListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for Triggers that are matched by the
     * given Matcher.
     * 
     * If no matcher is provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param triggerListener the trigger listener to register for the matched trigger events
     * @param matcher the matcher to filter the triggers for the listener
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addTriggerListener(TriggerListener triggerListener, Matcher<TriggerKey> matcher);
    
    /**
     * Add the given <code>{@link TriggerListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for Triggers that are matched by ANY of the
     * given Matchers.
     * 
     * If no matcher is provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param triggerListener the trigger listener to register for the matched trigger events
     * @param matchers the set of matchers to filter the triggers for the listener
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addTriggerListener(TriggerListener triggerListener, Matcher<TriggerKey> ... matchers);

    /**
     * Add the given <code>{@link TriggerListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for Triggers that are matched by ANY of the
     * given Matchers.
     * 
     * If no matcher is provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param triggerListener the trigger listener to register for the matched trigger events
     * @param matchers the list of matchers to filter the triggers for the listener
     *
     * @see Matcher
     * @see org.quartz.impl.matchers.EverythingMatcher
     */
    public void addTriggerListener(TriggerListener triggerListener, List<Matcher<TriggerKey>> matchers);

    /**
     * Add the given Matcher to the set of matchers for which the listener
     * will receive events if ANY of the matchers match.
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @param matcher the additional matcher to apply for selecting events
     * @return true if the identified listener was found and updated
     */
    public boolean addTriggerListenerMatcher(String listenerName, Matcher<TriggerKey> matcher);

    /**
     * Remove the given Matcher to the set of matchers for which the listener
     * will receive events if ANY of the matchers match.
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @param matcher the additional matcher to apply for selecting events
     * @return true if the given matcher was found and removed from the listener's list of matchers
     */
    public boolean removeTriggerListenerMatcher(String listenerName, Matcher<TriggerKey> matcher);

    /**
     * Set the set of Matchers for which the listener
     * will receive events if ANY of the matchers match.
     * 
     * <p>Removes any existing matchers for the identified listener!</p>
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @param matchers the matchers to apply for selecting events
     * @return true if the given matcher was found and removed from the listener's list of matchers
     */
    public boolean setTriggerListenerMatchers(String listenerName, List<Matcher<TriggerKey>> matchers);

    /**
     * Get the set of Matchers for which the listener
     * will receive events if ANY of the matchers match.
     * 
     *  
     * @param listenerName the name of the listener to add the matcher to
     * @return the matchers registered for selecting events for the identified listener
     */
    public List<Matcher<TriggerKey>> getTriggerListenerMatchers(String listenerName);

    /**
     * Remove the identified <code>{@link TriggerListener}</code> from the <code>Scheduler</code>.
     *
     * @param name the name of the trigger listener to remove
     * @return true if the identified listener was found in the list, and
     *         removed.
     */
    public boolean removeTriggerListener(String name);

    /**
     * Get a List containing all of the <code>{@link TriggerListener}</code>s 
     * in the <code>Scheduler</code>, in the order in which they were registered.
     *
     * @return the list of all trigger listeners that are registered
     */
    public List<TriggerListener> getTriggerListeners();

    /**
     * Get the <code>{@link TriggerListener}</code> that has the given name.
     *
     * @param name the name of the trigger listener to return
     * @return the named trigger listener
     */
    public TriggerListener getTriggerListener(String name);

    /**
     * Register the given <code>{@link SchedulerListener}</code> with the
     * <code>Scheduler</code>.
     *
     * @param schedulerListener the scheduler listener to register
     */
    public void addSchedulerListener(SchedulerListener schedulerListener);

    /**
     * Remove the given <code>{@link SchedulerListener}</code> from the
     * <code>Scheduler</code>.
     *
     * @param schedulerListener the scheduler listener to remove
     * @return true if the identified listener was found in the list, and
     *         removed.
     */
    public boolean removeSchedulerListener(SchedulerListener schedulerListener);

    /**
     * Get a List containing all of the <code>{@link SchedulerListener}</code>s
     * registered with the <code>Scheduler</code>, in the order in which they were registered.
     *
     * @return the list all all schedule listeners currently registered
     */
    public List<SchedulerListener> getSchedulerListeners();

}
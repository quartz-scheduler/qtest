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
package org.quartz.impl.matchers;

import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.utils.Key;

/**
 * Matches on group (ignores name) property of Keys.
 *  
 * @author jhouse
 */
public class GroupMatcher<T extends Key<?>> extends StringMatcher<T> {
  
    private static final long serialVersionUID = -3275767650469343849L;

    protected GroupMatcher(String compareTo, StringOperatorName compareWith) {
        super(compareTo, compareWith);
    }
    
    /**
     * Create a GroupMatcher that matches groups equaling the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupEquals(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.EQUALS);
    }

    /**
     * Create a GroupMatcher that matches job groups equaling the given string.
     */
    public static GroupMatcher<JobKey> jobGroupEquals(String compareTo) {
        return GroupMatcher.groupEquals(compareTo);
    }
    
    /**
     * Create a GroupMatcher that matches trigger groups equaling the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupEquals(String compareTo) {
        return GroupMatcher.groupEquals(compareTo);
    }
    
    /**
     * Create a GroupMatcher that matches groups starting with the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupStartsWith(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.STARTS_WITH);
    }

    /**
     * Create a GroupMatcher that matches job groups starting with the given string.
     */
    public static GroupMatcher<JobKey> jobGroupStartsWith(String compareTo) {
        return GroupMatcher.groupStartsWith(compareTo);
    }
    
    /**
     * Create a GroupMatcher that matches trigger groups starting with the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupStartsWith(String compareTo) {
        return GroupMatcher.groupStartsWith(compareTo);
    }

    /**
     * Create a GroupMatcher that matches groups ending with the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupEndsWith(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.ENDS_WITH);
    }

    /**
     * Create a GroupMatcher that matches job groups ending with the given string.
     */
    public static GroupMatcher<JobKey> jobGroupEndsWith(String compareTo) {
        return GroupMatcher.groupEndsWith(compareTo);
    }
    
    /**
     * Create a GroupMatcher that matches trigger groups ending with the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupEndsWith(String compareTo) {
        return GroupMatcher.groupEndsWith(compareTo);
    }
    
    /**
     * Create a GroupMatcher that matches groups containing the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupContains(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.CONTAINS);
    }

    /**
     * Create a GroupMatcher that matches job groups containing the given string.
     */
    public static GroupMatcher<JobKey> jobGroupContains(String compareTo) {
        return GroupMatcher.groupContains(compareTo);
    }
    
    /**
     * Create a GroupMatcher that matches trigger groups containing the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupContains(String compareTo) {
        return GroupMatcher.groupContains(compareTo);
    }

    /**
     * Create a GroupMatcher that matches all groups.
     */
    public static <T extends Key<T>> GroupMatcher<T> anyGroup() {
        return new GroupMatcher<T>("", StringOperatorName.ANYTHING);
    }

    /**
     * Create a GroupMatcher that matches all job groups.
     */
    public static GroupMatcher<JobKey> anyJobGroup() {
        return GroupMatcher.anyGroup();
    }

    /**
     * Create a GroupMatcher that matches all trigger groups.
     */
    public static GroupMatcher<TriggerKey> anyTriggerGroup() {
        return GroupMatcher.anyGroup();
    }

    @Override
    protected String getValue(T key) {
        return key.getGroup();
    }

}

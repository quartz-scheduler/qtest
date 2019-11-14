
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
 * An exception that is thrown to indicate that there has been a failure in the
 * scheduler's underlying persistence mechanism.
 * 
 * @author James House
 */
public class JobPersistenceException extends SchedulerException {
  
    private static final long serialVersionUID = -8924958757341995694L;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a <code>JobPersistenceException</code> with the given message.
     * </p>
     *
     * @param msg a textual description of the persistence mechanism failure
     */
    public JobPersistenceException(String msg) {
        super(msg);
    }


    /**
     * <p>
     * Create a <code>JobPersistenceException</code> with the given message
     * and cause.
     * </p>
     *
     * @param msg a textual description of the persistence mechanism failure
     * @param cause the cause of the persistence mechanism failure
     */
    public JobPersistenceException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

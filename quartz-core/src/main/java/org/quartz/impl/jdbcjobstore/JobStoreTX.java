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

package org.quartz.impl.jdbcjobstore;

import java.sql.Connection;

import org.quartz.JobPersistenceException;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerSignaler;

/**
 * <p>
 * <code>JobStoreTX</code> is meant to be used in a standalone environment.
 * Both commit and rollback will be handled by this class.
 * </p>
 * 
 * <p>
 * If you need a <code>{@link org.quartz.spi.JobStore}</code> class to use
 * within an application-server environment, use <code>{@link
 * org.quartz.impl.jdbcjobstore.JobStoreCMT}</code>
 * instead.
 * </p>
 * 
 * @author <a href="mailto:jeff@binaryfeed.org">Jeffrey Wescott</a>
 * @author James House
 */
public class JobStoreTX extends JobStoreSupport {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    @Override
    public void initialize(ClassLoadHelper classLoadHelper,
            SchedulerSignaler schedSignaler) throws SchedulerConfigException {

        super.initialize(classLoadHelper, schedSignaler);

        getLog().info("JobStoreTX initialized.");
    }

    /**
     * For <code>JobStoreTX</code>, the non-managed TX connection is just 
     * the normal connection because it is not CMT.
     * 
     * @see JobStoreSupport#getConnection()
     */
    @Override
    protected Connection getNonManagedTXConnection()
        throws JobPersistenceException {
        return getConnection();
    }
    
    /**
     * Execute the given callback having optionally aquired the given lock.
     * For <code>JobStoreTX</code>, because it manages its own transactions
     * and only has the one datasource, this is the same behavior as 
     * executeInNonManagedTXLock().
     * 
     * @param lockName The name of the lock to aquire, for example 
     * "TRIGGER_ACCESS".  If null, then no lock is aquired, but the
     * lockCallback is still executed in a transaction.
     * 
     * @see JobStoreSupport#executeInNonManagedTXLock(String, TransactionCallback, TransactionValidator)
     * @see JobStoreCMT#executeInLock(String, TransactionCallback)
     * @see JobStoreSupport#getNonManagedTXConnection()
     * @see JobStoreSupport#getConnection()
     */
    @Override
    protected Object executeInLock(
            String lockName, 
            TransactionCallback txCallback) throws JobPersistenceException {
        return executeInNonManagedTXLock(lockName, txCallback, null);
    }
}
// EOF

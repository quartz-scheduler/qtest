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
import java.sql.SQLException;

import org.quartz.JobPersistenceException;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.SerializationHelper;
import org.quartz.utils.DBConnectionManager;

/**
 * <p>
 * <code>JobStoreCMT</code> is meant to be used in an application-server
 * environment that provides container-managed-transactions. No commit /
 * rollback will be1 handled by this class.
 * </p>
 * 
 * <p>
 * If you need commit / rollback, use <code>{@link
 * org.quartz.impl.jdbcjobstore.JobStoreTX}</code>
 * instead.
 * </p>
 * 
 * @author <a href="mailto:jeff@binaryfeed.org">Jeffrey Wescott</a>
 * @author James House
 * @author Srinivas Venkatarangaiah
 *  
 */
public class JobStoreCMT extends JobStoreSupport {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected String nonManagedTxDsName;

    // Great name huh?
    protected boolean dontSetNonManagedTXConnectionAutoCommitFalse = false;

    
    protected boolean setTxIsolationLevelReadCommitted = false;
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Set the name of the <code>DataSource</code> that should be used for
     * performing database functions.
     * </p>
     */
    public void setNonManagedTXDataSource(String nonManagedTxDsName) {
        this.nonManagedTxDsName = nonManagedTxDsName;
    }

    /**
     * <p>
     * Get the name of the <code>DataSource</code> that should be used for
     * performing database functions.
     * </p>
     */
    public String getNonManagedTXDataSource() {
        return nonManagedTxDsName;
    }

    public boolean isDontSetNonManagedTXConnectionAutoCommitFalse() {
        return dontSetNonManagedTXConnectionAutoCommitFalse;
    }

    /**
     * Don't call set autocommit(false) on connections obtained from the
     * DataSource. This can be helpfull in a few situations, such as if you
     * have a driver that complains if it is called when it is already off.
     * 
     * @param b
     */
    public void setDontSetNonManagedTXConnectionAutoCommitFalse(boolean b) {
        dontSetNonManagedTXConnectionAutoCommitFalse = b;
    }


    public boolean isTxIsolationLevelReadCommitted() {
        return setTxIsolationLevelReadCommitted;
    }

    /**
     * Set the transaction isolation level of DB connections to sequential.
     * 
     * @param b
     */
    public void setTxIsolationLevelReadCommitted(boolean b) {
        setTxIsolationLevelReadCommitted = b;
    }
    

    @Override
    public void initialize(ClassLoadHelper loadHelper,
            SchedulerSignaler signaler, SerializationHelper serializationHelper) throws SchedulerConfigException {

        if (nonManagedTxDsName == null) {
            throw new SchedulerConfigException(
                "Non-ManagedTX DataSource name not set!  " +
                "If your 'org.quartz.jobStore.dataSource' is XA, then set " + 
                "'org.quartz.jobStore.nonManagedTXDataSource' to a non-XA "+ 
                "datasource (for the same DB).  " + 
                "Otherwise, you can set them to be the same.");
        }

        if (getLockHandler() == null) {
            // If the user hasn't specified an explicit lock handler, 
            // then we *must* use DB locks with CMT...
            setUseDBLocks(true);
        }

        super.initialize(loadHelper, signaler, serializationHelper);

        getLog().info("JobStoreCMT initialized.");
    }
    
    @Override
    public void shutdown() {

        super.shutdown();
        
        try {
            DBConnectionManager.getInstance().shutdown(getNonManagedTXDataSource());
        } catch (SQLException sqle) {
            getLog().warn("Database connection shutdown unsuccessful.", sqle);
        }
    }

    @Override
    protected Connection getNonManagedTXConnection()
        throws JobPersistenceException {
        Connection conn = null;
        try {
            conn = DBConnectionManager.getInstance().getConnection(
                    getNonManagedTXDataSource());
        } catch (SQLException sqle) {
            throw new JobPersistenceException(
                "Failed to obtain DB connection from data source '"
                        + getNonManagedTXDataSource() + "': "
                        + sqle.toString(), sqle);
        } catch (Throwable e) {
            throw new JobPersistenceException(
                "Failed to obtain DB connection from data source '"
                        + getNonManagedTXDataSource() + "': "
                        + e.toString(), e);
        }

        if (conn == null) { 
            throw new JobPersistenceException(
                "Could not get connection from DataSource '"
                        + getNonManagedTXDataSource() + "'"); 
        }

        // Protect connection attributes we might change.
        conn = getAttributeRestoringConnection(conn);
        
        // Set any connection connection attributes we are to override.
        try {
            if (!isDontSetNonManagedTXConnectionAutoCommitFalse()) {
                conn.setAutoCommit(false);
            }
            
            if (isTxIsolationLevelReadCommitted()) {
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
        } catch (SQLException sqle) {
            getLog().warn("Failed to override connection auto commit/transaction isolation.", sqle);
        } catch (Throwable e) {
            try { conn.close(); } catch(Throwable tt) {}
            
            throw new JobPersistenceException(
                "Failure setting up connection.", e);
        }
        
        return conn;
    }
    
    /**
     * Execute the given callback having optionally acquired the given lock.  
     * Because CMT assumes that the connection is already part of a managed
     * transaction, it does not attempt to commit or rollback the 
     * enclosing transaction.
     * 
     * @param lockName The name of the lock to acquire, for example 
     * "TRIGGER_ACCESS".  If null, then no lock is acquired, but the
     * txCallback is still executed in a transaction.
     * 
     * @see JobStoreSupport#executeInNonManagedTXLock(String, TransactionCallback)
     * @see JobStoreTX#executeInLock(String, TransactionCallback)
     * @see JobStoreSupport#getNonManagedTXConnection()
     * @see JobStoreSupport#getConnection()
     */
    @Override
    protected Object executeInLock(
            String lockName, 
            TransactionCallback txCallback) throws JobPersistenceException {
        boolean transOwner = false;
        Connection conn = null;
        try {
            if (lockName != null) {
                // If we aren't using db locks, then delay getting DB connection 
                // until after acquiring the lock since it isn't needed.
                if (getLockHandler().requiresConnection()) {
                    conn = getConnection();
                }
                
                transOwner = getLockHandler().obtainLock(conn, lockName);
            }

            if (conn == null) {
                conn = getConnection();
            }

            return txCallback.execute(conn);
        } finally {
            try {
                releaseLock(lockName, transOwner);
            } finally {
                cleanupConnection(conn);
            }
        }
    }
}

// EOF

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
package org.quartz.impl.jdbcjobstore;

import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.NotSerializableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.quartz.JobPersistenceException;
import org.quartz.TriggerKey;
import org.quartz.spi.OperableTrigger;
import org.slf4j.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.simpl.SimpleClassLoadHelper;

import junit.framework.TestCase;
import org.quartz.simpl.ObjectStreamSerializationImpl;

public class StdJDBCDelegateTest extends TestCase {

    public void testSerializeJobData() throws IOException, NoSuchDelegateException {
        StdJDBCDelegate delegate = new StdJDBCDelegate();
        delegate.initialize(LoggerFactory.getLogger(getClass()), "QRTZ_", "TESTSCHED", "INSTANCE", new SimpleClassLoadHelper(), 
                false, "", new ObjectStreamSerializationImpl());
        
        JobDataMap jdm = new JobDataMap();
        delegate.serializeJobData(jdm).close();

        jdm.clear();
        jdm.put("key", "value");
        jdm.put("key2", null);
        delegate.serializeJobData(jdm).close();

        jdm.clear();
        jdm.put("key1", "value");
        jdm.put("key2", null);
        jdm.put("key3", new Object());
        try {
            delegate.serializeJobData(jdm);
            fail();
        } catch (NotSerializableException e) {
            assertTrue(e.getMessage().indexOf("key3") >= 0);
        }
    }

    public void testSelectBlobTriggerWithNoBlobContent() throws JobPersistenceException, SQLException, IOException, ClassNotFoundException {
        StdJDBCDelegate jdbcDelegate = new StdJDBCDelegate();
        jdbcDelegate.initialize(LoggerFactory.getLogger(getClass()), "QRTZ_", "TESTSCHED", "INSTANCE", new SimpleClassLoadHelper(), 
                false, "", new ObjectStreamSerializationImpl());

        Connection conn = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // First result set has results, second has none
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString(Constants.COL_TRIGGER_TYPE)).thenReturn(Constants.TTYPE_BLOB);

        OperableTrigger trigger = jdbcDelegate.selectTrigger(conn, TriggerKey.triggerKey("test"));
        assertNull(trigger);

    }

    public void testSelectSimpleTriggerWithExceptionWithExtendedProps() throws SQLException, JobPersistenceException, IOException, ClassNotFoundException {
        TriggerPersistenceDelegate persistenceDelegate = mock(TriggerPersistenceDelegate.class);
        IllegalStateException exception = new IllegalStateException();
        when(persistenceDelegate.loadExtendedTriggerProperties(any(Connection.class), any(TriggerKey.class))).thenThrow(exception);

        StdJDBCDelegate jdbcDelegate = new TestStdJDBCDelegate(persistenceDelegate);
        jdbcDelegate.initialize(LoggerFactory.getLogger(getClass()), "QRTZ_", "TESTSCHED", "INSTANCE", new SimpleClassLoadHelper(), 
                false, "", new ObjectStreamSerializationImpl());

        Connection conn = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Mock basic trigger data
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(Constants.COL_TRIGGER_TYPE)).thenReturn(Constants.TTYPE_SIMPLE);

        try {
            jdbcDelegate.selectTrigger(conn, TriggerKey.triggerKey("test"));
            fail("Trigger selection should result in exception");
        } catch (IllegalStateException e) {
            assertSame(exception, e);
        }
        verify(persistenceDelegate).loadExtendedTriggerProperties(any(Connection.class), any(TriggerKey.class));

    }

    public void testSelectSimpleTriggerWithDeleteBeforeSelectExtendedProps() throws JobPersistenceException, ClassNotFoundException, SQLException, IOException {
        TriggerPersistenceDelegate persistenceDelegate = mock(TriggerPersistenceDelegate.class);
        when(persistenceDelegate.loadExtendedTriggerProperties(any(Connection.class), any(TriggerKey.class))).thenThrow(new IllegalStateException());

        StdJDBCDelegate jdbcDelegate = new TestStdJDBCDelegate(persistenceDelegate);
        jdbcDelegate.initialize(LoggerFactory.getLogger(getClass()), "QRTZ_", "TESTSCHED", "INSTANCE", new SimpleClassLoadHelper(), 
                false, "", new ObjectStreamSerializationImpl());

        Connection conn = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // First result set has results, second has none
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString(Constants.COL_TRIGGER_TYPE)).thenReturn(Constants.TTYPE_SIMPLE);

        OperableTrigger trigger = jdbcDelegate.selectTrigger(conn, TriggerKey.triggerKey("test"));
        assertNull(trigger);
        verify(persistenceDelegate).loadExtendedTriggerProperties(any(Connection.class), any(TriggerKey.class));
    }

    public void testSelectTriggerToAcquireHonorsMaxCount() throws SQLException {

        StdJDBCDelegate jdbcDelegate = new StdJDBCDelegate();

        Connection conn = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(anyString())).thenReturn("test");

        List<TriggerKey> triggerKeys = jdbcDelegate.selectTriggerToAcquire(conn, Long.MAX_VALUE, Long.MIN_VALUE, 10);

        assertThat(triggerKeys, iterableWithSize(10));
    }

    static class TestStdJDBCDelegate extends StdJDBCDelegate {

        private final TriggerPersistenceDelegate testDelegate;

        public TestStdJDBCDelegate(TriggerPersistenceDelegate testDelegate) {
            this.testDelegate = testDelegate;
        }

        @Override
        public TriggerPersistenceDelegate findTriggerPersistenceDelegate(String discriminator) {
            return testDelegate;
        }
    }

}

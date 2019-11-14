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

package org.quartz.utils;

import com.zaxxer.hikari.HikariDataSource;
import org.quartz.SchedulerException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>
 * A <code>ConnectionProvider</code> implementation that creates its own
 * pool of connections.
 * </p>
 *
 * <p>
 * This class uses HikariCP (https://brettwooldridge.github.io/HikariCP/) as
 * the underlying pool implementation.</p>
 *
 * @see DBConnectionManager
 * @see ConnectionProvider
 *
 * @author Ludovic Orban
 */
public class HikariCpPoolingConnectionProvider implements PoolingConnectionProvider {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constants.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /** This pooling provider name. */
    public static final String POOLING_PROVIDER_NAME = "hikaricp";

    /** Discard connections after they have been idle this many seconds.  0 disables the feature. Default is 0.*/
    private static final String DB_DISCARD_IDLE_CONNECTIONS_SECONDS = "discardIdleConnectionsSeconds";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Data members.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private HikariDataSource datasource;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constructors.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public HikariCpPoolingConnectionProvider(String dbDriver, String dbURL,
                                             String dbUser, String dbPassword, int maxConnections,
                                             String dbValidationQuery) throws SQLException, SchedulerException {
        initialize(
                dbDriver, dbURL, dbUser, dbPassword,
                maxConnections, dbValidationQuery, 0);
    }

    /**
     * Create a connection pool using the given properties.
     *
     * <p>
     * The properties passed should contain:
     *
     * <UL>
     * <LI>{@link #DB_DRIVER}- The database driver class name
     * <LI>{@link #DB_URL}- The database URL
     * <LI>{@link #DB_USER}- The database user
     * <LI>{@link #DB_PASSWORD}- The database password
     * <LI>{@link #DB_MAX_CONNECTIONS}- The maximum # connections in the pool,
     * optional
     * <LI>{@link #DB_VALIDATION_QUERY}- The sql validation query, optional
     * </UL>
     *
     * @param config
     *            configuration properties
     */
    public HikariCpPoolingConnectionProvider(Properties config) throws SchedulerException, SQLException {
        PropertiesParser cfg = new PropertiesParser(config);
        initialize(
                cfg.getStringProperty(DB_DRIVER),
                cfg.getStringProperty(DB_URL),
                cfg.getStringProperty(DB_USER, ""),
                cfg.getStringProperty(DB_PASSWORD, ""),
                cfg.getIntProperty(DB_MAX_CONNECTIONS, DEFAULT_DB_MAX_CONNECTIONS),
                cfg.getStringProperty(DB_VALIDATION_QUERY),
                cfg.getIntProperty(DB_DISCARD_IDLE_CONNECTIONS_SECONDS, 0));
    }
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Create the underlying C3PO ComboPooledDataSource with the 
     * default supported properties.
     * @throws SchedulerException
     */
    private void initialize(
            String dbDriver,
            String dbURL,
            String dbUser,
            String dbPassword,
            int maxConnections,
            String dbValidationQuery,
            int maxIdleSeconds) throws SQLException, SchedulerException {
        if (dbURL == null) {
            throw new SQLException(
                    "DBPool could not be created: DB URL cannot be null");
        }

        if (dbDriver == null) {
            throw new SQLException(
                    "DBPool '" + dbURL + "' could not be created: " +
                            "DB driver class name cannot be null!");
        }

        if (maxConnections < 0) {
            throw new SQLException(
                    "DBPool '" + dbURL + "' could not be created: " +
                            "Max connections must be greater than zero!");
        }


        datasource = new HikariDataSource();
        datasource.setDriverClassName(dbDriver);
        datasource.setJdbcUrl(dbURL);
        datasource.setUsername(dbUser);
        datasource.setPassword(dbPassword);
        datasource.setMaximumPoolSize(maxConnections);
        datasource.setIdleTimeout(maxIdleSeconds);

        if (dbValidationQuery != null) {
            datasource.setConnectionTestQuery(dbValidationQuery);
        }
    }

    /**
     * Get the HikariCP HikariDataSource created during initialization.
     *
     * <p>
     * This can be used to set additional data source properties in a 
     * subclass's constructor.
     * </p>
     */
    public HikariDataSource getDataSource() {
        return datasource;
    }

    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    public void shutdown() throws SQLException {
        datasource.close();
    }

    public void initialize() throws SQLException {
        // do nothing, already initialized during constructor call
    }
}

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

import org.quartz.Trigger;

/**
 * <p>
 * This interface extends <code>{@link
 * org.quartz.impl.jdbcjobstore.Constants}</code>
 * to include the query string constants in use by the <code>{@link
 * org.quartz.impl.jdbcjobstore.StdJDBCDelegate}</code>
 * class.
 * </p>
 * 
 * @author <a href="mailto:jeff@binaryfeed.org">Jeffrey Wescott</a>
 */
public interface StdJDBCConstants extends Constants {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    // table prefix substitution string
    String TABLE_PREFIX_SUBST = "{0}";

    // table prefix substitution string
    String SCHED_NAME_SUBST = "{1}";

    // QUERIES
    String UPDATE_TRIGGER_STATES_FROM_OTHER_STATES = "UPDATE "
            + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS
            + " SET "
            + COL_TRIGGER_STATE
            + " = ?"
            + " WHERE "
            + COL_SCHEDULER_NAME 
            + " = " + SCHED_NAME_SUBST + " AND ("
            + COL_TRIGGER_STATE
            + " = ? OR "
            + COL_TRIGGER_STATE + " = ?)";

    String SELECT_MISFIRED_TRIGGERS = "SELECT * FROM "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
        + " AND NOT ("
        + COL_MISFIRE_INSTRUCTION + " = " + Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY + ") AND " 
        + COL_NEXT_FIRE_TIME + " < ? "
        + "ORDER BY " + COL_NEXT_FIRE_TIME + " ASC, " + COL_PRIORITY + " DESC";
    
    String SELECT_TRIGGERS_IN_STATE = "SELECT "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST + " AND "
            + COL_TRIGGER_STATE + " = ?";

    String SELECT_MISFIRED_TRIGGERS_IN_STATE = "SELECT "
        + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST + " AND NOT ("
        + COL_MISFIRE_INSTRUCTION + " = " + Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY + ") AND " 
        + COL_NEXT_FIRE_TIME + " < ? AND " + COL_TRIGGER_STATE + " = ? "
        + "ORDER BY " + COL_NEXT_FIRE_TIME + " ASC, " + COL_PRIORITY + " DESC";

    String COUNT_MISFIRED_TRIGGERS_IN_STATE = "SELECT COUNT("
        + COL_TRIGGER_NAME + ") FROM "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST + " AND NOT ("
        + COL_MISFIRE_INSTRUCTION + " = " + Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY + ") AND " 
        + COL_NEXT_FIRE_TIME + " < ? " 
        + "AND " + COL_TRIGGER_STATE + " = ?";
    
    String SELECT_HAS_MISFIRED_TRIGGERS_IN_STATE = "SELECT "
        + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST + " AND NOT ("
        + COL_MISFIRE_INSTRUCTION + " = " + Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY + ") AND " 
        + COL_NEXT_FIRE_TIME + " < ? " 
        + "AND " + COL_TRIGGER_STATE + " = ? "
        + "ORDER BY " + COL_NEXT_FIRE_TIME + " ASC, " + COL_PRIORITY + " DESC";

    String SELECT_MISFIRED_TRIGGERS_IN_GROUP_IN_STATE = "SELECT "
        + COL_TRIGGER_NAME
        + " FROM "
        + TABLE_PREFIX_SUBST
        + TABLE_TRIGGERS
        + " WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST + " AND NOT ("
        + COL_MISFIRE_INSTRUCTION + " = " + Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY + ") AND " 
        + COL_NEXT_FIRE_TIME
        + " < ? AND "
        + COL_TRIGGER_GROUP
        + " = ? AND " + COL_TRIGGER_STATE + " = ? "
        + "ORDER BY " + COL_NEXT_FIRE_TIME + " ASC, " + COL_PRIORITY + " DESC";


    String DELETE_FIRED_TRIGGERS = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String INSERT_JOB_DETAIL = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " (" 
            + COL_SCHEDULER_NAME + ", " + COL_JOB_NAME
            + ", " + COL_JOB_GROUP + ", " + COL_DESCRIPTION + ", "
            + COL_JOB_CLASS + ", " + COL_IS_DURABLE + ", " 
            + COL_IS_NONCONCURRENT +  ", " + COL_IS_UPDATE_DATA + ", " 
            + COL_REQUESTS_RECOVERY + ", "
            + COL_JOB_DATAMAP + ") " + " VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String UPDATE_JOB_DETAIL = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " SET "
            + COL_DESCRIPTION + " = ?, " + COL_JOB_CLASS + " = ?, "
            + COL_IS_DURABLE + " = ?, " 
            + COL_IS_NONCONCURRENT + " = ?, " + COL_IS_UPDATE_DATA + " = ?, " 
            + COL_REQUESTS_RECOVERY + " = ?, "
            + COL_JOB_DATAMAP + " = ? " + " WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    String SELECT_TRIGGERS_FOR_JOB = "SELECT "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    String SELECT_TRIGGERS_FOR_CALENDAR = "SELECT "
        + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE " 
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
        + " AND " + COL_CALENDAR_NAME
        + " = ?";

    String DELETE_JOB_DETAIL = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    String SELECT_JOB_NONCONCURRENT = "SELECT "
            + COL_IS_NONCONCURRENT + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_JOB_DETAILS + " WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    String SELECT_JOB_EXISTENCE = "SELECT " + COL_JOB_NAME
            + " FROM " + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    String UPDATE_JOB_DATA = "UPDATE " + TABLE_PREFIX_SUBST
            + TABLE_JOB_DETAILS + " SET " + COL_JOB_DATAMAP + " = ? "
            + " WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    String SELECT_JOB_DETAIL = "SELECT *" + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";
            

    String SELECT_NUM_JOBS = "SELECT COUNT(" + COL_JOB_NAME
            + ") " + " FROM " + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String SELECT_JOB_GROUPS = "SELECT DISTINCT("
            + COL_JOB_GROUP + ") FROM " + TABLE_PREFIX_SUBST
            + TABLE_JOB_DETAILS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String SELECT_JOBS_IN_GROUP_LIKE = "SELECT " + COL_JOB_NAME + ", " + COL_JOB_GROUP
            + " FROM " + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_GROUP + " LIKE ?";

    String SELECT_JOBS_IN_GROUP = "SELECT " + COL_JOB_NAME + ", " + COL_JOB_GROUP
            + " FROM " + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_GROUP + " = ?";

    String INSERT_TRIGGER = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " (" + COL_SCHEDULER_NAME + ", " + COL_TRIGGER_NAME
            + ", " + COL_TRIGGER_GROUP + ", " + COL_JOB_NAME + ", "
            + COL_JOB_GROUP + ", " + COL_DESCRIPTION
            + ", " + COL_NEXT_FIRE_TIME + ", " + COL_PREV_FIRE_TIME + ", "
            + COL_TRIGGER_STATE + ", " + COL_TRIGGER_TYPE + ", "
            + COL_START_TIME + ", " + COL_END_TIME + ", " + COL_CALENDAR_NAME
            + ", " + COL_MISFIRE_INSTRUCTION + ", " + COL_JOB_DATAMAP + ", " + COL_PRIORITY + ") "
            + " VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String INSERT_SIMPLE_TRIGGER = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_SIMPLE_TRIGGERS + " ("
            + COL_SCHEDULER_NAME + ", "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + ", "
            + COL_REPEAT_COUNT + ", " + COL_REPEAT_INTERVAL + ", "
            + COL_TIMES_TRIGGERED + ") " + " VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?, ?, ?)";

    String INSERT_CRON_TRIGGER = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_CRON_TRIGGERS + " ("
            + COL_SCHEDULER_NAME + ", "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + ", "
            + COL_CRON_EXPRESSION + ", " + COL_TIME_ZONE_ID + ") "
            + " VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?, ?)";

    String INSERT_BLOB_TRIGGER = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_BLOB_TRIGGERS + " ("
            + COL_SCHEDULER_NAME + ", "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + ", " + COL_BLOB
            + ") " + " VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?)";

    String UPDATE_TRIGGER_SKIP_DATA = "UPDATE " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " SET " + COL_JOB_NAME + " = ?, "
            + COL_JOB_GROUP + " = ?, " 
            + COL_DESCRIPTION + " = ?, " + COL_NEXT_FIRE_TIME + " = ?, "
            + COL_PREV_FIRE_TIME + " = ?, " + COL_TRIGGER_STATE + " = ?, "
            + COL_TRIGGER_TYPE + " = ?, " + COL_START_TIME + " = ?, "
            + COL_END_TIME + " = ?, " + COL_CALENDAR_NAME + " = ?, "
            + COL_MISFIRE_INSTRUCTION + " = ?, " + COL_PRIORITY 
            + " = ? WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME
            + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String UPDATE_TRIGGER = "UPDATE " + TABLE_PREFIX_SUBST
        + TABLE_TRIGGERS + " SET " + COL_JOB_NAME + " = ?, "
        + COL_JOB_GROUP + " = ?, "
        + COL_DESCRIPTION + " = ?, " + COL_NEXT_FIRE_TIME + " = ?, "
        + COL_PREV_FIRE_TIME + " = ?, " + COL_TRIGGER_STATE + " = ?, "
        + COL_TRIGGER_TYPE + " = ?, " + COL_START_TIME + " = ?, "
        + COL_END_TIME + " = ?, " + COL_CALENDAR_NAME + " = ?, "
        + COL_MISFIRE_INSTRUCTION + " = ?, " + COL_PRIORITY + " = ?, " 
        + COL_JOB_DATAMAP + " = ? WHERE " 
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
        + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";
    
    String UPDATE_SIMPLE_TRIGGER = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_SIMPLE_TRIGGERS + " SET "
            + COL_REPEAT_COUNT + " = ?, " + COL_REPEAT_INTERVAL + " = ?, "
            + COL_TIMES_TRIGGERED + " = ? WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME
            + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String UPDATE_CRON_TRIGGER = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_CRON_TRIGGERS + " SET "
            + COL_CRON_EXPRESSION + " = ?, " + COL_TIME_ZONE_ID  
            + " = ? WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME
            + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String UPDATE_BLOB_TRIGGER = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_BLOB_TRIGGERS + " SET " + COL_BLOB
            + " = ? WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND "
            + COL_TRIGGER_GROUP + " = ?";

    String SELECT_TRIGGER_EXISTENCE = "SELECT "
            + COL_TRIGGER_NAME + " FROM " + TABLE_PREFIX_SUBST + TABLE_TRIGGERS
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP
            + " = ?";

    String UPDATE_TRIGGER_STATE = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " SET " + COL_TRIGGER_STATE
            + " = ?" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND "
            + COL_TRIGGER_GROUP + " = ?";

    String UPDATE_TRIGGER_STATE_FROM_STATE = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " SET " + COL_TRIGGER_STATE
            + " = ?" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND "
            + COL_TRIGGER_GROUP + " = ? AND " + COL_TRIGGER_STATE + " = ?";

    String UPDATE_TRIGGER_GROUP_STATE_FROM_STATE = "UPDATE "
            + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS
            + " SET "
            + COL_TRIGGER_STATE
            + " = ?"
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP
            + " LIKE ? AND "
            + COL_TRIGGER_STATE + " = ?";

    String UPDATE_TRIGGER_STATE_FROM_STATES = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " SET " + COL_TRIGGER_STATE
            + " = ?" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND "
            + COL_TRIGGER_GROUP + " = ? AND (" + COL_TRIGGER_STATE + " = ? OR "
            + COL_TRIGGER_STATE + " = ? OR " + COL_TRIGGER_STATE + " = ?)";

    String UPDATE_TRIGGER_GROUP_STATE_FROM_STATES = "UPDATE "
            + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS
            + " SET "
            + COL_TRIGGER_STATE
            + " = ?"
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP
            + " LIKE ? AND ("
            + COL_TRIGGER_STATE
            + " = ? OR "
            + COL_TRIGGER_STATE
            + " = ? OR "
            + COL_TRIGGER_STATE + " = ?)";

    String UPDATE_JOB_TRIGGER_STATES = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " SET " + COL_TRIGGER_STATE
            + " = ? WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_NAME + " = ? AND " + COL_JOB_GROUP
            + " = ?";

    String UPDATE_JOB_TRIGGER_STATES_FROM_OTHER_STATE = "UPDATE "
            + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS
            + " SET "
            + COL_TRIGGER_STATE
            + " = ? WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_NAME
            + " = ? AND "
            + COL_JOB_GROUP
            + " = ? AND " + COL_TRIGGER_STATE + " = ?";

    String DELETE_SIMPLE_TRIGGER = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_SIMPLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String DELETE_CRON_TRIGGER = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_CRON_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String DELETE_BLOB_TRIGGER = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_BLOB_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String DELETE_TRIGGER = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_NUM_TRIGGERS_FOR_JOB = "SELECT COUNT("
            + COL_TRIGGER_NAME + ") FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_NAME + " = ? AND "
            + COL_JOB_GROUP + " = ?";

    String SELECT_JOB_FOR_TRIGGER = "SELECT J."
            + COL_JOB_NAME + ", J." + COL_JOB_GROUP + ", J." + COL_IS_DURABLE
            + ", J." + COL_JOB_CLASS + ", J." + COL_REQUESTS_RECOVERY + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " T, " + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS
            + " J WHERE T." + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND J." + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST 
            + " AND T." + COL_TRIGGER_NAME + " = ? AND T."
            + COL_TRIGGER_GROUP + " = ? AND T." + COL_JOB_NAME + " = J."
            + COL_JOB_NAME + " AND T." + COL_JOB_GROUP + " = J."
            + COL_JOB_GROUP;

    String SELECT_TRIGGER = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_TRIGGER_DATA = "SELECT " + 
            COL_JOB_DATAMAP + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";
        
    String SELECT_TRIGGER_STATE = "SELECT "
            + COL_TRIGGER_STATE + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND "
            + COL_TRIGGER_GROUP + " = ?";

    String SELECT_TRIGGER_STATUS = "SELECT "
            + COL_TRIGGER_STATE + ", " + COL_NEXT_FIRE_TIME + ", "
            + COL_JOB_NAME + ", " + COL_JOB_GROUP + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_SIMPLE_TRIGGER = "SELECT *" + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_SIMPLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_CRON_TRIGGER = "SELECT *" + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_CRON_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_BLOB_TRIGGER = "SELECT *" + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_BLOB_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_NUM_TRIGGERS = "SELECT COUNT("
            + COL_TRIGGER_NAME + ") " + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String SELECT_NUM_TRIGGERS_IN_GROUP = "SELECT COUNT("
            + COL_TRIGGER_NAME + ") " + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_TRIGGER_GROUPS = "SELECT DISTINCT("
            + COL_TRIGGER_GROUP + ") FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String SELECT_TRIGGER_GROUPS_FILTERED = "SELECT DISTINCT("
            + COL_TRIGGER_GROUP + ") FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST + " AND " + COL_TRIGGER_GROUP + " LIKE ?";

    String SELECT_TRIGGERS_IN_GROUP_LIKE = "SELECT "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM " + TABLE_PREFIX_SUBST + TABLE_TRIGGERS
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP + " LIKE ?";

    String SELECT_TRIGGERS_IN_GROUP = "SELECT "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM " + TABLE_PREFIX_SUBST + TABLE_TRIGGERS
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP + " = ?";

    String INSERT_CALENDAR = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_CALENDARS + " (" + COL_SCHEDULER_NAME + ", " + COL_CALENDAR_NAME
            + ", " + COL_CALENDAR + ") " + " VALUES(" + SCHED_NAME_SUBST + ", ?, ?)";

    String UPDATE_CALENDAR = "UPDATE " + TABLE_PREFIX_SUBST
            + TABLE_CALENDARS + " SET " + COL_CALENDAR + " = ? " + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_CALENDAR_NAME + " = ?";

    String SELECT_CALENDAR_EXISTENCE = "SELECT "
            + COL_CALENDAR_NAME + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_CALENDARS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_CALENDAR_NAME + " = ?";

    String SELECT_CALENDAR = "SELECT *" + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_CALENDARS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_CALENDAR_NAME + " = ?";

    String SELECT_REFERENCED_CALENDAR = "SELECT "
            + COL_CALENDAR_NAME + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_CALENDAR_NAME + " = ?";

    String DELETE_CALENDAR = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_CALENDARS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_CALENDAR_NAME + " = ?";

    String SELECT_NUM_CALENDARS = "SELECT COUNT("
            + COL_CALENDAR_NAME + ") " + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_CALENDARS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String SELECT_CALENDARS = "SELECT " + COL_CALENDAR_NAME
            + " FROM " + TABLE_PREFIX_SUBST + TABLE_CALENDARS
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String SELECT_NEXT_FIRE_TIME = "SELECT MIN("
            + COL_NEXT_FIRE_TIME + ") AS " + ALIAS_COL_NEXT_FIRE_TIME
            + " FROM " + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_STATE + " = ? AND " + COL_NEXT_FIRE_TIME + " >= 0";

    String SELECT_TRIGGER_FOR_FIRE_TIME = "SELECT "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_STATE + " = ? AND " + COL_NEXT_FIRE_TIME + " = ?";

    String SELECT_NEXT_TRIGGER_TO_ACQUIRE = "SELECT "
        + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + ", "
        + COL_NEXT_FIRE_TIME + ", " + COL_PRIORITY + " FROM "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
        + " AND " + COL_TRIGGER_STATE + " = ? AND " + COL_NEXT_FIRE_TIME + " <= ? " 
        + "AND (" + COL_MISFIRE_INSTRUCTION + " = -1 OR (" +COL_MISFIRE_INSTRUCTION+ " <> -1 AND "+ COL_NEXT_FIRE_TIME + " >= ?)) "
        + "ORDER BY "+ COL_NEXT_FIRE_TIME + " ASC, " + COL_PRIORITY + " DESC";
    
    
    String INSERT_FIRED_TRIGGER = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS + " (" + COL_SCHEDULER_NAME + ", " + COL_ENTRY_ID
            + ", " + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + ", "
            + COL_INSTANCE_NAME + ", "
            + COL_FIRED_TIME + ", " + COL_SCHED_TIME + ", " + COL_ENTRY_STATE + ", " + COL_JOB_NAME
            + ", " + COL_JOB_GROUP + ", " + COL_IS_NONCONCURRENT + ", "
            + COL_REQUESTS_RECOVERY + ", " + COL_PRIORITY
            + ") VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String UPDATE_FIRED_TRIGGER = "UPDATE "
        + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS + " SET " 
        + COL_INSTANCE_NAME + " = ?, "
        + COL_FIRED_TIME + " = ?, " + COL_SCHED_TIME + " = ?, " + COL_ENTRY_STATE + " = ?, " + COL_JOB_NAME
        + " = ?, " + COL_JOB_GROUP + " = ?, " + COL_IS_NONCONCURRENT + " = ?, "
        + COL_REQUESTS_RECOVERY + " = ? WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
        + " AND " + COL_ENTRY_ID + " = ?";

    String SELECT_INSTANCES_FIRED_TRIGGERS = "SELECT * FROM "
            + TABLE_PREFIX_SUBST
            + TABLE_FIRED_TRIGGERS
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_INSTANCE_NAME + " = ?";

    String SELECT_INSTANCES_RECOVERABLE_FIRED_TRIGGERS = "SELECT * FROM "
            + TABLE_PREFIX_SUBST
            + TABLE_FIRED_TRIGGERS
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_INSTANCE_NAME + " = ? AND " + COL_REQUESTS_RECOVERY + " = ?";

    String SELECT_JOB_EXECUTION_COUNT = "SELECT COUNT("
            + COL_TRIGGER_NAME + ") FROM " + TABLE_PREFIX_SUBST
            + TABLE_FIRED_TRIGGERS + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_NAME + " = ? AND "
            + COL_JOB_GROUP + " = ?";

    String SELECT_FIRED_TRIGGERS = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String SELECT_FIRED_TRIGGER = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_FIRED_TRIGGER_GROUP = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_FIRED_TRIGGERS_OF_JOB = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_NAME + " = ? AND " + COL_JOB_GROUP + " = ?";

    String SELECT_FIRED_TRIGGERS_OF_JOB_GROUP = "SELECT * FROM "
            + TABLE_PREFIX_SUBST
            + TABLE_FIRED_TRIGGERS
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_JOB_GROUP + " = ?";

    String DELETE_FIRED_TRIGGER = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_ENTRY_ID + " = ?";

    String DELETE_INSTANCES_FIRED_TRIGGERS = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_FIRED_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_INSTANCE_NAME + " = ?";

    String DELETE_NO_RECOVERY_FIRED_TRIGGERS = "DELETE FROM "
            + TABLE_PREFIX_SUBST
            + TABLE_FIRED_TRIGGERS
            + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_INSTANCE_NAME + " = ?" + COL_REQUESTS_RECOVERY + " = ?";

    String DELETE_ALL_SIMPLE_TRIGGERS = "DELETE FROM " + TABLE_PREFIX_SUBST + "SIMPLE_TRIGGERS " + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    String DELETE_ALL_SIMPROP_TRIGGERS = "DELETE FROM " + TABLE_PREFIX_SUBST + "SIMPROP_TRIGGERS " + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    String DELETE_ALL_CRON_TRIGGERS = "DELETE FROM " + TABLE_PREFIX_SUBST + "CRON_TRIGGERS" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    String DELETE_ALL_BLOB_TRIGGERS = "DELETE FROM " + TABLE_PREFIX_SUBST + "BLOB_TRIGGERS" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    String DELETE_ALL_TRIGGERS = "DELETE FROM " + TABLE_PREFIX_SUBST + "TRIGGERS" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    String DELETE_ALL_JOB_DETAILS = "DELETE FROM " + TABLE_PREFIX_SUBST + "JOB_DETAILS" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    String DELETE_ALL_CALENDARS = "DELETE FROM " + TABLE_PREFIX_SUBST + "CALENDARS" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    String DELETE_ALL_PAUSED_TRIGGER_GRPS = "DELETE FROM " + TABLE_PREFIX_SUBST + "PAUSED_TRIGGER_GRPS" + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    
    String SELECT_FIRED_TRIGGER_INSTANCE_NAMES = 
            "SELECT DISTINCT " + COL_INSTANCE_NAME + " FROM "
            + TABLE_PREFIX_SUBST
            + TABLE_FIRED_TRIGGERS
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;
    
    String INSERT_SCHEDULER_STATE = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_SCHEDULER_STATE + " ("
            + COL_SCHEDULER_NAME + ", "
            + COL_INSTANCE_NAME + ", " + COL_LAST_CHECKIN_TIME + ", "
            + COL_CHECKIN_INTERVAL + ") VALUES(" + SCHED_NAME_SUBST + ", ?, ?, ?)";

    String SELECT_SCHEDULER_STATE = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_SCHEDULER_STATE + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_INSTANCE_NAME + " = ?";

    String SELECT_SCHEDULER_STATES = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_SCHEDULER_STATE
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String DELETE_SCHEDULER_STATE = "DELETE FROM "
        + TABLE_PREFIX_SUBST + TABLE_SCHEDULER_STATE + " WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
        + " AND " + COL_INSTANCE_NAME + " = ?";

    String UPDATE_SCHEDULER_STATE = "UPDATE "
        + TABLE_PREFIX_SUBST + TABLE_SCHEDULER_STATE + " SET " 
        + COL_LAST_CHECKIN_TIME + " = ? WHERE "
        + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
        + " AND " + COL_INSTANCE_NAME + " = ?";

    String INSERT_PAUSED_TRIGGER_GROUP = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_PAUSED_TRIGGERS + " ("
            + COL_SCHEDULER_NAME + ", "
            + COL_TRIGGER_GROUP + ") VALUES(" + SCHED_NAME_SUBST + ", ?)";

    String SELECT_PAUSED_TRIGGER_GROUP = "SELECT "
            + COL_TRIGGER_GROUP + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_PAUSED_TRIGGERS + " WHERE " 
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP + " = ?";

    String SELECT_PAUSED_TRIGGER_GROUPS = "SELECT "
        + COL_TRIGGER_GROUP + " FROM " + TABLE_PREFIX_SUBST
        + TABLE_PAUSED_TRIGGERS
        + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    String DELETE_PAUSED_TRIGGER_GROUP = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_PAUSED_TRIGGERS + " WHERE "
            + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST
            + " AND " + COL_TRIGGER_GROUP + " LIKE ?";

    String DELETE_PAUSED_TRIGGER_GROUPS = "DELETE FROM "
            + TABLE_PREFIX_SUBST + TABLE_PAUSED_TRIGGERS
            + " WHERE " + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST;

    //  CREATE TABLE qrtz_scheduler_state(INSTANCE_NAME VARCHAR2(80) NOT NULL,
    // LAST_CHECKIN_TIME NUMBER(13) NOT NULL, CHECKIN_INTERVAL NUMBER(13) NOT
    // NULL, PRIMARY KEY (INSTANCE_NAME));

}

// EOF

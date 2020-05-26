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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.quartz.spi.ClassLoadHelper;
import org.slf4j.Logger;

/**
 * <p>
 * This is a driver delegate for the MSSQL JDBC driver.
 * </p>
 * 
 * @author <a href="mailto:jeff@binaryfeed.org">Jeffrey Wescott</a>
 */
public class MSSQLDelegate extends StdJDBCDelegate {

    //---------------------------------------------------------------------------
    // protected methods that can be overridden by subclasses
    //---------------------------------------------------------------------------

    /**
     * <p>
     * This method should be overridden by any delegate subclasses that need
     * special handling for BLOBs. The default implementation uses standard
     * JDBC <code>java.sql.Blob</code> operations.
     * </p>
     * 
     * @param rs
     *          the result set, already queued to the correct row
     * @param colName
     *          the column name for the BLOB
     * @return the deserialized Object from the ResultSet BLOB
     * @throws ClassNotFoundException
     *           if a class found during deserialization cannot be found
     * @throws IOException
     *           if deserialization causes an error
     */
    @Override           
    protected Object getObjectFromBlob(ResultSet rs, String colName)
        throws ClassNotFoundException, IOException, SQLException {
        InputStream binaryInput = rs.getBinaryStream(colName);

        if(binaryInput == null || binaryInput.available() == 0) {
            return null;
        }

        Object obj = null;

        obj = serializationHelper.readObject(binaryInput);

        return obj;
    }

    @Override           
    protected Object getJobDataFromBlob(ResultSet rs, String colName)
        throws ClassNotFoundException, IOException, SQLException {
        if (canUseProperties()) {
            InputStream binaryInput = rs.getBinaryStream(colName);
            return binaryInput;
        }
        return getObjectFromBlob(rs, colName);
    }
}

// EOF

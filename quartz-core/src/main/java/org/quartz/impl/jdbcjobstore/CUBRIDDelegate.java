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

import com.mchange.v2.c3p0.C3P0ProxyConnection;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import org.quartz.spi.ClassLoadHelper;
import org.slf4j.Logger;

/**
 * <p> This is a driver delegate for the CUBRID JDBC driver. For Quartz 2.x </p>
 * Blob handling instructions at
 * http://www.cubrid.org/manual/831/en/Using%20BLOB|CLOB Also at
 * http://www.cubrid.org/wiki_tutorials/entry/working-with-cubrid-blob-clob-data-types
 *
 * @author Timothy Anyona
 */
public class CUBRIDDelegate extends StdJDBCDelegate {

    /**
     * <p> This method should be overridden by any delegate subclasses that need
     * special handling for BLOBs. The default implementation uses standard JDBC
     * <code>java.sql.Blob</code> operations. </p>
     *
     * @param rs the result set, already queued to the correct row
     * @param colName the column name for the BLOB
     * @return the deserialized Object from the ResultSet BLOB
     * @throws ClassNotFoundException if a class found during deserialization
     * cannot be found
     * @throws IOException if deserialization causes an error
     */
    @Override
    protected Object getObjectFromBlob(ResultSet rs, String colName)
            throws ClassNotFoundException, IOException, SQLException {

        Object obj = null;
        InputStream binaryInput;

        Blob blob = rs.getBlob(colName);
        byte[] bytes = blob.getBytes(1, (int) blob.length());

        if (bytes != null && bytes.length != 0) {
            binaryInput = new ByteArrayInputStream(bytes);

            obj = serializationHelper.readObject(binaryInput);
        }

        return obj;
    }

    @Override
    protected Object getJobDataFromBlob(ResultSet rs, String colName)
            throws ClassNotFoundException, IOException, SQLException {

        if (canUseProperties()) {
            InputStream binaryInput;

            Blob blob = rs.getBlob(colName);
            byte[] bytes = blob.getBytes(1, (int) blob.length());

            if (bytes == null || bytes.length == 0) {
                return null;
            }
            binaryInput = new ByteArrayInputStream(bytes);
            return binaryInput;
        }

        return getObjectFromBlob(rs, colName);
    }

    /**
     * Sets the designated parameter to the byte array of the given
     * <code>ByteArrayOutputStream</code>. Will set parameter value to null if
     * the
     * <code>ByteArrayOutputStream</code> is null. This just wraps
     * <code>{@link PreparedStatement#setBytes(int, byte[])}</code> by default,
     * but it can be overloaded by subclass delegates for databases that don't
     * explicitly support storing bytes in this way.
     */
    @Override
    protected void setBytes(PreparedStatement ps, int index, ByteArrayOutputStream baos)
            throws SQLException {
        
        byte[] byteArray;
        if (baos == null) {
            //saving 0 byte blob may cause error? like http://dev.naver.com/projects/cubrid/issue/13710 - (0 byte bit)
            //alternativly store null since blob not null columns are not allowed (cubrid 8.4.1). may be allowed in future versions?
            byteArray = new byte[0];
        } else {
            byteArray = baos.toByteArray();
        }

        //quartz 2.x uses c3p0, c3p0 doesn't support createBlob method as of 0.9.2        
        Connection conn = ps.getConnection();
        if (conn instanceof C3P0ProxyConnection) {
            try {
                C3P0ProxyConnection c3p0Conn = (C3P0ProxyConnection) conn;
                Method m = Connection.class.getMethod("createBlob", new Class[]{}); //will call createBlob method on the underlying connection
                Object[] args = new Object[]{}; //arguments to be passed to the method. none in this case
                Blob blob = (Blob) c3p0Conn.rawConnectionOperation(m, C3P0ProxyConnection.RAW_CONNECTION, args); 
                blob.setBytes(1, byteArray);
                ps.setBlob(index, blob);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Blob blob = ps.getConnection().createBlob();
            blob.setBytes(1, byteArray);
            ps.setBlob(index, blob);
        }
    }
}

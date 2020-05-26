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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 * This is a driver delegate for Intersystems Caché database.
 * </p>
 * 
 * <p>
 * Works with the Oracle table creation scripts / schema.
 * </p>
 * 
 * @author Franck Routier
 * @author <a href="mailto:alci@mecadu.org">Franck Routier</a>
 */
public class CacheDelegate extends StdJDBCDelegate {
        
    //---------------------------------------------------------------------------
    // protected methods that can be overridden by subclasses
    //---------------------------------------------------------------------------
  
    /**
     * Sets the designated parameter to the byte array of the given
     * <code>ByteArrayOutputStream</code>. Will set parameter value to null if the
     * <code>ByteArrayOutputStream</code> is null.
     * This just wraps <code>{@link PreparedStatement#setBytes(int, byte[])}</code>
     * by default, but it can be overloaded by subclass delegates for databases that
     * don't explicitly support storing bytes in this way.
     */
    @Override
    protected void setBytes(PreparedStatement ps, int index, ByteArrayOutputStream baos) throws SQLException {
        ps.setObject(index, ((baos == null) ? null : baos.toByteArray()), java.sql.Types.BLOB);
    } 

    /**
     * {@inheritDoc}
     * <p>
     * Caché requires {@code java.sql.Blob} instances to be explicitly freed.
     */
    @Override
    protected Object getObjectFromBlob(ResultSet rs, String colName) throws ClassNotFoundException, IOException, SQLException {
        Blob blob = rs.getBlob(colName);
        if (blob == null) {
            return null;
        } else {
            try {
                if (blob.length() == 0) {
                    return null;
                } else {
                    InputStream binaryInput = blob.getBinaryStream();
                    if (binaryInput == null) {
                        return null;
                    } else if (binaryInput instanceof ByteArrayInputStream && ((ByteArrayInputStream) binaryInput).available() == 0 ) {
                        return null;
                    } else {
                        return serializationHelper.readObject(binaryInput);
                    }
                }
            } finally {
                blob.free();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Caché requires {@code java.sql.Blob} instances to be explicitly freed.
     */
    @Override
    protected Object getJobDataFromBlob(ResultSet rs, String colName) throws ClassNotFoundException, IOException, SQLException {
        if (canUseProperties()) {
            Blob blob = rs.getBlob(colName);
            if (blob == null) {
                return null;
            } else {
                return new BlobFreeingStream(blob, blob.getBinaryStream());
            }
        } else {
            return getObjectFromBlob(rs, colName);
        }
    }
    
    private static class BlobFreeingStream extends InputStream {
        
        private final Blob source;
        private final InputStream delegate;

        private BlobFreeingStream(Blob blob, InputStream stream) {
            this.source = blob;
            this.delegate = stream;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                try {
                    source.free();
                } catch (SQLException ex) {
                    throw new IOException(ex);
                }
            }
        }

        @Override
        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }
    }
}


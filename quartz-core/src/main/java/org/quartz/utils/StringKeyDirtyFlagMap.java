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
package org.quartz.utils;

import java.io.Serializable;

/**
 * <p>
 * An implementation of <code>Map</code> that wraps another <code>Map</code>
 * and flags itself 'dirty' when it is modified, enforces that all keys are
 * Strings. 
 * </p>
 * 
 * <p>
 * All allowsTransientData flag related methods are deprecated as of version 1.6.
 * </p>
 */
public class StringKeyDirtyFlagMap extends DirtyFlagMap<String, Object> {
    static final long serialVersionUID = -9076749120524952280L;
    
    /**
     * @deprecated JDBCJobStores no longer prune out transient data.  If you
     * include non-Serializable values in the Map, you will now get an 
     * exception when attempting to store it in a database.
     */
    private boolean allowsTransientData = false;

    public StringKeyDirtyFlagMap() {
        super();
    }

    public StringKeyDirtyFlagMap(int initialCapacity) {
        super(initialCapacity);
    }

    public StringKeyDirtyFlagMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return getWrappedMap().hashCode();
    }
    
    /**
     * Get a copy of the Map's String keys in an array of Strings.
     *
     * @return the keys as an array of Strings
     */
    public String[] getKeys() {
        return keySet().toArray(new String[size()]);
    }

    /**
     * Tell the <code>StringKeyDirtyFlagMap</code> that it should
     * allow non-<code>Serializable</code> values.  Enforces that the Map 
     * doesn't already include transient data.
     *
     * @param allowsTransientData if non-serializable data is allowed
     *
     * @deprecated JDBCJobStores no longer prune out transient data.  If you
     * include non-Serializable values in the Map, you will now get an 
     * exception when attempting to store it in a database.
     */
    public void setAllowsTransientData(boolean allowsTransientData) {
    
        if (containsTransientData() && !allowsTransientData) {
            throw new IllegalStateException(
                "Cannot set property 'allowsTransientData' to 'false' "
                    + "when data map contains non-serializable objects.");
        }
    
        this.allowsTransientData = allowsTransientData;
    }

    /**
     * Whether the <code>StringKeyDirtyFlagMap</code> allows 
     * non-<code>Serializable</code> values.
     *
     * @return non-serializable data is allowed
     *
     * @deprecated JDBCJobStores no longer prune out transient data.  If you
     * include non-Serializable values in the Map, you will now get an 
     * exception when attempting to store it in a database.
     */
    public boolean getAllowsTransientData() {
        return allowsTransientData;
    }

    /**
     * Determine whether any values in this Map do not implement 
     * <code>Serializable</code>.  Always returns false if this Map
     * is flagged to not allow transient data.
     *
     * @return is non-serializable data included
     *
     * @deprecated JDBCJobStores no longer prune out transient data.  If you
     * include non-Serializable values in the Map, you will now get an 
     * exception when attempting to store it in a database.
     */
    public boolean containsTransientData() {
        if (!getAllowsTransientData()) { // short circuit...
            return false;
        }
    
        String[] keys = getKeys();
        for (int i = 0; i < keys.length; i++) {
            Object o = super.get(keys[i]);
            if (!(o instanceof Serializable)) {
                return true;
            }
        }
    
        return false;
    }

    /**
     * Removes any data values in the map that are non-Serializable.  Does 
     * nothing if this Map does not allow transient data.
     * 
     * @deprecated JDBCJobStores no longer prune out transient data.  If you
     * include non-Serializable values in the Map, you will now get an 
     * exception when attempting to store it in a database.
     */
    public void removeTransientData() {
        if (!getAllowsTransientData()) { // short circuit...
            return;
        }
    
        String[] keys = getKeys();
        for (int i = 0; i < keys.length; i++) {
            Object o = super.get(keys[i]);
            if (!(o instanceof Serializable)) {
                remove(keys[i]);
            }
        }
    }

    // Due to Generic enforcement, this override method is no longer needed.
//    /**
//     * <p>
//     * Adds the name-value pairs in the given <code>Map</code> to the 
//     * <code>StringKeyDirtyFlagMap</code>.
//     * </p>
//     * 
//     * <p>
//     * All keys must be <code>String</code>s.
//     * </p>
//     */
//    @Override
//    public void putAll(Map<String, Object> map) {
//        for (Iterator<?> entryIter = map.entrySet().iterator(); entryIter.hasNext();) {
//            Map.Entry<?,?> entry = (Map.Entry<?,?>) entryIter.next();
//            
//            // will throw IllegalArgumentException if key is not a String
//            put(entry.getKey(), entry.getValue());
//        }
//    }

    /**
     * <p>
     * Adds the given <code>int</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>int</code> value
     */
    public void put(String key, int value) {
        super.put(key, Integer.valueOf(value));
    }

    /**
     * <p>
     * Adds the given <code>long</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>long</code> value
     */
    public void put(String key, long value) {
        super.put(key, Long.valueOf(value));
    }

    /**
     * <p>
     * Adds the given <code>float</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>float</code> value
     */
    public void put(String key, float value) {
        super.put(key, Float.valueOf(value));
    }

    /**
     * <p>
     * Adds the given <code>double</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>double</code> value
     */
    public void put(String key, double value) {
        super.put(key, Double.valueOf(value));
    }

    /**
     * <p>
     * Adds the given <code>boolean</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>boolean</code> value
     */
    public void put(String key, boolean value) {
        super.put(key, Boolean.valueOf(value));
    }

    /**
     * <p>
     * Adds the given <code>char</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>char</code> value
     */
    public void put(String key, char value) {
        super.put(key, Character.valueOf(value));
    }

    /**
     * <p>
     * Adds the given <code>String</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>String</code> value
     */
    public void put(String key, String value) {
        super.put(key, value);
    }

    /**
     * <p>
     * Adds the given <code>Object</code> value to the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code>key
     * @param value the mapping's <code>Object</code> value
     */
    @Override
    public Object put(String key, Object value) {
        return super.put((String)key, value);
    }
    
    /**
     * <p>
     * Retrieve the identified <code>int</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>int</code> value
     * @throws ClassCastException
     *           if the identified object is not an Integer.
     */
    public int getInt(String key) {
        Object obj = get(key);
    
        try {
            if(obj instanceof Integer)
                return ((Integer) obj).intValue();
            return Integer.parseInt((String)obj);
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not an Integer.");
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>long</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>long</code> value
     * @throws ClassCastException
     *           if the identified object is not a Long.
     */
    public long getLong(String key) {
        Object obj = get(key);
    
        try {
            if(obj instanceof Long)
                return ((Long) obj).longValue();
            return Long.parseLong((String)obj);
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not a Long.");
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>float</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>float</code> value
     * @throws ClassCastException
     *           if the identified object is not a Float.
     */
    public float getFloat(String key) {
        Object obj = get(key);
    
        try {
            if(obj instanceof Float)
                return ((Float) obj).floatValue();
            return Float.parseFloat((String)obj);
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not a Float.");
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>double</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>double</code> value
     * @throws ClassCastException
     *           if the identified object is not a Double.
     */
    public double getDouble(String key) {
        Object obj = get(key);
    
        try {
            if(obj instanceof Double)
                return ((Double) obj).doubleValue();
            return Double.parseDouble((String)obj);
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not a Double.");
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>boolean</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>boolean</code> value
     * @throws ClassCastException
     *           if the identified object is not a Boolean.
     */
    public boolean getBoolean(String key) {
        Object obj = get(key);
    
        try {
            if(obj instanceof Boolean)
                return ((Boolean) obj).booleanValue();
            return Boolean.parseBoolean((String)obj);
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not a Boolean.");
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>char</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>char</code> value
     * @throws ClassCastException
     *           if the identified object is not a Character.
     */
    public char getChar(String key) {
        Object obj = get(key);
    
        try {
            if(obj instanceof Character)
                return ((Character) obj).charValue();
            return ((String)obj).charAt(0);
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not a Character.");
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>String</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>String</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public String getString(String key) {
        Object obj = get(key);
    
        try {
            return (String) obj;
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not a String.");
        }
    }
}

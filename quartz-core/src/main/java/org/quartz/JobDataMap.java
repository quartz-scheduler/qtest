
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

import java.io.Serializable;
import java.util.Map;

import org.quartz.utils.StringKeyDirtyFlagMap;

/**
 * Holds state information for <code>Job</code> instances.
 * 
 * <p>
 * <code>JobDataMap</code> instances are stored once when the <code>Job</code>
 * is added to a scheduler. They are also re-persisted after every execution of
 * jobs annotated with <code>@PersistJobDataAfterExecution</code>.
 * </p>
 * 
 * <p>
 * <code>JobDataMap</code> instances can also be stored with a 
 * <code>Trigger</code>.  This can be useful in the case where you have a Job
 * that is stored in the scheduler for regular/repeated use by multiple 
 * Triggers, yet with each independent triggering, you want to supply the
 * Job with different data inputs.  
 * </p>
 * 
 * <p>
 * The <code>JobExecutionContext</code> passed to a Job at execution time 
 * also contains a convenience <code>JobDataMap</code> that is the result
 * of merging the contents of the trigger's JobDataMap (if any) over the
 * Job's JobDataMap (if any).  
 * </p>
 *
 * <p>
 * Update since 2.2.4 - We keep an dirty flag for this map so that whenever you modify(add/delete) any of the entries,
 * it will set to "true". However if you create new instance using an exising map with {@link #JobDataMap(Map)}, then
 * the dirty flag will NOT be set to "true" until you modify the instance.
 * </p>
 * 
 * @see Job
 * @see PersistJobDataAfterExecution
 * @see Trigger
 * @see JobExecutionContext
 * 
 * @author James House
 */
public class JobDataMap extends StringKeyDirtyFlagMap implements Serializable {

    private static final long serialVersionUID = -6939901990106713909L;
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create an empty <code>JobDataMap</code>.
     * </p>
     */
    public JobDataMap() {
        super(15);
    }

    /**
     * <p>
     * Create a <code>JobDataMap</code> with the given data.
     * </p>
     *
     * @param map the initial data map
     */
    public JobDataMap(Map<?, ?> map) {
        this();
        @SuppressWarnings("unchecked") // casting to keep API compatible and avoid compiler errors/warnings.
        Map<String, Object> mapTyped = (Map<String, Object>)map;
        putAll(mapTyped);

        // When constructing a new data map from another existing map, we should NOT mark dirty flag as true
        // Use case: loading JobDataMap from DB
        clearDirtyFlag();
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Adds the given <code>boolean</code> value as a string version to the
     * <code>Job</code>'s data map.
     *
     * @param key the mapping's key
     * @param value the mapping's <code>boolean</code> value
     * </p>
     */
    public void putAsString(String key, boolean value) {
        String strValue = Boolean.valueOf(value).toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>Boolean</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>Boolean</code> value
     */
    public void putAsString(String key, Boolean value) {
        String strValue = value.toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>char</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>char</code> value
     */
    public void putAsString(String key, char value) {
        String strValue = Character.valueOf(value).toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>Character</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>Character</code> value
     */
    public void putAsString(String key, Character value) {
        String strValue = value.toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>double</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>double</code> value
     */
    public void putAsString(String key, double value) {
        String strValue = Double.toString(value);

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>Double</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>Double</code> value
     */
    public void putAsString(String key, Double value) {
        String strValue = value.toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>float</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>float</code> value
     */
    public void putAsString(String key, float value) {
        String strValue = Float.toString(value);

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>Float</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>Float</code> value
     */
    public void putAsString(String key, Float value) {
        String strValue = value.toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>int</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>int</code> value
     */
    public void putAsString(String key, int value) {
        String strValue = Integer.valueOf(value).toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>Integer</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>Integer</code> value
     */
    public void putAsString(String key, Integer value) {
        String strValue = value.toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>long</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>long</code> value
     */
    public void putAsString(String key, long value) {
        String strValue = Long.valueOf(value).toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Adds the given <code>Long</code> value as a string version to the
     * <code>Job</code>'s data map.
     * </p>
     *
     * @param key the mapping's key
     * @param value the mapping's <code>Long</code> value
     */
    public void putAsString(String key, Long value) {
        String strValue = value.toString();

        super.put(key, strValue);
    }

    /**
     * <p>
     * Retrieve the identified <code>int</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>int</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public int getIntFromString(String key) {
        Object obj = get(key);

        return new Integer((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>int</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>int</code> value
     * @throws ClassCastException
     *           if the identified object is not a String or Integer.
     */
    public int getIntValue(String key) {
        Object obj = get(key);

        if(obj instanceof String) {
            return getIntFromString(key);
        } else {
            return getInt(key);
        }
    }
    
    /**
     * <p>
     * Retrieve the identified <code>int</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>Integer</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public Integer getIntegerFromString(String key) {
        Object obj = get(key);

        return new Integer((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>boolean</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>boolean</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public boolean getBooleanValueFromString(String key) {
        Object obj = get(key);

        return Boolean.valueOf((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>boolean</code> value from the 
     * <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>boolean</code> value
     * @throws ClassCastException
     *           if the identified object is not a String or Boolean.
     */
    public boolean getBooleanValue(String key) {
        Object obj = get(key);

        if(obj instanceof String) {
            return getBooleanValueFromString(key);
        } else {
            return getBoolean(key);
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>Boolean</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>boolean</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public Boolean getBooleanFromString(String key) {
        Object obj = get(key);

        return Boolean.valueOf((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>char</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>char</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public char getCharFromString(String key) {
        Object obj = get(key);

        return ((String) obj).charAt(0);
    }

    /**
     * <p>
     * Retrieve the identified <code>Character</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>Character</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public Character getCharacterFromString(String key) {
        Object obj = get(key);

        return ((String) obj).charAt(0);
    }

    /**
     * <p>
     * Retrieve the identified <code>double</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>double</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public double getDoubleValueFromString(String key) {
        Object obj = get(key);

        return Double.valueOf((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>double</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>double</code> value
     * @throws ClassCastException
     *           if the identified object is not a String or Double.
     */
    public double getDoubleValue(String key) {
        Object obj = get(key);

        if(obj instanceof String) {
            return getDoubleValueFromString(key);
        } else {
            return getDouble(key);
        }
    }

    /**
     * <p>
     * Retrieve the identified <code>Double</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>Double</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public Double getDoubleFromString(String key) {
        Object obj = get(key);

        return new Double((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>float</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>float</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public float getFloatValueFromString(String key) {
        Object obj = get(key);

        return new Float((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>float</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>float</code> value
     * @throws ClassCastException
     *           if the identified object is not a String or Float.
     */
    public float getFloatValue(String key) {
        Object obj = get(key);

        if(obj instanceof String) {
            return getFloatValueFromString(key);
        } else {
            return getFloat(key);
        }
    }
    
    /**
     * <p>
     * Retrieve the identified <code>Float</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>Float</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public Float getFloatFromString(String key) {
        Object obj = get(key);

        return new Float((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>long</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>long</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public long getLongValueFromString(String key) {
        Object obj = get(key);

        return new Long((String) obj);
    }

    /**
     * <p>
     * Retrieve the identified <code>long</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>long</code> value
     * @throws ClassCastException
     *           if the identified object is not a String or Long.
     */
    public long getLongValue(String key) {
        Object obj = get(key);

        if(obj instanceof String) {
            return getLongValueFromString(key);
        } else {
            return getLong(key);
        }
    }
    
    /**
     * <p>
     * Retrieve the identified <code>Long</code> value from the <code>JobDataMap</code>.
     * </p>
     *
     * @param key the mapping's <code>String</code> key
     * @return the mapping's <code>Long</code> value
     * @throws ClassCastException
     *           if the identified object is not a String.
     */
    public Long getLongFromString(String key) {
        Object obj = get(key);

        return new Long((String) obj);
    }
}

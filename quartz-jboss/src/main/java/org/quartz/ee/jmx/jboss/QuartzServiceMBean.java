
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

package org.quartz.ee.jmx.jboss;

import org.jboss.system.ServiceMBean;

/**
 * Interface exposed via JMX for MBean for configuring, starting, and
 * binding to JNDI a Quartz Scheduler instance.
 *  
 * <p> 
 * Sample MBean deployment descriptor: 
 * <a href="doc-files/quartz-service.xml">quartz-service.xml</a>
 * </p>
 * 
 * <p> 
 * <b>Note:</b> The Scheduler instance bound to JNDI is not Serializable, so 
 * you will get a null reference back if you try to retrieve it from outside
 * the JBoss server in which it was bound.  If you have a need for remote 
 * access to a Scheduler instance you may want to consider using Quartz's RMI 
 * support instead.  
 * </p>
 * 
 * @see org.quartz.ee.jmx.jboss.QuartzService
 * 
 * @author Andrew Collins
 */
public interface QuartzServiceMBean extends ServiceMBean {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    void setJndiName(String jndiName) throws Exception;

    String getJndiName();

    void setProperties(String properties);

    void setPropertiesFile(String propertiesFile);
    
    void setStartScheduler(boolean startScheduler);    
}

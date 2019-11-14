
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import org.jboss.naming.NonSerializableFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * JBoss specific MBean implementation for configuring, starting, and
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
 * @see org.quartz.ee.jmx.jboss.QuartzServiceMBean
 * 
 * @author Andrew Collins
 */
public class QuartzService extends ServiceMBeanSupport implements
        QuartzServiceMBean {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private Properties properties;

    private StdSchedulerFactory schedulerFactory;

    private String jndiName;

    private String propertiesFile;

    private boolean error;

    private boolean useProperties;

    private boolean usePropertiesFile;

    /*
    * If true, the scheduler will be started. If false, the scheduler is initailized 
    * (and available) but start() is not called - it will not execute jobs. 
    */
    private boolean startScheduler = true;
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public QuartzService() {
        // flag initialization errors
        error = false;

        // use PropertiesFile attribute
        usePropertiesFile = false;
        propertiesFile = "";

        // use Properties attribute
        useProperties = false;
        properties = new Properties();

        // default JNDI name for Scheduler
        jndiName = "Quartz";
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void setJndiName(String jndiName) throws Exception {
        String oldName = this.jndiName;
        this.jndiName = jndiName;

        if (super.getState() == STARTED) {
            unbind(oldName);

            try {
                rebind();
            } catch (NamingException ne) {
                log.error("Failed to rebind Scheduler", ne);

                throw new SchedulerConfigException(
                        "Failed to rebind Scheduler - ", ne);
            }
        }
    }

    public String getJndiName() {
        return jndiName;
    }

    @Override
    public String getName() {
        return "QuartzService(" + jndiName + ")";
    }

    public void setProperties(String properties) {
        if (usePropertiesFile) {
            log
                    .error("Must specify only one of 'Properties' or 'PropertiesFile'");

            error = true;

            return;
        }

        useProperties = true;

        try {
            properties = properties.replace(File.separator, "/");
            ByteArrayInputStream bais = new ByteArrayInputStream(properties
                    .getBytes());
            this.properties = new Properties();
            this.properties.load(bais);
        } catch (IOException ioe) {
            // should not happen
        }
    }

    public String getProperties() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            properties.store(baos, "");

            return new String(baos.toByteArray());
        } catch (IOException ioe) {
            // should not happen
            return "";
        }
    }

    public void setPropertiesFile(String propertiesFile) {
        if (useProperties) {
            log
                    .error("Must specify only one of 'Properties' or 'PropertiesFile'");

            error = true;

            return;
        }

        usePropertiesFile = true;

        this.propertiesFile = propertiesFile;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setStartScheduler(boolean startScheduler) {
        this.startScheduler = startScheduler;
    }
    
    public boolean getStartScheduler() {
        return startScheduler;
    }    
    
    @Override
    public void createService() throws Exception {
        log.info("Create QuartzService(" + jndiName + ")...");

        if (error) {
            log
                    .error("Must specify only one of 'Properties' or 'PropertiesFile'");

            throw new Exception(
                    "Must specify only one of 'Properties' or 'PropertiesFile'");
        }

        schedulerFactory = new StdSchedulerFactory();

        try {
            if (useProperties) {
                schedulerFactory.initialize(properties);
            }

            if (usePropertiesFile) {
                schedulerFactory.initialize(propertiesFile);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Scheduler", e);

            throw new SchedulerConfigException(
                    "Failed to initialize Scheduler - ", e);
        }

        log.info("QuartzService(" + jndiName + ") created.");
    }

    @Override
    public void destroyService() throws Exception {
        log.info("Destroy QuartzService(" + jndiName + ")...");

        schedulerFactory = null;

        log.info("QuartzService(" + jndiName + ") destroyed.");
    }

    @Override
    public void startService() throws Exception {
        log.info("Start QuartzService(" + jndiName + ")...");

        try {
            rebind();
        } catch (NamingException ne) {
            log.error("Failed to rebind Scheduler", ne);

            throw new SchedulerConfigException("Failed to rebind Scheduler - ",
                    ne);
        }

        try {
            Scheduler scheduler = schedulerFactory.getScheduler();

            if (startScheduler) {
                scheduler.start();
            } else {
                log.info("Skipping starting the scheduler (will not run jobs).");
            }
        } catch (Exception e) {
            log.error("Failed to start Scheduler", e);

            throw new SchedulerConfigException("Failed to start Scheduler - ",
                    e);
        }

        log.info("QuartzService(" + jndiName + ") started.");
    }

    @Override
    public void stopService() throws Exception {
        log.info("Stop QuartzService(" + jndiName + ")...");

        try {
            Scheduler scheduler = schedulerFactory.getScheduler();

            scheduler.shutdown();
        } catch (Exception e) {
            log.error("Failed to shutdown Scheduler", e);

            throw new SchedulerConfigException(
                    "Failed to shutdown Scheduler - ", e);
        }

        unbind(jndiName);

        log.info("QuartzService(" + jndiName + ") stopped.");
    }

    private void rebind() throws NamingException, SchedulerException {
        InitialContext rootCtx = null;
        try {
            rootCtx = new InitialContext();
            Name fullName = rootCtx.getNameParser("").parse(jndiName);
            Scheduler scheduler = schedulerFactory.getScheduler();
            NonSerializableFactory.rebind(fullName, scheduler, true);
        } finally {
            if (rootCtx != null) { 
                try { 
                    rootCtx.close(); 
                } catch (NamingException ignore) {} 
            }
        }
    }
    
    private void unbind(String name) {
        InitialContext rootCtx = null;
        try {
            rootCtx = new InitialContext();
            rootCtx.unbind(name);
            NonSerializableFactory.unbind(name);
        } catch (NamingException e) {
            log.warn("Failed to unbind scheduler with jndiName: " + name, e); 
        } finally {
            if (rootCtx != null) { 
                try { 
                    rootCtx.close(); 
                } catch (NamingException ignore) {} 
            }
        }
    }
}

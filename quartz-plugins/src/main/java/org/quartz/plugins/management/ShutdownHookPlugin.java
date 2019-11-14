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

package org.quartz.plugins.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

/**
 * This plugin catches the event of the JVM terminating (such as upon a CRTL-C)
 * and tells the scheuler to shutdown.
 * 
 * @see org.quartz.Scheduler#shutdown(boolean)
 * 
 * @author James House
 */
public class ShutdownHookPlugin implements SchedulerPlugin {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private boolean cleanShutdown = true;

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public ShutdownHookPlugin() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Determine whether or not the plug-in is configured to cause a clean
     * shutdown of the scheduler.
     * 
     * <p>
     * The default value is <code>true</code>.
     * </p>
     * 
     * @see org.quartz.Scheduler#shutdown(boolean)
     */
    public boolean isCleanShutdown() {
        return cleanShutdown;
    }

    /**
     * Set whether or not the plug-in is configured to cause a clean shutdown
     * of the scheduler.
     * 
     * <p>
     * The default value is <code>true</code>.
     * </p>
     * 
     * @see org.quartz.Scheduler#shutdown(boolean)
     */
    public void setCleanShutdown(boolean b) {
        cleanShutdown = b;
    }

    protected Logger getLog() {
        return log;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * SchedulerPlugin Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     * 
     * @throws org.quartz.SchedulerConfigException
     *           if there is an error initializing.
     */
    public void initialize(String name, final Scheduler scheduler, ClassLoadHelper classLoadHelper)
        throws SchedulerException {

        getLog().info("Registering Quartz shutdown hook.");

        Thread t = new Thread("Quartz Shutdown-Hook "
                + scheduler.getSchedulerName()) {
            @Override
            public void run() {
                getLog().info("Shutting down Quartz...");
                try {
                    scheduler.shutdown(isCleanShutdown());
                } catch (SchedulerException e) {
                    getLog().info(
                            "Error shutting down Quartz: " + e.getMessage(), e);
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(t);
    }

    public void start() {
        // do nothing.
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    public void shutdown() {
        // nothing to do in this case (since the scheduler is already shutting
        // down)
    }

}

// EOF

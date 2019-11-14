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

package org.quartz.jobs.ee.jmx;


import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * Generic JMX invoker Job.  It supports any number or type of parameters
 * to the JMX bean.<br>
 * 
 * The required parameters are as follows (case doesn't matter):<br>
 * <dl>
 * <dt><strong>JMX_OBJECTNAME</strong></dt>
 * <dd>This is the fully qualifed name of the object (ie in JBoss to lookup
 * the log4j jmx bean you would specify "jboss.system:type=Log4jService,service=Logging"</dd>
 * <dt><strong>JMX_METHOD</strong></dt>
 * <dd>This is the method to invoke on the specified JMX Bean. (ie in JBoss to
 * change the log level you would specify "setLoggerLevel"</dd>
 * <dt><strong>JMX_PARAMDEFS</strong></dt>
 * <dd>This is a definition of the parameters to be passed to the specified method
 * and their corresponding java types.  Each parameter definition is comma seperated
 * and has the following parts: &lt;type&gt;:&lt;name&gt;. Type is the java type for the parameter.  
 * The following types are supported:<br>
 * <b>i</b> - is for int<br>
 * <b>l</b> - is for long<br>
 * <b>f</b> - is for float<br>
 * <b>d</b> - is for double<br>
 * <b>s</b> - is for String<br>
 * <b>b</b> - is for boolean<br>
 * For ilfdb use lower for native type and upper for object wrapper. The name portion
 * of the definition is the name of the parameter holding the string value. (ie
 * s:fname,s:lname would require 2 parameters of the name fname and lname and
 * would be passed in that order to the method.</dd>
 * </dl>
 * 
 * @author James Nelson (jmn@provident-solutions.com) -- Provident Solutions LLC
 * 
 */
public class JMXInvokerJob implements Job {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Object[] params=null;
            String[] types=null;
            String objName = null;
            String objMethod = null;
            
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            
            String[] keys = jobDataMap.getKeys();
            for (int i = 0; i < keys.length; i++) {
                String value = jobDataMap.getString(keys[i]);
                if ("JMX_OBJECTNAME".equalsIgnoreCase(keys[i])) {
                    objName = value;
                } else if ("JMX_METHOD".equalsIgnoreCase(keys[i])) {
                    objMethod = value;
                } else if("JMX_PARAMDEFS".equalsIgnoreCase(keys[i])) {
                    String[] paramdefs=split(value, ",");
                    params=new Object[paramdefs.length];
                    types=new String[paramdefs.length];
                    for(int k=0;k<paramdefs.length;k++) {
                        String parts[]=  split(paramdefs[k], ":");
                        if (parts.length<2) {
                            throw new Exception("Invalid parameter definition: required parts missing "+paramdefs[k]);
                        }
                        switch(parts[0].charAt(0)) {
                            case 'i':
                                params[k]=Integer.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Integer.TYPE.getName();
                                break;
                            case 'I':
                                params[k]=Integer.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Integer.class.getName();
                                break;
                            case 'l':
                                params[k]=Long.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Long.TYPE.getName();
                                break;
                            case 'L':
                                params[k]=Long.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Long.class.getName();
                                break;
                            case 'f':
                                params[k]=Float.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Float.TYPE.getName();
                                break;
                            case 'F':
                                params[k]=Float.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Float.class.getName();
                                break;
                            case 'd':
                                params[k]=Double.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Double.TYPE.getName();
                                break;
                            case 'D':
                                params[k]=Double.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Double.class.getName();
                                break;
                            case 's':
                                params[k]=jobDataMap.getString(parts[1]);
                                types[k]=String.class.getName();
                                break;
                            case 'b':
                                params[k]= Boolean.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Boolean.TYPE.getName();
                                break;
                            case 'B':
                                params[k]= Boolean.valueOf(jobDataMap.getString(parts[1]));
                                types[k]=Boolean.class.getName();
                                break;
                        }
                    }
                }
            }
            
            if (objName==null || objMethod==null) { 
                throw new Exception("Required parameters missing");
            }
            
            context.setResult(invoke(objName, objMethod, params, types));
        } catch (Exception e) {
            String m = "Caught a " + e.getClass().getName() + " exception : " + e.getMessage();
            getLog().error(m, e);
            throw new JobExecutionException(m, e, false);
        }
    }
  
    private String[] split(String str, String splitStr) // Same as String.split(.) in JDK 1.4
    {
        LinkedList<String> l = new LinkedList<String>();
    
        StringTokenizer strTok = new StringTokenizer(str, splitStr);
        while(strTok.hasMoreTokens()) {
            String tok = strTok.nextToken();
            l.add(tok);
        }
    
        return (String[])l.toArray(new String[l.size()]);
    }

    private Object invoke(String objectName, String method, Object[] params, String[] types) throws Exception {
        MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
        ObjectName mbean = new ObjectName(objectName);

        if (server == null) {
            throw new Exception("Can't find mbean server");
        }

        getLog().info("invoking " + method);
        return server.invoke(mbean, method, params, types);
    }

    protected Logger getLog() {
        return log;
    }

}

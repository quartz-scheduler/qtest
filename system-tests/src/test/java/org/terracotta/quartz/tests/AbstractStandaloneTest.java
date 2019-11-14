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
package org.terracotta.quartz.tests;

import ch.qos.logback.core.Layout;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.StdSchedulerFactory;
import org.terracotta.test.util.TestBaseUtil;
import org.terracotta.tests.base.AbstractClientBase;
import org.terracotta.tests.base.AbstractTestBase;
import org.terracotta.toolkit.ToolkitFactory;

import com.tc.test.config.model.TestConfig;

import java.util.List;

public abstract class AbstractStandaloneTest extends AbstractTestBase {

  protected AbstractStandaloneTest(TestConfig testConfig, Class<? extends AbstractClientBase>... c) {
    super(testConfig);
    testConfig.getL2Config().setOffHeapEnabled(true);
    testConfig.getL2Config().setMaxOffHeapDataSize(512);
    testConfig.getL2Config().setMaxHeap(1024);
    testConfig.getL2Config().setMinHeap(256);
    testConfig.getClientConfig().setClientClasses(c);
    if (isDisabled()) {
      disableTest();
    }
  }

  protected boolean isDisabled() {
    return false;
  }

  @Override
  protected String createClassPath(Class client) {
    List<String> toolkitRuntime = TestBaseUtil.getToolkitRuntimeDependencies(ToolkitFactory.class);
    String test = TestBaseUtil.jarFor(client);
    String quartz = TestBaseUtil.jarFor(StdSchedulerFactory.class);
    String quartzJobs = TestBaseUtil.jarFor(MyJob.class);
    String logging = TestBaseUtil.jarFor(org.slf4j.LoggerFactory.class);
    String binder = TestBaseUtil.jarFor(org.slf4j.impl.StaticLoggerBinder.class);
    String logback = TestBaseUtil.jarFor(Layout.class);
    String junit = TestBaseUtil.jarFor(org.junit.Assert.class);
    String mockito = TestBaseUtil.jarFor(org.hamcrest.core.Is.class);

    return makeClasspath(toolkitRuntime, test, quartz, quartzJobs, logging, binder, logback, junit, mockito);
  }
  
  /** An empty job for testing purpose. */
  public static class MyJob implements Job {
      public void execute(JobExecutionContext context) throws JobExecutionException {
          //
      }
  }
}

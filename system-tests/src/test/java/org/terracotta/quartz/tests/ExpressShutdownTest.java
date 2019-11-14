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

import com.tc.test.config.model.TestConfig;
import com.tc.util.runtime.Vm;

public class ExpressShutdownTest extends AbstractStandaloneTest {

  public ExpressShutdownTest(TestConfig testConfig) {
    super(testConfig, ShutdownClient.class);

    testConfig.getClientConfig().addExtraClientJvmArg("-XX:MaxPermSize=64M");
    testConfig.getClientConfig().addExtraClientJvmArg("-XX:+HeapDumpOnOutOfMemoryError");
    testConfig.getClientConfig().addExtraClientJvmArg("-XX:SoftRefLRUPolicyMSPerMB=0");
  }
}

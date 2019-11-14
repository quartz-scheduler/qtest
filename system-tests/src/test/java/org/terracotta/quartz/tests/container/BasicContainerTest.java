/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.terracotta.quartz.tests.container;

import ch.qos.logback.core.Layout;
import org.apache.commons.io.IOUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.LoggerFactory;
import org.terracotta.quartz.TerracottaJobStore;
import org.terracotta.toolkit.ToolkitFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.tc.test.server.appserver.deployment.AbstractOneServerDeploymentTest;
import com.tc.test.server.appserver.deployment.DeploymentBuilder;
import com.tc.test.server.appserver.deployment.TempDirectoryUtil;
import com.tc.test.server.appserver.deployment.WebApplicationServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;

public class BasicContainerTest extends AbstractOneServerDeploymentTest {

  private static final String CONTEXT = "BasicContainerTest";

  public static Test suite() {
    return new BasicContainerTestSetup();
  }

  public void testBasics() throws Exception {
    System.out.println("Running test");
    WebClient conversation = new WebClient();

    // do insert on server0
    WebResponse response1 = request(server0, "", conversation);
    assertEquals("OK", response1.getContentAsString().trim());
  }

  private WebResponse request(WebApplicationServer server, String params, WebClient con) throws Exception {
    return server.ping("/" + CONTEXT + "/BasicTestServlet?" + params, con);
  }

  private static class BasicContainerTestSetup extends OneServerTestSetup {

    public BasicContainerTestSetup() {
      super(BasicContainerTest.class, CONTEXT);
    }

    @Override
    protected void configureWar(DeploymentBuilder builder) {
      builder.addDirectoryOrJARContainingClass(ToolkitFactory.class); // toolkit-runtime
      builder.addDirectoryOrJARContainingClass(StdSchedulerFactory.class); // core quartz
      builder.addDirectoryOrJARContainingClass(TerracottaJobStore.class); // quartz-terracotta
      builder.addDirectoryOrJARContainingClass(LoggerFactory.class); // sl4j-api
       builder.addDirectoryOrJARContainingClass(Layout.class); // logback

      builder.addFileAsResource(createConfigFile(), "WEB-INF/classes/");

      builder.addServlet("BasicTestServlet", "/BasicTestServlet/*", BasicTestServlet.class, null, false);
    }

    private File createConfigFile() {
      try {
        File configFile = writeConfigFile(TempDirectoryUtil.getTempDirectory(this.getClass()), getServerManager()
            .getServerTcConfig().getTsaPort());
        System.out.println("Wrote temp config file at: " + configFile.getAbsolutePath());
        return configFile;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private File writeConfigFile(File tempDirectory, int dsoPort) throws IOException {
      InputStream in = null;
      FileOutputStream out = null;

      try {
        in = getClass().getClassLoader().getResourceAsStream("org/terracotta/quartz/tests/container/quartz.properties");
        File rv = new File(tempDirectory, "basic-quartz.properties");
        out = new FileOutputStream(rv);
        String template = IOUtils.toString(in);
        String config = template.replace("__PORT__", String.valueOf(dsoPort));
        out.write(config.getBytes());
        return rv;
      } finally {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
    }
  }

}

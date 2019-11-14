/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terracotta.quartz.upgradability.serialization;


import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.TimeZone;

import org.hamcrest.collection.ArrayMatching;
import org.junit.Test;
import org.quartz.CronExpression;

import static org.junit.Assume.assumeThat;
import static org.terracotta.upgradability.serialization.SerializationUpgradabilityTesting.validateSerializedForm;

/**
 *
 * @author cdennis
 */
public class CronExpressionSerializationTest {

  private static final Comparator<CronExpression> COMPARATOR = new Comparator<CronExpression>() {

    @Override
    public int compare(CronExpression o1, CronExpression o2) {
      if (o1.getCronExpression().equals(o2.getCronExpression()) && o1.getTimeZone().equals(o2.getTimeZone())) {
        return 0;
      } else {
        return -1;
      }
    }
  };
  
  @Test
  public void testSimpleCron() throws ParseException, IOException, ClassNotFoundException {
    CronExpression expression = new CronExpression("0 0 12 * * ?");
    validateSerializedForm(expression, COMPARATOR, "serializedforms/CronExpressionSerializationTest.testSimpleCron.ser");
  }
  
  @Test
  public void testComplexCron() throws ParseException, IOException, ClassNotFoundException {
    CronExpression expression = new CronExpression("0 0/5 14,18,20-23 ? JAN,MAR,SEP MON-FRI 2002-2010");
    validateSerializedForm(expression, COMPARATOR, "serializedforms/CronExpressionSerializationTest.testComplexCron.ser");
  }
  
  @Test
  public void testCronWithTimeZone() throws ParseException, IOException, ClassNotFoundException {
    assumeThat(TimeZone.getAvailableIDs(), ArrayMatching.hasItemInArray("Antarctica/South_Pole"));
    CronExpression expression = new CronExpression("0 0 12 * * ?");
    expression.setTimeZone(new SimplisticTimeZone("Terra Australis"));
    validateSerializedForm(expression, COMPARATOR, "serializedforms/CronExpressionSerializationTest.testCronWithTimeZone.ser");
  }
}

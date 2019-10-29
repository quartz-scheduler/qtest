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
package org.quartz;

import java.io.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CronExpressionTest extends SerializationTestSupport {
    private static final String[] VERSIONS = new String[] {"1.5.2"};

    private static final TimeZone EST_TIME_ZONE = TimeZone.getTimeZone("US/Eastern"); 

    /**
     * Get the object to serialize when generating serialized file for future
     * tests, and against which to validate deserialized object.
     */
    @Override
    protected Object getTargetObject() throws ParseException {
        CronExpression cronExpression = new CronExpression("0 15 10 * * ? 2005");
        cronExpression.setTimeZone(EST_TIME_ZONE);
        
        return cronExpression;
    }
    
    /**
     * Get the Quartz versions for which we should verify
     * serialization backwards compatibility.
     */
    @Override
    protected String[] getVersions() {
        return VERSIONS;
    }
    
    /**
     * Verify that the target object and the object we just deserialized 
     * match.
     */
    @Override
    protected void verifyMatch(Object target, Object deserialized) {
        CronExpression targetCronExpression = (CronExpression)target;
        CronExpression deserializedCronExpression = (CronExpression)deserialized;
        
        assertNotNull(deserializedCronExpression);
        assertEquals(targetCronExpression.getCronExpression(), deserializedCronExpression.getCronExpression());
        assertEquals(targetCronExpression.getTimeZone(), deserializedCronExpression.getTimeZone());
    }
    
    /*
     * Test method for 'org.quartz.CronExpression.isSatisfiedBy(Date)'.
     */
    public void testIsSatisfiedBy() throws Exception {
        CronExpression cronExpression = new CronExpression("0 15 10 * * ? 2005");
        
        Calendar cal = Calendar.getInstance();
        
        cal.set(2005, Calendar.JUNE, 1, 10, 15, 0);
        assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));
        
        cal.set(Calendar.YEAR, 2006);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));

        cal = Calendar.getInstance();
        cal.set(2005, Calendar.JUNE, 1, 10, 16, 0);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));

        cal = Calendar.getInstance();
        cal.set(2005, Calendar.JUNE, 1, 10, 14, 0);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));
    }

    public void testLastDayOffset() throws Exception {
        CronExpression cronExpression = new CronExpression("0 15 10 L-2 * ? 2010");
        
        Calendar cal = Calendar.getInstance();
        
        cal.set(2010, Calendar.OCTOBER, 29, 10, 15, 0); // last day - 2
        assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));
        
        cal.set(2010, Calendar.OCTOBER, 28, 10, 15, 0);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));
        
        cronExpression = new CronExpression("0 15 10 L-5W * ? 2010");
        
        cal.set(2010, Calendar.OCTOBER, 26, 10, 15, 0); // last day - 5
        assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));
        
        cronExpression = new CronExpression("0 15 10 L-1 * ? 2010");
        
        cal.set(2010, Calendar.OCTOBER, 30, 10, 15, 0); // last day - 1
        assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));
        
        cronExpression = new CronExpression("0 15 10 L-1W * ? 2010");
        
        cal.set(2010, Calendar.OCTOBER, 29, 10, 15, 0); // nearest weekday to last day - 1 (29th is a friday in 2010)
        assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));
        
    }

    /*
     * QUARTZ-571: Showing that expressions with months correctly serialize.
     */
    public void testQuartz571() throws Exception {
        CronExpression cronExpression = new CronExpression("19 15 10 4 Apr ? ");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cronExpression);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CronExpression newExpression = (CronExpression) ois.readObject();

        assertEquals(newExpression.getCronExpression(), cronExpression.getCronExpression());

        // if broken, this will throw an exception
        newExpression.getNextValidTimeAfter(new Date());
    }

    /**
     * QTZ-259 : last day offset causes repeating fire time
     * 
     */
 	public void testQtz259() throws Exception {
 		CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule("0 0 0 L-2 * ? *");
 		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("test").withSchedule(schedBuilder).build();
 				
 		int i = 0;
 		Date pdate = trigger.getFireTimeAfter(new Date());
 		while (++i < 26) {
 			Date date = trigger.getFireTimeAfter(pdate);
 			System.out.println("fireTime: " + date + ", previousFireTime: " + pdate);
 			assertFalse("Next fire time is the same as previous fire time!", pdate.equals(date));
 			pdate = date;
 		}
 	}
    
    /**
     * QTZ-259 : last day offset causes repeating fire time
     * 
     */
 	public void testQtz259LW() throws Exception {
 		CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule("0 0 0 LW * ? *");
 		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("test").withSchedule(schedBuilder).build();
 				
 		int i = 0;
 		Date pdate = trigger.getFireTimeAfter(new Date());
 		while (++i < 26) {
 			Date date = trigger.getFireTimeAfter(pdate);
 			System.out.println("fireTime: " + date + ", previousFireTime: " + pdate);
 			assertFalse("Next fire time is the same as previous fire time!", pdate.equals(date));
 			pdate = date;
 		}
 	}
 	
    /*
     * QUARTZ-574: Showing that storeExpressionVals correctly calculates the month number
     */
    public void testQuartz574() {
        try {
            new CronExpression("* * * * Foo ? ");
            fail("Expected ParseException did not fire for non-existent month");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Invalid Month value:"));
        }

        try {
            new CronExpression("* * * * Jan-Foo ? ");
            fail("Expected ParseException did not fire for non-existent month");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Invalid Month value:"));
        }
    }

    public void testQuartz621() {
        try {
            new CronExpression("0 0 * * * *");
            fail("Expected ParseException did not fire for wildcard day-of-month and day-of-week");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."));
        }
        try {
            new CronExpression("0 0 * 4 * *");
            fail("Expected ParseException did not fire for specified day-of-month and wildcard day-of-week");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."));
        }
        try {
            new CronExpression("0 0 * * * 4");
            fail("Expected ParseException did not fire for wildcard day-of-month and specified day-of-week");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."));
        }
    }

    public void testQuartz640() throws ParseException {
        try {
            new CronExpression("0 43 9 1,5,29,L * ?");
            fail("Expected ParseException did not fire for L combined with other days of the month");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Support for specifying 'L' and 'LW' with other days of the month is not implemented"));
        }
        try {
            new CronExpression("0 43 9 ? * SAT,SUN,L");
            fail("Expected ParseException did not fire for L combined with other days of the week");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Support for specifying 'L' with other days of the week is not implemented"));
        }
        try {
            new CronExpression("0 43 9 ? * 6,7,L");
            fail("Expected ParseException did not fire for L combined with other days of the week");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("Support for specifying 'L' with other days of the week is not implemented"));
        }
        try {
            new CronExpression("0 43 9 ? * 5L");
        } catch(ParseException pe) {
            fail("Unexpected ParseException thrown for supported '5L' expression.");
        }
    }
    
    
    public void testQtz96() throws ParseException {
        try {
            new CronExpression("0/5 * * 32W 1 ?");
            fail("Expected ParseException did not fire for W with value larger than 31");
        } catch(ParseException pe) {
            assertTrue("Incorrect ParseException thrown", 
                pe.getMessage().startsWith("The 'W' option does not make sense with values larger than"));
        }
    }

    public void testQtz395_CopyConstructorMustPreserveTimeZone () throws ParseException {
        TimeZone nonDefault = TimeZone.getTimeZone("Europe/Brussels");
        if (nonDefault.equals(TimeZone.getDefault())) {
            nonDefault = EST_TIME_ZONE;
        }
        CronExpression cronExpression = new CronExpression("0 15 10 * * ? 2005");
        cronExpression.setTimeZone(nonDefault);

        CronExpression copyCronExpression = new CronExpression(cronExpression);
        assertEquals(nonDefault, copyCronExpression.getTimeZone());
    }

    // Issue #58
    public void testSecRangeIntervalAfterSlash() throws Exception {
        // Test case 1
        try {
            new CronExpression("/120 0 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in '_blank/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 60 : 120");
        }

        // Test case 2
        try {
            new CronExpression("0/120 0 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in in '0/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 60 : 120");
        }

        // Test case 3
        try {
            new CronExpression("/ 0 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in '_blank/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }

        // Test case 4
        try {
            new CronExpression("0/ 0 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in '0/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }
    }


    // Issue #58
    public void testMinRangeIntervalAfterSlash() throws Exception {
        // Test case 1
        try {
            new CronExpression("0 /120 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in '_blank/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 60 : 120");
        }

        // Test case 2
        try {
            new CronExpression("0 0/120 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in in '0/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 60 : 120");
        }

        // Test case 3
        try {
            new CronExpression("0 / 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in '_blank/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }

        // Test case 4
        try {
            new CronExpression("0 0/ 8-18 ? * 2-6");
            fail("Cron did not validate bad range interval in '0/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }
    }

    // Issue #58
    public void testHourRangeIntervalAfterSlash() throws Exception {
        // Test case 1
        try {
            new CronExpression("0 0 /120 ? * 2-6");
            fail("Cron did not validate bad range interval in '_blank/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 24 : 120");
        }

        // Test case 2
        try {
            new CronExpression("0 0 0/120 ? * 2-6");
            fail("Cron did not validate bad range interval in in '0/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 24 : 120");
        }

        // Test case 3
        try {
            new CronExpression("0 0 / ? * 2-6");
            fail("Cron did not validate bad range interval in '_blank/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }

        // Test case 4
        try {
            new CronExpression("0 0 0/ ? * 2-6");
            fail("Cron did not validate bad range interval in '0/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }
    }

    // Issue #58
    public void testDayOfMonthRangeIntervalAfterSlash() throws Exception {
        // Test case 1
        try {
            new CronExpression("0 0 0 /120 * 2-6");
            fail("Cron did not validate bad range interval in '_blank/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 31 : 120");
        }

        // Test case 2
        try {
            new CronExpression("0 0 0 0/120 * 2-6");
            fail("Cron did not validate bad range interval in in '0/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 31 : 120");
        }

        // Test case 3
        try {
            new CronExpression("0 0 0 / * 2-6");
            fail("Cron did not validate bad range interval in '_blank/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }

        // Test case 4
        try {
            new CronExpression("0 0 0 0/ * 2-6");
            fail("Cron did not validate bad range interval in '0/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }
    }

    // Issue #58
    public void testMonthRangeIntervalAfterSlash() throws Exception {
        // Test case 1
        try {
            new CronExpression("0 0 0 ? /120 2-6");
            fail("Cron did not validate bad range interval in '_blank/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 12 : 120");
        }

        // Test case 2
        try {
            new CronExpression("0 0 0 ? 0/120 2-6");
            fail("Cron did not validate bad range interval in in '0/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 12 : 120");
        }

        // Test case 3
        try {
            new CronExpression("0 0 0 ? / 2-6");
            fail("Cron did not validate bad range interval in '_blank/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }

        // Test case 4
        try {
            new CronExpression("0 0 0 ? 0/ 2-6");
            fail("Cron did not validate bad range interval in '0/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }
    }



    // Issue #58
    public void testDayOfWeekRangeIntervalAfterSlash() throws Exception {
        // Test case 1
        try {
            new CronExpression("0 0 0 ? * /120");
            fail("Cron did not validate bad range interval in '_blank/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 7 : 120");
        }

        // Test case 2
        try {
            new CronExpression("0 0 0 ? * 0/120");
            fail("Cron did not validate bad range interval in in '0/xxx' form");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Increment > 7 : 120");
        }

        // Test case 3
        try {
            new CronExpression("0 0 0 ? * /");
            fail("Cron did not validate bad range interval in '_blank/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }

        // Test case 4
        try {
            new CronExpression("0 0 0 ? * 0/");
            fail("Cron did not validate bad range interval in '0/_blank'");
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "'/' must be followed by an integer.");
        }
    }
    
    public void testGetTimeBefore() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2019, 02, 29, 13, 32, 05);
        final Date now = calendar.getTime();

        calendar.set(2019, 02, 29, 13, 30, 00);
        assertEquals(calendar.getTime(), new CronExpression("0 */5 * * * ? *").getTimeBefore(now));

        calendar.set(2018, 00, 01, 00, 00, 00);
        assertEquals(calendar.getTime(), new CronExpression("0 0 0 1 1 ? */2").getTimeBefore(now));
    }

    // execute with version number to generate a new version's serialized form
    public static void main(String[] args) throws Exception {
        new CronExpressionTest().writeJobDataFile("1.5.2");
    }

}

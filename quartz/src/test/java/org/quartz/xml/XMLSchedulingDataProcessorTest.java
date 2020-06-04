package org.quartz.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.xml.sax.SAXParseException;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.jobs.NoOpJob;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;

/**
 * Unit test for XMLSchedulingDataProcessor.
 *
 * @author Zemian Deng
 */
public class XMLSchedulingDataProcessorTest extends TestCase {
	
	/** QTZ-185
	 * <p>The default XMLSchedulingDataProcessor will setOverWriteExistingData(true), and we want to
	 * test programmatically overriding this value.
	 * 
	 * <p>Note that XMLSchedulingDataProcessor#processFileAndScheduleJobs(Scheduler,boolean) will only
	 * read default "quartz_data.xml" in current working directory. So to test this, we must create
	 * this file. If this file already exist, it will be overwritten! 
	 */
	public void testOverwriteFlag() throws Exception {
		//Prepare a quartz_data.xml in current working directory by copy a test case file.
		File file = new File(XMLSchedulingDataProcessor.QUARTZ_XML_DEFAULT_FILE_NAME);
		copyResourceToFile("/org/quartz/xml/simple-job-trigger.xml", file);
		
		Scheduler scheduler = null;
		try {
			StdSchedulerFactory factory = new StdSchedulerFactory("org/quartz/xml/quartz-test.properties");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			
			// Let's setup a fixture job data that we know test is not going modify it.
			JobDetail job = new JobDetail("job1", NoOpJob.class);
			job.getJobDataMap().put("foo", "dont_chg_me");
			Trigger trigger = new SimpleTrigger("job1", SimpleTrigger.REPEAT_INDEFINITELY, 60 * 60 * 1000);
			scheduler.scheduleJob(job, trigger);			
			
			ClassLoadHelper clhelper = new CascadingClassLoadHelper();
			clhelper.initialize();
			XMLSchedulingDataProcessor processor = new XMLSchedulingDataProcessor(clhelper);
			try {
				processor.processFileAndScheduleJobs(scheduler, false);
				fail("OverWriteExisting flag didn't work. We should get Exception when overwrite is set to false.");
			} catch (ObjectAlreadyExistsException e) {
				// This is expected. Do nothing.
			}
			
			// We should still have what we start with.
			assertEquals(1, scheduler.getJobNames("DEFAULT").length);
			assertEquals(1, scheduler.getTriggerNames("DEFAULT").length);
			
			job = scheduler.getJobDetail("job1", "DEFAULT");
			String fooValue = job.getJobDataMap().getString("foo");
			assertEquals("dont_chg_me", fooValue);
		} finally {
			// remove test file
			if(file.exists() && !file.delete())
				throw new RuntimeException("Failed to remove test file " + file);
			
			// shutdown scheduler
			if (scheduler != null)
				scheduler.shutdown();
		}
	}
	
	private void copyResourceToFile(String resName, File file) throws IOException {
		// Copy streams
		InputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			// Copy input resource stream to output file.
			inStream = getClass().getResourceAsStream(resName);
			outStream = new FileOutputStream(file);
			
			int BLOCK_SIZE = 1024 * 1024 * 5; // 5 MB
			byte[] buffer = new byte[BLOCK_SIZE];
			int len = -1;
			while ((len = inStream.read(buffer, 0, BLOCK_SIZE)) != -1) {
				outStream.write(buffer, 0, len);
			}
		} finally {
			if (outStream != null)
				outStream.close();
			if (inStream != null)
				inStream.close();
		}
	}
	
	/** QTZ-187 */
	public void testDirectivesNoOverwriteWithIgnoreDups() throws Exception {
		Scheduler scheduler = null;
		try {
			StdSchedulerFactory factory = new StdSchedulerFactory("org/quartz/xml/quartz-test.properties");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			
			// Setup existing job with same names as in xml data.
			JobDetail job = new JobDetail("job1", NoOpJob.class);
			Trigger trigger = new SimpleTrigger("job1", SimpleTrigger.REPEAT_INDEFINITELY, 60 * 60 * 1000);
			scheduler.scheduleJob(job, trigger);

			job = new JobDetail("job2", NoOpJob.class);
			trigger = new SimpleTrigger("job2", SimpleTrigger.REPEAT_INDEFINITELY, 60 * 60 * 1000);
			
			scheduler.scheduleJob(job, trigger);
			
			// Now load the xml data with directives: overwrite-existing-data=false, ignore-duplicates=true
			ClassLoadHelper clhelper = new CascadingClassLoadHelper();
			clhelper.initialize();
			XMLSchedulingDataProcessor processor = new XMLSchedulingDataProcessor(clhelper);
			processor.processFileAndScheduleJobs("org/quartz/xml/directives_no-overwrite_ignoredups.xml", scheduler);
			assertEquals(2, scheduler.getJobNames("DEFAULT").length);
			assertEquals(2, scheduler.getTriggerNames("DEFAULT").length);
		} finally {
			if (scheduler != null)
				scheduler.shutdown();
		}
	}
	
	/** QTZ-146 */
	public void _testXsdSchemaValidationOnVariousTriggers() throws Exception {
		Scheduler scheduler = null;
		try {
			StdSchedulerFactory factory = new StdSchedulerFactory("org/quartz/xml/quartz-test.properties");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			ClassLoadHelper clhelper = new CascadingClassLoadHelper();
			clhelper.initialize();
			XMLSchedulingDataProcessor processor = new XMLSchedulingDataProcessor(clhelper);
			processor.processFileAndScheduleJobs("org/quartz/xml/job-scheduling-data-1.8_trigger-samples.xml", scheduler);
			assertEquals(1, scheduler.getJobNames("DEFAULT").length);
			assertEquals(32, scheduler.getTriggerNames("DEFAULT").length);
		} finally {
			if (scheduler != null)
				scheduler.shutdown();
		}
	}

	public void testXmlParserConfiguration() throws Exception {
		Scheduler scheduler = null;
		try {

			StdSchedulerFactory factory = new StdSchedulerFactory("org/quartz/xml/quartz-test.properties");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			ClassLoadHelper clhelper = new CascadingClassLoadHelper();
			clhelper.initialize();
			XMLSchedulingDataProcessor processor = new XMLSchedulingDataProcessor(clhelper);
			processor.processFileAndScheduleJobs("org/quartz/xml/bad-job-config.xml", scheduler);

			final JobDetail jobDetail = scheduler.getJobDetail("xxe", "native");
			final String description = jobDetail.getDescription();

			fail("Expected parser configuration to block DOCTYPE. The following was injected into the job description field: " + description);
		} catch (SAXParseException e) {
			assertTrue(e.getMessage().contains("DOCTYPE is disallowed"));
		} finally {
			if (scheduler != null)
				scheduler.shutdown();
		}
	}
}

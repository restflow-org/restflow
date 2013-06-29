package org.restflow.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.restflow.RestFlow;
import org.restflow.metadata.RunMetadata;
import org.restflow.reporter.TraceReporter;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;
import org.restflow.util.TestUtilities;


abstract public class RestFlowCommandTestCase extends TestCase {

	private static String TEST_RUNS_DIRECTORY = TestUtilities.getTestRunsDirectoryPath();
	
	protected String testResourcePath;
	protected File testResourceDirectory;	
	protected String testName;
	protected RunMetadata testRun;
	protected File testRunDirectory;
	protected File testWorkingDirectory;
	
	protected final static String EOL = System.getProperty("line.separator");
	
	protected void createTestEnvironment(String resourcePath) throws Exception {
		testResourcePath = resourcePath;
		testResourceDirectory = new File(PortableIO.getCurrentDirectoryPath() + testResourcePath);
		testName = testResourceDirectory.getName();
		testWorkingDirectory = PortableIO.createUniqueTimeStampedDirectory(TEST_RUNS_DIRECTORY, testName);
	}

	protected void runRestFlowWithArguments(final String[] args) throws Exception {
		
		StdoutRecorder stdoutRecorder = new StdoutRecorder(true, true);

		stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				testRun = RestFlow.loadAndRunWorkflow(args);
			}
		});

		testRunDirectory = new File(testRun.getRunDirectory());
	}

	protected String getExpected(String filename) throws IOException {
		return PortableIO.readTextFileOnFilesystem(testResourceDirectory + "/" + filename);
	}

	protected void assertFileResourcesMatchExactly(String resourcePath) throws Exception {
		File expectedRoot = new File(testResourceDirectory + "/expected/");
		File actualRoot = new File(testWorkingDirectory.getAbsolutePath());
		TestUtilities.assertFilesystemResourceValid(expectedRoot, actualRoot, resourcePath);
	}

	protected void assertFileMatchesTemplate(String resourcePath) throws Exception {
		File expectedRoot = new File(testResourceDirectory + "/expected/");
		File actualRoot = new File(testWorkingDirectory.getAbsolutePath());		
		TestUtilities.assertFilesystemResourceMatchesTemplate(expectedRoot, actualRoot, resourcePath);
	}

	public String getTraceReport() throws Exception {
		return TraceReporter.getReport(testRun.getTrace());
	}
}

package org.restflow.test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.restflow.RestFlow;
import org.restflow.metadata.RunMetadata;
import org.restflow.reporter.TraceReporter;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;
import org.restflow.util.TestUtilities;
import org.restflow.util.PortableIO.StreamSink;


abstract public class RestFlowCommandTestCase extends TestCase {

	private static String TEST_RUNS_DIRECTORY = TestUtilities.getTestRunsDirectoryPath();
	
	protected String testCaseResourcePath;
	protected File testCaseResourceDirectory;
	protected File testResourceDirectory;	
	protected File testRunDirectory;
	protected File testWorkingDirectory;
	protected RunMetadata testRun;
	
	protected final static String EOL = System.getProperty("line.separator");
	
	@Override
	public void setUp() throws IOException {
		testCaseResourceDirectory = new File(PortableIO.getCurrentDirectoryPath() + testCaseResourcePath);
	}
	
	protected void initializeTestEnvironment(String testDirectoryName) throws Exception {
		testResourceDirectory = new File(testCaseResourceDirectory + "/" + testDirectoryName);
		testWorkingDirectory = PortableIO.createUniqueTimeStampedDirectory(TEST_RUNS_DIRECTORY, testDirectoryName);
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

	protected void assertFileResourcesMatchExactly(String expectedDirectory, String expectedResourcePath, String actualResourcePath) throws Exception {
		File expectedRoot = new File(expectedDirectory + "/" + expectedResourcePath);
		File actualRoot = new File(testWorkingDirectory.getAbsolutePath());
		TestUtilities.assertFilesystemResourceValid(expectedRoot, actualRoot, actualResourcePath);
	}

	protected void assertFileMatchesTemplate(String resourcePath) throws Exception {
		File expectedRoot = new File(testResourceDirectory + "/expected/");
		File actualRoot = new File(testWorkingDirectory.getAbsolutePath());		
		TestUtilities.assertFilesystemResourceMatchesTemplate(expectedRoot, actualRoot, resourcePath);
	}

	public String getTraceReport() throws Exception {
		return TraceReporter.getReport(testRun.getTrace());
	}
	
	protected static void assertMatchesPattern(String expected, String actual) throws IOException, InterruptedException {
		
		if (!Pattern.compile(expected).matcher(actual).matches()) {
			assertEquals(expected, actual);
		}
	}
}

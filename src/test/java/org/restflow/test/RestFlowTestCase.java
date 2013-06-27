package org.restflow.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.restflow.beans.TextScanner;
import org.restflow.util.Contract;
import org.restflow.util.PortableIO;
import org.restflow.util.TestUtilities;
import org.restflow.util.PortableIO.StreamSink;

import junit.framework.TestCase;

public abstract class RestFlowTestCase extends TestCase {

	protected final static String EOL = System.getProperty("line.separator");
	
	public void setUp() throws Exception {
		super.setUp();
		Contract.setThrowExceptions(true);
	}
	
	static StreamSink[] run(String cmdLine, String stdIn) throws IOException, InterruptedException {
		
		Process p = Runtime.getRuntime().exec(cmdLine);

		PrintStream p_stdin = new PrintStream(p.getOutputStream(),true);
		p_stdin.print(stdIn);
		p_stdin.close();
		
		StreamSink[] p_out = { new StreamSink(p.getInputStream()), new StreamSink(p.getErrorStream()) };
		
		Thread p1 = new Thread(p_out[0]);
		p1.start();
		Thread p2 = new Thread(p_out[1]);
		p2.start();
		
		p.waitFor();
		p1.join();
		p2.join();
		
		System.out.println("STDOUT: <<<<<" + EOL + p_out[0].toString() + ">>>>");
		System.out.println("STDERR: <<<<<" + EOL + p_out[1].toString() + ">>>>");
		return p_out;
	}	
	
	public File getRunDirectoryForTest(String testName) throws Exception {
		 String testRunsDirectory = TestUtilities.getTestRunsDirectoryPath();
		 File testDirectory = PortableIO.createUniqueTimeStampedDirectory(testRunsDirectory, testName);
		 return testDirectory;
	}
	
	protected static void verifyRunRegexp(String cmdLine, String stdIn, String expected_stdout, String expected_stderr) throws IOException, InterruptedException {
		StreamSink[] p_out = run(cmdLine, stdIn);
	
		if (!Pattern.compile(expected_stdout,Pattern.DOTALL).matcher(p_out[0].toString()).matches()) {
			fail("STDOUT does not match " + expected_stdout + PortableIO.EOL + " STDOUT was" + EOL + p_out[0].toString());
		}
		
		if (!Pattern.compile(expected_stderr,Pattern.DOTALL).matcher(p_out[1].toString()).matches()) {
			fail("STDERR does not match " + expected_stderr + PortableIO.EOL + " STDERR was" + EOL + p_out[1].toString());
		}
	}

	protected static void assertMatchesRegexp(String expected, String actual) throws IOException, InterruptedException {
	
		if (!Pattern.compile(expected).matcher(actual).matches()) {
			assertEquals(expected, actual);
		}
	}
	
	public void verifyRunExact(String cmdLine, String stdIn, String expected_stdout, String expected_stderr) throws IOException, InterruptedException {
		StreamSink[] p_out = run(cmdLine, stdIn);
	
		assertEquals(expected_stderr, p_out[1].toString());
		assertEquals(expected_stdout, p_out[0].toString());
	}





	
}

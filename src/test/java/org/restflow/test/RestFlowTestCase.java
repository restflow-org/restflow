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


	public static Map<String,Object> verifyRunWithTemplate(String template, String text) throws Exception {
	
	TextScanner s = new TextScanner();
	s.addDefaultTags();

	s.setAbsorbWhiteSpaceSymbol("~");
	
	s.setTokenMatcherPrefix("<%");
	s.setTokenMatcherSuffix("%>");
	
	
	List<String> params = new Vector<String>();
	params.add("<%match:EXPECTED_TEXT%>");
	s.getTags().put("DATE","<%month:STRING%> <%day:INT%>");			
	s.getTags().put("TIME","<%hours:INT%>:<%minutes:INT%>:<%seconds:INT%>");								
	s.getTags().put("LOG_STAMP","<%:DATE%> <%time:TIME%>");								
	s.getTags().put("EXPECTED_TEXT",template);												
	
	s.setTemplate(params);
	s.compile();
	// match this type of log stamp Feb 24 10:18:21
	
	Map<String,Object> result = s.search(text);
	return result;
	}

	public static void assertStringMatchesTemplate(String template, String actual) throws Exception {

		Map<String,Object> result = verifyRunWithTemplate(template, actual);
		if (result.get("match") == null) {
			assertEquals(template, actual);
		}
	}
	
	public void assertFilesystemResourceValid(File expectedRoot, File actualRoot, String relativePath) throws Exception {

		// access the expected resource and make sure it exists
		File expected = new File(expectedRoot.getAbsolutePath() + "/" + relativePath);
		assertTrue(expected.exists());
		
		// access the actual resource and make sure it exists
		File actual = new File(actualRoot.getAbsolutePath() + "/" + relativePath);
		assertTrue(actual.exists());
		
		// handle case where expected resource is a file
		if (expected.isFile()) {
			
			assertTrue(actual.isFile());
			assertFileContentsSame(expected, actual);

		// handle case where expected resource is a directory
		} else {
			
			assertTrue(expected.isDirectory());
			assertTrue(actual.isDirectory());

			//_assertDirectoryContentsSame(relativePath, expected, actual);			

			String[] resourceNameArray = expected.list();
			int resourceCount = 0;
			for (int i = 0; i < resourceNameArray.length; i++) {
				String resourceName = resourceNameArray[i];
				if (resourceName.charAt(0) != '.') {
					String relativeResourcePath = relativePath + "/" + resourceName;
					assertFilesystemResourceValid(expectedRoot, actualRoot, relativeResourcePath);
					resourceCount++;
				}
			}
			assertEquals(resourceCount, actual.list().length);
		
		}
	}

	public void assertFilesystemResourceMatchesTemplate(File expectedRoot, File actualRoot, String relativePath) throws Exception {

		// access the expected resource and make sure it exists
		File expected = new File(expectedRoot.getAbsolutePath() + "/" + relativePath);
		assertTrue(expected.exists());
		
		// access the actual resource and make sure it exists and is a file
		File actual = new File(actualRoot.getAbsolutePath() + "/" + relativePath);
		assertTrue(actual.exists());
		assertTrue(actual.isFile());
		
		String templateText = PortableIO.readTextFile(expected);
		String actualText = PortableIO.readTextFile(actual);
		WorkflowTestCase.assertStringMatchesTemplate(templateText, actualText);
	}
	
//	private void _assertDirectoryContentsSame(String relativeDirectoryPath, File expected, File actual) throws Exception {
//		
//		String[] resourceNameArray = expected.list();
//		int resourceCount = 0;
//		for (int i = 0; i < resourceNameArray.length; i++) {
//			String resourceName = resourceNameArray[i];
//			if (resourceName.charAt(0) != '.') {
//				String relativeResourcePath = relativeDirectoryPath + "/" + resourceName;
//				assertFilesystemResourceValid(relativeResourcePath);
//				resourceCount++;
//			}
//		}
//		assertEquals(resourceCount, actual.list().length);
//	}
	
	private void assertFileContentsSame(File expected, File actual) throws Exception {
		
		String expectedContents = PortableIO.readTextFile(expected);
		String actualContents = PortableIO.readTextFile(actual);
		assertEquals(expectedContents, actualContents);
	}

	protected void assertStringsEqualWhenLineEndingsNormalized(String one, String two) {		 
		assertEquals(PortableIO.normalizeLineEndings(one), PortableIO.normalizeLineEndings(two));
	}
	
}

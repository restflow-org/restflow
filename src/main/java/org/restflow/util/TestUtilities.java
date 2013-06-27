package org.restflow.util;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.restflow.WorkflowContext;
import org.restflow.beans.TextScanner;


public class TestUtilities {

	public static String getTestRunsDirectoryPath() {
	 
		// get the runs directory location from environment variable if defined
		String testRunsDirectoryPath = System.getenv("RESTFLOW_TESTRUNS_DIR");
		
		// otherwise put the runs directory within the user's temporary directory
		if (testRunsDirectoryPath == null) {
			testRunsDirectoryPath = WorkflowContext.getUserTemporaryDirectoryPath() + "/testruns";
		}
		
		// return the path to the test runs directory
		return testRunsDirectoryPath;
	}

	public static void assertStringMatchesTemplate(String template, String actual) throws Exception {

		Map<String,Object> result = verifyRunWithTemplate(template, actual);
		if (result.get("match") == null) {
			TestCase.assertEquals(template, actual);
		}
	}
	
	public static void assertFilesystemResourceValid(File expectedRoot, File actualRoot, String relativePath) throws Exception {

		// access the expected resource and make sure it exists
		File expected = new File(expectedRoot.getAbsolutePath() + "/" + relativePath);
		TestCase.assertTrue(expected.exists());
		
		// access the actual resource and make sure it exists
		File actual = new File(actualRoot.getAbsolutePath() + "/" + relativePath);
		TestCase.assertTrue(actual.exists());
		
		// handle case where expected resource is a file
		if (expected.isFile()) {
			
			TestCase.assertTrue(actual.isFile());
			assertFileContentsSame(expected, actual);

		// handle case where expected resource is a directory
		} else {
			
			TestCase.assertTrue(expected.isDirectory());
			TestCase.assertTrue(actual.isDirectory());

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
			TestCase.assertEquals(resourceCount, actual.list().length);
		
		}
	}

	public static void assertFilesystemResourceMatchesTemplate(File expectedRoot, File actualRoot, String relativePath) throws Exception {

		// access the expected resource and make sure it exists
		File expected = new File(expectedRoot.getAbsolutePath() + "/" + relativePath);
		TestCase.assertTrue(expected.exists());
		
		// access the actual resource and make sure it exists and is a file
		File actual = new File(actualRoot.getAbsolutePath() + "/" + relativePath);
		TestCase.assertTrue(actual.exists());
		TestCase.assertTrue(actual.isFile());
		
		String templateText = PortableIO.readTextFile(expected);
		String actualText = PortableIO.readTextFile(actual);
		assertStringMatchesTemplate(templateText, actualText);
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
	
	public static void assertFileContentsSame(File expected, File actual) throws Exception {
		
		String expectedContents = PortableIO.readTextFile(expected);
		String actualContents = PortableIO.readTextFile(actual);
		TestCase.assertEquals(expectedContents, actualContents);
	}

	public static void assertStringsEqualWhenLineEndingsNormalized(String one, String two) {		 
		TestCase.assertEquals(PortableIO.normalizeLineEndings(one), PortableIO.normalizeLineEndings(two));
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
}

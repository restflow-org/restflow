package org.restflow.util;

import org.restflow.WorkflowContext;


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
}

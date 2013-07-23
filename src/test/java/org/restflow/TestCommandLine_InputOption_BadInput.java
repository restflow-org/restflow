package org.restflow;

import org.restflow.test.RestFlowCommandTestCase;

public class TestCommandLine_InputOption_BadInput extends RestFlowCommandTestCase {

	private String testResourcePath = "/src/test/resources/org/restflow/test/TestCommandLineIO/";

	public void test_InputRepeater_WrongInputName() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=4",
				"-i", "badInputValue=3" 
			}
		);
		
		System.out.println(testRun.getStderr());
		assertTrue(testRun.getStderr().contains("InputRepeater does not accept input 'badInputValue'"));
	}

	public void test_InputRepeater_MissingInput() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
			}
		);

		assertTrue(testRun.getStderr().contains("InputRepeater requires missing input 'inputValue'"));
	}	
}
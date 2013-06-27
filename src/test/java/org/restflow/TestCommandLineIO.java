package org.restflow;

import org.restflow.test.RestFlowCommandTestCase;

public class TestCommandLineIO extends RestFlowCommandTestCase {

	private String testResourcePath = "/src/test/resources/org/restflow/test/TestCommandLineIO/";
	
	public void test_NumberAdder_IntegerInputs() throws Exception {
				
		createTestEnvironment(testResourcePath + "Adder");
		
		runRestFlowWithArguments( 
			" -base " + testWorkingDirectory +
			" -run run" +
			" -f classpath:/org/restflow/test/TestCommandLineIO/NumberAdder/NumberAdder.yaml" +
			" -i a=3" +
			" -i b=17" +
			" -o c:" + testWorkingDirectory + "/sum.txt"
		);
		
//		assertEquals(getExpected("stderr.txt"), testRun.getStderr());
//		assertEquals(getExpected("stdout.txt"), testRun.getStdoutText());
//	
//		assertFileResourcesMatchExactly("output.tabular");
	}

}
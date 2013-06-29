package org.restflow;

import org.restflow.test.RestFlowCommandTestCase;

public class TestCommandLine_StandardOutput extends RestFlowCommandTestCase {

	private String testResourcePath = "/src/test/resources/org/restflow/test/TestCommandLineIO/";

	public void test_NoOutputToStandardOut_Value() throws Exception {
		createTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=3"
			}
		);
		assertEquals("", testRun.getStderr());		
		assertEquals("", testRun.getStdoutText());
	}

	public void test_OutputToStandardOut_Value_UnqualifiedOption() throws Exception {
		createTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=3",
				"-o", "outputValue"
			}
		);
		assertEquals("", testRun.getStderr());		
		assertEquals("3", testRun.getStdoutText());
	}

	public void test_OutputToStandardOut_Value_ColonMinus() throws Exception {
		createTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=3",
				"-o", "outputValue:-"
			}
		);
		assertEquals("", testRun.getStderr());		
		assertEquals("3", testRun.getStdoutText());
	}

	public void test_NoOutputToStandardOut_File() throws Exception {
		createTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/StringConcatenator.yaml",
				"-i", "s1='hello '",
				"-i", "s2=world",
			}
		);
		assertEquals("", testRun.getStderr());
		assertEquals("", testRun.getStdoutText());		
	}
		
	public void test_OutputToStandardOut_File_UnqualifiedOption() throws Exception {
		createTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/StringConcatenator.yaml",
				"-i", "s1='hello '",
				"-i", "s2=world",
				"-o", "s1s2"
			}
		);
		assertEquals("", testRun.getStderr());
		assertEquals("hello world", testRun.getStdoutText());		
	}
		
	public void test_OutputToStandardOut_File_ColonMinus() throws Exception {
		createTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/StringConcatenator.yaml",
				"-i", "s1='hello '",
				"-i", "s2=world",
				"-o", "s1s2:-"
			}
		);
		assertEquals("", testRun.getStderr());
		assertEquals("hello world", testRun.getStdoutText());		
	}
}

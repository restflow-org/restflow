package org.restflow;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.restflow.test.RestFlowCommandTestCase;

public class TestCommandLine_OutputOption extends RestFlowCommandTestCase {

	private String testResourcePath = "/src/test/resources/org/restflow/test/TestCommandLineIO/";

	public void test_NoOutputToStandardOut_Value() throws Exception {
		initializeTestEnvironment(testResourcePath);
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

	public void test_OutputToStandardOut_Value_NoDash() throws Exception {
		initializeTestEnvironment(testResourcePath);
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

	public void test_OutputToStandardOut_Value_Dash() throws Exception {
		initializeTestEnvironment(testResourcePath);
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
		initializeTestEnvironment(testResourcePath);
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
		
	public void test_OutputToStandardOut_File_NoDash() throws Exception {
		initializeTestEnvironment(testResourcePath);
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
		
	public void test_OutputToStandardOut_File_Dash() throws Exception {
		initializeTestEnvironment(testResourcePath);
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

	public void test_TwoOutputs_BothToFiles() throws Exception {
		initializeTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/IntegerDivider.yaml",
				"-w", "IntegerDivider",
				"-i", "numerator=23",
				"-i", "divisor=5",
				"-o", "quotient:" + testWorkingDirectory + "/quotient.txt",
				"-o", "remainder:" + testWorkingDirectory + "/remainder.txt"
			}
		);
		assertEquals("", testRun.getStderr());
		assertEquals("4", FileUtils.readFileToString(new File(testWorkingDirectory + "/quotient.txt")));
		assertEquals("3", FileUtils.readFileToString(new File(testWorkingDirectory + "/remainder.txt")));
	}
	
	public void test_TwoOutputs_OneToStandardOut() throws Exception {
		initializeTestEnvironment(testResourcePath);
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/IntegerDivider.yaml",
				"-w", "IntegerDivider",
				"-i", "numerator=23",
				"-i", "divisor=5",
				"-o", "quotient",
				"-o", "remainder:" + testWorkingDirectory + "/remainder.txt"
			}
		);
		assertEquals("", testRun.getStderr());
		assertEquals("4", testRun.getStdoutText());
		assertEquals("3", FileUtils.readFileToString(new File(testWorkingDirectory + "/remainder.txt")));
	}

	public void test_TwoOutputs_BothToStandardOut_NoDash() throws Exception {
		initializeTestEnvironment(testResourcePath);

		try {
			runRestFlowWithArguments( new String[] {
					"-base", testWorkingDirectory.getPath(),
					"-run", "run",
					"-f", "classpath:/org/restflow/test/TestCommandLineIO/IntegerDivider.yaml",
					"-w", "IntegerDivider",
					"-i", "numerator=23",
					"-i", "divisor=5",
					"-o", "quotient",
					"-o", "remainder"
				}
			);
			fail("Exception expected");
		} catch (Exception e) {
			assertEquals("Only one output may be sent to stdout.", e.getMessage());
		}
	}

	public void test_TwoOutputs_BothToStandardOut_Dash() throws Exception {
		initializeTestEnvironment(testResourcePath);

		try {
			runRestFlowWithArguments( new String[] {
					"-base", testWorkingDirectory.getPath(),
					"-run", "run",
					"-f", "classpath:/org/restflow/test/TestCommandLineIO/IntegerDivider.yaml",
					"-w", "IntegerDivider",
					"-i", "numerator=23",
					"-i", "divisor=5",
					"-o", "quotient:-",
					"-o", "remainder:-"
				}
			);
			fail("Exception expected");
		} catch (Exception e) {
			assertEquals("Only one output may be sent to stdout.", e.getMessage());
		}
	}

	public void test_InvalidOutputName() throws Exception {
		initializeTestEnvironment(testResourcePath);

		try {
			runRestFlowWithArguments( new String[] {
					"-base", testWorkingDirectory.getPath(),
					"-run", "run",
					"-f", "classpath:/org/restflow/test/TestCommandLineIO/IntegerDivider.yaml",
					"-w", "IntegerDivider",
					"-i", "numerator=23",
					"-i", "divisor=5",
					"-o", "dividend:-"
				}
			);
			fail("Exception expected");
		} catch (Exception e) {
			assertEquals("IntegerDivider does not produce requested output dividend.", e.getMessage());
		}
	}

}

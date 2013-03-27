package org.restflow.test.system;

import org.restflow.test.WorkflowTestCase;

public class TestSampleWorkflowsUnix extends WorkflowTestCase {

	public TestSampleWorkflowsUnix() {
		super("samples");
	}
	
	public void test_Hello8() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello8", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello8_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("hello8_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_bash_program() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("files", "bashProgram", _dataDrivenDirector(), "BashProgram");
		assertStringsEqualWhenLineEndingsNormalized(_getExpectedStdout("bashProgram_stdout.txt"), _runner.getStdoutRecording());		
		assertEquals(_getExpectedResultFile("bashProgram_trace.txt"), _runner.getTraceReport());;		
	}
}

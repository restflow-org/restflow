package org.restflow.test.system;

import org.restflow.test.WorkflowTestCase;

public class TestWorkflowsTcl extends WorkflowTestCase {

	public TestWorkflowsTcl() {
		super("src/test/resources/workflows/");
	}

	public void test_HelloWorld_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();
		_loadAndRunWorkflow("HelloWorld", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_BranchingWorkflow_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();

		_loadAndRunWorkflow("BranchingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_MergingWorkflow_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();

		_loadAndRunWorkflow("MergingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_CountToThree_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();

		_loadAndRunWorkflow("CountToThree", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_IntegerFilter_TclhActor_DataDrivenDirector() throws Exception {
		configureForTclActor();
		
		_loadAndRunWorkflow("IntegerFilter", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
	
	public void test_AdderLoop_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();

		_loadAndRunWorkflow("AdderLoop", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}		
	
	public void test_IntegerStreamMergeDuplicates_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
		System.out.println(_runner.getStderrRecording());
	}	
	
	public void test_HammingSequence_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();
		
		_loadAndRunWorkflow("HammingSequence", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}
	
	public void test_FailedScript_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();
		_loadAndRunWorkflow("BadScript", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		
		//look for the tcl error
		assertTrue(_runner.getStderrRecording().startsWith(
				"Actor <BadScript>[BadNode]<BadActor> threw exception: java.lang.Exception: invalid command name \"output\""));
		assertTrue(_runner.getStderrRecording().contains("while executing"));
		assertTrue(_runner.getStderrRecording().contains("\"output = $value\""));
	}
	
}

package org.restflow.test.system;

import org.restflow.test.WorkflowTestCase;

public class TestWorkflowsPython extends WorkflowTestCase {

	public TestWorkflowsPython() {
		super("workflows");
	}

	public void test_HelloWorld_PythonActor_DataDrivenDirector() throws Exception {
		configureForPythonActor();
		_loadAndRunWorkflow("HelloWorld", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_BranchingWorkflow_PythonActor_DataDrivenDirector() throws Exception {
		configureForPythonActor();

		_loadAndRunWorkflow("BranchingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}
	
	public void test_MergingWorkflow_PythonActor_DataDrivenDirector() throws Exception {
		configureForPythonActor();

		_loadAndRunWorkflow("MergingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_CountToThree_PythonActor_DataDrivenDirector() throws Exception {
		configureForPythonActor();

		_loadAndRunWorkflow("CountToThree", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_IntegerFilter_PythonActor_DataDrivenDirector() throws Exception {
		configureForPythonActor();
		
		_loadAndRunWorkflow("IntegerFilter", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
	
	public void test_AdderLoop_PythonActor_DataDrivenDirector() throws Exception {
		configureForPythonActor();

		_loadAndRunWorkflow("AdderLoop", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}		
	
	public void test_IntegerStreamMergeDuplicates_PythonActor_DataDrivenDirector() throws Exception {
		configureForPythonActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
		System.out.println(_runner.getStderrRecording());
	}	
	
	public void test_HammingSequence_PythonActor_MTDataDrivenDirector() throws Exception {
		configureForPythonActor();
		
		_loadAndRunWorkflow("HammingSequence", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}	
}

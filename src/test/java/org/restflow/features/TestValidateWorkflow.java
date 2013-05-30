package org.restflow.features;

import org.restflow.RestFlow;
import org.restflow.WorkflowRunner;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestValidateWorkflow extends RestFlowTestCase {

	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void testValidate_NoOutflowVariablesInTemplate() throws Exception {
		
		final String workflowFile = "classpath:/org/restflow/test/TestValidateWorkflow/FileProtocolWithoutOutflowUriVariables" +
			WorkflowRunner.YAML_EXTENSION;

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				RestFlow.loadAndRunWorkflow( 
					new String[]{ "-v", "-f", workflowFile, "-base", "RESTFLOW_TESTRUNS_DIR" } );
			}
		});
		
		assertTrue(recorder.getStderrRecording().contains(
				"No variables in outflow template with file scheme: file:/messages/greeting on CreateGreeting"));
	}
}
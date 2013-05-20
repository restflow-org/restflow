package org.restflow.features;

import org.restflow.RestFlow;
import org.restflow.WorkflowRunner;
import org.restflow.metadata.RunMetadata;
import org.restflow.test.RestFlowTestCase;


public class TestValidateOnly extends RestFlowTestCase {

	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void testOne() throws Exception {
		
		String workflowFile = "src/test/resources/ssrl/workflow/RestFlow/workflow_fileProtocolWithoutUriVariables" +
			WorkflowRunner.YAML_EXTENSION;

		@SuppressWarnings("unused")
		RunMetadata metadata = RestFlow.loadAndRunWorkflow( 
			new String[]{ "-v", "-f", workflowFile, "-base", "RESTFLOW_TESTRUNS_DIR" } );
	}

//	public void testAutodrug() throws Exception {
//		
//		String workflowFile = "/Users/tmcphillips/AutoDrug/EclipseWorkspace/Workflows/src/main/workflows/Green" +
//			WorkflowRunner.YAML_EXTENSION;
//
//		RunMetaData metadata = RestFlow.loadAndRunWorkflow( 
//			new String[]{ 
//				"-v", 
//				"-f", 		workflowFile,
//				"-w", 		"AutoDrugWorkflow",
//				"-base", 	"RESTFLOW_TESTRUNS_DIR" 
//				} );
//	}

}
package org.restflow;

import org.restflow.WorkflowRunner;
import org.restflow.test.RestFlowTestCase;


public class TestWorkflowRunner extends RestFlowTestCase {

	public void testWorkflowDefinitionStream_HelloWorld_JavaActors() throws Exception {
		
		String yamlString =
			"imports:                                           " + EOL +
			"                                                   " + EOL +
			"  - classpath:/common/types.yaml                   " + EOL +
			"  - classpath:/common/java/actors.yaml             " + EOL +
			"  - classpath:/common/directors.yaml               " + EOL +
			"                                                   " + EOL +
			"components:                                        " + EOL +
			"                                                   " + EOL +
			"  - id: CreateGreeting                             " + EOL +
			"    type: Node                                     " + EOL +
			"    properties:                                    " + EOL +
			"      actor: !ref ConstantSource                   " + EOL +
			"      constants:                                   " + EOL +
			"        value: Hello world!                        " + EOL +
			"      outflows:                                    " + EOL +
			"        value: /greeting                           " + EOL +
			"                                                   " + EOL +
			"  - id: RenderGreeting                             " + EOL +
			"    type: Node                                     " + EOL +
			"    properties:                                    " + EOL +
			"      actor: !ref PrintStreamWriter                " + EOL +
			"      inflows:                                     " + EOL +
			"        message: /greeting                         " + EOL +
			"                                                   " + EOL +
			"  - id: HelloWorld                                 " + EOL +
			"    type: Workflow                                 " + EOL +
			"    properties:                                    " + EOL +
			"      director: !ref PublishSubscribeDirector      " + EOL +
			"      nodes:                                       " + EOL +
			"        - !ref CreateGreeting                      " + EOL +
			"        - !ref RenderGreeting                      " + EOL
		;
		
		WorkflowRunner runner = new WorkflowRunner.Builder()
			.workflowDefinitionString(yamlString)
			.suppressWorkflowStdout(true)
			.build();
		
		runner.run();
		
		assertEquals(
				"Hello world!" + EOL, 
			runner.getStdoutRecording());
	}
}

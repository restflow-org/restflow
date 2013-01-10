package org.restflow;

import org.restflow.WorkflowRunner;
import org.restflow.metadata.Trace;
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

	public void testWorkflowDefinitionStream_HelloWorld_GroovyActorNodes() throws Exception {
		
		String yamlString =
			"imports:                                           " + EOL +
			"                                                   " + EOL +
			"  - classpath:/common/types.yaml                   " + EOL +
			"  - classpath:/common/directors.yaml               " + EOL +
			"                                                   " + EOL +
			"components:                                        " + EOL +
			"                                                   " + EOL +
			"  - id: HelloWorld                                 " + EOL +
			"    type: Workflow                                 " + EOL +
			"    properties:                                    " + EOL +
			"      director: !ref PublishSubscribeDirector      " + EOL +
			"      nodes:                                       " + EOL +
			"        - !ref CreateGreeting                      " + EOL +
			"        - !ref RenderGreeting                      " + EOL +
			"                                                   " + EOL +
			"  - id: CreateGreeting                             " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      constants:                                   " + EOL +
			"        value: Hello world!                        " + EOL +
			"      actor.step: output=value;                    " + EOL +
			"      outflows:                                    " + EOL +
			"        output: /greeting                          " + EOL +
			"                                                   " + EOL +
			"  - id: RenderGreeting                             " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      inflows:                                     " + EOL +
			"        message: /greeting                         " + EOL +
			"      actor.step: println(message)                 " + EOL
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


	public void testWorkflowDefinitionStream_DataSequence() throws Exception {
		
		String yamlString =
			"imports:                                           " + EOL +
			"                                                   " + EOL +
			"  - classpath:/common/types.yaml                   " + EOL +
			"  - classpath:/common/directors.yaml               " + EOL +
			"                                                   " + EOL +
			"components:                                        " + EOL +
			"                                                   " + EOL +
			"  - id: Top                                        " + EOL +
			"    type: Workflow                                 " + EOL +
			"    properties:                                    " + EOL +
			"      director: !ref PublishSubscribeDirector      " + EOL +
			"      nodes:                                       " + EOL +
			"        - !ref CreateMultipliers                   " + EOL +
			"        - !ref Multiply                            " + EOL +
			"        - !ref RenderProducts                      " + EOL +
			"                                                   " + EOL +
			"  - id: CreateMultipliers                          " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      sequences:                                   " + EOL +
			"        value:                                     " + EOL +
			"          - 5                                      " + EOL +
			"          - 10                                     " + EOL +
			"          - 11                                     " + EOL +
			"      actor.step: output=value;                    " + EOL +
			"      outflows:                                    " + EOL +
			"        output: /multiplier                        " + EOL +
			"                                                   " + EOL +
			"  - id: Multiply                                   " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      inflows:                                     " + EOL +
			"        multiplier: /multiplier                    " + EOL +
			"      actor.step: product = multiplier * 3;        " + EOL +
			"      outflows:                                    " + EOL +
			"        product: /product                          " + EOL +
			"                                                   " + EOL +
			"  - id: RenderProducts                             " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      inflows:                                     " + EOL +
			"        product: /product                          " + EOL +
			"      actor.step: println(product)                 " + EOL
		;
		
		WorkflowRunner runner = new WorkflowRunner.Builder()
			.workflowDefinitionString(yamlString)
			.suppressWorkflowStdout(false)
			.build();
		
		runner.run();
		
		assertEquals(
				"15" + EOL + 
				"30" + EOL + 
				"33" + EOL, 
			runner.getStdoutRecording());
	}

	public void testWorkflowDefinitionStream_NestedWorkflow() throws Exception {
		
		String yamlString =
			"imports:                                           " + EOL +
			"                                                   " + EOL +
			"  - classpath:/common/types.yaml                   " + EOL +
			"  - classpath:/common/directors.yaml               " + EOL +
			"                                                   " + EOL +
			"components:                                        " + EOL +
			"                                                   " + EOL +
			"  - id: Top                                        " + EOL +
			"    type: Workflow                                 " + EOL +
			"    properties:                                    " + EOL +
			"      director: !ref PublishSubscribeDirector      " + EOL +
			"      nodes:                                       " + EOL +
			"        - !ref CreateMultipliers                   " + EOL +
			"        - !ref MultiplyInSubworkflow               " + EOL +
			"        - !ref RenderProducts                      " + EOL +
			"                                                   " + EOL +
			"  - id: CreateMultipliers                          " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      sequences:                                   " + EOL +
			"        value:                                     " + EOL +
			"          - 5                                      " + EOL +
			"          - 10                                     " + EOL +
			"          - 11                                     " + EOL +
			"      actor.step: output=value;                    " + EOL +
			"      outflows:                                    " + EOL +
			"        output: /multiplier                        " + EOL +
			"                                                   " + EOL +
			"  - id: MultiplyInSubworkflow                      " + EOL +
			"    type: Node                                     " + EOL +
			"    properties:                                    " + EOL +
			"      nestedUriPrefix: /{STEP}                     " + EOL +
			"      inflows:                                     " + EOL +
			"        multiplier: /multiplier                    " + EOL +
			"      actor: !ref MultiplierWorkflow               " + EOL +
			"      outflows:                                    " + EOL +
			"        product: /product                          " + EOL +
			"                                                   " + EOL +
			"  - id: RenderProducts                             " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      inflows:                                     " + EOL +
			"        product: /product                          " + EOL +
			"      actor.step: println(product)                 " + EOL +
			"                                                   " + EOL +
			"  - id: MultiplierWorkflow                         " + EOL +
			"    type: Workflow                                 " + EOL +
			"    properties:                                    " + EOL +
			"      director: !ref PublishSubscribeDirector      " + EOL +
			"      nodes:                                       " + EOL +
			"        - !ref ImportMultiplier                    " + EOL +
			"        - !ref Multiply                            " + EOL +
			"        - !ref ExportProduct                       " + EOL +
			"      inputs:                                      " + EOL +
			"        multiplier:                                " + EOL +			
			"      outputs:                                     " + EOL +
			"        product:                                   " + EOL +
			"                                                   " + EOL +
			"  - id: ImportMultiplier                           " + EOL +
			"    type: InPortal                                 " + EOL +
			"    properties:                                    " + EOL +
			"      outflows:                                    " + EOL +
			"        multiplier: /multiplier                    " + EOL +
			"                                                   " + EOL +
			"  - id: Multiply                                   " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      inflows:                                     " + EOL +
			"        multiplier: /multiplier                    " + EOL +
			"      actor.step: product = multiplier * 3;        " + EOL +
			"      outflows:                                    " + EOL +
			"        product: /product                          " + EOL +
			"                                                   " + EOL +
			"  - id: ExportProduct                              " + EOL +
			"    type: OutPortal                                " + EOL +
			"    properties:                                    " + EOL +
			"      inflows:                                     " + EOL +
			"        product: /product                          " + EOL
		;
		
		WorkflowRunner runner = new WorkflowRunner.Builder()
			.workflowDefinitionString(yamlString)
			.workflowName("Top")
			.suppressWorkflowStdout(false)
			.closeTraceRecorderAfterRun(false)
			.build();
		
		runner.run();
		
		assertEquals(
				"15" + EOL + 
				"30" + EOL + 
				"33" + EOL, 
			runner.getStdoutRecording());
		
		Trace trace = runner.getTrace();
		
		assertEquals(
				"rf_node(['Top'])."												+ EOL +
				"rf_node(['Top','CreateMultipliers'])."							+ EOL +
				"rf_node(['Top','MultiplyInSubworkflow'])."						+ EOL +
				"rf_node(['Top','MultiplyInSubworkflow','ExportProduct'])."		+ EOL +
				"rf_node(['Top','MultiplyInSubworkflow','ImportMultiplier'])."	+ EOL +
				"rf_node(['Top','MultiplyInSubworkflow','Multiply'])."			+ EOL +
				"rf_node(['Top','RenderProducts'])."							+ EOL,
			trace.getWorkflowNodesProlog());
		
		assertEquals(
				"/1/multiplier: 5"		+ EOL +
				"/1/product/1: 15"		+ EOL +
				"/2/multiplier: 10"		+ EOL +
				"/2/product/1: 30"		+ EOL +
				"/3/multiplier: 11"		+ EOL +
				"/3/product/1: 33"		+ EOL +
				"/multiplier/1: 5"		+ EOL +
				"/multiplier/2: 10"		+ EOL +
				"/multiplier/3: 11"		+ EOL +
				"/product/1: 15"		+ EOL +
				"/product/2: 30"		+ EOL +
				"/product/3: 33"		+ EOL, 
			trace.getResourcesYaml());

		assertEquals(
				"StepID NodeID ParentStepID StepNumber UpdateCount "		+ EOL +
				"------ ------ ------------ ---------- ----------- "		+ EOL +
				"1      1                   1          0           "		+ EOL +
				"2      2      1            1          0           "		+ EOL +
				"3      3      1            1          0           "		+ EOL +
				"4      4      3            1          0           "		+ EOL +
				"5      5      3            1          0           "		+ EOL +
				"6      6      3            1          0           "		+ EOL +
				"7      7      1            1          0           "		+ EOL +
				"8      2      1            2          0           "		+ EOL +
				"9      3      1            2          0           "		+ EOL +
				"10     4      9            2          0           "		+ EOL +
				"11     5      9            2          0           "		+ EOL +
				"12     6      9            2          0           "		+ EOL +
				"13     7      1            2          0           "		+ EOL +
				"14     2      1            3          0           "		+ EOL +
				"15     3      1            3          0           "		+ EOL +
				"16     4      15           3          0           "		+ EOL +
				"17     5      15           3          0           "		+ EOL +
				"18     6      15           3          0           "		+ EOL +
				"19     7      1            3          0           "		+ EOL, 
			trace.dumpStepTable_NoTimestamps());
		
		assertEquals(
				"rf_event(w,'Top.CreateMultipliers','1','output','/multiplier/1')."								+ EOL +
				"rf_event(w,'Top.CreateMultipliers','2','output','/multiplier/2')."								+ EOL +
				"rf_event(w,'Top.CreateMultipliers','3','output','/multiplier/3')."								+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow','1','multiplier','/multiplier/1')."						+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow','1','product','/product/1')."							+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow','2','multiplier','/multiplier/2')."						+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow','2','product','/product/2')."							+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow','3','multiplier','/multiplier/3')."						+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow','3','product','/product/3')."							+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow.ExportProduct','1','product','/1/product/1')."			+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow.ExportProduct','2','product','/2/product/1')."			+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow.ExportProduct','3','product','/3/product/1')."			+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow.ImportMultiplier','1','multiplier','/1/multiplier')."	+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow.ImportMultiplier','2','multiplier','/2/multiplier')."	+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow.ImportMultiplier','3','multiplier','/3/multiplier')."	+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow.Multiply','1','multiplier','/1/multiplier')."			+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow.Multiply','1','product','/1/product/1')."				+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow.Multiply','2','multiplier','/2/multiplier')."			+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow.Multiply','2','product','/2/product/1')."				+ EOL +
				"rf_event(r,'Top.MultiplyInSubworkflow.Multiply','3','multiplier','/3/multiplier')."			+ EOL +
				"rf_event(w,'Top.MultiplyInSubworkflow.Multiply','3','product','/3/product/1')."				+ EOL +
				"rf_event(r,'Top.RenderProducts','1','product','/product/1')."									+ EOL +
				"rf_event(r,'Top.RenderProducts','2','product','/product/2')."									+ EOL +
				"rf_event(r,'Top.RenderProducts','3','product','/product/3')."									+ EOL, 
			trace.getDataEventsProlog());

	}
}

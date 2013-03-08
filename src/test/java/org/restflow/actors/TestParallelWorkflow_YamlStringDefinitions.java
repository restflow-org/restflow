package org.restflow.actors;

import org.restflow.WorkflowRunner;
import org.restflow.metadata.Trace;
import org.restflow.test.RestFlowTestCase;


public class TestParallelWorkflow_YamlStringDefinitions extends RestFlowTestCase {

	public void testWorkflowDefinitionStream_ParallelSubworkflow() throws Exception {
		
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
			"      director: !ref MTDataDrivenDirector          " + EOL +
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
			"    type: ParallelWorkflow                         " + EOL +
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
		
		assertMatchesRegexp(
				"NodeID ParentStepID StepNumber UpdateCount "		+ EOL +
				"------ ------------ ---------- ----------- "		+ EOL +
				"1      ..           1          0           "		+ EOL +
				"2      ..           1          0           "		+ EOL +
				"2      ..           2          0           "		+ EOL +
				"2      ..           3          0           "		+ EOL +
				"3      ..           1          0           "		+ EOL +
				"3      ..           2          0           "		+ EOL +
				"3      ..           3          0           "		+ EOL +
				"4      ..           1          0           "		+ EOL +
				"4      ..           2          0           "		+ EOL +
				"4      ..           3          0           "		+ EOL +
				"5      ..           1          0           "		+ EOL +
				"5      ..           2          0           "		+ EOL +
				"5      ..           3          0           "		+ EOL +
				"6      ..           1          0           "		+ EOL +
				"6      ..           2          0           "		+ EOL +
				"6      ..           3          0           "		+ EOL +
				"7      ..           1          0           "		+ EOL +
				"7      ..           2          0           "		+ EOL +
				"7      ..           3          0           "		+ EOL, 
			trace.dumpTable(
					 "Step",
					 new String[] {"NodeID", "ParentStepID", "StepNumber", "UpdateCount"},
					 "ORDER BY NodeID, StepNumber"
			)
		);
		
		assertEquals(
				"NodeName                                   StepNumber PortName     Uri                Value " 	+ EOL +
				"------------------------------------------ ---------- ------------ ------------------ ----- " 	+ EOL +
				"<Top>[CreateMultipliers]                   1          output       /multiplier/1      5     " 	+ EOL +
				"<Top>[CreateMultipliers]                   2          output       /multiplier/2      10    " 	+ EOL +
				"<Top>[CreateMultipliers]                   3          output       /multiplier/3      11    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow]               1          product      /product/1         15    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow]               2          product      /product/2         30    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow]               3          product      /product/3         33    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow][ImportMultipl 1          multiplier   /1/multiplier      5     " 	+ EOL +
				"<Top>[MultiplyInSubworkflow][ImportMultipl 2          multiplier   /2/multiplier      10    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow][ImportMultipl 3          multiplier   /3/multiplier      11    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow][Multiply]     1          product      /1/product/1       15    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow][Multiply]     2          product      /2/product/1       30    " 	+ EOL +
				"<Top>[MultiplyInSubworkflow][Multiply]     3          product      /3/product/1       33    " 	+ EOL, 
			trace.dumpTable(
				"NodePublishedResource",
				new String [] {"NodeName                                  ", "StepNumber", "PortName    ", "Uri               ", "Value"},
				"ORDER BY NodeName, PortName, Uri")
				);
	}
	
	public void testWorkflowDefinitionStream_ConcurrentParallelSubworkflow() throws Exception {
		
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
			"      director: !ref MTDataDrivenDirector          " + EOL +
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
			"      maxConcurrency: 2                            " + EOL +
			"      ordered: true                                " + EOL +
			"                                                   " + EOL +
			"  - id: RenderProducts                             " + EOL +
			"    type: GroovyActorNode                          " + EOL +
			"    properties:                                    " + EOL +
			"      inflows:                                     " + EOL +
			"        product: /product                          " + EOL +
			"      actor.step: println(product)                 " + EOL +
			"                                                   " + EOL +
			"  - id: MultiplierWorkflow                         " + EOL +
			"    type: ParallelWorkflow                         " + EOL +
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
			"      actor.step: |                                " + EOL +
			"        product = multiplier * 3;                  " + EOL +
			"        //Thread.sleep(5000)                       " + EOL +
			"        Thread.sleep((int)(Math.random() * 10)) ;  " + EOL +
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

		assertMatchesRegexp(
				"NodeID ParentStepID StepNumber UpdateCount "		+ EOL +
				"------ ------------ ---------- ----------- "		+ EOL +
				"1                   1          0           "		+ EOL +
				"2      ..           1          0           "		+ EOL +
				"2      ..           2          0           "		+ EOL +
				"2      ..           3          0           "		+ EOL +
				"3      ..           1          0           "		+ EOL +
				"3      ..           2          0           "		+ EOL +
				"3      ..           3          0           "		+ EOL +
				"4      ..           1          0           "		+ EOL +
				"4      ..           2          0           "		+ EOL +
				"4      ..           3          0           "		+ EOL +
				"5      ..           1          0           "		+ EOL +
				"5      ..           2          0           "		+ EOL +
				"5      ..           3          0           "		+ EOL +
				"6      ..           1          0           "		+ EOL +
				"6      ..           2          0           "		+ EOL +
				"6      ..           3          0           "		+ EOL +
				"7      ..           1          0           "		+ EOL +
				"7      ..           2          0           "		+ EOL +
				"7      ..           3          0           "		+ EOL, 
			trace.dumpTable(
					 "Step",
					 new String[] {"NodeID", "ParentStepID", "StepNumber", "UpdateCount"},
					 "ORDER BY NodeID, StepNumber"
			)
		);
		
		assertMatchesRegexp(
				"NodeName                                       StepNumber PortName     Uri                Value " 	+ EOL +
				"---------------------------------------------- ---------- ------------ ------------------ ----- " 	+ EOL +
				"<Top>\\[CreateMultipliers\\]                       1          output       /multiplier/1      5     " 	+ EOL +
				"<Top>\\[CreateMultipliers\\]                       2          output       /multiplier/2      10    " 	+ EOL +
				"<Top>\\[CreateMultipliers\\]                       3          output       /multiplier/3      11    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]                   1          product      /product/1         15    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]                   2          product      /product/2         30    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]                   3          product      /product/3         33    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]\\[ImportMultiplier\\] .          multiplier   /1/multiplier      5     " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]\\[ImportMultiplier\\] .          multiplier   /2/multiplier      10    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]\\[ImportMultiplier\\] .          multiplier   /3/multiplier      11    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]\\[Multiply\\]         .          product      /1/product/1       15    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]\\[Multiply\\]         .          product      /2/product/1       30    " 	+ EOL +
				"<Top>\\[MultiplyInSubworkflow\\]\\[Multiply\\]         .          product      /3/product/1       33    " 	+ EOL, 
			trace.dumpTable(
				"NodePublishedResource",
				new String [] {"NodeName                                      ", "StepNumber", "PortName    ", "Uri               ", "Value"},
				"ORDER BY NodeName, PortName, Uri"
			)
		);
	}	
}

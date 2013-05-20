package org.restflow.reporter;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.InflowProperty;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.nodes.GroovyNodeBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.reporter.GroovyTemplateReporter;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestGroovyTemplateReporter extends RestFlowTestCase {

	private WorkflowContext _context;
	
	public void setUp() throws Exception {
		super.setUp();
		_context = new WorkflowContextBuilder().build();
	}
	
	public void test_WorkflowReporters_WithInputAndOutput() throws Exception {
		
		final Workflow workflow = new WorkflowBuilder()
		
			.name("DoublerWorkflow")
			.context(_context)
			.director(new MTDataDrivenDirector())
			
			.inflow("a", "/original")

			.preambleReporter(new GroovyTemplateReporter(
					"*** The preamble report ***" 	+ EOL +
					 "a = ${inputs.get('a')}" 		+ EOL
				)
			)

			.node(new GroovyNodeBuilder()
				.name("doubler")
				.inflow("/original", "x")
				.step("y = 3 * x;")
				.outflow("y", "/tripled"))
					
			.outflow("/tripled", "b")

			.finalReporter(new GroovyTemplateReporter(
					"*** The final report ***"		+ EOL +
					 "a = ${inputs.get('a')}"		+ EOL +
					 "b = ${outputs.get('b')}" 		+ EOL
				)
			)

			.build();
		
		workflow.configure();
		
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {

				for (int i = 0; i <= 5; i++) {
					workflow.set("a", i);
					
					workflow.initialize();
					workflow.run();
					workflow.wrapup();
					
					int b = (Integer) workflow.get("b");
					assertEquals(b, 3 * i);
				}
				
				workflow.dispose();
			}
		});
		
		assertEquals(
				"*** The preamble report ***" 		+ EOL +
				"a = 0" 							+ EOL +
				"*** The final report ***" 			+ EOL +
				"a = 0" 							+ EOL +
				"b = 0" 							+ EOL +
				"*** The preamble report ***" 		+ EOL +
				"a = 1" 							+ EOL +
				"*** The final report ***" 			+ EOL +
				"a = 1" 							+ EOL +
				"b = 3" 							+ EOL +
				"*** The preamble report ***" 		+ EOL +
				"a = 2" 							+ EOL +
				"*** The final report ***" 			+ EOL +
				"a = 2" 							+ EOL +
				"b = 6" 							+ EOL +
				"*** The preamble report ***" 		+ EOL +
				"a = 3" 							+ EOL +
				"*** The final report ***" 			+ EOL +
				"a = 3" 							+ EOL +
				"b = 9" 							+ EOL +
				"*** The preamble report ***" 		+ EOL +
				"a = 4" 							+ EOL +
				"*** The final report ***" 			+ EOL +
				"a = 4" 							+ EOL +
				"b = 12" 							+ EOL +
				"*** The preamble report ***" 		+ EOL +
				"a = 5" 							+ EOL +
				"*** The final report ***" 			+ EOL +
				"a = 5" 							+ EOL +
				"b = 15"							+ EOL,
			recorder.getStdoutRecording());
			
			assertEquals("", recorder.getStderrRecording());
	}
	
	public void test_WorkflowReporters_DoublyNestedWorkflow_MultiStep() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()
		
		.name("TopWF")
		.context(_context)
		.prefix("/RUN")
		
		.inflow("v", "/topmultiplier")
			
		.preambleReporter(new GroovyTemplateReporter(
				"Preamble report for step ${STEP} and run ${RUN} of ${workflow}:" 	+ 
				"                          v = ${inputs.get('v')}" 			+ EOL
			)
		)

		.node(new WorkflowNodeBuilder()
			.name("SubWF")
			.prefix("/Sub{RUN}/")
			
			.inflow("/topmultiplier", "a", "/subinmultiplier")
			
			.preambleReporter(new GroovyTemplateReporter(
					"Preamble report for step ${STEP} and run ${RUN} of ${workflow}:" 	+
					"              a = ${inputs.get('a')}" 			+ EOL
				)
			)
			
			.node(new GroovyNodeBuilder()
				.name("GenerateScaleFactors")
				.context(_context)
				.sequence("constant", new Object [] {2, 5, 30})
				.step("value=constant")
				.outflow("value", "/scaleFactor"))					
					
			.node(new JavaNodeBuilder()
				.name("ScaleMultipliers")
				.inflow("/scaleFactor", "s")
				.inflow("/subinmultiplier", "c", InflowProperty.ReceiveOnce)
				.bean(new Object() {
					public int c, s, v;
					public void step() { v = s * c; }
				})
				.outflow("v", "/scaledmultiplier"))

			.node(new WorkflowNodeBuilder()
				.name("SubSubWF")
				.prefix("subsub{RUN}")
				.inflow("/scaledmultiplier", "b", "/multiplier")
				
				.preambleReporter(new GroovyTemplateReporter(
						 "Preamble report for step ${STEP} and run ${RUN} of ${workflow}:" +
						 "  b = ${inputs.get('b')}" 		+ EOL
					)
				)
				
				.node(new JavaNodeBuilder()
					.name("MultiplySequenceByEight")
					.inflow("/multiplier", "b")
					.bean(new Object() {
						public int a, b, c;
						public void step() { c = 8 * b; }
					})
					.outflow("c", "/product"))
	
				.node(new JavaNodeBuilder()
					.name("RenderProducts")
					.inflow("/product", "v")
					.bean(new Object() {
						public int v;
						public void step() { System.out.println(v); }
					}))
					
				.finalReporter(new GroovyTemplateReporter(
						"Final report for step ${STEP} and run ${RUN} of $workflow:" 	+ 
						 "     b = ${inputs.get('b')}"		+ 
						 "  c = ${outputs.get('c')}" 	+ EOL
					)
				)

				.outflow("/product", "c", "/product")
			)
			
			.outflow("/product", "d", "/result")
			
			.finalReporter(new GroovyTemplateReporter(
					"Final report for step ${STEP} and run ${RUN} of $workflow:" 	+
					 "                 d = ${outputs.get('d')}"	+ EOL
				)
			)
		)

		.outflow("/result", "e")
		
		.finalReporter(new GroovyTemplateReporter(
				"Final report for step ${STEP} and run ${RUN} of $workflow:" 	+
				 "                             e = ${outputs.get('e')}"	+ EOL + EOL
			)
		)
		
		.build();

		workflow.configure();
		
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				for (int i = 1; i <= 3; i++) {
					workflow.set("v", i);
					workflow.initialize();
					workflow.run();
					workflow.wrapup();
					assertEquals(i * 16, workflow.get("e"));
				}
			}
		});
		
		assertEquals(
			"Preamble report for step 1 and run 1 of TopWF:                          v = 1"					+ EOL +
			"Preamble report for step 1 and run 1 of TopWF.SubWF.SubWF:              a = 1"					+ EOL +
			"Preamble report for step 1 and run 1 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 2"					+ EOL +
			"16"																							+ EOL +
			"Final report for step 1 and run 1 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 2  c = 16"			+ EOL +
			"Preamble report for step 2 and run 2 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 5"					+ EOL +
			"40"																							+ EOL +
			"Final report for step 2 and run 2 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 5  c = 40"			+ EOL +
			"Preamble report for step 3 and run 3 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 30"				+ EOL +
			"240"																							+ EOL +
			"Final report for step 3 and run 3 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 30  c = 240"		+ EOL +
			"Final report for step 1 and run 1 of TopWF.SubWF.SubWF:                 d = 16"				+ EOL +
			"Final report for step 1 and run 1 of TopWF:                             e = 16"				+ EOL +
			""																								+ EOL +
			"Preamble report for step 1 and run 2 of TopWF:                          v = 2"					+ EOL +
			"Preamble report for step 1 and run 2 of TopWF.SubWF.SubWF:              a = 2"					+ EOL +
			"Preamble report for step 1 and run 4 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 4"					+ EOL +
			"32"																							+ EOL +
			"Final report for step 1 and run 4 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 4  c = 32"			+ EOL +
			"Preamble report for step 2 and run 5 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 10"				+ EOL +
			"80"																							+ EOL +
			"Final report for step 2 and run 5 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 10  c = 80"		+ EOL +
			"Preamble report for step 3 and run 6 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 60"				+ EOL +
			"480"																							+ EOL +
			"Final report for step 3 and run 6 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 60  c = 480"		+ EOL +
			"Final report for step 1 and run 2 of TopWF.SubWF.SubWF:                 d = 32"				+ EOL +
			"Final report for step 1 and run 2 of TopWF:                             e = 32"				+ EOL +
			""																								+ EOL +
			"Preamble report for step 1 and run 3 of TopWF:                          v = 3"					+ EOL +
			"Preamble report for step 1 and run 3 of TopWF.SubWF.SubWF:              a = 3"					+ EOL +
			"Preamble report for step 1 and run 7 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 6"					+ EOL +
			"48"																							+ EOL +
			"Final report for step 1 and run 7 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 6  c = 48"			+ EOL +
			"Preamble report for step 2 and run 8 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 15"				+ EOL +
			"120"																							+ EOL +
			"Final report for step 2 and run 8 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 15  c = 120"		+ EOL +
			"Preamble report for step 3 and run 9 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 90"				+ EOL +
			"720"																							+ EOL +
			"Final report for step 3 and run 9 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 90  c = 720"		+ EOL +
			"Final report for step 1 and run 3 of TopWF.SubWF.SubWF:                 d = 48"				+ EOL +
			"Final report for step 1 and run 3 of TopWF:                             e = 48"				+ EOL +
			""																								+ EOL,
			recorder.getStdoutRecording());
		
//		assertEquals("", recorder.getStderrRecording());
		
		workflow.reset();

		recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.initialize();
				for (int i = 1; i <= 3; i++) {
					workflow.set("v", i);
					workflow.run();
					assertEquals(i * 16, workflow.get("e"));
				}
				workflow.wrapup();
			}
		});
				
		assertEquals(
				"Preamble report for step 1 and run 1 of TopWF:                          v = 1"					+ EOL +
				"Preamble report for step 1 and run 1 of TopWF.SubWF.SubWF:              a = 1"					+ EOL +
				"Preamble report for step 1 and run 1 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 2"					+ EOL +
				"16"																							+ EOL +
				"Final report for step 1 and run 1 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 2  c = 16"			+ EOL +
				"Preamble report for step 2 and run 2 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 5"					+ EOL +
				"40"																							+ EOL +
				"Final report for step 2 and run 2 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 5  c = 40"			+ EOL +
				"Preamble report for step 3 and run 3 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 30"				+ EOL +
				"240"																							+ EOL +
				"Final report for step 3 and run 3 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 30  c = 240"		+ EOL +
				"Final report for step 1 and run 1 of TopWF.SubWF.SubWF:                 d = 16"				+ EOL +
				"Final report for step 1 and run 1 of TopWF:                             e = 16"				+ EOL +
				""																								+ EOL +
				"Preamble report for step 2 and run 2 of TopWF:                          v = 2"					+ EOL +
				"Preamble report for step 1 and run 2 of TopWF.SubWF.SubWF:              a = 2"					+ EOL +
				"Preamble report for step 1 and run 4 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 4"					+ EOL +
				"32"																							+ EOL +
				"Final report for step 1 and run 4 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 4  c = 32"			+ EOL +
				"Preamble report for step 2 and run 5 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 10"				+ EOL +
				"80"																							+ EOL +
				"Final report for step 2 and run 5 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 10  c = 80"		+ EOL +
				"Preamble report for step 3 and run 6 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 60"				+ EOL +
				"480"																							+ EOL +
				"Final report for step 3 and run 6 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 60  c = 480"		+ EOL +
				"Final report for step 1 and run 2 of TopWF.SubWF.SubWF:                 d = 32"				+ EOL +
				"Final report for step 2 and run 2 of TopWF:                             e = 32"				+ EOL +
				""																								+ EOL +
				"Preamble report for step 3 and run 3 of TopWF:                          v = 3"					+ EOL +
				"Preamble report for step 1 and run 3 of TopWF.SubWF.SubWF:              a = 3"					+ EOL +
				"Preamble report for step 1 and run 7 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 6"					+ EOL +
				"48"																							+ EOL +
				"Final report for step 1 and run 7 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 6  c = 48"			+ EOL +
				"Preamble report for step 2 and run 8 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 15"				+ EOL +
				"120"																							+ EOL +
				"Final report for step 2 and run 8 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 15  c = 120"		+ EOL +
				"Preamble report for step 3 and run 9 of TopWF.SubWF.SubSubWF.SubSubWF:  b = 90"				+ EOL +
				"720"																							+ EOL +
				"Final report for step 3 and run 9 of TopWF.SubWF.SubSubWF.SubSubWF:     b = 90  c = 720"		+ EOL +
				"Final report for step 1 and run 3 of TopWF.SubWF.SubWF:                 d = 48"				+ EOL +
				"Final report for step 3 and run 3 of TopWF:                             e = 48"				+ EOL +
				""																								+ EOL,
				recorder.getStdoutRecording());
			
		workflow.dispose();
	}
	
	public void test_WorkflowReporter_TraceQueries() throws Exception {

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()
			
			.context(_context)
			.name("TopWorkflow")
			
			.node(new JavaNodeBuilder()
				.name("TheOnlyNode")	
				.bean(new Object() {
						public void step() { System.out.println("Hello world!");}
					})
				)
				
			.preambleReporter(new GroovyTemplateReporter(
					"Nodes:" 									+ EOL +
					"${trace.getWorkflowNodesProlog()}"
				)
			)

			.finalReporter(new GroovyTemplateReporter(
					"Data events:" 								+ EOL +
					"${trace.getStepEventsProlog()}"
				)
			)
			
			.reporter("MyReport", new GroovyTemplateReporter(
					"${trace.getNodeStepCountsYaml()}"
				)
			)

			.reporter("MyOtherReport", new GroovyTemplateReporter(
					"<%	java.sql.ResultSet rs = trace.query('			"	+
					"       SELECT NodeName, StepNumber, StartTime 		"	+
					"       FROM Step 									" 	+
					"       JOIN Node ON Step.NodeID = Node.NodeID 		" 	+
					"       ORDER BY NodeName, StepNumber');			"	+
					"	while (rs.next()) println(						"	+
					"       rs.getString('NodeName') + ' ' + 			"	+
					"       rs.getString('StepNumber'));				"
				)
			)

			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout showing three values printed
		assertEquals(
				"Nodes:"									+ EOL +
				"rf_node(['TopWorkflow'])."					+ EOL +
				"rf_node(['TopWorkflow','TheOnlyNode'])."	+ EOL +
				"Hello world!" 								+ EOL +
				"Data events:" 								+ EOL +
				"rf_step(TopWorkflow','1')."				+ EOL +
				"rf_step(TopWorkflow.TheOnlyNode','1')." 	+ EOL ,
			recorder.getStdoutRecording());

		assertEquals(
				"TopWorkflow: 1"				+ EOL +
				"TopWorkflow.TheOnlyNode: 1"	+ EOL,	
			workflow.getReport("MyReport"));

		assertEquals(
				"TopWorkflow 1"					+ EOL +
				"TopWorkflow.TheOnlyNode 1" 	+ EOL,
			workflow.getReport("MyOtherReport"));
	}
}

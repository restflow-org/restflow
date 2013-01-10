package org.restflow.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.metadata.Trace;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.reporter.JavaReporter;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestJavaReporter extends RestFlowTestCase {

	private WorkflowContext _context;
	
	public void setUp() throws Exception {
		super.setUp();
		_context = new WorkflowContextBuilder().build();
	}
	
	public void test_WorkflowReporters_MultipleReporters() throws Exception {

		final Workflow workflow = new WorkflowBuilder()
			
			.context(_context)	
			
			.node(new JavaNodeBuilder().bean(
									new Object() {
										public void step() { System.out.println("Hello world!");}
									}))

			.reporter("Report1", 	new JavaReporter() {
										public String getReport() {
											return "The first report.";
										}
									})
			
			.reporter("Report2",	new JavaReporter() {
										public String getReport() {
											return "The second report.";
										}
									})
			
			.reporter("Report3", 	new JavaReporter() {
										public String getReport() {
											return "The third report.";
										}
									})
			
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout showing three values printed
		assertEquals(
			"Hello world!" 	+ EOL ,
			recorder.getStdoutRecording());

		assertEquals("The first report.",	workflow.getReport("Report1"));
		assertEquals("The second report.", 	workflow.getReport("Report2"));
		assertEquals("The third report.", 	workflow.getReport("Report3"));
	}
	
	public void test_WorkflowReporters_PreambleAndFinalReporters() throws Exception {

		final Workflow workflow = new WorkflowBuilder()
			.context(_context)	
			.node(new JavaNodeBuilder().bean(
									new Object() {
										public void step() { System.out.println("Hello world!");}
									}))
			.preambleReporter(new JavaReporter() {
									public String getReport() {
										return "The preamble report." + EOL;
									}
								})
			.finalReporter(new JavaReporter() {
									public String getReport() {
										return "The final report." + EOL;
									}
								})
			.build();
		
		workflow.configure();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.initialize();				
				workflow.run();
				workflow.wrapup();
			}
		});
			
		// confirm expected stdout showing preamble and final reports bracketing workflow output
		assertEquals(
			"The preamble report." 		+ EOL +
			"Hello world!" 				+ EOL +
			"The final report."			+ EOL,
			recorder.getStdoutRecording());
	}
	
	
	public void test_WorkflowReporter_TraceQueries() throws Exception {

		final Workflow workflow = new WorkflowBuilder()
			
			.context(_context)
			.name("TopWorkflow")
			
			.node(new JavaNodeBuilder()
				.name("TheOnlyNode")	
				.bean(new Object() {
						public void step() { System.out.println("Hello world!");}
					})
				)
				
			.preambleReporter(new JavaReporter() {
					public String getReport() throws SQLException {
						Trace trace = (Trace)_reportModel.get("trace");
						StringBuffer buffer = new StringBuffer();
						buffer.append("Nodes:" + EOL);
						buffer.append(trace.getWorkflowNodesProlog());
						return buffer.toString();
					}
				}
			)

			.finalReporter(new JavaReporter() {
					public String getReport() throws SQLException {
						Trace trace = (Trace)_reportModel.get("trace");
						StringBuffer buffer = new StringBuffer();
						buffer.append("Data events:" + EOL);
						buffer.append(trace.getStepEventsProlog());
						return buffer.toString();
					}
				}
			)
			
			.reporter("MyReport", new JavaReporter() {
					public String getReport() throws SQLException {
						Trace trace = (Trace)_reportModel.get("trace");
						return trace.getNodeStepCountsYaml();
					}
				}
			)

			.reporter("MyOtherReport", new JavaReporter() {
					public String getReport() throws SQLException {
						
						Trace trace = (Trace)_reportModel.get("trace");
						StringBuffer buffer = new StringBuffer();
						
						ResultSet rs = trace.query(
								"SELECT NodeName, StepNumber, StartTime " 		+
								"FROM Step " 									+
								"    JOIN Node ON Step.NodeID = Node.NodeID " 	+
								"ORDER BY NodeName, StepNumber");
						
						while (rs.next()) buffer.append(
								rs.getString("NodeName")   + " "				+
								rs.getString("StepNumber") + EOL);
						
						return buffer.toString();
					}
				}
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
				"TopWorkflow: 1"							+ EOL +
				"TopWorkflow.TheOnlyNode: 1" + EOL,	
			workflow.getReport("MyReport"));

		assertEquals(
				"TopWorkflow 1"								+ EOL +
				"TopWorkflow.TheOnlyNode 1" 				+ EOL,	
			workflow.getReport("MyOtherReport"));
	}
}

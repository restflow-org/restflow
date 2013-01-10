package org.restflow.reporter;

import java.sql.ResultSet;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.metadata.Trace;
import org.restflow.nodes.GroovyNodeBuilder;
import org.restflow.test.WorkflowTestCase;


public class TestTraceQueryMethods_VolatileMetadata  extends WorkflowTestCase {

	private WorkflowContext _context;
	
	public TestTraceQueryMethods_VolatileMetadata() {
		super("src/test/resources/workflows/");
	}

	public void setUp() throws Exception {
		super.setUp();
		_context = new WorkflowContextBuilder().build();
	}

	public void test_PrologQueries_SimpleWorkflow() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
		
			.name("DoublerWorkflow")
			.context(_context)
			.director(new MTDataDrivenDirector())
			
			.inflow("a", "/original")

			.node(new GroovyNodeBuilder()
				.name("doubler")
				.inflow("/original", "x")
				.step("y = 3 * x;")
				.outflow("y", "/{x}/tripled")
				.stepsOnce())
					
			.outflow("/{m}/tripled", "b")

			.build();
		
		workflow.configure();
		
		workflow.set("a", 4);
		
		workflow.initialize();
		workflow.run();
		workflow.wrapup();
		
		int b = (Integer) workflow.get("b");
		assertEquals(b, 12);
	
		workflow.dispose();
		
		Trace trace = _context.getTrace();
		
		assertEquals(
				"rf_node(['DoublerWorkflow'])."						+ EOL +
				"rf_node(['DoublerWorkflow','doubler'])."			+ EOL +
				"rf_node(['DoublerWorkflow','inportal'])."			+ EOL + 
				"rf_node(['DoublerWorkflow','outportal'])."			+ EOL + 
				""													+ EOL + 
				"rf_port('DoublerWorkflow','a',in)."				+ EOL +
				"rf_port('DoublerWorkflow','b',out)." 				+ EOL +
				"rf_port('DoublerWorkflow.doubler','x',in)."		+ EOL +
				"rf_port('DoublerWorkflow.doubler','y',out)."		+ EOL + 
				"rf_port('DoublerWorkflow.inportal','a',out)."		+ EOL + 
				"rf_port('DoublerWorkflow.outportal','b',in)."		+ EOL + 
				""													+ EOL + 
				"rf_link('DoublerWorkflow.doubler','y',e1)."		+ EOL + 
				"rf_link('DoublerWorkflow.outportal','b',e1)."		+ EOL + 
				"rf_link('DoublerWorkflow.inportal','a',e2)."		+ EOL + 
				"rf_link('DoublerWorkflow.doubler','x',e2)."		+ EOL, 
			trace.getWorkflowGraphProlog());
		
		assertEquals(
				"rf_event(r,'DoublerWorkflow','1','a','null')."						+ EOL + 
				"rf_event(w,'DoublerWorkflow','1','b','null')."						+ EOL + 
				"rf_event(r,'DoublerWorkflow.doubler','1','x','/original')."		+ EOL + 
				"rf_event(w,'DoublerWorkflow.doubler','1','y','/4/tripled')."		+ EOL + 
				"rf_event(w,'DoublerWorkflow.inportal','1','a','/original')."		+ EOL + 
				"rf_event(r,'DoublerWorkflow.outportal','1','b','/4/tripled')."		+ EOL +
				"rf_event(w,'DoublerWorkflow.doubler','1','y@x','4')."				+ EOL +
				"rf_event(r,'DoublerWorkflow.outportal','1','b@x','4')."			+ EOL,
			trace.getPortEventsProlog());
		
		assertEquals(
				"rf_step(DoublerWorkflow','1')."				+ EOL +
				"rf_step(DoublerWorkflow.inportal','1')."		+ EOL + 
				"rf_step(DoublerWorkflow.doubler','1')."		+ EOL + 
				"rf_step(DoublerWorkflow.outportal','1')."		+ EOL,
			trace.getStepEventsProlog());
		
		assertEquals(
				"DoublerWorkflow: 1"				+ EOL +
				"DoublerWorkflow.doubler: 1"		+ EOL + 
				"DoublerWorkflow.inportal: 1"		+ EOL + 
				"DoublerWorkflow.outportal: 1"		+ EOL,
			trace.getNodeStepCountsYaml()
		);
		
		ResultSet rs = trace.query(
				"SELECT Uri, Value FROM Data JOIN Resource ON Data.DataID = Resource.DataID WHERE Uri IS NOT NULL ORDER BY Uri");
		StringBuffer buffer = new StringBuffer();
		while (rs.next()) buffer.append(rs.getString("Uri") + ": " + rs.getString("Value") + EOL);
		assertEquals(	"/4/tripled: 12" 	+ EOL +
					 	"/original: 4"		+ EOL,
					 buffer.toString());
	}
}

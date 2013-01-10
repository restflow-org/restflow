package org.restflow.features;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.SubworkflowBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.ContextProtocol;
import org.restflow.data.InflowProperty;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


@SuppressWarnings("unused")
public class TestExceptionOutflows extends RestFlowTestCase {

	private WorkflowContext 		_context;
	private ConsumableObjectStore	_store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();		
	}
	
	public void test_TopLevelReceiveOnceInflow() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("OneShotInflow")
			.context(_context)
			.director(new DataDrivenDirector())
			
			.node(new JavaNodeBuilder()
				.name("CreateSingletonData")
				.constant("c", 5)
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplier"))

			.node(new JavaNodeBuilder()
				.name("CreateSequenceData")
				.sequence("c", new Integer[] {3, 8, 2})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplicand"))
			
			.node(new JavaNodeBuilder()
				.name("MultiplySequenceBySingleton")
				.inflow("/multiplier", "a", InflowProperty.ReceiveOnce)
				.inflow("/multiplicand", "b")
				.bean(new Object() {
					public int a, b, c;
					public void step() { c = a * b; }
				})
				.outflow("c", "/product"))

			.node(new JavaNodeBuilder()
				.name("RenderProducts")
				.inflow("/product", "v")
				.bean(new Object() {
					public int v;
					public void step() { System.out.println(v); }
				}))
				
			.build();
		
		workflow.configure();

		for (int i = 0; i < 5; i++) {
		
			workflow.initialize();

			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
	
			// confirm expected stdout
			assertEquals(
					"15" 	+ EOL +
					"40"	+ EOL +
					"10"	+ EOL, 
				recorder.getStdoutRecording());
	
			assertEquals(
				"Warning:  Run 1 of workflow 'OneShotInflow' wrapped up with unused data packets:" 		+ EOL +
				"1 packet in inflow 'a' on node 'MultiplySequenceBySingleton' with URI '/multiplier'"	+ EOL,
				recorder.getStderrRecording());
			
			// confirm expected published data
			assertEquals(5, 	_store.take("/multiplier"));
			assertEquals(3, 	_store.take("/multiplicand/1"));
			assertEquals(8, 	_store.take("/multiplicand/2"));
			assertEquals(2, 	_store.take("/multiplicand/3"));
			assertEquals(15, 	_store.take("/product/1"));
			assertEquals(40, 	_store.take("/product/2"));
			assertEquals(10, 	_store.take("/product/3"));

			assertTrue(_store.isEmpty());

			workflow.reset();
		}
	}
}

package org.restflow.nodes;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Workflow;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.metadata.BasicTraceRecorder;
import org.restflow.metadata.WritableTrace;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNode;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestTopWorkflowNode extends RestFlowTestCase {
	private WorkflowContext _context;
	private ConsumableObjectStore _store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
	}
	
	public void test_WorkflowNode_HelloWorld_OneNode_Java() throws Exception {

		WorkflowNode topNode = new WorkflowNodeBuilder()
			.context(_context)			
			.node(new JavaNodeBuilder()
				.bean(new Object() {
				public void step() { System.out.println("Hello world!");}
				}))	
		.build();
		
		topNode.elaborate();

		BasicTraceRecorder traceRecorder = (BasicTraceRecorder) _context.getTraceRecorder();
		traceRecorder.createTrace();
		WritableTrace writableTrace = _context.getWritableTrace();
		writableTrace.storeWorkflowNode(topNode, null);

		topNode.configure();
		topNode.initialize();
		
		final Workflow workflow = (Workflow)((ActorWorkflowNode)topNode).getActor();

		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout showing three values printed
		assertEquals(
			"Hello world!" 	+ EOL ,
			recorder.getStdoutRecording());

		assertEquals(0, _store.size());
	}
	
	
	public void test_WorkflowNode_HelloWorld_TwoNodes() throws Exception {

		WorkflowNode topNode = new WorkflowNodeBuilder()
			
			.context(_context)
			.name("HelloWorld")
			.prefix("/top")
			
			.node(new JavaNodeBuilder()
				.name("CreateGreeting")
				.bean(new Object() {
					public String greeting;
					public void step() { greeting = "Hello!"; }
					})
				.outflow("greeting", "/greeting"))
			
			.node(new JavaNodeBuilder()
				.name("PrintGreeting")
				.inflow("/greeting", "text")
				.bean(new Object() {
					public String text;
					public void step() { System.out.println(text); }
					}))
			
			.build();

		topNode.elaborate();
		
		BasicTraceRecorder traceRecorder = (BasicTraceRecorder) _context.getTraceRecorder();
		traceRecorder.createTrace();
		WritableTrace writableTrace = _context.getWritableTrace();
		writableTrace.storeWorkflowNode(topNode, null);
		
		topNode.configure();
		topNode.initialize();

		final Workflow workflow = (Workflow)((ActorWorkflowNode)topNode).getActor();

		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout showing three values printed
		assertEquals(
			"Hello!" 	+ EOL ,
			recorder.getStdoutRecording());

		System.out.println(_store);
		
		assertEquals("Hello!", _store.take("/top/greeting"));
		assertEquals(0, _store.size());
	}
	
}

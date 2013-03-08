package org.restflow.features;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.ActorStatus;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestWarnOfUnusedData extends RestFlowTestCase {

	private WorkflowContext _context;
	private ConsumableObjectStore _store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
	}
	
	
	public void test_ThreeNodeNoExcessDataWorkflow_PublishSubscribeDirector() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("ThreeNodeWorkflowWithExcessData")
			.context(_context)
			.director(new PublishSubscribeDirector())
			
			.node(new JavaNodeBuilder()
				.name("CreateMultiplier")
				.sequence("c", new Integer[] {5, 7})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplier"))

			.node(new JavaNodeBuilder()
				.name("CreateMultiplicands")
				.sequence("c", new Integer[] {3, 8})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplicand"))
			
			.node(new JavaNodeBuilder()
				.name("CreateOffsets")
				.sequence("c", new Integer[] {10, 20})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/offset"))

			.node(new JavaNodeBuilder()
				.name("MultiplyData")
				.inflow("/multiplier", "a")
				.inflow("/multiplicand", "b")
				.inflow("/offset", "c")
				.bean(new Object() {
					public int a, b, c, d;
					public void step() { d = a * b + c; }
				})
				.outflow("d", "/result"))

			.node(new JavaNodeBuilder()
				.name("RenderResult")
				.inflow("/result", "v")
				.bean(new Object() {
					public int v;
					public void step() { System.out.println(v); }
				}))
				
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder runRecorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals(
				"25" 	+ EOL +
				"76"	+ EOL, 
				runRecorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals("", runRecorder.getStderrRecording());
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder wrapupRecorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.wrapup();}});

		// confirm expected stdout
		assertEquals("", wrapupRecorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals("", wrapupRecorder.getStderrRecording());
		
		workflow.dispose();
		
		// confirm expected published data
		assertEquals(5, 	_store.take("/multiplier/1"));
		assertEquals(7, 	_store.take("/multiplier/2"));
		assertEquals(3, 	_store.take("/multiplicand/1"));
		assertEquals(8, 	_store.take("/multiplicand/2"));
		assertEquals(10, 	_store.take("/offset/1"));
		assertEquals(20, 	_store.take("/offset/2"));
		assertEquals(25, 	_store.take("/result/1"));
		assertEquals(76, 	_store.take("/result/2"));

		assertTrue(_store.isEmpty());
		
		workflow.reset();
	}
	
	public void test_ThreeNodeExcessDataWorkflow_OneDisabledInflow_PublishSubscribeDirector() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("ThreeNodeWorkflowWithExcessData")
			.context(_context)
			.director(new PublishSubscribeDirector())
			
			.node(new JavaNodeBuilder()
				.name("CreateMultiplier")
				.sequence("c", new Integer[] {5, 7})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplier"))

			.node(new JavaNodeBuilder()
				.name("CreateMultiplicands")
				.sequence("c", new Integer[] {3, 8})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplicand"))
			
			.node(new JavaNodeBuilder()
				.name("CreateOffsets")
				.sequence("c", new Integer[] {10, 20})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/offset"))

			.node(new JavaNodeBuilder()
				.name("MultiplyData")
				.inflow("/multiplier", "a")
				.inflow("/multiplicand", "b")
				.inflow("/offset", "c")
				.bean(new Object() {
					public int a, b, c, d;
					private ActorStatus _actorStatus;
					public void setStatus(ActorStatus actorStatus) {_actorStatus = actorStatus;}
					public void step() { d = a * b + c; _actorStatus.disableInput("c"); }
				})
				.outflow("d", "/result"))

			.node(new JavaNodeBuilder()
				.name("RenderResult")
				.inflow("/result", "v")
				.bean(new Object() {
					public int v;
					public void step() { System.out.println(v); }
				}))
				
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals(
				"25" 	+ EOL +
				"66"	+ EOL, 
				recorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals(
				"Warning:  Run 1 of workflow <ThreeNodeWorkflowWithExcessData> wrapped up with unused data packets:" 	+ EOL + 
				"1 packet in queue 'c' on node [MultiplyData] with URI '/offset/2'"										+ EOL,
				recorder.getStderrRecording());
		
		workflow.wrapup();
		workflow.dispose();
		
		// confirm expected published data
		assertEquals(5, 	_store.take("/multiplier/1"));
		assertEquals(7, 	_store.take("/multiplier/2"));
		assertEquals(3, 	_store.take("/multiplicand/1"));
		assertEquals(8, 	_store.take("/multiplicand/2"));
		assertEquals(10, 	_store.take("/offset/1"));
		assertEquals(20, 	_store.take("/offset/2"));
		assertEquals(25, 	_store.take("/result/1"));
		assertEquals(66, 	_store.take("/result/2"));

		assertTrue(_store.isEmpty());
		
		workflow.reset();
	}
	
	public void test_ThreeNodeNoExcessDataWorkflow_OneDisabledInflow_PublishSubscribeDirector() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("ThreeNodeWorkflowWithExcessData")
			.context(_context)
			.director(new PublishSubscribeDirector())
			
			.node(new JavaNodeBuilder()
				.name("CreateMultiplier")
				.sequence("c", new Integer[] {5, 7})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplier"))

			.node(new JavaNodeBuilder()
				.name("CreateMultiplicands")
				.sequence("c", new Integer[] {3, 8})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplicand"))
			
			.node(new JavaNodeBuilder()
				.name("CreateOffsets")
				.sequence("c", new Integer[] {10})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/offset"))

			.node(new JavaNodeBuilder()
				.name("MultiplyData")
				.inflow("/multiplier", "a")
				.inflow("/multiplicand", "b")
				.inflow("/offset", "c")
				.bean(new Object() {
					public int a, b, c, d;
					private ActorStatus _actorStatus;
					public void setStatus(ActorStatus actorStatus) {_actorStatus = actorStatus;}
					public void step() { d = a * b + c; _actorStatus.disableInput("c"); }
				})
				.outflow("d", "/result"))

			.node(new JavaNodeBuilder()
				.name("RenderResult")
				.inflow("/result", "v")
				.bean(new Object() {
					public int v;
					public void step() { System.out.println(v); }
				}))
				
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals(
				"25" 	+ EOL +
				"66"	+ EOL, 
				recorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		workflow.wrapup();
		workflow.dispose();
		
		// confirm expected published data
		assertEquals(5, 	_store.take("/multiplier/1"));
		assertEquals(7, 	_store.take("/multiplier/2"));
		assertEquals(3, 	_store.take("/multiplicand/1"));
		assertEquals(8, 	_store.take("/multiplicand/2"));
		assertEquals(10, 	_store.take("/offset"));
		assertEquals(25, 	_store.take("/result/1"));
		assertEquals(66, 	_store.take("/result/2"));

		assertTrue(_store.isEmpty());
		
		workflow.reset();
	}
	
	public void test_ThreeNodeExcessDataWorkflow_PublishSubscribeDirector() throws Exception {

//		WrapupResult.setIgnoreEndOfStreamPackets(false);
		
		final Workflow workflow = new WorkflowBuilder()

			.name("ThreeNodeWorkflowWithExcessData")
			.context(_context)
			.director(new PublishSubscribeDirector())
			
			.node(new JavaNodeBuilder()
				.name("CreateMultiplier")
				.constant("c", 5)
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplier"))

			.node(new JavaNodeBuilder()
				.name("CreateMultiplicands")
				.sequence("c", new Integer[] {3, 8, 2})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplicand"))
			
			.node(new JavaNodeBuilder()
				.name("CreateOffsets")
				.sequence("c", new Integer[] {10, 20})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/offset"))

			.node(new JavaNodeBuilder()
				.name("MultiplyData")
				.inflow("/multiplier", "a")
				.inflow("/multiplicand", "b")
				.inflow("/offset", "c")
				.bean(new Object() {
					public int a, b, c, d;
					public void step() { d = a * b + c; }
				})
				.outflow("d", "/result"))

			.node(new JavaNodeBuilder()
				.name("RenderResult")
				.inflow("/result", "v")
				.bean(new Object() {
					public int v;
					public void step() { System.out.println(v); }
				}))
				
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder runRecorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals(
				"25" 	+ EOL, 
				runRecorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals(
				"Warning:  Run 1 of workflow <ThreeNodeWorkflowWithExcessData> wrapped up with unused data packets:" 	+ EOL +
				"1 packet in inflow 'b' on node [MultiplyData] with URI '/multiplicand/2'"								+ EOL +
				"1 packet in queue 'b' on node [MultiplyData] with URI '/multiplicand/3'" 								+ EOL +
				"1 packet in inflow 'c' on node [MultiplyData] with URI '/offset/2'"									+ EOL,
				runRecorder.getStderrRecording());

		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder wrapupRecorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.wrapup();}});

		// confirm expected stdout
		assertEquals("", wrapupRecorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals("", wrapupRecorder.getStderrRecording());
		
		workflow.dispose();
		
		// confirm expected published data
		assertEquals(5, 	_store.take("/multiplier"));
		assertEquals(3, 	_store.take("/multiplicand/1"));
		assertEquals(8, 	_store.take("/multiplicand/2"));
		assertEquals(2, 	_store.take("/multiplicand/3"));
		assertEquals(10, 	_store.take("/offset/1"));
		assertEquals(20, 	_store.take("/offset/2"));
		assertEquals(25, 	_store.take("/result/1"));

		assertTrue(_store.isEmpty());
		
		workflow.reset();
	}

	public void test_ThreeNodeExcessDataWorkflow_DataDrivenDirector() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("ThreeNodeWorkflowWithExcessData")
			.context(_context)
			.director(new DataDrivenDirector())
			
			.node(new JavaNodeBuilder()
				.name("CreateMultiplier")
				.constant("c", 5)
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplier"))

			.node(new JavaNodeBuilder()
				.name("CreateMultiplicands")
				.sequence("c", new Integer[] {3, 8, 2})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/multiplicand"))
			
			.node(new JavaNodeBuilder()
				.name("CreateOffsets")
				.sequence("c", new Integer[] {10, 20})
				.bean(new Object() {
					public int c, v;
					public void step() { v = c; }
				})
				.outflow("v", "/offset"))

			.node(new JavaNodeBuilder()
				.name("MultiplyData")
				.inflow("/multiplier", "a")
				.inflow("/multiplicand", "b")
				.inflow("/offset", "c")
				.bean(new Object() {
					public int a, b, c, d;
					public void step() { d = a * b + c; }
				})
				.outflow("d", "/result"))

			.node(new JavaNodeBuilder()
				.name("RenderResult")
				.inflow("/result", "v")
				.bean(new Object() {
					public int v;
					public void step() { System.out.println(v); }
				}))
				
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder runRecorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals(
				"25" 	+ EOL, 
				runRecorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals(
			"Warning:  Run 1 of workflow <ThreeNodeWorkflowWithExcessData> wrapped up with unused data packets:" 	+ EOL +
			"1 packet in outflow 'output' on node [BufferNode-for-MultiplyData-b] with URI '/multiplicand/3'" 		+ EOL +
			"1 packet in inflow 'b' on node [MultiplyData] with URI '/multiplicand/2'" 								+ EOL +
			"1 packet in inflow 'c' on node [MultiplyData] with URI '/offset/2'" 									+ EOL,
			runRecorder.getStderrRecording());

		// run the workflow while capturing stdout and stderr 
		StdoutRecorder wrapupRecorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.wrapup();}});

		// confirm expected stdout
		assertEquals("", wrapupRecorder.getStdoutRecording());

		// confirm that no error messages were generated
		assertEquals("", wrapupRecorder.getStderrRecording());
		
		workflow.dispose();
		
		// confirm expected published data
		assertEquals(5, 	_store.take("/multiplier"));
		assertEquals(3, 	_store.take("/multiplicand/1"));
		assertEquals(8, 	_store.take("/multiplicand/2"));
		assertEquals(2, 	_store.take("/multiplicand/3"));
		assertEquals(10, 	_store.take("/offset/1"));
		assertEquals(20, 	_store.take("/offset/2"));
		assertEquals(25, 	_store.take("/result/1"));

		assertTrue(_store.isEmpty());
		
		workflow.reset();
	}

	
}

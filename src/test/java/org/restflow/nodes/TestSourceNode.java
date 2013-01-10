package org.restflow.nodes;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.GroovyActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.FileProtocol;
import org.restflow.data.Packet;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.GroovyNodeBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.SourceNode;
import org.restflow.nodes.SourceNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestSourceNode extends RestFlowTestCase {
	
	private WorkflowContext			_context;
	private WorkflowContext			_noopContext;
	private ConsumableObjectStore 	_store;
	
	public void setUp() throws Exception {
		super.setUp();
		
		_store = new ConsumableObjectStore(); 
		
		_context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.build();

		_noopContext = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.recorder(new NoopTraceRecorder())
			.build();
	}
	
	public void test_SourceNode_ReadOnce() throws Exception {

		SourceNode source = (SourceNode)new SourceNodeBuilder()
			.context(_noopContext)
			.protocol(new FileProtocol())
			.resource("src/test/resources/unit/TestSourceNode/test.txt")
			.outflow("a", "/a")
			.build();
				
		source.configure();
		source.initialize();
		
		source.trigger();
		
		Packet packet = source.getOutputPacket("a");
		assertEquals("hello", packet.getResource("/a").getData());
		
		source.wrapup();
		source.dispose();
	}
	
	public void test_SourceNode_ReadTwiceNoReset() throws Exception {
		
		SourceNode source = (SourceNode) new SourceNodeBuilder()
			.context(_noopContext)
			.protocol(new FileProtocol())
			.resource("src/test/resources/unit/TestSourceNode/test.txt")
			.outflow("a", "/a")
			.build();
		
		source.configure();
		
		source.initialize();
		source.trigger();
		Packet packet = source.getOutputPacket("a");
		assertEquals("hello", packet.getResource("/a").getData());
		
		source.trigger();
		Exception e = null;
		try {
			packet = source.getOutputPacket("a");
		} catch(Exception ex) {
			e = ex;
		}
		assertNotNull(e);
		assertEquals("Request for packet on empty outflow 'a' on node ", e.getMessage());
	}
	
	public void test_SourceNode_ReadTwiceWithReset() throws Exception {
		
		SourceNode source = (SourceNode) new SourceNodeBuilder()
			.context(_noopContext)
			.protocol(new FileProtocol())
			.resource("src/test/resources/unit/TestSourceNode/test.txt")
			.outflow("a", "/a")
			.build();
		
		source.configure();
		
		source.initialize();
		source.trigger();
		Packet packet = source.getOutputPacket("a");
		assertEquals("hello", packet.getResource("/a").getData());
		
		source.initialize();
		source.trigger();
		packet = source.getOutputPacket("a");
		assertEquals("hello", packet.getResource("/a").getData());
		
		source.wrapup();
		source.dispose();
	}

	public void test_SourceNode_MinimalWorkflow_ExplicitSourceNode() throws Exception {

		final Workflow workflow = new WorkflowBuilder()
	
			.context(_context)
			.prefix("/run{RUN}/")

			.node(new SourceNodeBuilder()
				.protocol(new FileProtocol())
				.resource("src/test/resources/unit/TestSourceNode/test.txt")
				.outflow("a", "/a")
			)
				
			.node(new ActorNodeBuilder()
				.inflow("/a", "value")
				.actor(new GroovyActorBuilder()
					.step("println value"))
			)

		.build();	
		
		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
				workflow.run();
				workflow.run();
			}
		});

		assertEquals(
				"hello" + EOL + 
				"hello" + EOL + 
				"hello" + EOL, 
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals("hello", _store.take("/run1/a"));
		assertEquals("hello", _store.take("/run2/a"));
		assertEquals("hello", _store.take("/run3/a"));
		assertEquals(0, _store.size());
	}

	public void test_SourceNode_MinimalWorkflow_ImplicitSourceNode() throws Exception {

		final Workflow workflow = new WorkflowBuilder()
		
			.prefix("/run{RUN}/")
			.context(_context)
			
			.node(new ActorNodeBuilder()
				.inflow("file:src/test/resources/unit/TestSourceNode/test.txt", "value")
				.actor(new GroovyActorBuilder()
					.step("println value")))

			.build();

		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
				workflow.run();
			}
		});

		assertEquals(
				"hello" + EOL + 
				"hello" + EOL,
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals("hello", _store.take("/run1/src/test/resources/unit/TestSourceNode/test.txt"));
		assertEquals("hello", _store.take("/run2/src/test/resources/unit/TestSourceNode/test.txt"));
		
		assertEquals(0, _store.size());
	}

	
	public void test_SourceNode_NestedWorkflow_ExternalDriver() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("EmphasizeGreeting")
			.context(_context)
			.prefix("/run/{exclamationCount}")
			
			.inflow("exclamationCount", "/count")
	
			.node(new JavaNodeBuilder() 
				.name("CreateEmphasis")
				.inflow("/count", "n")
				.stepsOnce()
				.bean(new CloneableBean() {
					public int n;
					public String e;
					public void step() {
						StringBuffer buffer = new StringBuffer("");
						for(int i = 0; i < n; i++) {
							buffer.append('!');
						}
						e = buffer.toString();
					}
				})
				.outflow("e", "/exclamation")
			)
			
			.node(new WorkflowNodeBuilder()
	
				.name("Emphasize")
				.stepsOnce()
			
				.inflow("/exclamation", "/emphasis")
			
				.node(new SourceNodeBuilder()
					.name("ReadGreetingFile")
					.protocol(new FileProtocol())
					.resource("src/test/resources/unit/TestSourceNode/test.txt")
					.outflow("greeting", "/greeting")
				)

				.node(new JavaNodeBuilder()
					.name("ConcatenateGreetingAndEmphasis")
					.inflow("/emphasis", "e")
					.inflow("/greeting", "g")
					.stepsOnce()
					.bean(new CloneableBean() {
						public String e, g, m;
						public void step() {m = g + e;}
						})
					.outflow("m", "/message")
				)
				
				.node(new GroovyNodeBuilder()
					.name("PrintGreeting")
					.inflow("/message", "value")
					.step("println value")
				)						
			)

		.build();
		
		workflow.configure();
		workflow.initialize();
		
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				for (int i = 0; i <= 5; i++) {
					workflow.set("exclamationCount", i);
					workflow.run();
				}
			}
		});
	
		
		assertEquals(
				"hello" 		+ EOL + 
				"hello!" 		+ EOL + 
				"hello!!" 		+ EOL + 
				"hello!!!" 		+ EOL + 
				"hello!!!!" 	+ EOL + 
				"hello!!!!!" 	+ EOL, 
				recorder.getStdoutRecording());
		
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(0,			_store.take("/run/0/count"));
		assertEquals(1,			_store.take("/run/1/count"));
		assertEquals(2,			_store.take("/run/2/count"));
		assertEquals(3,			_store.take("/run/3/count"));
		assertEquals(4,			_store.take("/run/4/count"));
		assertEquals(5,			_store.take("/run/5/count"));

		assertEquals("",		_store.take("/run/0/exclamation"));
		assertEquals("!",		_store.take("/run/1/exclamation"));
		assertEquals("!!",		_store.take("/run/2/exclamation"));
		assertEquals("!!!",		_store.take("/run/3/exclamation"));
		assertEquals("!!!!",	_store.take("/run/4/exclamation"));
		assertEquals("!!!!!",	_store.take("/run/5/exclamation"));

		assertEquals("",		_store.take("/run/0/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!",		_store.take("/run/1/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!",		_store.take("/run/2/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!!",		_store.take("/run/3/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!!!",	_store.take("/run/4/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!!!!",	_store.take("/run/5/EmphasizeGreeting.Emphasize/emphasis"));

		assertEquals("hello",	_store.take("/run/0/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",	_store.take("/run/1/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",	_store.take("/run/2/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",	_store.take("/run/3/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",	_store.take("/run/4/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",	_store.take("/run/5/EmphasizeGreeting.Emphasize/greeting"));
		
		assertEquals("hello",		_store.take("/run/0/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!",		_store.take("/run/1/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!",		_store.take("/run/2/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!!",	_store.take("/run/3/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!!!",	_store.take("/run/4/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!!!!",	_store.take("/run/5/EmphasizeGreeting.Emphasize/message"));
		
		assertEquals(0, _store.size());
	}

	public void test_SourceNode_DoublyNestedWorkflow_ExternalDriver() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("EmphasizeGreeting")
			.context(_context)
			.prefix("/run/{RUN}")

			.inflow("exclamationCount", "/count")
	
			.node(new JavaNodeBuilder() 
				.name("CreateEmphasis")
				.inflow("/count", "n")
				.stepsOnce()
				.bean(new CloneableBean() {
					public int n;
					public String e;
					public void step() {
						StringBuffer buffer = new StringBuffer("");
						for(int i = 0; i < n; i++) {
							buffer.append('!');
						}
						e = buffer.toString();
					}
				})
				.outflow("e", "/exclamation")
			)
			
			.node(new WorkflowNodeBuilder()
	
				.name("Emphasize")
				.stepsOnce()
				
				.inflow("/exclamation", "/emphasis")
			
				.node(new WorkflowNodeBuilder()

					.name("GetGreeting")
					.stepsOnce()
				
					.node(new SourceNodeBuilder()
						.name("ReadGreetingFile")
						.protocol(new FileProtocol())
						.resource("src/test/resources/unit/TestSourceNode/test.txt")
						.outflow("text", "/text")
					)
					
					.outflow("/text", "/greeting")
				)

				.node(new JavaNodeBuilder()
					.name("ConcatenateGreetingAndEmphasis")
					.inflow("/emphasis", "e")
					.inflow("/greeting", "g")
					.stepsOnce()
					.bean(new CloneableBean() {
						public String e, g, m;
						public void step() {m = g + e;}
						})
					.outflow("m", "/message")
				)
				
				.node(new GroovyNodeBuilder()
					.name("PrintGreeting")
					.inflow("/message", "value")
					.step("println value")
				)
			)

		.build();
		
		workflow.configure();
		workflow.initialize();
		
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				for (int i = 0; i <= 5; i++) {
					workflow.set("exclamationCount", i);
					workflow.run();
				}
			}
		});
	
		assertEquals(
				"hello" 		+ EOL + 
				"hello!" 		+ EOL + 
				"hello!!" 		+ EOL + 
				"hello!!!" 		+ EOL + 
				"hello!!!!" 	+ EOL + 
				"hello!!!!!" 	+ EOL, 
				recorder.getStdoutRecording());
		
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(0,			_store.take("/run/1/count"));
		assertEquals(1,			_store.take("/run/2/count"));
		assertEquals(2,			_store.take("/run/3/count"));
		assertEquals(3,			_store.take("/run/4/count"));
		assertEquals(4,			_store.take("/run/5/count"));
		assertEquals(5,			_store.take("/run/6/count"));

		assertEquals("",			_store.take("/run/1/exclamation"));
		assertEquals("!",			_store.take("/run/2/exclamation"));
		assertEquals("!!",			_store.take("/run/3/exclamation"));
		assertEquals("!!!",			_store.take("/run/4/exclamation"));
		assertEquals("!!!!",		_store.take("/run/5/exclamation"));
		assertEquals("!!!!!",		_store.take("/run/6/exclamation"));

		assertEquals("",			_store.take("/run/1/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!",			_store.take("/run/2/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!",			_store.take("/run/3/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!!",			_store.take("/run/4/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!!!",		_store.take("/run/5/EmphasizeGreeting.Emphasize/emphasis"));
		assertEquals("!!!!!",		_store.take("/run/6/EmphasizeGreeting.Emphasize/emphasis"));

		assertEquals("hello",		_store.take("/run/1/EmphasizeGreeting.Emphasize/EmphasizeGreeting.Emphasize.GetGreeting/text"));
		assertEquals("hello",		_store.take("/run/2/EmphasizeGreeting.Emphasize/EmphasizeGreeting.Emphasize.GetGreeting/text"));
		assertEquals("hello",		_store.take("/run/3/EmphasizeGreeting.Emphasize/EmphasizeGreeting.Emphasize.GetGreeting/text"));
		assertEquals("hello",		_store.take("/run/4/EmphasizeGreeting.Emphasize/EmphasizeGreeting.Emphasize.GetGreeting/text"));
		assertEquals("hello",		_store.take("/run/5/EmphasizeGreeting.Emphasize/EmphasizeGreeting.Emphasize.GetGreeting/text"));
		assertEquals("hello",		_store.take("/run/6/EmphasizeGreeting.Emphasize/EmphasizeGreeting.Emphasize.GetGreeting/text"));

		assertEquals("hello",		_store.take("/run/1/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",		_store.take("/run/2/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",		_store.take("/run/3/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",		_store.take("/run/4/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",		_store.take("/run/5/EmphasizeGreeting.Emphasize/greeting"));
		assertEquals("hello",		_store.take("/run/6/EmphasizeGreeting.Emphasize/greeting"));
		
		assertEquals("hello",		_store.take("/run/1/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!",		_store.take("/run/2/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!",		_store.take("/run/3/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!!",	_store.take("/run/4/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!!!",	_store.take("/run/5/EmphasizeGreeting.Emphasize/message"));
		assertEquals("hello!!!!!",	_store.take("/run/6/EmphasizeGreeting.Emphasize/message"));

		assertEquals(0, _store.size());
	}	

	public void test_SourceNode_NestedWorkflow_InternalDriver() throws Exception {

		final Workflow workflow = new WorkflowBuilder()

			.name("EmphasizeGreeting")
			.context(_context)
	
			.node(new GroovyNodeBuilder()
				.name("PrintGreeting")
				.sequence("e", new Integer[] {0, 1, 2, 3, 4, 5} )
				.step("v = e;")
				.outflow("v", "/count")
			)
	
			.node(new JavaNodeBuilder() 
				.name("CreateEmphasis")
				.inflow("/count", "n")
				.bean(new CloneableBean() {
					public int n;
					public String e;
					public void step() {
						StringBuffer buffer = new StringBuffer("");
						for(int i = 0; i < n; i++) {
							buffer.append('!');
						}
						e = buffer.toString();
					}
				})
				.outflow("e", "/exclamation")
			)
			
			.node(new WorkflowNodeBuilder()
	
				.name("Emphasize")
				.prefix("/subrun{RUN}/")
			
				.inflow("/exclamation", "/emphasis")
			
				.node(new SourceNodeBuilder()
					.name("ReadGreetingFile")
					.protocol(new FileProtocol())
					.resource("src/test/resources/unit/TestSourceNode/test.txt")
					.outflow("greeting", "/greeting")
				)

				.node(new JavaNodeBuilder()
					.name("ConcatenateGreetingAndEmphasis")
					.inflow("/emphasis", "e")
					.inflow("/greeting", "g")
					.stepsOnce()
					.bean(new CloneableBean() {
						public String e, g, m;
						public void step() {m = g + e;}
						})
					.outflow("m", "/message"))
				
				.node(new GroovyNodeBuilder()
					.name("PrintGreeting")
					.inflow("/message", "value")
					.step("println value")
				)									
			)
			
		.build();
		
		workflow.configure();
		workflow.initialize();
		
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
			}
		});
	
		assertEquals(
				"hello" 		+ EOL + 
				"hello!" 		+ EOL + 
				"hello!!" 		+ EOL + 
				"hello!!!" 		+ EOL + 
				"hello!!!!" 	+ EOL + 
				"hello!!!!!" 	+ EOL, 
				recorder.getStdoutRecording());
		
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(0,			_store.take("/count/1"));
		assertEquals(1,			_store.take("/count/2"));
		assertEquals(2,			_store.take("/count/3"));
		assertEquals(3,			_store.take("/count/4"));
		assertEquals(4,			_store.take("/count/5"));
		assertEquals(5,			_store.take("/count/6"));

		assertEquals("",		_store.take("/exclamation/1"));
		assertEquals("!",		_store.take("/exclamation/2"));
		assertEquals("!!",		_store.take("/exclamation/3"));
		assertEquals("!!!",		_store.take("/exclamation/4"));
		assertEquals("!!!!",	_store.take("/exclamation/5"));
		assertEquals("!!!!!",	_store.take("/exclamation/6"));

		assertEquals("",		_store.take("/subrun1/emphasis"));
		assertEquals("!",		_store.take("/subrun2/emphasis"));
		assertEquals("!!",		_store.take("/subrun3/emphasis"));
		assertEquals("!!!",		_store.take("/subrun4/emphasis"));
		assertEquals("!!!!",	_store.take("/subrun5/emphasis"));
		assertEquals("!!!!!",	_store.take("/subrun6/emphasis"));

		assertEquals("hello",	_store.take("/subrun1/greeting"));
		assertEquals("hello",	_store.take("/subrun2/greeting"));
		assertEquals("hello",	_store.take("/subrun3/greeting"));
		assertEquals("hello",	_store.take("/subrun4/greeting"));
		assertEquals("hello",	_store.take("/subrun5/greeting"));
		assertEquals("hello",	_store.take("/subrun6/greeting"));
		
		assertEquals("hello",		_store.take("/subrun1/message"));
		assertEquals("hello!",		_store.take("/subrun2/message"));
		assertEquals("hello!!",		_store.take("/subrun3/message"));
		assertEquals("hello!!!",	_store.take("/subrun4/message"));
		assertEquals("hello!!!!",	_store.take("/subrun5/message"));
		assertEquals("hello!!!!!",	_store.take("/subrun6/message"));
		
		assertEquals(0, _store.size());
	}
}

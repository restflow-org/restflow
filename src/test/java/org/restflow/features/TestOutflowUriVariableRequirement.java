package org.restflow.features;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.DataProtocol;
import org.restflow.data.Packet;
import org.restflow.data.SingleResourcePacket;
import org.restflow.directors.Director;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.metadata.BasicTraceRecorder;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;


public class TestOutflowUriVariableRequirement extends RestFlowTestCase {

	private WorkflowContext _context;
	private WorkflowContext _noopContext;
	private ConsumableObjectStore _store;
	
	public void setUp() throws Exception {
		
		_store = new ConsumableObjectStore();
	
		_context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("nosuffix", new DataProtocol() {
				public boolean supportsSuffixes() { return false; }
			})
			.build();

		_noopContext = new WorkflowContextBuilder()
			.store(_store)
			.recorder(new NoopTraceRecorder())
			.scheme("nosuffix", new DataProtocol() {
				public boolean supportsSuffixes() { return false; }
			})
			.build();
	}
	
	public void test_IsolatedNode_NoOutflowVariables_DataProtocol() throws Exception {

		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("s")
			.actor(new JavaActorBuilder()
				.bean(new Object() {
					public String s;
					public String g;
					public void step() { g = s; }
				}))
			.outflow("g", "/greeting")
			.build();
		
		node.elaborate();
		
		BasicTraceRecorder recorder = (BasicTraceRecorder) _context.getTraceRecorder();
		recorder.createTrace();
		_context.getWritableTrace().storeWorkflowNode(node, null);
		
		node.configure();
		node.initialize();
		
		Packet helloPacket = new SingleResourcePacket("Hello");
		recorder.recordPacketCreated(helloPacket, null);
		node.setInputPacket("s", helloPacket);
		node.trigger();
		assertEquals("Hello", node.readValueFromOutflow("g"));
		assertEquals("Hello", _store.take("/greeting/1"));
		assertEquals(0, _store.size());
		
		Packet goodbyePacket = new SingleResourcePacket("Goodbye");
		recorder.recordPacketCreated(goodbyePacket, null);
		node.setInputPacket("s", goodbyePacket);
		node.trigger();
		assertEquals("Goodbye", node.readValueFromOutflow("g"));
		assertEquals("Goodbye", _store.take("/greeting/2"));
		assertEquals(0, _store.size());

		Packet laterPacket = new SingleResourcePacket("Later");
		recorder.recordPacketCreated(laterPacket, null);
		node.setInputPacket("s", laterPacket);
		node.trigger();
		assertEquals("Later", node.readValueFromOutflow("g"));
		assertEquals("Later", _store.take("/greeting/3"));
		assertEquals(0, _store.size());
		
		node.wrapup();
		node.dispose();
	}

	public void test_IsolatedNode_NoOutflowVariables_NoSuffixProtocol() throws Exception {

		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("s")
			.actor(new JavaActorBuilder()
				.bean(new Object() {
					public String s;
					public String g;
					public void step() { g = s; }
				}))
			.outflow("g", "nosuffix:/greeting")
			.build();
		
		node.elaborate();
		
		Exception e = null;
		try {
			node.configure();
		} catch (Exception ex) {
			e = ex;
		}
		assertNotNull(e);
		assertEquals("URI template for outflow g must include at least one variable.", e.getMessage());
	}
	
	public void test_IsolatedNode_NoOutflowVariables_NoSuffixProtocol_StepsOnce() throws Exception {

		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("s")
			.actor(new JavaActorBuilder()
				.bean(new Object() {
					public String s;
					public String g;
					public void step() { g = s; }
				}))
			.outflow("g", "nosuffix:/greeting")
			.stepsOnce()
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		BasicTraceRecorder recorder = (BasicTraceRecorder) _context.getTraceRecorder();
		recorder.createTrace();
		_context.getWritableTrace().storeWorkflowNode(node, null);
		
		Packet helloPacket = new SingleResourcePacket("Hello");
		recorder.recordPacketCreated(helloPacket, null);
		node.setInputPacket("s", helloPacket);
		node.trigger();
		assertEquals("Hello", node.readValueFromOutflow("g"));
		assertEquals("Hello", _store.take("/greeting"));
		assertEquals(0, _store.size());
		
		node.trigger();
		assertEquals(AbstractWorkflowNode.EndOfStreamPacket, node.getOutputPacket("g"));
		assertEquals(0, _store.size());

		node.wrapup();
		node.dispose();
	}
	
	public void test_IsolatedNode_NoSuffixProtocol_WithOutflowVariable() throws Exception {

		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("i")
			.inflow("s")
			.actor(new JavaActorBuilder()
				.bean(new Object() {
					public int i;
					public String s;
					public String g;
					public void step() { g = s; }
				}))
			.outflow("g", "nosuffix:/{i}/greeting")
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		BasicTraceRecorder recorder = (BasicTraceRecorder) _context.getTraceRecorder();
		recorder.createTrace();
		_context.getWritableTrace().storeWorkflowNode(node, null);
		
		Packet packet;
		
		packet = new SingleResourcePacket(17);
		recorder.recordPacketCreated(packet, null);
		node.setInputPacket("i", packet);
		
		packet = new SingleResourcePacket("Hello");
		recorder.recordPacketCreated(packet, null);
		node.setInputPacket("s", packet);
		
		node.trigger();
		assertEquals("Hello", node.readValueFromOutflow("g"));
		assertEquals("Hello", _store.take("/17/greeting"));
		assertEquals(0, _store.size());
		
		packet = new SingleResourcePacket(12);
		recorder.recordPacketCreated(packet, null);
		node.setInputPacket("i", packet);

		
		packet = new SingleResourcePacket("Goodbye");
		recorder.recordPacketCreated(packet, null);
		node.setInputPacket("s", packet);
		
		node.trigger();
		assertEquals("Goodbye", node.readValueFromOutflow("g"));
		assertEquals("Goodbye", _store.take("/12/greeting"));
		assertEquals(0, _store.size());

		packet = new SingleResourcePacket("Hello");
		recorder.recordPacketCreated(packet, null);
		node.setInputPacket("s", packet);

		packet = new SingleResourcePacket(42);
		recorder.recordPacketCreated(packet, null);
		node.setInputPacket("i", packet);

		
		packet = new SingleResourcePacket("Later");
		recorder.recordPacketCreated(packet, null);
		node.setInputPacket("s", packet);
		
		node.trigger();
		assertEquals("Later", node.readValueFromOutflow("g"));
		assertEquals("Later", _store.take("/42/greeting"));
		assertEquals(0, _store.size());
		
		node.wrapup();
		node.dispose();
	}
	
	public void test_SingleNodeWorkflow_NoOutflowVariables_DataProtocol() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("i", "/input")
			.node(new ActorNodeBuilder()
				.inflow("/input", "s")
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public String s;
						public String g;
						public void step() { g = s; }
					}))
				.outflow("g", "/greeting"))
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("i", "Hello");
		workflow.run();
		
		assertEquals("Hello", _store.take("/input"));
		assertEquals("Hello", _store.take("/greeting/1"));
		assertEquals(0, _store.size());
	}
	
	public void test_SingleNodeWorkflow_NoOutflowVariables_NoSuffixProtocol() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("i", "/input")
			.node(new ActorNodeBuilder()
				.inflow("/input", "s")
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public String s;
						public String g;
						public void step() { g = s; }
					}))
				.outflow("g", "nosuffix:/greeting"))
			.build();
		
		Exception e = null;
		try {
			workflow.configure();
		} catch (Exception ex) {
			e = ex;
		}
		assertNotNull(e);
		assertEquals("URI template for outflow g must include at least one variable.", e.getMessage());
	}
	
	public void test_SingleNodeWorkflow_NoOutflowVariables_NoSuffixProtocol_NodeStepsOnce() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("wi", "/input")
			.inflow("ws", "/string")
			.node(new ActorNodeBuilder()
				.inflow("/input", "ai")
				.inflow("/string", "as")
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public int ai;
						public String as;
						public String g;
						public void step() { g = as; }
					}))
				.outflow("g", "nosuffix:/greeting")
				.stepsOnce())
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("wi", 17);
		workflow.set("ws", "Hello");
		workflow.run();
		
		assertEquals(17, _store.take("/input"));
		assertEquals("Hello", _store.take("/string"));
		assertEquals("Hello", _store.take("/greeting"));
		assertEquals(0, _store.size());
	}
	
	public void test_SingleNodeWorkflow_NoOutflowVariables_NoSuffixProtocol_DirectorStepsOnce() throws Exception {
		
		Director director = new PublishSubscribeDirector();
		director.setNodesStepOnce(true);
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.director(director)
			.inflow("wi", "/input")
			.inflow("ws", "/string")
			.node(new ActorNodeBuilder()
				.inflow("/input", "ai")
				.inflow("/string", "as")
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public int ai;
						public String as;
						public String g;
						public void step() { g = as; }
					}))
				.outflow("g", "nosuffix:/greeting"))
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("wi", 17);
		workflow.set("ws", "Hello");
		workflow.run();
		
		assertEquals(17, _store.take("/input"));
		assertEquals("Hello", _store.take("/string"));
		assertEquals("Hello", _store.take("/greeting"));
		assertEquals(0, _store.size());
	}
	
	public void test_SingleNodeWorkflow_WithOutflowVariable() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("wi", "/input")
			.inflow("ws", "/string")
			.node(new ActorNodeBuilder()
				.inflow("/input", "ai")
				.inflow("/string", "as")
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public int ai;
						public String as;
						public String g;
						public void step() { g = as; }
					}))
			.outflow("g", "nosuffix:/{ai}/greeting"))
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("wi", 17);
		workflow.set("ws", "Hello");
		workflow.run();
		
		assertEquals(17, _store.take("/input"));
		assertEquals("Hello", _store.take("/string"));
		assertEquals("Hello", _store.take("/17/greeting"));
		assertEquals(0, _store.size());
	}
	
	
	public void test_SingleNodeWorkflow_WithOutflowVariable_MultiRuns() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.prefix("/run{RUN}")
			.inflow("wi", "/input")
			.inflow("ws", "/string")
			.node(new ActorNodeBuilder()
				.inflow("/input", "ai")
				.inflow("/string", "as")
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public int ai;
						public String as;
						public String g;
						public void step() { g = as; }
					}))
			.outflow("g", "nosuffix:/{ai}/greeting")
				.stepsOnce())
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("wi", 17);
		workflow.set("ws", "Hello");
		workflow.run();
		
		assertEquals(17, _store.take("/run1/input"));
		assertEquals("Hello", _store.take("/run1/string"));
		assertEquals("Hello", _store.take("/run1/17/greeting"));
		assertEquals(0, _store.size());

		workflow.set("wi", 12);
		workflow.set("ws", "Goodbye");
		workflow.run();

		assertEquals(12, _store.take("/run2/input"));
		assertEquals("Goodbye", _store.take("/run2/string"));
		assertEquals("Goodbye", _store.take("/run2/12/greeting"));
		assertEquals(0, _store.size());
		
		workflow.set("wi", 42);
		workflow.set("ws", "Later");
		workflow.run();
		
		assertEquals(42, _store.take("/run3/input"));
		assertEquals("Later", _store.take("/run3/string"));
		assertEquals("Later", _store.take("/run3/42/greeting"));
		assertEquals(0, _store.size());
	}

	public void test_DoublyNestedSingleNodeWorkflow_NoOutflowVariables_DataProtocol() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("i", "/input")
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				.inflow("/input", "/input")
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/input", "/input")
					.node(new ActorNodeBuilder()
						.inflow("/input", "s")
						.actor(new JavaActorBuilder()
							.bean(new Object() {
								public String s;
								public String g;
								public void step() { g = s; }
							}))
						.outflow("g", "/greeting"))
				)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("i", "Hello");
		workflow.run();
		
		assertEquals("Hello", _store.take("/input"));
		assertEquals("Hello", _store.take("/sub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/greeting/1"));
		assertEquals(0, _store.size());
	}
	
	public void test_DoublyNestedSingleNodeWorkflow_NoOutflowVariables_NoSuffixProtocol() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("i", "/input")
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				.inflow("/input", "/input")
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/input", "/input")
					.node(new ActorNodeBuilder()
						.inflow("/input", "s")
						.actor(new JavaActorBuilder()
							.bean(new Object() {
								public String s;
								public String g;
								public void step() { g = s; }
							}))
						.outflow("g", "nosuffix:/greeting"))
				)
			)
			.build();
		
		Exception e = null;
		try {
			workflow.configure();
		} catch (Exception ex) {
			e = ex;
		}
		assertNotNull(e);
		assertEquals("URI template for outflow g must include at least one variable.", e.getMessage());
	}
	
	
	public void test_DoublyNestedSingleNodeWorkflow_NoOutflowVariables_DataProtocol_NodeStepsOnce() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("i", "/input")
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				.inflow("/input", "/input")
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/input", "/input")
					.node(new ActorNodeBuilder()
						.inflow("/input", "s")
						.actor(new JavaActorBuilder()
							.bean(new Object() {
								public String s;
								public String g;
								public void step() { g = s; }
							}))
						.outflow("g", "nosuffix:/greeting")
						.stepsOnce())
				)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("i", "Hello");
		workflow.run();
		
		assertEquals("Hello", _store.take("/input"));
		assertEquals("Hello", _store.take("/sub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/greeting"));
		assertEquals(0, _store.size());
	}
	
	public void test_DoublyNestedSingleNodeWorkflow_NoOutflowVariables_DataProtocol_DirectorStepsOnce() throws Exception {
		
		Director director = new PublishSubscribeDirector();
		director.setNodesStepOnce(true);
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("i", "/input")
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				.inflow("/input", "/input")
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.director(director)
					.inflow("/input", "/input")
					.node(new ActorNodeBuilder()
						.inflow("/input", "s")
						.actor(new JavaActorBuilder()
							.bean(new Object() {
								public String s;
								public String g;
								public void step() { g = s; }
							}))
						.outflow("g", "nosuffix:/greeting"))
				)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("i", "Hello");
		workflow.run();
		
		assertEquals("Hello", _store.take("/input"));
		assertEquals("Hello", _store.take("/sub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/greeting"));
		assertEquals(0, _store.size());
	}
	
	public void test_DoublyNestedSingleNodeWorkflow_WithOutflowVariables() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.inflow("i", "/input")
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				.inflow("/input", "/input")
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/input", "/input")
					.node(new ActorNodeBuilder()
						.inflow("/input", "s")
						.actor(new JavaActorBuilder()
							.bean(new Object() {
								public String s;
								public String g;
								public void step() { g = s; }
							}))
						.outflow("g", "nosuffix:/greeting_{STEP}"))
				)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("i", "Hello");
		workflow.run();
		
		assertEquals("Hello", _store.take("/input"));
		assertEquals("Hello", _store.take("/sub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/input"));
		assertEquals("Hello", _store.take("/sub1/subsub1/greeting_1"));
		assertEquals(0, _store.size());
	}

	public void test_DoublyNestedSingleNodeWorkflow_WithOutflowVariables_MultiRuns() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.name("Top")
			.prefix("/run{RUN}")
			.inflow("wi", "/input")
			.inflow("ws", "/string")
			.node(new WorkflowNodeBuilder()
				.name("Sub")
				.prefix("/sub{STEP}")
				.inflow("/input", "/input")
				.inflow("/string", "/string")
				.node(new WorkflowNodeBuilder()
					.name("SubSub")
					.prefix("/subsub{STEP}")
					.inflow("/input", "/input")
					.inflow("/string", "/string")
					.node(new ActorNodeBuilder()
						.name("CopyNode")
						.inflow("/input", "ai")
						.inflow("/string", "as")
						.actor(new JavaActorBuilder()
							.bean(new Object() {
								public int ai;
								public String as;
								public String g;
								public void step() { g = as; }
							}))
						.outflow("g", "nosuffix:/{ai}/greeting")
					)
				)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.set("wi", 17);
		workflow.set("ws", "Hello");
		workflow.run();
		
		assertEquals(17, 		_store.take("/run1/input"));
		assertEquals("Hello",	_store.take("/run1/string"));
		assertEquals(17, 		_store.take("/run1/sub1/input"));
		assertEquals("Hello", 	_store.take("/run1/sub1/string"));
		assertEquals(17, 		_store.take("/run1/sub1/subsub1/input"));
		assertEquals("Hello", 	_store.take("/run1/sub1/subsub1/string"));
		assertEquals("Hello", 	_store.take("/run1/sub1/subsub1/17/greeting"));
		assertEquals(0, _store.size());
		
		workflow.set("wi", 12);
		workflow.set("ws", "Goodbye");
		workflow.run();

		assertEquals(12, 		_store.take("/run2/input"));
		assertEquals("Goodbye",	_store.take("/run2/string"));
		assertEquals(12, 		_store.take("/run2/sub1/input"));
		assertEquals("Goodbye", _store.take("/run2/sub1/string"));
		assertEquals(12, 		_store.take("/run2/sub1/subsub1/input"));
		assertEquals("Goodbye", _store.take("/run2/sub1/subsub1/string"));
		assertEquals("Goodbye", _store.take("/run2/sub1/subsub1/12/greeting"));
		assertEquals(0, _store.size());

		workflow.set("wi", 42);
		workflow.set("ws", "Later");
		workflow.run();
		
		assertEquals(42, 		_store.take("/run3/input"));
		assertEquals("Later",	_store.take("/run3/string"));
		assertEquals(42, 		_store.take("/run3/sub1/input"));
		assertEquals("Later", 	_store.take("/run3/sub1/string"));
		assertEquals(42, 		_store.take("/run3/sub1/subsub1/input"));
		assertEquals("Later", 	_store.take("/run3/sub1/subsub1/string"));
		assertEquals("Later", 	_store.take("/run3/sub1/subsub1/42/greeting"));
		assertEquals(0, _store.size());
	}
}

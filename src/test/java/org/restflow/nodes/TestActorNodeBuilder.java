package org.restflow.nodes;

import java.sql.SQLException;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Actor;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.actors.Actor.ActorFSM;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.nodes.WorkflowNode;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestActorNodeBuilder extends RestFlowTestCase {

	private WorkflowContext _context;
	
	public void setUp() throws Exception {
		_context = new WorkflowContextBuilder()
			.recorder(new NoopTraceRecorder())
			.build();
	}
	
	@Override
	public void tearDown() {
		try {
			_context.getTraceRecorder().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void test_JavaActor_OneInflowOneOutflow() throws Exception {

		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("x")
			.actor(new JavaActorBuilder()
				.context(_context)
				.bean(new DoublerBeanWithAccessors()))
			.outflow("y")
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		node.writeValueToInflow("x", 5);
		node.trigger();
		assertEquals(10, node.readValueFromOutflow("y"));

		node.writeValueToInflow("x", 7);
		node.trigger();
		assertEquals(14, node.readValueFromOutflow("y"));
		
		node.wrapup();
		node.dispose();
	}
	
	public void test_JavaActor_OneInflowOneOutflow_StandaloneActor() throws Exception {

		Actor actor = new JavaActorBuilder()
			.context(_context)
			.bean(new DoublerBeanWithAccessors())
			.build();
		
		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("x")
			.actor(actor)
			.outflow("y")
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		node.writeValueToInflow("x", 5);
		node.trigger();
		assertEquals(10, node.readValueFromOutflow("y"));

		node.writeValueToInflow("x", 7);
		node.trigger();
		assertEquals(14, node.readValueFromOutflow("y"));
		
		node.wrapup();
		node.dispose();
	}
	
	public void test_GroovyActor_OneInflowOneOutflow() throws Exception {
		
		@SuppressWarnings("unused")
		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("x")
			.actor(new JavaActorBuilder()
				.context(_context)
				.bean(new Object() {
					public int x, y;
					public void step() { y = 2 * x; }
				}))
			.outflow("y")
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		node.writeValueToInflow("x", 5);
		node.trigger();
		assertEquals(10, node.readValueFromOutflow("y"));

		node.writeValueToInflow("x", 7);
		node.trigger();
		assertEquals(14, node.readValueFromOutflow("y"));
		
		node.wrapup();
		node.dispose();
	}
	

	public void test_GroovyActor_OneInflowOneOutflow_StandaloneActor() throws Exception {
		
		@SuppressWarnings("unused")
		Actor actor = new JavaActorBuilder()
			.context(_context)
			.bean(new Object() {
				public int x, y;
				public void step() { y = 2 * x; }
			})
			.build();
		
		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("x")
			.actor(actor)
			.outflow("y")
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		node.writeValueToInflow("x", 5);
		node.trigger();
		assertEquals(10, node.readValueFromOutflow("y"));

		node.writeValueToInflow("x", 7);
		node.trigger();
		assertEquals(14, node.readValueFromOutflow("y"));
		
		node.wrapup();
		node.dispose();
	}
		
	
	public void test_TwoInflowsOneOutflow() throws Exception {
		
		@SuppressWarnings("unused")
		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("a")
			.inflow("b")
			.actor(new JavaActorBuilder()
				.context(_context)
				.bean(new Object() {
					public int a, b, c;
					public void step() { c = a * b; }
				}))
			.outflow("c")
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		node.writeValueToInflow("a", 5);
		node.writeValueToInflow("b", 2);
		node.trigger();
		assertEquals(10, node.readValueFromOutflow("c"));

		node.writeValueToInflow("a", 7);
		node.writeValueToInflow("b", 3);
		node.trigger();
		assertEquals(21, node.readValueFromOutflow("c"));
		
		node.wrapup();
		node.dispose();
	}


	public void test_TwoInflowsOneOutflow_StandaloneActor() throws Exception {
		
		@SuppressWarnings("unused")
		Actor actor = new JavaActorBuilder()
			.context(_context)
			.bean(new Object() {
				public int a, b, c;
				public void step() { c = a * b; }
			})
			.build();
		
		ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()
			.context(_context)
			.inflow("a")
			.inflow("b")
			.actor(actor)
			.outflow("c")
			.build();
		
		node.elaborate();
		node.configure();
		node.initialize();
		
		node.writeValueToInflow("a", 5);
		node.writeValueToInflow("b", 2);
		node.trigger();
		assertEquals(10, node.readValueFromOutflow("c"));

		node.writeValueToInflow("a", 7);
		node.writeValueToInflow("b", 3);
		node.trigger();
		assertEquals(21, node.readValueFromOutflow("c"));
		
		node.wrapup();
		node.dispose();
	}

	
	public void test_NodeWithStdoutAndInitializeAndWrapup() throws Exception {
		
		@SuppressWarnings("unused")
		final ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()		
			.context(_context)
			.actor(new JavaActorBuilder()
					.state("total")
					.bean(new Object() {
						public int x, y, total;
						public void initialize() {total = 0; System.out.println(total);}
						public void step() {total += x; y = total; System.out.println(y);}
						public void wrapup() {System.out.println(total);}
					}))
			.inflow("x")
			.outflow("y")
			.build();
		
		node.elaborate();
		node.configure();
		
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {

				node.initialize();

				node.writeValueToInflow("x", 5);
				node.trigger();
				assertEquals(5, node.readValueFromOutflow("y"));
		
				node.writeValueToInflow("x", 7);
				node.trigger();
				assertEquals(12, node.readValueFromOutflow("y"));
		
				node.wrapup();
			}
		});
		
		assertEquals(
				"0" + EOL + 
				"5" + EOL + 
				"12" + EOL + 
				"12" + EOL, 
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());

		node.dispose();
	}

	public void test_NodeWithStdoutAndInitializeAndWrapup_StandaloneActor() throws Exception {
		
		@SuppressWarnings("unused")
		Actor actor = new JavaActorBuilder()
			.context(_context)
			.state("total")
			.bean(new Object() {
				public int x, y, total;
				public void initialize() {total = 0; System.out.println(total);}
				public void step() {total += x; y = total; System.out.println(y);}
				public void wrapup() {System.out.println(total);}
			})
			.build();
		
		final ActorWorkflowNode node = (ActorWorkflowNode) new ActorNodeBuilder()		
			.context(_context)
			.actor(actor)
			.inflow("x")
			.outflow("y")
			.build();
		
		node.elaborate();
		node.configure();
		
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {

				node.initialize();

				node.writeValueToInflow("x", 5);
				node.trigger();
				assertEquals(5, node.readValueFromOutflow("y"));
		
				node.writeValueToInflow("x", 7);
				node.trigger();
				assertEquals(12, node.readValueFromOutflow("y"));
		
				node.wrapup();
			}
		});
		
		assertEquals(
				"0" + EOL + 
				"5" + EOL + 
				"12" + EOL + 
				"12" + EOL, 
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());

		node.dispose();
	}

	
	public void test_WorkflowBuilder_NoNames() throws Exception {

		@SuppressWarnings("unused")
		Workflow workflow = new WorkflowBuilder()

			.context(_context)
			.director(new PublishSubscribeDirector())

			.node(new ActorNodeBuilder()
				.constant("input", 2)
				.actor(new JavaActorBuilder()
					.context(_context)
					.bean(new Object() {
						public int input, output;
						public void step() {output = input; System.out.println(output);}
					}))
				.outflow("output", "/original")
			)
				
			.node(new ActorNodeBuilder()
				.inflow("/original", "x" )
				.actor(new JavaActorBuilder()
					.context(_context)
					.bean(new DoublerBeanWithoutAccessors()))
				.outflow("y", "/doubled")
				.maxConcurrency(2)
			)
				
			.node(new ActorNodeBuilder()
				.inflow("/doubled", "value")
				.actor(new JavaActorBuilder()
				.context(_context)
					.context(_context)
					.bean(new Object() {
						public int value;
						public void step() { System.out.println(value); }
					}
			)))
			
			.build();
		
		workflow.configure();
		workflow.initialize();

		ActorWorkflowNode node = (ActorWorkflowNode)workflow.node("anonymous_node_2");
		assertEquals(ActorFSM.CONFIGURED, node.getActor().state());
		
		workflow.run();
		workflow.wrapup();
		workflow.dispose();
	}
	
	public void test_WorkflowBuilder_NoNames_StandaloneActors() throws Exception {

		@SuppressWarnings("unused")
		Actor actor1 = new JavaActorBuilder()
			.context(_context)
			.bean(new Object() {
				public Object input, output;
				public void step() {output = input; System.out.println(output);}
			})
			.build();
		
		Actor actor2 = new JavaActorBuilder()
			.context(_context)
			.bean(new DoublerBeanWithoutAccessors())
			.build();
		
		@SuppressWarnings("unused")
		Actor actor3 = new JavaActorBuilder()
			.context(_context)
			.bean(new Object() {
				public Object value;
				public void step() { System.out.println(value);}
			})
			.build();
			
		Workflow workflow = new WorkflowBuilder()

			.context(_context)
			.director(new PublishSubscribeDirector())

			.node(new ActorNodeBuilder()
				.constant("input", 2)
				.actor(actor1)
				.outflow("output", "/original")
			)
				
			.node(new ActorNodeBuilder()
				.inflow("/original", "x" )
				.actor(actor2)
				.outflow("y", "/doubled")
				.maxConcurrency(2)
			)
				
			.node(new ActorNodeBuilder()
				.inflow("/doubled", "value")
				.actor(actor3)
			)
			
			.build();
		
		workflow.configure();
		workflow.initialize();

		ActorWorkflowNode node = (ActorWorkflowNode)workflow.node("anonymous_node_2");
		assertEquals(ActorFSM.CONFIGURED, node.getActor().state());
		
		workflow.run();
		workflow.wrapup();
		workflow.dispose();
	}


	public void test_WorkflowBuilder_NoNames_StandaloneActorsAndNodes() throws Exception {

		@SuppressWarnings("unused")
		Actor actor1 = new JavaActorBuilder()
			.context(_context)
			.bean(new Object() {
				public Object input, output;
				public void step() {output = input; System.out.println(output);}
			})
			.build();
		
		Actor actor2 = new JavaActorBuilder()
			.context(_context)
			.bean(new DoublerBeanWithoutAccessors())
			.build();
		
		@SuppressWarnings("unused")
		Actor actor3 = new JavaActorBuilder()
			.context(_context)
			.bean(new Object() {
				public Object value;
				public void step() { System.out.println(value);}
			})
			.build();
		
		WorkflowNode node1 = new ActorNodeBuilder()
			.constant("input", 2)
			.actor(actor1)
			.outflow("output", "/original")
			.build();
		
		WorkflowNode node2 = new ActorNodeBuilder()
			.inflow("/original", "x" )
			.actor(actor2)
			.outflow("y", "/doubled")
			.maxConcurrency(2)
			.build();
		
		WorkflowNode node3 = new ActorNodeBuilder()
			.inflow("/doubled", "value")
			.actor(actor3)
			.build();
		
		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.director(new PublishSubscribeDirector())
			.node(node1)
			.node(node2)
			.node(node3)
			.build();
		
		workflow.configure();
		workflow.initialize();

		ActorWorkflowNode node = (ActorWorkflowNode)workflow.node("anonymous_node_2");
		assertEquals(ActorFSM.CONFIGURED, node.getActor().state());
		
		workflow.run();
		workflow.wrapup();
		workflow.dispose();
	}

	
	public static class DoublerBeanWithAccessors {
		private int _x, _y;
		public void setX(int x) {_x = x;}
		public void step() {_y = 2 * _x;}
		public int getY() {return _y;}
	}
	
	
	public static class DoublerBeanWithoutAccessors extends CloneableBean {
		private int _x, _y;
		public void setX(int x) {_x = x;}
		public void step() {_y = 2 * _x;}
		public int getY() {return _y;}
	}
}

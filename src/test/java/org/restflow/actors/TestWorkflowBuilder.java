package org.restflow.actors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.GroovyActorBuilder;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.actors.Actor.ActorFSM;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.directors.Director.DirectorFSM;
import org.restflow.metadata.MetadataManager;
import org.restflow.metadata.RunMetadata;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.GroovyNodeBuilder;
import org.restflow.nodes.InPortalBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.OutPortalBuilder;
import org.restflow.nodes.TclNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.nodes.TestActorNodeBuilder.DoublerBeanWithoutAccessors;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


@SuppressWarnings("unused")
public class TestWorkflowBuilder extends RestFlowTestCase {
	
	private WorkflowContext _context;
	private ConsumableObjectStore _store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
	}
	
	public void test_WorkflowBuilder_HelloWorld_OneNode_Java() throws Exception {

		final Workflow workflow = new WorkflowBuilder()
			.context(_context)			
			.node(new JavaNodeBuilder()
				.bean(new Object() {
					public void step() { System.out.println("Hello world!");}
					}))	
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
		assertEquals(0, _store.size());
	}
	
	public void test_WorkflowBuilder_HelloWorld_OneNode_Groovy() throws Exception {

		Workflow workflow = new WorkflowBuilder()
			.name("Hello")
			.context(_context)
			.node(new GroovyNodeBuilder()
				.step("println 'Hello world!'"))
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.run();

		assertEquals(0, _store.size());
	}
	
	public void test_WorkflowBuilder_HelloWorld_OneNode_Tcl() throws Exception {

		Workflow workflow = new WorkflowBuilder()
			.context(_context)
			.node(new TclNodeBuilder()
				.step("puts [list Hello world!]"))
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.run();

		assertEquals(0, _store.size());
	}
	
	public void test_WorkflowBuilder_HelloWorld_TwoNodes() throws Exception {

		final Workflow workflow = new WorkflowBuilder()
			
			.context(_context)
			.name("HelloWorld")
			
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

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout showing three values printed
		assertEquals(
			"Hello!" 	+ EOL ,
			recorder.getStdoutRecording());

		assertEquals("Hello!", _store.take("/greeting"));
		assertEquals(0, _store.size());
	}
	
	public void test_WorkflowBuilder_NoNames() throws Exception {

		Workflow workflow = new WorkflowBuilder()
		
			.context(_context)
			
			.director(new PublishSubscribeDirector())
			
			.node(new GroovyNodeBuilder()
				.constant("a", 2)
				.step("b=a; println b")
				.outflow("b", "/original"))

			.node(new JavaNodeBuilder()
				.stepsOnce()
				.inflow("/original", "x")
				.bean(new DoublerBeanWithoutAccessors())
				.outflow("y", "/doubled")
				.maxConcurrency(2))
				
			.node(new GroovyNodeBuilder()
				.inflow("/doubled", "value")
				.step("println value"))
			
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		workflow.run();
		
		assertEquals(2, _store.take("/original"));
		assertEquals(4, _store.take("/doubled"));
		
		assertEquals(0, _store.size());
		
		workflow.wrapup();
		workflow.dispose();
	}
	
	public void test_WorkflowWithConcurrentActor_JavaActor() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
		
			.name("DoublerWorkflow")
			.context(_context)
			.director(new MTDataDrivenDirector())
			
			.node(new GroovyNodeBuilder()
				.name("source")
				.sequence("c", new Object [] {
						2, 
						4, 
						6, 
						8, 
						10, 
						12})
				.step("o=c")
				.outflow("o", "/original"))
				
			.node(new JavaNodeBuilder()
				.name("doubler")
				.inflow("/original", "x")
				.bean(new CloneableBean() {
						public int x, y;
						public void step() {
							System.out.println("Computing 3 * " + x);
							y = 3 * x;
						}
					})
				.outflow("y", "/tripled")
				.maxConcurrency(6)
				.ordered(false))
				
			.node(new GroovyNodeBuilder()
				.name("printer")
				.inflow("/tripled", "value")
				.step("println value"))
		
			.build();
		
		workflow.configure();
		workflow.initialize();
		workflow.run();
		workflow.wrapup();
		workflow.dispose();
		
		assertEquals(2, _store.take("/original/1"));
		assertEquals(4, _store.take("/original/2"));
		assertEquals(6, _store.take("/original/3"));
		assertEquals(8, _store.take("/original/4"));
		assertEquals(10, _store.take("/original/5"));
		assertEquals(12, _store.take("/original/6"));
		
		Set<Integer> triples = new HashSet<Integer>();
		triples.add((Integer)_store.take("/tripled/1"));
		triples.add((Integer)_store.take("/tripled/2"));
		triples.add((Integer)_store.take("/tripled/3"));
		triples.add((Integer)_store.take("/tripled/4"));
		triples.add((Integer)_store.take("/tripled/5"));
		triples.add((Integer)_store.take("/tripled/6"));
		
		assertEquals(0, _store.size());

		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));
		
		assertEquals(0, triples.size());
	}

	public void test_WorkflowWithConcurrentActor_GroovyActor() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
		
			.name("DoublerWorkflow")
			.context(_context)
			.director(new MTDataDrivenDirector())
			
			.node(new GroovyNodeBuilder()
				.name("source")
				.sequence("c", new Object [] {
						2,
						4,
						6,
						8,
						10,
						12})
				.step("o=c")
				.outflow("o", "/original"))
				
			.node(new GroovyNodeBuilder()
				.name("doubler")
				.inflow("/original", "x")
				.step(	"println 'Computing 3 * ' + x;		" 	+
						"y = 3 * x;							")
				.outflow("y", "/tripled")
				.maxConcurrency(6)
				.ordered(false))
				
			.node(new GroovyNodeBuilder()
				.name("printer")
				.inflow("/tripled", "value")
				.step("println value"))
		
			.build();
		
		workflow.configure();
		workflow.initialize();
		workflow.run();
		workflow.wrapup();
		workflow.dispose();
		
		assertEquals(2, _store.take("/original/1"));
		assertEquals(4, _store.take("/original/2"));
		assertEquals(6, _store.take("/original/3"));
		assertEquals(8, _store.take("/original/4"));
		assertEquals(10, _store.take("/original/5"));
		assertEquals(12, _store.take("/original/6"));
		
		Set<Integer> triples = new HashSet<Integer>();
		triples.add((Integer)_store.take("/tripled/1"));
		triples.add((Integer)_store.take("/tripled/2"));
		triples.add((Integer)_store.take("/tripled/3"));
		triples.add((Integer)_store.take("/tripled/4"));
		triples.add((Integer)_store.take("/tripled/5"));
		triples.add((Integer)_store.take("/tripled/6"));
		
		assertEquals(0, _store.size());

		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));
		
		assertEquals(0, triples.size());
	}


	public void test_WorkflowWithNestedConcurrentActor_JavaActor() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.name("DoublerWorkflow")
			.context(_context)
			.director(new MTDataDrivenDirector())
			
			.node(new WorkflowNodeBuilder()
				.director(new MTDataDrivenDirector())
				.prefix("/sub{STEP}")

				.node(new GroovyNodeBuilder()
					.name("trigger")
					.context(_context)
					.sequence("constant", new Object [] {"A", "B", "C"})
					.step("value=constant")
					.outflow("value", "/trigger"))
					
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.director(new MTDataDrivenDirector())
					.inflow("/trigger", "/discard")

					.node(new GroovyNodeBuilder()
						.name("source")
						.sequence("c", new Object [] {
								2, 
								4, 
								6, 
								8, 
								10, 
								12})
						.step("o=c")
						.outflow("o", "/original"))
				
					.node(new JavaNodeBuilder()
						.name("doubler")
						.inflow("/original", "x")
						.bean(new CloneableBean() {
								public int x, y;
								public void step() { y = 3 * x; }
							})
						.maxConcurrency(3)
						.ordered(false)
						.outflow("y", "/tripled"))
	
					.node(new GroovyNodeBuilder()
						.name("printer")
						.inflow("/tripled", "value")
						.step("println value"))
					)
				)
					
			.build();
		
		workflow.configure();
		workflow.initialize();
		workflow.run();
		workflow.wrapup();
		workflow.dispose();

		assertEquals("A", 	_store.take("/sub1/trigger/1"));
		assertEquals("B", 	_store.take("/sub1/trigger/2"));
		assertEquals("C", 	_store.take("/sub1/trigger/3"));

		assertEquals("A", 	_store.take("/sub1/subsub1/discard"));
		assertEquals("B", 	_store.take("/sub1/subsub2/discard"));
		assertEquals("C", 	_store.take("/sub1/subsub3/discard"));

		assertEquals(2, _store.take("/sub1/subsub1/original/1"));
		assertEquals(4, _store.take("/sub1/subsub1/original/2"));
		assertEquals(6, _store.take("/sub1/subsub1/original/3"));
		assertEquals(8, _store.take("/sub1/subsub1/original/4"));
		assertEquals(10, _store.take("/sub1/subsub1/original/5"));
		assertEquals(12, _store.take("/sub1/subsub1/original/6"));

		assertEquals(2, _store.take("/sub1/subsub2/original/1"));
		assertEquals(4, _store.take("/sub1/subsub2/original/2"));
		assertEquals(6, _store.take("/sub1/subsub2/original/3"));
		assertEquals(8, _store.take("/sub1/subsub2/original/4"));
		assertEquals(10, _store.take("/sub1/subsub2/original/5"));
		assertEquals(12, _store.take("/sub1/subsub2/original/6"));

		assertEquals(2, _store.take("/sub1/subsub3/original/1"));
		assertEquals(4, _store.take("/sub1/subsub3/original/2"));
		assertEquals(6, _store.take("/sub1/subsub3/original/3"));
		assertEquals(8, _store.take("/sub1/subsub3/original/4"));
		assertEquals(10, _store.take("/sub1/subsub3/original/5"));
		assertEquals(12, _store.take("/sub1/subsub3/original/6"));
		
		Set<Integer> triples = new HashSet<Integer>();
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/1"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/2"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/3"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/4"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/5"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/6"));
		
		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));

		triples.add((Integer)_store.take("/sub1/subsub2/tripled/1"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/2"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/3"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/4"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/5"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/6"));
		
		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));

		triples.add((Integer)_store.take("/sub1/subsub3/tripled/1"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/2"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/3"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/4"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/5"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/6"));
		
		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));
		
		assertEquals(0, triples.size());
	}
	

	public void test_WorkflowWithNestedConcurrentActor_GroovyActor() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
			.name("DoublerWorkflow")
			.context(_context)
			.director(new MTDataDrivenDirector())
			
			.node(new WorkflowNodeBuilder()
				.director(new MTDataDrivenDirector())
				.prefix("/sub{STEP}")

				.node(new GroovyNodeBuilder()
					.name("trigger")
					.context(_context)
					.sequence("constant", new Object [] {"A", "B", "C"})
					.step("value=constant")
					.outflow("value", "/trigger"))
					
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.director(new MTDataDrivenDirector())
					.inflow("/trigger", "/discard")

					.node(new GroovyNodeBuilder()
						.name("source")
						.sequence("c", new Object [] {
								2, 
								4, 
								6, 
								8, 
								10, 
								12})
						.step("o=c")
						.outflow("o", "/original"))
				
					.node(new GroovyNodeBuilder()
						.name("doubler")
						.inflow("/original", "x")
						.step("y = 3 * x")
						.outflow("y", "/yy")
						.maxConcurrency(3)
						.ordered(false)
						.outflow("y", "/tripled"))
	
					.node(new GroovyNodeBuilder()
						.name("printer")
						.inflow("/tripled", "value")
						.step("println value"))
					)
				)
					
			.build();
		
		workflow.configure();
		workflow.initialize();
		workflow.run();
		workflow.wrapup();
		workflow.dispose();

		assertEquals("A", 	_store.take("/sub1/trigger/1"));
		assertEquals("B", 	_store.take("/sub1/trigger/2"));
		assertEquals("C", 	_store.take("/sub1/trigger/3"));

		assertEquals("A", 	_store.take("/sub1/subsub1/discard"));
		assertEquals("B", 	_store.take("/sub1/subsub2/discard"));
		assertEquals("C", 	_store.take("/sub1/subsub3/discard"));

		assertEquals(2, _store.take("/sub1/subsub1/original/1"));
		assertEquals(4, _store.take("/sub1/subsub1/original/2"));
		assertEquals(6, _store.take("/sub1/subsub1/original/3"));
		assertEquals(8, _store.take("/sub1/subsub1/original/4"));
		assertEquals(10, _store.take("/sub1/subsub1/original/5"));
		assertEquals(12, _store.take("/sub1/subsub1/original/6"));

		assertEquals(2, _store.take("/sub1/subsub2/original/1"));
		assertEquals(4, _store.take("/sub1/subsub2/original/2"));
		assertEquals(6, _store.take("/sub1/subsub2/original/3"));
		assertEquals(8, _store.take("/sub1/subsub2/original/4"));
		assertEquals(10, _store.take("/sub1/subsub2/original/5"));
		assertEquals(12, _store.take("/sub1/subsub2/original/6"));

		assertEquals(2, _store.take("/sub1/subsub3/original/1"));
		assertEquals(4, _store.take("/sub1/subsub3/original/2"));
		assertEquals(6, _store.take("/sub1/subsub3/original/3"));
		assertEquals(8, _store.take("/sub1/subsub3/original/4"));
		assertEquals(10, _store.take("/sub1/subsub3/original/5"));
		assertEquals(12, _store.take("/sub1/subsub3/original/6"));
		
		Set<Integer> triples = new HashSet<Integer>();
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/1"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/2"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/3"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/4"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/5"));
		triples.add((Integer)_store.take("/sub1/subsub1/tripled/6"));
		
		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));

		triples.add((Integer)_store.take("/sub1/subsub2/tripled/1"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/2"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/3"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/4"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/5"));
		triples.add((Integer)_store.take("/sub1/subsub2/tripled/6"));
		
		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));

		triples.add((Integer)_store.take("/sub1/subsub3/tripled/1"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/2"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/3"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/4"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/5"));
		triples.add((Integer)_store.take("/sub1/subsub3/tripled/6"));
		
		assertTrue(triples.remove(6));
		assertTrue(triples.remove(12));
		assertTrue(triples.remove(18));
		assertTrue(triples.remove(24));
		assertTrue(triples.remove(30));
		assertTrue(triples.remove(36));
		
		assertEquals(0, triples.size());
	}
	
	public void test_WorkflowWithInputAndOutput() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
		
			.name("DoublerWorkflow")
			.context(_context)
			.director(new MTDataDrivenDirector())
			
			.inflow("a", "/original")

			.node(new GroovyNodeBuilder()
				.name("doubler")
				.inflow("/original", "x")
				.step("y = 3 * x;")
				.outflow("y", "/tripled"))
					
			.outflow("/tripled", "b")
			
			.build();
		
		workflow.configure();

		for (int i = 0; i <= 10; i++) {
			workflow.initialize();
			workflow.set("a", i);
			workflow.run();
			int b = (Integer) workflow.get("b");
			System.out.println(b);
			workflow.wrapup();
		}
		
		workflow.dispose();
	}

	public void test_WorkflowWithTwoInputsAndOutput() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()

			.context(_context)
		
			.inflow("a", "/multiplier")
			.inflow("b", "/multiplicand")
			
			.node(new JavaNodeBuilder()
				.inflow("/multiplier", "x")
				.inflow("/multiplicand", "y")
				.bean(new CloneableBean() {
					public int x, y, z;
					public void step() {z = x * y;}
					})
				.outflow("z", "/product"))
			
			.node(new GroovyNodeBuilder()
				.inflow("/product", "value")
				.step("println value"))
				
			.outflow("/product", "c")
			
			.build();
		
		workflow.configure();
		
		for (int i = 0; i <= 10; i++) {
			workflow.initialize();
			workflow.set("a", i);
			workflow.set("b", i * 3);
			workflow.run();
			assertEquals(3 * i * i, workflow.get("c"));
			workflow.wrapup();
		}
	}
	
	public void test_SubWorkflowMultiRunsNoResets() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()

			.name("top")
			.context(_context)
			.prefix("/run{RUN}")
			
			.inflow("u", "/inputNumber")
		
			.node(new GroovyNodeBuilder() 
				.inflow("/inputNumber", "m")
				.stepsOnce()
				.step("n = m + 1")
				.outflow("n", "/incrementedInputNumber"))
			
			.node(new WorkflowNodeBuilder()
			
				.stepsOnce()
			
				.name("multiplier")
				.inflow("/inputNumber", "/multiplier")
				.inflow("/incrementedInputNumber", "/multiplicand")
					
				.node(new JavaNodeBuilder()
					.inflow("/multiplier", "x")
					.inflow("/multiplicand", "y")
					.stepsOnce()
					.bean(new CloneableBean() {
						public int x, y, z;
						public void step() {z = x * y; System.out.println(x + "*" + y);}
						})
					.outflow("z", "/product"))
				
				.node(new GroovyNodeBuilder()
					.inflow("/product", "value")
					.step("println value"))
					
				.outflow("/product", "/outputNumber")
				)
				
			.outflow("/outputNumber", "v")
			
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		for (int u = 1; u <= 3; u++) {
			workflow.set("u", u);
			workflow.run();
			assertEquals(u*(u+1), workflow.get("v"));
		}
		
		assertEquals(1, _store.take("/run1/inputNumber"));
		assertEquals(2, _store.take("/run2/inputNumber"));
		assertEquals(3, _store.take("/run3/inputNumber"));
		assertNull(     _store.take("/run4/inputNumber"));

		assertEquals(2, _store.take("/run1/incrementedInputNumber"));
		assertEquals(3, _store.take("/run2/incrementedInputNumber"));
		assertEquals(4, _store.take("/run3/incrementedInputNumber"));
		assertNull(     _store.take("/run4/incrementedInputNumber"));
		
		assertEquals(1, _store.take("/run1/multiplier1_1/multiplier"));
		assertEquals(2, _store.take("/run2/multiplier2_1/multiplier"));
		assertEquals(3, _store.take("/run3/multiplier3_1/multiplier"));
		assertNull(     _store.take("/run4/multiplier4_1/multiplier"));

		assertEquals(2, _store.take("/run1/multiplier1_1/multiplicand"));
		assertEquals(3, _store.take("/run2/multiplier2_1/multiplicand"));
		assertEquals(4, _store.take("/run3/multiplier3_1/multiplicand"));
		assertNull(     _store.take("/run4/multiplier4_1/multiplicand"));

		assertEquals( 2, _store.take("/run1/multiplier1_1/product"));
		assertEquals( 6, _store.take("/run2/multiplier2_1/product"));
		assertEquals(12, _store.take("/run3/multiplier3_1/product"));
		assertNull(      _store.take("/run4/multiplier4_1/product"));
		
		assertEquals( 2, _store.take("/run1/outputNumber"));
		assertEquals( 6, _store.take("/run2/outputNumber"));
		assertEquals(12, _store.take("/run3/outputNumber"));
		assertNull(      _store.take("/run4/outputNumber"));
		
		assertEquals(0, _store.size());
	}
	
	public void test_SubWorkflowMultiRunsWithResets() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()

			.name("top")
			.context(_context)
			.inflow("u", "/inputNumber")
		
			.node(new GroovyNodeBuilder() 
				.inflow("/inputNumber", "m")
				.stepsOnce()
				.step("n = m + 1")
				.outflow("n", "/incrementedInputNumber"))
			
			.node(new WorkflowNodeBuilder()
			
				.name("multiplier")
				.stepsOnce()
				
				.inflow("/inputNumber", "/multiplier")
				.inflow("/incrementedInputNumber", "/multiplicand")
					
				.node(new JavaNodeBuilder()
					.inflow("/multiplier", "x")
					.inflow("/multiplicand", "y")
					.stepsOnce()
					.bean(new CloneableBean() {
						public int x, y, z;
						public void step() {z = x * y; System.out.println(x + "*" + y);}
						})
					.outflow("z", "/product"))
				
				.node(new GroovyNodeBuilder()
					.inflow("/product", "value")
					.stepsOnce()
					.step("println value"))
					
				.outflow("/product", "/outputNumber"))
				
			.outflow("/outputNumber", "v")
			
			.build();
		
		workflow.configure();
		
		for (int u = 1; u <= 3; u++) {
			
			workflow.reset();
			workflow.initialize();
			
			workflow.set("u", u);
			workflow.run();
			
			int v = (Integer)workflow.get("v");
			
			assertEquals(u*(u+1), v);
			
			assertEquals(u,   _store.take("/inputNumber"));
			assertEquals(u+1, _store.take("/incrementedInputNumber"));
			assertEquals(u,   _store.take("multiplier1_1/multiplier"));
			assertEquals(u+1, _store.take("multiplier1_1/multiplicand"));
			assertEquals(v,   _store.take("multiplier1_1/product"));
			assertEquals(v,   _store.take("/outputNumber"));
			assertEquals(0,   _store.size());
		}
	}
}

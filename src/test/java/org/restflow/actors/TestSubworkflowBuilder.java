package org.restflow.actors;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.SubworkflowBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.actors.Actor.ActorFSM;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.directors.Director.DirectorFSM;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.InPortalBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.OutPortalBuilder;
import org.restflow.nodes.TestActorNodeBuilder.DoublerBeanWithoutAccessors;
import org.restflow.test.RestFlowTestCase;


@SuppressWarnings("unused")
public class TestSubworkflowBuilder extends RestFlowTestCase {

	private WorkflowContext _context;
	private ConsumableObjectStore _store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
	}
	
	public void test_SubWorkflowBuilder() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
		
			.name ("Top")
			
			.context(_context)
		
			.inflow("u", "/inputNumber")

			.node (new JavaNodeBuilder() 
				.inflow("/inputNumber", "m")
				.bean(new CloneableBean() {
						public int m, n;
						public void step() { n = m + 1; }
					})
				.outflow("n", "/incrementedInputNumber"))
			
			.node(new ActorNodeBuilder()
			
				.inflow("/inputNumber", "a")
				.inflow("/incrementedInputNumber", "b")
			
				.actor(new SubworkflowBuilder()
				
					.name("Sub")
				
					.context(_context)
		
					.inflow("a", "/multiplier")
					.inflow("b", "/multiplicand")
					
					.prefix("Sub{RUN}")
					
					.node(new JavaNodeBuilder()
						.inflow("/multiplier", "x")
						.inflow("/multiplicand", "y")
						.bean(new CloneableBean() {
							public int x, y, z;
							public void step() {z = x * y; System.out.println(x + "*" + y);}
							})
						.outflow("z", "/product"))
					
					.node(new JavaNodeBuilder()
						.inflow("/product", "value")
						.bean(new Object() {
							public int value;
							public void step() { System.out.println(value); }
							}))
						
					.outflow("/product", "c")
					
					.build())
					
				.outflow("c", "/outputNumber"))
				
			.outflow("/outputNumber", "v")

			.build();

		workflow.configure();
		
		for (int i = 0; i <= 20; i++) {
			workflow.initialize();
			workflow.set("u", i);
			workflow.run();
			assertEquals(i * (i + 1), workflow.get("v"));
			workflow.wrapup();
		}
	}
}

package org.restflow.nodes;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.GroovyActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.actors.Actor.ActorFSM;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.Director.DirectorFSM;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.nodes.InPortalBuilder;
import org.restflow.nodes.OutPortalBuilder;
import org.restflow.test.RestFlowTestCase;


public class TestInPortalBuilder extends RestFlowTestCase {
	
	private WorkflowContext			_context;
	private ConsumableObjectStore 	_store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();	 
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
	}

	public void test_WorkflowWithInPortalAndOutPortal() throws Exception {
		
		Workflow workflow = new WorkflowBuilder()
		
			.name("DoublerWorkflow")
			.context(_context)
			.prefix("/Run{RUN}/")
			.director(new MTDataDrivenDirector())
			
			.input("a")
			
			.node(new InPortalBuilder()
				.outflow("a", "/original")
				.build()				
			)

			.node(new ActorNodeBuilder()
				.name("doubler")
				.inflow("/original", "x")
				.actor(new GroovyActorBuilder()
					.step("y = 3 * x;"))
				.outflow("y", "/doubled")
			)
			
			.node(new OutPortalBuilder()
				.inflow("b", "/doubled")
				.build()
			)
		
			.output("b")
			
			.build();

		workflow.configure();
		workflow.initialize();

		assertEquals(DirectorFSM.INITIALIZED, workflow.director().state());
		
		ActorWorkflowNode node = (ActorWorkflowNode)workflow.node("doubler");
		assertEquals(ActorFSM.CONFIGURED, node.getActor().state());

		for (int i = 0; i <= 10; i++) {
			workflow.set("a", i);
			workflow.run();
			int b = (Integer) workflow.get("b");
			System.out.println(b);
		}
		
		workflow.wrapup();
		workflow.dispose();
	}
}

package org.restflow.actors;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.ActorRunner;
import org.restflow.actors.JavaActor;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.OrderedActorRunner;
import org.restflow.exceptions.RestFlowException;
import org.restflow.test.RestFlowTestCase;


public class TestOrderedActorRunner extends RestFlowTestCase {

	private JavaActor _actor;
	private WorkflowContext _context;
	
	@SuppressWarnings("unused")
	public void setUp() throws Exception {
		
		super.setUp();
		
		_context = new WorkflowContextBuilder().build();
		
		_actor = new JavaActorBuilder()
			.name("TestActor")
			.context(_context)
			.bean(new Object() {
				public boolean throwExceptionOnStep = false;
				public boolean actorStepped = false;
				public void step() throws RestFlowException {
					actorStepped = true;
					if (throwExceptionOnStep) {
						throw new RestFlowException("Error stepping actor");
					}
				}
			})
			.build();
	
		_actor.elaborate();
		_actor.configure();
		_actor.initialize();
	}
	
	public void testConstructor() {

		Map<String,Object> variables = new HashMap<String,Object>();
		ActorRunner runner = new OrderedActorRunner(_actor, variables);
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		assertNull(runner.getException());
	}
	
	public void testRun_NoException() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		ActorRunner runner = new OrderedActorRunner(_actor, variables);
		assertEquals(false, _actor.getFieldValue("actorStepped"));
		
		runner.run();
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		assertEquals(true, _actor.getFieldValue("actorStepped"));

		Exception exception = runner.getException();
		assertNull(exception);
	}

	public void testRun_WithException() throws Exception {
		
		_actor.setFieldValue("throwExceptionOnStep", true);
		Map<String,Object> variables = new HashMap<String,Object>();
		ActorRunner runner = new OrderedActorRunner(_actor, variables);
		assertEquals(false, _actor.getFieldValue("actorStepped"));
		
		runner.run();
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		assertEquals(true, _actor.getFieldValue("actorStepped"));

		Exception exception = runner.getException();		
		assertNotNull(exception);
		assertEquals("Exception in step() method of actor 'TestActor'", exception.getMessage());
	}

	public void testStart_NoException() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		OrderedActorRunner runner = new OrderedActorRunner(_actor, variables);
		assertEquals(false, _actor.getFieldValue("actorStepped"));
		
		runner.start();
		runner.waitForCompletion();
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		assertEquals(true, _actor.getFieldValue("actorStepped"));

		Exception exception = runner.getException();		
		assertNull(exception);
	}
	
	public void testStart_WithException() throws Exception {
		
		_actor.setFieldValue("throwExceptionOnStep", true);

		Map<String,Object> variables = new HashMap<String,Object>();
		OrderedActorRunner runner = new OrderedActorRunner(_actor, variables);
		assertEquals(false, _actor.getFieldValue("actorStepped"));
		
		runner.start();
		runner.waitForCompletion();
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		assertEquals(true, _actor.getFieldValue("actorStepped"));

		Exception exception = runner.getException();
		assertNotNull(exception);
		assertEquals("Exception in step() method of actor 'TestActor'", exception.getMessage());
	}	
}

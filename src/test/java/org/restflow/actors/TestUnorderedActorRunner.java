package org.restflow.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.ActorRunner;
import org.restflow.actors.AsynchronousActorRunner;
import org.restflow.actors.JavaActor;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.UnorderedActorRunner;
import org.restflow.exceptions.RestFlowException;
import org.restflow.test.RestFlowTestCase;


public class TestUnorderedActorRunner extends RestFlowTestCase {

	private JavaActor _actor;
	private WorkflowContext _context;
	
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

		BlockingQueue<ActorRunner> queue = new LinkedBlockingQueue<ActorRunner>(1);		
		assertEquals(0, queue.size());

		Map<String,Object> variables = new HashMap<String,Object>();
		ActorRunner runner = new UnorderedActorRunner(_actor, variables, queue);
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		assertNull(runner.getException());
		assertEquals(0, queue.size());
	}
	
	public void testConstructor_NullQueueArgument() {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		BlockingQueue<ActorRunner> queue = null;
		Exception e = null;
		try {
			new UnorderedActorRunner(_actor, variables, queue);
		} catch (IllegalArgumentException ex) { 
			e = ex;
		}
		assertEquals("The queue argument cannot be null.", e.getMessage());
	}

	
	public void testRun_NoException() throws Exception {
		
		BlockingQueue<ActorRunner> queue = new LinkedBlockingQueue<ActorRunner>(1);
		assertEquals(0, queue.size());

		Map<String,Object> variables = new HashMap<String,Object>();
		ActorRunner runner = new UnorderedActorRunner(_actor, variables, queue);
		assertEquals(false, _actor.getFieldValue("actorStepped"));
		
		runner.run();
		assertEquals(true, _actor.getFieldValue("actorStepped"));
		assertEquals(1, queue.size());
		assertSame(runner, queue.take());
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		
		Exception exception = runner.getException();		
		assertNull(exception);
	}

	public void testRun_WithException() throws Exception {
		
		BlockingQueue<ActorRunner> queue = new LinkedBlockingQueue<ActorRunner>(1);
		assertEquals(0, queue.size());

		_actor.setFieldValue("throwExceptionOnStep", true);
		Map<String,Object> variables = new HashMap<String,Object>();
		ActorRunner runner = new UnorderedActorRunner(_actor, variables, queue);
		assertEquals(false, _actor.getFieldValue("actorStepped"));
		
		runner.run();
		assertEquals(true, _actor.getFieldValue("actorStepped"));
		assertEquals(1, queue.size());
		assertSame(runner, queue.take());
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		
		Exception exception = runner.getException();
		assertNotNull(exception);
		assertEquals("Exception in step() method of actor 'TestActor'", exception.getMessage());
	}

	public void testStart_NoException() throws Exception {
		
		BlockingQueue<ActorRunner> queue = new LinkedBlockingQueue<ActorRunner>(1);
		assertEquals(0, queue.size());

		Map<String,Object> variables = new HashMap<String,Object>();
		AsynchronousActorRunner runner = new UnorderedActorRunner(_actor, variables, queue);
		assertEquals(false, _actor.getFieldValue("actorStepped"));
		
		runner.start();
		runner.waitForCompletion();
		assertEquals(true, _actor.getFieldValue("actorStepped"));
		assertEquals(1, queue.size());
		assertSame(runner, queue.take());
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		
		Exception exception = runner.getException();
		assertNull(exception);
	}
	
	public void testStart_WithException() throws Exception {
		
		BlockingQueue<ActorRunner> queue = new LinkedBlockingQueue<ActorRunner>(1);
		assertEquals(0, queue.size());

		_actor.setFieldValue("throwExceptionOnStep", true);
		Map<String,Object> variables = new HashMap<String,Object>();
		AsynchronousActorRunner runner = new UnorderedActorRunner(_actor, variables, queue);
		assertEquals(false, _actor.getFieldValue("actorStepped"));

		runner.start();
		runner.waitForCompletion();
		assertEquals(true, _actor.getFieldValue("actorStepped"));
		assertEquals(1, queue.size());
		assertSame(runner, queue.take());	
		assertSame(_actor, runner.getActor());
		assertSame(variables, runner.getVariables());
		
		Exception exception = runner.getException();
		assertNotNull(exception);
		assertEquals("Exception in step() method of actor 'TestActor'", exception.getMessage());
	}
}

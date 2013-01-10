package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.AbstractActorRunner;
import org.restflow.actors.ActorRunner;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.JavaActor;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.SynchronousActorRunner;
import org.restflow.exceptions.RestFlowException;
import org.restflow.nodes.SynchronousActorCompletionService;
import org.restflow.test.RestFlowTestCase;


public class TestSynchronousActorCompletionService extends RestFlowTestCase {
	
	private JavaActor _testActor;
	private JavaActor _doublerActor;
	private WorkflowContext _context;
	
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		_context = new WorkflowContextBuilder().build();
		
		_testActor = new JavaActorBuilder()
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
	
		_testActor.elaborate();
		_testActor.configure();
		_testActor.initialize();

		_doublerActor = new JavaActorBuilder()
			.name("DoublerActor")
			.context(_context)
			.input("value")
			.bean(new CloneableBean() {
				public Integer value;
				public Integer doubledValue;
				public void step() { doubledValue = 2 * value; }
			})
			.output("doubledValue")
			.build();

		_doublerActor.elaborate();
		_doublerActor.configure();
		_doublerActor.initialize();	
	}
	
	public void testTakeWithoutStart() {
			
		Exception illegalStateException = null;
		Exception interruptedException = null;
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		try {
			service.take();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		
		assertNull(interruptedException);
		assertNotNull(illegalStateException);
		assertEquals("Cannot take a runner when the service is stopped", illegalStateException.getMessage());
	}
	
	public void testTakeWithoutSubmit() {
		
		Exception illegalStateException = null;
		Exception interruptedException = null;
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		
		try {
			service.take();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		
		assertNull(interruptedException);
		assertNotNull(illegalStateException);
		assertEquals("Cannot take a runner before submitting one", illegalStateException.getMessage());
	}

	public void testSubmitWithoutStart() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		Exception illegalStateException = null;		
		try {
			service.submit(_testActor, variables);
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertNotNull(illegalStateException);
		assertEquals("Actor cannot be submitted because the service is stopped", illegalStateException.getMessage());
	}

	public void testShutdownWithoutStart() {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
	}

	public void testMultipleShutdowns() {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}
	
	public void testMultipleStarts() {
		
		Exception illegalStateException = null;
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
				
		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		
		try {
			service.start();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertNotNull(illegalStateException);
		assertEquals("Service cannot be started because it is already active", illegalStateException.getMessage());
	}

	public void testStartSubmitTakeShutdown() throws Exception {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());

		Map<String,Object> variables = new HashMap<String,Object>();
		service.submit(_testActor, variables);
		assertEquals(SynchronousActorCompletionService.Service.BLOCKED, service.getState());

		ActorRunner runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		assertSame(_testActor, runner.getActor());
		assertEquals(true, _testActor.getFieldValue("actorStepped"));
		
		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}
	
	public void testStartSubmitTakeSubmitTakeShutdown() throws Exception {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		Map<String,Object> variables = new HashMap<String,Object>();		
		service.submit(_testActor, variables);
		assertEquals(SynchronousActorCompletionService.Service.BLOCKED, service.getState());

		ActorRunner runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		assertSame(_testActor, runner.getActor());
		assertEquals(true, _testActor.getFieldValue("actorStepped"));

		_testActor.setFieldValue("actorStepped", false);
		service.submit(_testActor, variables);
		assertEquals(SynchronousActorCompletionService.Service.BLOCKED, service.getState());

		runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		assertSame(_testActor, runner.getActor());
		assertEquals(true, _testActor.getFieldValue("actorStepped"));
		
		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}

	public void testMultipleSubmits() {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());

		Map<String,Object> variables = new HashMap<String,Object>();		
		service.submit(_testActor, variables);
		assertEquals(SynchronousActorCompletionService.Service.BLOCKED, service.getState());

		Exception illegalStateException = null;
		try {
			service.submit(_testActor, variables);
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertNotNull(illegalStateException);
		assertEquals("Actor cannot be submitted without taking the prior one", illegalStateException.getMessage());
	}
	
	public void testStartSubmitTakeShutdownTake() throws Exception {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());

		Map<String,Object> variables = new HashMap<String,Object>();
		service.submit(_testActor, variables);
		assertEquals(SynchronousActorCompletionService.Service.BLOCKED, service.getState());

		ActorRunner runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		assertNotSame(SynchronousActorRunner.EndOfActorRunners, runner);
		
		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}

	public void test_StartShutdownTake() throws Exception {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());

		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		ActorRunner runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}

	public void test_StartShutdownTakeTake() throws Exception {
				
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());

		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		ActorRunner runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
		
		Exception illegalStateException = null;
		try {
			runner = service.take();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertEquals("Cannot take a runner when the service is stopped", illegalStateException.getMessage());
	}
	
	public void test_StartShutdownSubmit() throws Exception {
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());

		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		Map<String,Object> variables = new HashMap<String,Object>();
		Exception illegalStateException = null;
		try {
			service.submit(_testActor, variables);
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertEquals("Actor cannot be submitted because the service is shut down", illegalStateException.getMessage());
	}

	public void test_StartShutdownTakeShutdownTake() throws Exception {
		
		Exception illegalStateException = null;
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());

		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		ActorRunner runner = service.take();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);

		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		try {
			runner = service.take();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertEquals("Cannot take a runner when the service is stopped", illegalStateException.getMessage());
	}
	
	public void testSubmitTakeRepeat() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		SynchronousActorCompletionService service = new SynchronousActorCompletionService();
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
		
		for (int i = 0; i < 100; i++) {
			
			_doublerActor.setInputValue("value", i);

			service.submit(_doublerActor, variables);
			assertEquals(SynchronousActorCompletionService.Service.BLOCKED, service.getState());

			ActorRunner runner = service.take();
			assertEquals(SynchronousActorCompletionService.Service.IDLE, service.getState());
			Object doubledValue = runner.getActor().getOutputValue("doubledValue");
			assertEquals(2 * i, doubledValue);
		}
		
		service.shutdown();
		assertEquals(SynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		ActorRunner runner = service.take();
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
		assertEquals(SynchronousActorCompletionService.Service.STOPPED, service.getState());
	}	
}
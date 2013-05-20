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
import org.restflow.nodes.AsynchronousActorCompletionService;
import org.restflow.nodes.OrderedActorCompletionService;
import org.restflow.nodes.TestUnorderedActorCompletionService.ActorCompletionBarrier;
import org.restflow.test.RestFlowTestCase;


public class TestOrderedActorCompletionService extends RestFlowTestCase {	
	
	private JavaActor _doublerActor;
	private JavaActor _testActor;
	
	@SuppressWarnings("unused")
	public void setUp() throws Exception {
		
		super.setUp();
		
		WorkflowContext context = new WorkflowContextBuilder().build();

		_doublerActor = new JavaActorBuilder()
			.name("DoublerActor")
			.context(context)
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
		
		_testActor = new JavaActorBuilder()
		.name("TestActor")
		.context(context)
		.bean(new CloneableBean() {
			public boolean throwExceptionOnStep = false;
			public boolean actorStepped = false;
			public ActorCompletionBarrier barrier = new ActorCompletionBarrier();
			public void step() throws Exception {					
				while (barrier.isUp()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
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

	}
	
	public void testSubmitWithoutStart() throws Exception {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));

		Exception illegalStateException = null;		
		Map<String,Object> variables = new HashMap<String,Object>();
		try {
			service.submit(_testActor, variables);
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}		
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertNotNull(illegalStateException);
		assertEquals("Actor cannot be submitted because the service is stopped", illegalStateException.getMessage());
	}
	
	public void testStartShutdownSubmit() throws Exception {
		
		Exception illegalStateException = null;
		
		Map<String,Object> variables = new HashMap<String,Object>();

		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		try {
			service.submit(_testActor, variables);
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertNotNull(illegalStateException);
		assertEquals("Actor cannot be submitted because the service is shut down", illegalStateException.getMessage());
	}

	public void testTakeBeforeStart() throws InterruptedException {
		
		Exception illegalStateException = null;
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		try {
			service.take();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertNotNull(illegalStateException);
		assertEquals("Cannot take a runner because service is stopped", illegalStateException.getMessage());
	}

	public void testStartShutdownTakeTake() throws InterruptedException {
		
		Exception illegalStateException = null;
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());

		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		try {
			service.take();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertNotNull(illegalStateException);
		assertEquals("Cannot take a runner because service is stopped", illegalStateException.getMessage());
	}
	
	public void testShutdownWithoutStart() {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
	}
	
	public void testStartShutdownStart() {
		
		Exception illegalStateException = null;

		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		try {
			service.start();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertNotNull(illegalStateException);
		assertEquals("Service cannot be started because it is shut down", illegalStateException.getMessage());
	}

	public void testStartShutdownTakeStart() throws InterruptedException {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		service.take();
		
		service.start();
	}
	
	public void testMultipleShutdowns() {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}
	
	public void testMultipleStarts() {
		
		Exception illegalStateException = null;
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		try {
			service.start();
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertNotNull(illegalStateException);
		assertEquals("Service cannot be started because it is already active", illegalStateException.getMessage());
	}

	public void testStartSubmitTakeShutdown() throws Exception {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		Map<String,Object> variables = new HashMap<String,Object>();
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActor = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActor);
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertEquals(true, runActor.getFieldValue("actorStepped"));

		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}
	
	public void testStartSubmitTakeSubmitTakeShutdown() throws Exception {
				
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));

		Map<String,Object> variables = new HashMap<String,Object>();
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActorOne = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorOne);
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertEquals(true, runActorOne.getFieldValue("actorStepped"));

		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		
		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActorTwo = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorTwo);
		assertNotSame(runActorOne, runActorTwo);
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertEquals(true, runActorTwo.getFieldValue("actorStepped"));
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}

	public void testTwoSubmitsThenTwoTakes() throws Exception {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		Map<String,Object> variables = new HashMap<String,Object>();		
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		
		JavaActor runActorOne = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorOne);
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertEquals(true, runActorOne.getFieldValue("actorStepped"));

		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActorTwo = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorTwo);
		assertNotSame(runActorOne, runActorTwo);
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertEquals(true, runActorTwo.getFieldValue("actorStepped"));
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}
	
	public void testStartSubmitSubmitTakeShutdownTakeTake() throws Exception {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		Map<String,Object> variables = new HashMap<String,Object>();		
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());

		JavaActor runActorOne = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorOne);
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertEquals(true, runActorOne.getFieldValue("actorStepped"));

		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUTTING_DOWN, service.getState());
		
		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		JavaActor runActorTwo = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorTwo);
		assertNotSame(runActorOne, runActorTwo);
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		assertEquals(true, runActorTwo.getFieldValue("actorStepped"));
		
		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(SynchronousActorRunner.EndOfActorRunners, runner);
	}	
	
	public void testStartSubmitTakeShutdownTake() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();

		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false, _testActor.getFieldValue("actorStepped"));
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());

		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		
		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		assertNotSame(SynchronousActorRunner.EndOfActorRunners, runner);
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(SynchronousActorRunner.EndOfActorRunners, runner);
	}

	public void test_StartShutdownTake() throws Exception {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}

	public void test_StartShutdownTakeShutdown() throws Exception {
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
	}
		
	public void testSubmitTakeRepeat() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();

		AsynchronousActorCompletionService service = new OrderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		for (int i = 0; i < 10000; i++) {
			_doublerActor.setInputValue("value", i);
			
			service.submit(_doublerActor, variables);
			assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
			
			ActorRunner runner = service.take();
			assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
			Object doubledValue = runner.getActor().getOutputValue("doubledValue");
			assertEquals(2 * i, doubledValue);
		}
		
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}
	
	public void testSubmitRepeatTakeRepeat_OneChunk() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(500);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		for (int i = 0; i < 1000; i++) {
			_doublerActor.setInputValue("value", i);
			service.submit(_doublerActor, variables);
			assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		}
		
		for (int i = 0; i < 999; i++) {
			ActorRunner runner = service.take();
			assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
			Object doubledValue = runner.getActor().getOutputValue("doubledValue");
			assertEquals(2 * i, doubledValue);
		}

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		Object doubledValue = runner.getActor().getOutputValue("doubledValue");
		assertEquals(2 * 999, doubledValue);

		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		
		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}

	
	public void testSubmitRepeatShutdownTakeRepeat_OneChunk() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(500);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		for (int i = 0; i < 1000; i++) {
			_doublerActor.setInputValue("value", i);
			service.submit(_doublerActor, variables);
			assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		}
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUTTING_DOWN, service.getState());
		
		for (int i = 0; i < 999; i++) {
			ActorRunner runner = service.take();
			assertEquals(AsynchronousActorCompletionService.Service.SHUTTING_DOWN, service.getState());
			Object doubledValue = runner.getActor().getOutputValue("doubledValue");
			assertEquals(2 * i, doubledValue);
		}

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		Object doubledValue = runner.getActor().getOutputValue("doubledValue");
		assertEquals(2 * 999, doubledValue);

		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}
	
	public void testSubmitRepeatTakeRepeat_TenChunks() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		AsynchronousActorCompletionService service = new OrderedActorCompletionService(50);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		for (int chunk = 0; chunk < 10; chunk++) {
		
			for (int i = 0; i < 100; i++) {
				
				_doublerActor.setInputValue("value", i * chunk);
				
				service.submit(_doublerActor, variables);
				assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
			}
			
			for (int i = 0; i < 99; i++) {
				
				ActorRunner runner = service.take();
				assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
				Object doubledValue = runner.getActor().getOutputValue("doubledValue");
				assertEquals(2 * i * chunk, doubledValue);
			}

			ActorRunner runner = service.take();
			assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
			Object doubledValue = runner.getActor().getOutputValue("doubledValue");
			assertEquals(2 * 99 * chunk, doubledValue);
		}
		
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}

	
	public void testSubmitToFullQueue() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		for (int queueLength = 1; queueLength <= 10; queueLength++) {

			for (int actorCount = queueLength; actorCount <= 2 * queueLength + 1; actorCount++) {
				
				AsynchronousActorCompletionService service = new OrderedActorCompletionService(queueLength);
				assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

				service.start();
				assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
	
				for (int i = 1; i <= actorCount; i++) {
					service.submit(_testActor, variables);
					assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
				}
				
				for (int i = 1; i <= actorCount - 1; i++) {
					service.take();
					assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
				}

				service.take();
				assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
			}
		}
	}
	
	public void testSubmitToFullQueue_Async() throws Exception {
		
		final ActorCompletionBarrier barrier = (ActorCompletionBarrier) _testActor.getFieldValue("barrier");
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		for (int queueLength = 1; queueLength <= 5; queueLength += 2) {

			for (int actorCount = queueLength; actorCount <= 2 * queueLength + 1; actorCount++) {
				
				barrier.raise();
			
				OrderedActorCompletionService service = new OrderedActorCompletionService(queueLength);
				assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

				System.out.println("Starting service");			
				service.start();
				assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
					
				for (int i = 1; i <= actorCount; i++) {

					service.submit(_testActor, variables);
					assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
					System.out.println("Submitted actor " + i);					
					
					if (i == queueLength) {
						
						Thread thread = new Thread(new Runnable() {
							public void run() {
								try {
									Thread.sleep(100);
									barrier.lower();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						});
						thread.start();	
					}
				}
				
				int i;
				for (i = 1; i <= actorCount - 1; i++) {
					service.take();
					assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
					System.out.println("Took actor " + i);
			}
				
			service.take();
			assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
			System.out.println("Took actor " + i);

			}
		}
	}
}
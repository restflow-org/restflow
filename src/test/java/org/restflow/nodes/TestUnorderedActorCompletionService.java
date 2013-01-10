package org.restflow.nodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.bag.HashBag;
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
import org.restflow.nodes.UnorderedActorCompletionService;
import org.restflow.test.RestFlowTestCase;


public class TestUnorderedActorCompletionService extends RestFlowTestCase {
	
	private JavaActor _doublerActor;
	private JavaActor _testActor;
	private WorkflowContext _context;
	
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		_context = new WorkflowContextBuilder().build();
		
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
		
		_testActor = new JavaActorBuilder()
			.name("TestActor")
			.context(_context)
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
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());		
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		
		Map<String,Object> variables = new HashMap<String,Object>();
		Exception illegalStateException = null;
		try {
			service.submit(_testActor, variables);
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		assertNotNull(illegalStateException);
		assertEquals("Actor cannot be submitted because the service is stopped", illegalStateException.getMessage());
	}
	
	public void testSubmitAfterShutdown() throws Exception {
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());

		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
		assertEquals(false,_testActor.getFieldValue("actorStepped"));

		Exception illegalStateException = null;
		Map<String,Object> variables = new HashMap<String,Object>();
		try {
			service.submit(_testActor, variables);
		} catch (IllegalStateException e) {
			illegalStateException = e;
		}
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		assertNotNull(illegalStateException);
		assertEquals("Actor cannot be submitted because the service is shut down", illegalStateException.getMessage());
	}

	public void testTakeBeforeStart() throws InterruptedException {
		
		Exception illegalStateException = null;
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
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
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
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
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
	}
	
	public void testStartShutdownStart() {
		
		Exception illegalStateException = null;

		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
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

	public void testMultipleShutdowns() {
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
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
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
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
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false,_testActor.getFieldValue("actorStepped"));

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());

		Map<String,Object> variables = new HashMap<String,Object>();
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActor = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActor);
		assertEquals(true,runActor.getFieldValue("actorStepped"));
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}
	
	public void testStartSubmitTakeSubmitTakeShutdown() throws Exception {
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		
		Map<String,Object> variables = new HashMap<String,Object>();
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActorOne = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorOne);
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		assertEquals(true,runActorOne.getFieldValue("actorStepped"));

		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());

		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActorTwo = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorTwo);
		assertNotSame(runActorOne, runActorTwo);
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		assertEquals(true,runActorTwo.getFieldValue("actorStepped"));
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}

	public void testTwoSubmitsThenTwoTakes() throws Exception {		
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(2);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		
		Map<String,Object> variables = new HashMap<String,Object>();
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		
		service.submit(_testActor, variables);
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		assertEquals(false,_testActor.getFieldValue("actorStepped"));

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
		
		JavaActor runActorOne = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorOne);
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		assertEquals(true,runActorOne.getFieldValue("actorStepped"));

		runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
		
		JavaActor runActorTwo = (JavaActor)runner.getActor();
		assertNotSame(_testActor, runActorTwo);
		assertNotSame(runActorOne, runActorTwo);
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		assertEquals(true,runActorTwo.getFieldValue("actorStepped"));
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());
	}
	
	public void testStartSubmitTakeShutdownTake() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();

		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertEquals(false,_testActor.getFieldValue("actorStepped"));
		
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
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}

	public void test_StartShutdownTake() throws Exception {
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
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
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1);
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

		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(2);
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
		
		service.shutdown();
		assertEquals(AsynchronousActorCompletionService.Service.SHUT_DOWN, service.getState());

		ActorRunner runner = service.take();
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}
	
	public void testSubmitRepeatTakeRepeat_OneChunk() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(1000);
		service.start();
		
		Collection<Integer> expectedOutputs = new HashBag();
		Collection<Integer> actualOutputs = new HashBag();
		
		for (int i = 0; i < 1000; i++) {
			_doublerActor.setInputValue("value", i);
			service.submit(_doublerActor, variables);
			expectedOutputs.add(i * 2);
		}
		
		for (int i = 0; i < 1000; i++) {
			ActorRunner runner = service.take();
			Object doubledValue = runner.getActor().getOutputValue("doubledValue");
			actualOutputs.add((Integer) doubledValue);
			System.out.println(doubledValue);
		}

		assertTrue(expectedOutputs.containsAll(actualOutputs));
		assertTrue(actualOutputs.containsAll(expectedOutputs));
		
		service.shutdown();
		ActorRunner runner = service.take();
		assertSame(AbstractActorRunner.EndOfActorRunners, runner);
	}

	@SuppressWarnings("unchecked")
	public void testSubmitRepeatTakeRepeat_TenChunks() throws Exception {
		
		Map<String,Object> variables = new HashMap<String,Object>();
		
		AsynchronousActorCompletionService service = new UnorderedActorCompletionService(100);
		assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());
		
		service.start();
		assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());

		Collection<Integer> expectedOutputs = new HashBag();
		Collection<Integer> actualOutputs = new HashBag();

		for (int chunk = 0; chunk < 10; chunk++) {
		
			for (int i = 0; i < 100; i++) {
				_doublerActor.setInputValue("value", i * chunk);
				service.submit(_doublerActor, variables);
				assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
				expectedOutputs.add(i * chunk * 2);
			}
			
			for (int i = 0; i < 99; i++) {
				ActorRunner runner = service.take();
				assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
				Object doubledValue = runner.getActor().getOutputValue("doubledValue");
				System.out.println(doubledValue);
				actualOutputs.add((Integer) doubledValue);
			}
			
			ActorRunner runner = service.take();
			assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
			Object doubledValue = runner.getActor().getOutputValue("doubledValue");
			System.out.println(doubledValue);
			actualOutputs.add((Integer) doubledValue);
		}
		
		assertTrue(expectedOutputs.containsAll(actualOutputs));
		assertTrue(actualOutputs.containsAll(expectedOutputs));

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
				
				AsynchronousActorCompletionService service = new UnorderedActorCompletionService(queueLength);
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
			
				UnorderedActorCompletionService service = new UnorderedActorCompletionService(queueLength);
				assertEquals(AsynchronousActorCompletionService.Service.STOPPED, service.getState());

				System.out.println("Starting service");			
				service.start();
				assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());

				for (int i = 1; i <= actorCount; i++) {
					service.submit(_testActor, variables);
					assertEquals(AsynchronousActorCompletionService.Service.ACTIVE, service.getState());
					System.out.println("Submitted actor " + i);
					
					if (i == queueLength) {
						
						assertEquals(queueLength, service._runningActorQueue.size());
						assertEquals(0, service._steppedActorQueue.size());
						assertEquals(0, service._completedActorQueue.size());
						
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
				System.out.println("Took actor " + i);
				assertEquals(AsynchronousActorCompletionService.Service.IDLE, service.getState());
			}
		}
	}

	static class ActorCompletionBarrier {
		private boolean isUp = false;
		public synchronized void raise() { isUp = true; }
		public synchronized void lower() { isUp = false; }
		public synchronized boolean isUp() { return isUp; }
	}
}
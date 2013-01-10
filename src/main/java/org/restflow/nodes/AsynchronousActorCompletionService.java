package org.restflow.nodes;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.restflow.actors.AbstractActorRunner;
import org.restflow.actors.Actor;
import org.restflow.actors.ActorRunner;
import org.restflow.util.Contract;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public abstract class AsynchronousActorCompletionService implements ActorCompletionService {

	@GuardedBy("this") protected Thread _completionManagementThread;
	@GuardedBy("this") private int _activeActorCount;
	@GuardedBy("this") private Service _state;
	
	protected final BlockingQueue<ActorRunner> _runningActorQueue;
	protected final BlockingQueue<ActorRunner> _completedActorQueue;
	
	public AsynchronousActorCompletionService(int maxConcurrency) {
		
		super();
		
		_runningActorQueue = new LinkedBlockingQueue<ActorRunner>(maxConcurrency);
		_completedActorQueue = new LinkedBlockingQueue<ActorRunner>(maxConcurrency);

		synchronized(this) {
			_activeActorCount = 0;
			_state = Service.STOPPED;		
		}
	}
	
	public synchronized void start() {
		
		Contract.disallows(_state == Service.SHUTTING_DOWN, "Service cannot be started because it is shutting down");
		Contract.disallows(_state == Service.SHUT_DOWN, "Service cannot be started because it is shut down");
		Contract.disallows(_state == Service.IDLE, "Service cannot be started because it is already active");
		Contract.disallows(_state == Service.ACTIVE, "Service cannot be started because it is already active");
		Contract.requires(_state == Service.STOPPED);
		
//		_completionManagementThread.start();
		
		_state = Service.IDLE;
	}

	public synchronized Actor submit(Actor actorMaster, Map<String,Object> variables) {
		
		Contract.disallows(_state == Service.STOPPED, "Actor cannot be submitted because the service is stopped");
		Contract.disallows(_state == Service.SHUTTING_DOWN, "Actor cannot be submitted because the service is being shut down");
		Contract.disallows(_state == Service.SHUT_DOWN, "Actor cannot be submitted because the service is shut down");

		Actor actor = null;
		
		try {
			actor = (Actor) actorMaster.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		_state = Service.ACTIVE;
		_activeActorCount++;
				
		return actor;
	}

	
	
	public ActorRunner take() throws InterruptedException {
		
		_takeBegin();
		
		ActorRunner runner = _completedActorQueue.take();
		
		_takeEnd(runner);

		return runner;
	}
	
	private synchronized void _takeBegin() {
		
		Contract.disallows(_state == Service.STOPPED, "Cannot take a runner because service is stopped");

		if (_state == Service.IDLE) {
			_state = Service.ACTIVE; 
		}
	}
	
	private synchronized void _takeEnd(ActorRunner runner) {
		
		Contract.disallows(_state == Service.STOPPED, "Cannot take a runner because service is stopped");
		Contract.disallows(_state == Service.IDLE, "Cannot take a runner because service is idle");

		if (_state == Service.SHUT_DOWN) {
			_state = Service.STOPPED;
			Contract.requires(_activeActorCount == 0);
		} else {
			_activeActorCount--;
			if (_state == Service.ACTIVE && _activeActorCount == 0) {
				_state = Service.IDLE;
			} else if (_state == Service.SHUTTING_DOWN && _activeActorCount == 0) {
				Contract.disallows(runner == AbstractActorRunner.EndOfActorRunners);
				_state = Service.SHUT_DOWN;
			}
		}		
	}

	public synchronized void shutdown() {
		
		if (_state == Service.IDLE) {
			_state = Service.SHUT_DOWN;
			signalEndOfRunners();
		} else if (_state == Service.ACTIVE) {
			_state = Service.SHUTTING_DOWN;
			signalEndOfRunners();
		}
	}

	public abstract void signalEndOfRunners();
	
	protected Service getState() {
		return _state;
	}
	
	protected enum Service {
		STOPPED,
		IDLE,
		ACTIVE,
		SHUTTING_DOWN,
		SHUT_DOWN
	}
}

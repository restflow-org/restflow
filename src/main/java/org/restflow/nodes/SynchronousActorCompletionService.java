package org.restflow.nodes;

import java.util.Map;

import org.restflow.actors.AbstractActorRunner;
import org.restflow.actors.Actor;
import org.restflow.actors.ActorRunner;
import org.restflow.actors.SynchronousActorRunner;
import org.restflow.util.Contract;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  Its only mutable field is synchronized on the instance.
 */
@ThreadSafe()
public class SynchronousActorCompletionService implements ActorCompletionService {

	@GuardedBy("this") private ActorRunner _runner;
	@GuardedBy("this") private Service _state;
	
	public SynchronousActorCompletionService() {
		super();
		synchronized(this) {
			_state = Service.STOPPED;
		}
	}
	
	public synchronized void start() {
		
		Contract.requires(_state == Service.STOPPED, "Service cannot be started because it is already active");
		
		_state = Service.IDLE;
	}

	public synchronized Actor submit(Actor actor, Map<String,Object> variables) {
		
		Contract.disallows(_state == Service.STOPPED, "Actor cannot be submitted because the service is stopped");
		Contract.disallows(_state == Service.SHUT_DOWN, "Actor cannot be submitted because the service is shut down");
		Contract.disallows(_state == Service.BLOCKED, "Actor cannot be submitted without taking the prior one");
		
		_runner = new SynchronousActorRunner(actor, variables);
		_runner.run();
		
		_state = Service.BLOCKED;
		
		return actor;
	}
	
	public synchronized ActorRunner take() {
		
		Contract.disallows(_state == Service.STOPPED, "Cannot take a runner when the service is stopped");
		Contract.disallows(_state == Service.IDLE, "Cannot take a runner before submitting one");
				
		ActorRunner lastRunner = _runner;
		_runner = null;
		
		if (_state == Service.BLOCKED) {
			_state = Service.IDLE;
		} else if (_state == Service.SHUT_DOWN) {
			_state = Service.STOPPED;
		}
		
		return lastRunner;
	}

	public synchronized void shutdown() {
		
		if (_state == Service.BLOCKED || _state == Service.IDLE) {
			_runner = AbstractActorRunner.EndOfActorRunners;
			_state = Service.SHUT_DOWN;
		} 
	}
	
	protected Service getState() {
		return _state;
	}
	
	protected enum Service {
		STOPPED,
		IDLE,
		BLOCKED,
		SHUT_DOWN
	}
}
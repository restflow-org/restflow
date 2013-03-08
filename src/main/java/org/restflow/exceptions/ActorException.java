package org.restflow.exceptions;

import org.restflow.actors.Actor;

import net.jcip.annotations.Immutable;

/**
 * This class is immutable and thus thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class ActorException extends RestFlowException {
	
	private final Actor _actor;
	private final Exception _exception;

	public ActorException(Actor actor, Exception exception) {
		super(exception);
		_actor = actor;
		_exception = exception;
	}
	
	public ActorException(Actor actor, String message) {
		super(message);
		_actor = actor;
		_exception = null;
	}
	
	public ActorException(Actor actor, String message, Throwable cause) {
		super(message, cause);
		_actor = actor;
		_exception = null;
	}
	
	public Actor getActor() {
		return _actor;
	}

	public Exception getException() {
		return _exception;
	}

	public String getMessage() {
		if (_exception == null) {
			return super.getMessage();
		} else {
			return "Actor " + _actor.getFullyQualifiedActorName() + " threw exception: " +  _exception;
		}
	}
}
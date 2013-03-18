package org.restflow.exceptions;

import org.restflow.actors.Actor;

import net.jcip.annotations.Immutable;

/**
 * This class adds no state to its thread-safe superclass and is thus itself thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class NullInputException extends ActorException {
	
	public NullInputException(Actor actor, String label) {
		super(actor, "Null data received on non-nullable input '" + label + "' of actor '" + actor.getFullyQualifiedActorName() + "'");
	}
}
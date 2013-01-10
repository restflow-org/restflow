package org.restflow.exceptions;

import org.restflow.actors.Actor;

import net.jcip.annotations.Immutable;

/**
 * This class adds no state to its thread-safe superclass and is thus itself thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class NullOutputException extends ActorException {
	
	public NullOutputException(Actor actor, String label) {
		super(actor, "Null data produced on non-nullable output '" + label + "' of actor '" + actor + "'");
	}
}
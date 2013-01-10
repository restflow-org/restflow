package org.restflow.actors;

import java.util.Map;

import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  Its single mutable field refers to an immutable object and
 * is marked volatile.
 */
@ThreadSafe()
public class SynchronousActorRunner extends AbstractActorRunner {

	public SynchronousActorRunner(Actor actor, Map<String,Object> variables) {
		super(actor, variables);
	}
}

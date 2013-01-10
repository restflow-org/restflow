package org.restflow.actors;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  Its single mutable field refers to an immutable object and
 * is marked volatile.
 */
@ThreadSafe()
public class UnorderedActorRunner extends AsynchronousActorRunner {

	/*************************
	 *  private final fields *
	 *************************/
	private final BlockingQueue<ActorRunner> _completionQueue;
	
	/***********************
	 *  public constructor *
	 ***********************/
	
	public UnorderedActorRunner(Actor actor, Map<String,Object> variables, BlockingQueue<ActorRunner> queue) {	
		super(actor, variables);
		
		if (queue == null) {
			throw new IllegalArgumentException("The queue argument cannot be null.");
		}
		
		_completionQueue = queue;
	}
	
	/**********************
	 *  lifecycle methods *
	 **********************/
	
	public void run() {
		
		super.run();

		try {
			_completionQueue.put(this);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}
}

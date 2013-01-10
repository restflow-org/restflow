package org.restflow.actors;

import java.util.Map;

import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  Its single mutable field refers to an immutable object and
 * is marked volatile.
 */
@ThreadSafe()
public abstract class AsynchronousActorRunner extends AbstractActorRunner {

	/*************************
	 *  private final fields *
	 *************************/
	private final Thread _thread;
	
	/***********************
	 *  public constructor *
	 ***********************/
	
	public AsynchronousActorRunner(Actor actor, Map<String,Object> variables) {
		super(actor, variables);
		_thread = new Thread(this, actor.toString());
	}

	/**********************
	 *  lifecycle methods *
	 **********************/
	
	public void start() {
		_thread.start();
	}

	public void waitForCompletion() throws InterruptedException {
		_thread.join();
	}	
}

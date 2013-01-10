package org.restflow.actors;

import java.util.Map;

public abstract class AbstractActorRunner implements ActorRunner {

	/*******************
	 *  private fields *
	 *******************/
	private final Map<String,Object> _variables;

	/*********************
	 *  protected fields *
	 *********************/
	protected final Actor _actor;
	protected volatile Exception _exception;
	
	/************************
	 *  static final fields *
	 ************************/
	public static final ActorRunner EndOfActorRunners = 
		new EmptyActorRunner();
	
	/***********************
	 *  public constructor *
	 ***********************/
	public AbstractActorRunner(Actor actor, Map<String,Object> variables) {
		_actor = actor;
		_variables = variables;
		_exception = null;
	}
	
	/**************
	 *  accessors *
	 **************/
	@Override
	public Actor getActor() {
		return _actor;
	}

	@Override
	public Map<String,Object> getVariables() {
		return _variables;
	}
	
	@Override
	public Exception getException() {
		return _exception;
	}

	public String toString() {
		return "ActorRunner for " + _actor;
	}

	/**********************
	 *  lifecycle methods *
	 **********************/

	@Override
	public void run() {
		try {
			_actor.step();
		} catch (Exception e) {
			_exception = e;
		}
	}
	
	private static class EmptyActorRunner extends AbstractActorRunner {
		public EmptyActorRunner() {
			super(null, null);
		}
	}
}

package org.restflow.actors;

import java.util.Map;

public interface ActorRunner extends Runnable {
	public abstract Actor getActor();
	public abstract Map<String,Object> getVariables();
	public abstract Exception getException();
}
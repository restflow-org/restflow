package org.restflow.nodes;

import java.util.Map;

import org.restflow.actors.Actor;
import org.restflow.actors.ActorRunner;


public interface ActorCompletionService {
	Actor submit(Actor actor, Map<String,Object> variables);
	public ActorRunner take() throws InterruptedException;
	void shutdown();
	void start();
}

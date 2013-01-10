package org.restflow.actors;

import java.util.Map;

import org.restflow.WorkflowContext;


public interface ActorBuilder {
	public ActorBuilder context(WorkflowContext context);
	public ActorBuilder types(Map<String,String> types);
	public Actor build() throws Exception;
}

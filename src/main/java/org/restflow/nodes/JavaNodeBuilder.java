package org.restflow.nodes;

import org.restflow.actors.JavaActorBuilder;

public class JavaNodeBuilder extends ActorNodeBuilder {
	
	private JavaActorBuilder _javaActorBuilder = new JavaActorBuilder();

	public JavaNodeBuilder bean(Object object) {
		_javaActorBuilder.bean(object);
		return this;
	}
	
	public JavaNodeBuilder state(String name) {
		_javaActorBuilder.state(name);
		return this;
	}
	
	public ActorWorkflowNode build() throws Exception {
		_javaActorBuilder.context(_context);
		if (_actor == null) {
			_actor = _javaActorBuilder.build();
		}
		return super.build();
	}
}

package org.restflow.nodes;

import org.restflow.actors.TclActorBuilder;


public class TclNodeBuilder extends ActorNodeBuilder {
	
	private TclActorBuilder _tclActorBuilder = new TclActorBuilder();

	public TclNodeBuilder step(String script) {
		_tclActorBuilder.step(script);
		return this;
	}
	
	public TclNodeBuilder input(String name) {
		super.input(name);
		_tclActorBuilder.input(name);
		return this;
	}
	
	public TclNodeBuilder state(String name) {
		_tclActorBuilder.state(name);
		return this;
	}
	
	public TclNodeBuilder initialize(String string) {
		_tclActorBuilder.initialize(string);
		return this;
	}

	public ActorWorkflowNode build() throws Exception {
		
		_tclActorBuilder.context(_context);

		_actor = _tclActorBuilder.build();

		return super.build();
	}
}

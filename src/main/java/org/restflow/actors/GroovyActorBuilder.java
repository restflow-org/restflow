package org.restflow.actors;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;


public class GroovyActorBuilder implements ActorBuilder {
	
	private String	_name = "";
	private String _initialize = "";
	private String _step = "";
	private String _wrapup = "";
	private Map<String,Object>	_inputs = new HashMap<String,Object>(); 
	private Map<String,Object>	_outputs = new HashMap<String,Object>(); 
	private Map<String,Object> 	_state = new HashMap<String,Object>();
	private Map<String,String>	_types = new HashMap<String,String>();
	private WorkflowContext _context; 
		
	public GroovyActorBuilder state(String name) {
		_state.put(name,null);
		return this;
	}
	
	public GroovyActorBuilder initialize(String initialize) {
		_initialize = initialize;
		return this;
	}
	
	public GroovyActorBuilder step(String step) {
		_step = step;
		return this;
	}

	public GroovyActorBuilder wrapup(String wrapup) {
		_wrapup = wrapup;
		return this;
	}

	public GroovyActorBuilder input(String name) {
		_inputs.put(name, null);
		return this;
	}

	public GroovyActorBuilder input(String name, Map<String,Object> properties) {
		_inputs.put(name, properties);
		return this;
	}	
	
	public GroovyActorBuilder output(String name) {
		_outputs.put(name, null);
		return this;
	}

	public GroovyActorBuilder output(String name, Map<String,Object> properties) {
		_outputs.put(name, properties);
		return this;
	}

	public GroovyActorBuilder name(String name) {
		_name = name;
		return this;
	}

	public GroovyActorBuilder context(WorkflowContext context) {
		_context = context;
		return this;
	}
	

	@Override
	public ActorBuilder types(Map<String, String> types) {
		_types.putAll(types);
		return this;
	}
	
	public GroovyActor build() throws Exception {
		
		GroovyActor actor = new GroovyActor();
		
		actor.setName(_name);
		actor.setInputs(_inputs);
		actor.setOutputs(_outputs);
		actor.setState(_state);
		actor.setTypes(_types);
		actor.setInitialize(_initialize);
		actor.setStep(_step);
		actor.setWrapup(_wrapup);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		
		return actor;
	}
}

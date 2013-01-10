package org.restflow.actors;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.data.InputProperty;
import org.restflow.util.BitPattern;


public class TclActorBuilder implements ActorBuilder {
	
	private String	_name = "";
	private String _initialize = "";
	private String _step = "";
	private String _wrapup = "";
	private Map<String,Object> 	_inputs = new HashMap<String,Object>(); 
	private Map<String,Object> 	_outputs = new HashMap<String,Object>(); 
	private Map<String,Object> _state = new HashMap<String,Object>(); 
	private Map<String,String>	_types = new HashMap<String,String>();
	private WorkflowContext	_context = null;
		
	public TclActorBuilder state(String name) {
		_state.put(name,null);
		return this;
	}
	
	public TclActorBuilder initialize(String initialize) {
		_initialize = initialize;
		return this;
	}
	
	public TclActorBuilder step(String step) {
		_step = step;
		return this;
	}

	public TclActorBuilder wrapup(String wrapup) {
		_wrapup = wrapup;
		return this;
	}

	public TclActorBuilder input(String name) {
		_inputs.put(name, null);
		return this;
	}

	public TclActorBuilder input(String name, Map<String,Object> properties) {
		_inputs.put(name, properties);
		return this;
	}
	
	public TclActorBuilder input(String name, int propertyBitPattern) {
		
		Map<String,Object> properties = new HashMap<String,Object>();
		
		if (BitPattern.includes(InputProperty.Optional, propertyBitPattern)) {
			properties.put("optional", true);
		}
		
		if (BitPattern.includes(InputProperty.Nullable, propertyBitPattern)) {
			properties.put("nullable", true);
		}
		
		if (BitPattern.includes(InputProperty.DefaultReadinessFalse, propertyBitPattern)) {
			properties.put("defaultReadiness", false);
		}
		
		_inputs.put(name, properties);
		return this;
	}
	
	public TclActorBuilder output(String name) {
		_outputs.put(name, null);
		return this;
	}

	public TclActorBuilder output(String name, Map<String,Object> properties) {
		_outputs.put(name, properties);
		return this;
	}
	
	public TclActorBuilder context(WorkflowContext context) {
		_context = context;
		return this;
	}

	public TclActorBuilder name(String name) {
		_name = name;
		return this;
	}
	
	@Override
	public ActorBuilder types(Map<String, String> types) {
		_types.putAll(types);
		return this;
	}
	
	public TclActor build() throws Exception {
		
		TclActor actor = new TclActor();
		
		if (_context != null) {
			actor.setApplicationContext(_context);
		}

		actor.setName(_name);
		actor.setInputs(_inputs);
		actor.setOutputs(_outputs);
		actor.setState(_state);
		actor.setTypes(_types);
		actor.setInitialize(_initialize);
		actor.setStep(_step);
		actor.setWrapup(_wrapup);

		actor.afterPropertiesSet();
		
		return actor;
	}
}

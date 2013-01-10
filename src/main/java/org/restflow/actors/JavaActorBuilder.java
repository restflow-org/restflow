package org.restflow.actors;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.data.InputProperty;
import org.restflow.util.BitPattern;


public class JavaActorBuilder implements ActorBuilder {
	
	private Object 				_bean = null;
	private WorkflowContext 	_context;
	private Map<String,Object> 	_inputs = new HashMap<String,Object>(); 
	private String				_name = "";
	private Map<String,Object> 	_outputs = new HashMap<String,Object>(); 
	private Map<String,Object> 	_state = new HashMap<String,Object>();
	private Map<String,String>	_types = new HashMap<String,String>();
	private String				_stepMethod = null;
	
	public JavaActorBuilder stepMethod(String name) {
		_stepMethod = name;
		return this;
	}
	
	public JavaActorBuilder state(String name) {
		_state.put(name,null);
		return this;
	}

	public JavaActorBuilder name(String name) {
		_name = name;
		return this;
	}

	public JavaActorBuilder bean(Object bean) {
		_bean = bean;
		return this;
	}
	
	public JavaActorBuilder input(String name) {
		_inputs.put(name, null);
		return this;
	}

	public JavaActorBuilder input(String name, Map<String,Object> properties) {
		_inputs.put(name, properties);
		return this;
	}

	public JavaActorBuilder input(String name, int propertyBitPattern) {
		
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
	
	public JavaActorBuilder output(String name) {
		_outputs.put(name, null);
		return this;
	}

	public JavaActorBuilder output(String name, Map<String,Object> properties) {
		_outputs.put(name, properties);
		return this;
	}
	

	@Override
	public ActorBuilder types(Map<String, String> types) {
		_types.putAll(types);
		return this;
	}

	public JavaActor build() throws Exception {
		
		JavaActor actor = new JavaActor();
		actor.setName(_name);
		actor.setWrappedBean(_bean);
		actor.setStepMethod(_stepMethod);
		actor.setInputs(_inputs);
		actor.setOutputs(_outputs);
		actor.setTypes(_types);
		actor.setState(_state);
		actor.setApplicationContext(_context);
		actor.afterPropertiesSet();
		
		return actor;
	}

	public JavaActorBuilder context(WorkflowContext context) {
		_context = context;
		return this;
	}
}

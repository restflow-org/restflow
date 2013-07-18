package org.restflow.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.actors.Actor;
import org.restflow.actors.ActorBuilder;
import org.restflow.data.InflowProperty;
import org.restflow.util.BitPattern;


public class ActorNodeBuilder  {
	
	protected Actor 					_actor = null;
	protected Map<String, Object> 		_constants = new HashMap<String, Object>();
	protected Map<String, Object> 		_inflows = new HashMap<String, Object>();
	protected int						_maxConcurrency = 1;
	protected boolean					_repeatValues = false;
	protected String 					_name = "";
	protected Map<String, Object> 		_outflows = new HashMap<String, Object>();
	protected Map<String, String> 		_types = new HashMap<String, String>();
	protected Map<String, List<Object>>	_sequences = new HashMap<String, List<Object>>();
	protected boolean					_ordered = true;
	private boolean 					_endFlowOnNoOutput = false;
	private boolean 					_stepsOnce = false;
	private String 						_prefix;
	protected WorkflowContext			_context;
	private ActorBuilder 				_actorBuilder;
	
	public ActorNodeBuilder context(WorkflowContext context) {
		_context = context;
		return this;
	}
	
	public ActorNodeBuilder bean(Object object) {
		throw new UnsupportedOperationException();
	}
	
	public ActorNodeBuilder step(String string) {
		throw new UnsupportedOperationException();
	}
	
	public ActorNodeBuilder initialize(String string) {
		throw new UnsupportedOperationException();
	}
	
	public ActorNodeBuilder maxConcurrency(int m) {
		_maxConcurrency = m;
		return this;
	}

	public ActorNodeBuilder repeatValues(boolean value) {
		_repeatValues = value;
		return this;
	}
	
	public ActorNodeBuilder actor(Actor actor) {
		_actor = actor;
		return this;
	}
	
	public ActorNodeBuilder actor(ActorBuilder actorBuilder) {
		_actorBuilder = actorBuilder;
		return this;
	}
	
	public ActorNodeBuilder name(String name) {
		_name = name;
		return this;
	}
	
	public ActorNodeBuilder input(String name) {
		return this;
	}
	
	public ActorNodeBuilder type(String variableName, String type) {
		_types.put(variableName, type);
		return this;
	}

	public ActorNodeBuilder inflow(String expression, String label, Map<String,Object> properties) {
		
		input(label);

		Map<String,Object> inflowPropertyMap = new HashMap<String,Object>(properties);
		inflowPropertyMap.put("expression", expression);
		_inflows.put(label,inflowPropertyMap);

		return this;
	}

	public ActorNodeBuilder inflow(String expression, String label, int properties) {
		input(label);
		
		if (properties != 0) {
			
			Map<String,Object> inflowPropertyMap = new HashMap<String,Object>();
			inflowPropertyMap.put("expression", expression);
			
			if (BitPattern.includes(properties, InflowProperty.ReceiveOnce)) {
				inflowPropertyMap.put("receiveOnce", true);
			}
			
			_inflows.put(label,inflowPropertyMap);
			
		}else {
			inflow(expression, label);
		}
		
		return this;
	}

	
	public ActorNodeBuilder inflow(String inflowExpression, String label) {
		input(label);
		_inflows.put(label, inflowExpression);
		return this;
	}
	
	public ActorNodeBuilder inflow(String label) {
		_inflows.put(label, "");
		return this;
	}

	public ActorNodeBuilder outflow(String label) {
		_outflows.put(label, null);
		return this;
	}


	public ActorNodeBuilder outflow(String label, String outflowExpression) {
		_outflows.put(label, outflowExpression);
		return this;
	}
	
	public ActorNodeBuilder constant(String name, Object value) {
		_constants.put(name, value);
		input(name);
		return this;
	}
	
	public ActorNodeBuilder ordered(boolean ordered) {
		_ordered = ordered;
		return this;
	}

	public ActorNodeBuilder sequence(String name, List<Object> values) {
		_sequences.put(name, values);
		return this;
	}

	public ActorNodeBuilder sequence(String name, Object[] values) {
		input(name);
		List<Object> valueList = new ArrayList<Object>();
		for (Object value : values) {
			valueList.add(value);
		}
		_sequences.put(name, valueList);
		return this;
	}
	
	public ActorNodeBuilder state(String string) {
		return this;
	}

	public ActorNodeBuilder endFlowOnNoOutput() {
		_endFlowOnNoOutput = true;
		return this;
	}
	
	public ActorNodeBuilder stepsOnce() {
		_stepsOnce = true;
		return this;
	}

	public ActorNodeBuilder prefix(String prefix) {
		_prefix = prefix;
		return this;
	}

	public ActorNodeBuilder store(Map<String,Object> _store) {
		return this;
	}

	public ActorWorkflowNode build() throws Exception {
		
		if (_actorBuilder != null) {
			_actorBuilder.context(_context)
						 .types(_types);
			_actor = _actorBuilder.build();
		}
		
		ActorWorkflowNode node = new ActorWorkflowNode();
		
		node.setName(_name);
		node.setActor(_actor);
		node.setInflows(_inflows);
		node.setOutflows(_outflows);
		node.setConstants(_constants);
		node.setSequences(_sequences);
		node.setRepeatValues(_repeatValues);
		node.setMaxConcurrency(_maxConcurrency);
		node.setOrdered(_ordered);
		node.setStepsOnce(_stepsOnce);
		node.setEndFlowOnNoOutput(_endFlowOnNoOutput);
		node.setNestedUriPrefix(_prefix);
		
		if (_context != null) {
			node.setApplicationContext(_context);
			_actor.setApplicationContext(_context);
		}
				
		return node;
	}
}

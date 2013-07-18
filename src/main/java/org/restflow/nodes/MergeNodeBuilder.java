package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;


public class MergeNodeBuilder {
	
	private WorkflowContext			_context;
	private String 					_name = "";
	private Map<String,Object> 		_inflows = new HashMap<String, Object>();
	private Map<String,Object>		_outflows = new HashMap<String, Object>();
	
	public MergeNodeBuilder name(String name) {
		_name = name;
		return this;
	}

	public MergeNodeBuilder inflow(String expression) {
		_inflows.put("LabelFor" + expression, expression);
		return this;
	}

	public MergeNodeBuilder outflow(String expression) {
		_outflows.put("LabelFor" + expression, expression);
		return this;
	}

	public MergeNodeBuilder context(WorkflowContext context) {
		_context = context;
		return this;
	}

	public NonDeterministicMerge build() throws Exception {
		
		NonDeterministicMerge node = new NonDeterministicMerge();
		
		node.setApplicationContext(_context);
		node.setName(_name);
		node.setInflows(_inflows);
		node.setOutflows(_outflows);
		 
		return node;
	}
}


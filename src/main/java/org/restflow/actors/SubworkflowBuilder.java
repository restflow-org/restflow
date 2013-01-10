package org.restflow.actors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.directors.Director;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.InPortalBuilder;
import org.restflow.nodes.MergeNodeBuilder;
import org.restflow.nodes.OutPortalBuilder;
import org.restflow.nodes.SourceNodeBuilder;
import org.restflow.nodes.WorkflowNode;
import org.restflow.reporter.Reporter;


public class SubworkflowBuilder {
	
	private Director _director = null;
	private String _name = "";
	private List<WorkflowNode> _nodes = new LinkedList<WorkflowNode>();
	private Map<String,Object> _inputs = new HashMap<String,Object>();
	private Map<String,Object> _outputs = new HashMap<String,Object>();
	private Map<String,String> _ins = new HashMap<String,String>();
	private Map<String,String> _outs = new HashMap<String,String>();
	private List<ActorNodeBuilder> _actorNodeBuilders = new LinkedList<ActorNodeBuilder>();
	private List<SourceNodeBuilder> _sourceNodeBuilders = new LinkedList<SourceNodeBuilder>();
	private String _runUriPrefix = null;
	private List<MergeNodeBuilder> _mergeNodeBuilders = new LinkedList<MergeNodeBuilder>();
	protected WorkflowContext _context = null;

	private Reporter _preambleReporter;
	private Reporter _finalReporter;
	private Map<String,Reporter> _reporters = new HashMap<String,Reporter>();

	public SubworkflowBuilder name(String name) {
		_name = name;
		return this;
	}
	
	public SubworkflowBuilder director(Director director) {
		_director = director;
		return this;
	}
	
	public SubworkflowBuilder node(WorkflowNode node) throws Exception {
		_nodes.add(node);
		return this;
	}
	
	public SubworkflowBuilder node(MergeNodeBuilder node) throws Exception {
		_mergeNodeBuilders.add(node);
		return this;
	}

	
	public SubworkflowBuilder node(ActorNodeBuilder builder) {
		_actorNodeBuilders.add(builder);
		return this;
	}
	
	public SubworkflowBuilder node(SourceNodeBuilder builder) {
		_sourceNodeBuilders.add(builder);
		return this;
	}

	public SubworkflowBuilder inflow(String label, String expression, Map<String,Object> properties) {
		input(label, properties);
		_ins.put(label, expression);
		return this;
	}

	
	public SubworkflowBuilder inflow(String label, String expression) {
		input(label);
		_ins.put(label, expression);
		return this;
	}

	public SubworkflowBuilder outflow(String expression, String label) {
		output(label);
		_outs.put(label, expression);
		return this;
	}
	
	public SubworkflowBuilder input(String name, Map<String,Object> properties) {
		_inputs.put(name, properties);
		return this;
	}

	public SubworkflowBuilder input(String name) {
		_inputs.put(name, null);
		return this;
	}
	
	public SubworkflowBuilder output(String name) {
		_outputs.put(name, null);
		return this;
	}
	
	public SubworkflowBuilder context(WorkflowContext context) {
		_context = context;
		return this;
	}
	
	public SubworkflowBuilder prefix(String prefix) {
		_runUriPrefix = prefix;
		return this;
	}

	public SubworkflowBuilder reporter(String name, Reporter reporter) {
		_reporters.put(name, reporter);
		return this;		
	}
	
	public SubworkflowBuilder preambleReporter(Reporter reporter) {
		_preambleReporter = reporter;
		return this;
	}

	public SubworkflowBuilder finalReporter(Reporter reporter) {
		_finalReporter = reporter;
		return this;
	}


	public Workflow build() throws Exception {
		
		Workflow workflow = new Workflow();
		
		workflow.setName(_name);

		if (_context != null) {
			workflow.setApplicationContext(_context);
		} else {
			throw new Exception("No context provided for workflow " + workflow);
		}
			
		if (_director == null) {
			_director = new PublishSubscribeDirector();
		}
		_director.setApplicationContext(_context);
		_director.afterPropertiesSet();
		
		workflow.setDirector(_director);
		
		_buildInflows();

		// build the outportal
		if (_outs.size() > 0) {
			
			// create an outportal to manage the workflow outputs
			OutPortalBuilder outPortalBuilder = new OutPortalBuilder();
			for (Map.Entry<String,String> entry : _outs.entrySet()) {

				// add output to workflow
				_outputs.put(entry.getKey(),null);

				// add output to outportal
				outPortalBuilder
					.name("outportal")
					.inflow(entry.getKey(), entry.getValue());
			}
			
			// add the outport to workflow
			_nodes.add(outPortalBuilder.build());
		}
		
		for (ActorNodeBuilder actorNodeBuilder : _actorNodeBuilders) {
			actorNodeBuilder.context(_context);
			WorkflowNode node = actorNodeBuilder.build();
			_nodes.add(node);
		}

		for (MergeNodeBuilder mergeNodeBuilder : _mergeNodeBuilders) {
			mergeNodeBuilder.context(_context);
			WorkflowNode node = mergeNodeBuilder.build();
			_nodes.add(node);
		}
		
		for (SourceNodeBuilder sourceNodeBuilder : _sourceNodeBuilders) {
			sourceNodeBuilder.context(_context);
			WorkflowNode node = sourceNodeBuilder.build();
			_nodes.add(node);
		}
		
		workflow.setInputs(_inputs);
		workflow.setNodes(_nodes);
		workflow.setOutputs(_outputs);

		if (_runUriPrefix != null) {
			workflow.setUriPrefix(_runUriPrefix);
		}
		
		workflow.setReports(_reporters);
		workflow.setPreambleReporter(_preambleReporter);
		workflow.setFinalReporter(_finalReporter);
		
		workflow.afterPropertiesSet();
		
		return workflow;
	}
	
	
	private void _buildInflows() throws Exception {
		if (_ins.size() > 0) {
			InPortalBuilder inPortalBuilder = new InPortalBuilder()
				.name("inportal");
			for (Map.Entry<String,String> entry : _ins.entrySet()) {
				inPortalBuilder.outflow(entry.getKey(), entry.getValue());
			}
			_nodes.add(inPortalBuilder.build());
		}
	}

}

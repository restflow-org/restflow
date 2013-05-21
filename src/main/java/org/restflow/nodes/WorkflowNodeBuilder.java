package org.restflow.nodes;

import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.actors.SubworkflowBuilder;
import org.restflow.actors.Workflow;
import org.restflow.directors.Director;
import org.restflow.reporter.Reporter;


public class WorkflowNodeBuilder extends ActorNodeBuilder {
	
	protected SubworkflowBuilder	_subworkflowBuilder = new SubworkflowBuilder();
	protected int					_nextInputIndex = 0;
	protected int					_nextOutputIndex = 0;

	public WorkflowNodeBuilder name(String name) {
		super.name(name);
		_subworkflowBuilder.name(name);
		return this;
	}

	public WorkflowNodeBuilder node(WorkflowNode node) throws Exception {
		_subworkflowBuilder.node(node);			
		return this;
	}

	public WorkflowNodeBuilder node(ActorNodeBuilder builder) throws Exception {
		_subworkflowBuilder.node(builder);			
		return this;
	}
	
	public WorkflowNodeBuilder node(SourceNodeBuilder builder) {
		_subworkflowBuilder.node(builder);			
		return this;
	}

	public WorkflowNodeBuilder node(MergeNodeBuilder builder) throws Exception {
		_subworkflowBuilder.node(builder);			
		return this;
	}

	public WorkflowNodeBuilder input(String outerExpression, String label, String innerExpression) {
		super.inflow(outerExpression, label);
		_subworkflowBuilder.inflow(label, innerExpression);
		return this;
	}
	
	public WorkflowNodeBuilder inflow(String outerExpression, String label, String innerExpression, Map<String,Object> properties) {
		super.inflow(outerExpression, label, properties);
		_subworkflowBuilder.inflow(label, innerExpression, properties);
		return this;
	}

	public WorkflowNodeBuilder inflow(String outerExpression, String label, String innerExpression) {
		super.inflow(outerExpression, label);
		_subworkflowBuilder.inflow(label, innerExpression);
		return this;
	}

	public WorkflowNodeBuilder inflow(String expression, String label, int stepsOnce) {
		super.inflow(expression, label, stepsOnce);
		_subworkflowBuilder.inflow(label, expression);
		return this;
	}
	
	public WorkflowNodeBuilder inflow(String outerExpression, String innerExpression) {	
		String label = "Input" + _nextInputIndex++;
		super.inflow(outerExpression, label);
		_subworkflowBuilder.inflow(label, innerExpression);
		return this;
		
	}
	
	public WorkflowNodeBuilder stepsOnce() {
		super.stepsOnce();
		return this;
	}

	public WorkflowNodeBuilder outflow(String innerExpression, String label, String outerExpression) {
		super.outflow(label, outerExpression);
		_subworkflowBuilder.outflow(innerExpression, label);
		return this;
	}

	public WorkflowNodeBuilder outflow(String innerExpression, String outerExpression) {
		String label = "Output" + _nextOutputIndex++;
		super.outflow(label, outerExpression);
		_subworkflowBuilder.outflow(innerExpression, label);
		return this;
	}

	public WorkflowNodeBuilder prefix(String prefix) {
		super.prefix(prefix);
		return this;
	}

	public WorkflowNodeBuilder context(WorkflowContext context) {
		_subworkflowBuilder.context(context);
		return this;
	}

	public WorkflowNodeBuilder director(Director director) {
		_subworkflowBuilder.director(director);
		return this;
	}

	public WorkflowNodeBuilder preambleReporter(Reporter reporter) {
		_subworkflowBuilder.preambleReporter(reporter);
		return this;
	}

	public WorkflowNodeBuilder finalReporter(Reporter reporter) {
		_subworkflowBuilder.finalReporter(reporter);
		return this;
	}

	public ActorWorkflowNode build() throws Exception {

		ActorWorkflowNode node = super.build();
		Workflow workflow = _subworkflowBuilder.build();		
		node.setActor(workflow);
		
		return node;
	}

}

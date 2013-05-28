package org.restflow.nodes;

public class NodeInput {
	
	private WorkflowNode node;
	private String inputLabel;
	
	public NodeInput(WorkflowNode node, String inputLabel) {
		super();
		this.node = node;
		this.inputLabel = inputLabel;
	}
	public WorkflowNode getNode() {
		return node;
	}
	public void setNode(WorkflowNode node) {
		this.node = node;
	}
	public String getInputLabel() {
		return inputLabel;
	}
	public void setInputLabel(String inputLabel) {
		this.inputLabel = inputLabel;
	}

}
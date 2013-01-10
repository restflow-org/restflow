package org.restflow.exceptions;

public class WorkflowMissingInputException extends Exception {

	private String missingInput;
	private String workflowName;

	public WorkflowMissingInputException(String missingInput, String workflowName ) {
		super("Missing workflow input '" + missingInput + "' for workflow "
				+ workflowName );

		this.missingInput = missingInput;
		this.workflowName = workflowName;

	}

	public String getMissingInput() {
		return missingInput;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5932248562432106204L;

}
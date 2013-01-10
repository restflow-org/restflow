package org.restflow.exceptions;

import net.jcip.annotations.Immutable;

/**
 * This class adds no state to its thread-safe superclass and is thus itself thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class WorkflowRuntimeException extends RestFlowException {

	public WorkflowRuntimeException(String message) {
		super(message);
	}

	public WorkflowRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
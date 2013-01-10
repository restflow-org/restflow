package org.restflow.exceptions;

import net.jcip.annotations.Immutable;

/**
 * This class adds no state to its thread-safe superclass and is thus itself thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class IllegalWorkflowSpecException extends RestFlowException {

	public IllegalWorkflowSpecException(String message) {
		super(message);
	}

	public IllegalWorkflowSpecException(String message, Throwable cause) {
		super(message, cause);
	}
}
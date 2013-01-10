package org.restflow.exceptions;

import net.jcip.annotations.Immutable;

/**
 * This class adds no state to its thread-safe superclass and is thus itself thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class InputTypeException extends WorkflowRuntimeException {
	
	public InputTypeException(String message, Throwable cause) {
		super(message, cause);
	}
}
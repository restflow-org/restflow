package org.restflow.exceptions;

import net.jcip.annotations.Immutable;

/**
 * This class adds no state to its thread-safe superclass and is thus itself thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class RestFlowException extends Exception {

	public RestFlowException(String message) {
		super(message);
	}

	public RestFlowException(Throwable cause) {
		super(cause);
	}

	public RestFlowException(String message, Throwable cause) {
		super(message, cause);
	}
}
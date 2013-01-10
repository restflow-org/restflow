package org.restflow.exceptions;

import net.jcip.annotations.Immutable;

/**
 * This class adds no state to its thread-safe superclass and is thus itself thread safe.
 */
@Immutable()
@SuppressWarnings("serial")
public class NodeDeclarationException extends IllegalWorkflowSpecException {

	public NodeDeclarationException(String message) {
		super(message);
	}

	public NodeDeclarationException(String message, Throwable cause) {
		super(message, cause);
	}

}
package org.restflow.data;

public interface ProtocolReader {
	boolean stepsOnce();
	Object getExternalResource(String uriPath) throws Exception;
	void initialize();
}

package org.restflow.data;

/**
 * This interface represents a unit of information flow between nodes in a workflow.
 * 
 * @author Timothy McPhillips
 */
public interface Packet {

	String[] getMetadataKeys();
	Object[] getMetadataValues();
	Protocol getProtocol();
	PublishedResource getResource(String pathTemplate);
	void setID(Long packetID);
	Long getID();
}
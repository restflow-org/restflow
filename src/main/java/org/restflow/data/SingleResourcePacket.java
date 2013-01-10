package org.restflow.data;

import net.jcip.annotations.ThreadSafe;


/**
 * This class represents a unit of information flow between nodes in a workflow.
 * All fields of a Packet instance are immutable and the instance methods entirely passive.
 * Null packet payloads are explicitly allowed by this class and meaningful in RestFlow.
 *
 * This class is thread safe.  Its superclass is thread safe, and its single field
 * is a final reference to an immutable object.
 * 
 * @author Timothy McPhillips
 */
@ThreadSafe()
public class SingleResourcePacket extends AbstractPacket {

	///////////////////////////////////////////////////////////////////
	////                  private instance fields                  ////

	private final PublishedResource _resource;
	
	///////////////////////////////////////////////////////////////////
	////                    public constructors                    ////

	/**
	 * Initializes the instance fields of a new packet.
	 * 
	 * @param resource			The data resource represented by the packet.
	 * @param protocol			The protocol by which the packet is passed between nodes.
	 * @param metadataKeys		The names of any metadata items associated with the resource.
	 * @param metadataValues	The values of any metadata items associated with the resource, and named via
	 * 							via the metadataKeys parameter.
	 */
	public SingleResourcePacket(PublishedResource resource, Protocol protocol, String[] metadataKeys, Object[] metadataValues) {
		
		super(protocol, metadataKeys, metadataValues);
		
		if (resource == null) {
			_resource = new PublishedResource(null);
		} else {
			_resource = resource;
		}
	}
	

	public SingleResourcePacket(PublishedResource resource) {
		this(resource, defaultProtocol, emptyStringArray, emptyStringArray);
	}

	/**
	 * Initializes the instance fields of a new packet using default values for all fields other than
	 * the data, and creating a new published resource for the data.
	 * 
	 * @param data	The data represented by the packet.
	 */
	public SingleResourcePacket(Object data) {
		this(new PublishedResource(data), defaultProtocol, emptyStringArray, emptyStringArray);
	}
	
	///////////////////////////////////////////////////////////////////
	////                      public accessors                    ////
	
	public PublishedResource getResource(String key) {
		return _resource;
	}

	public PublishedResource getResource() throws Exception {
		return _resource;
	}
}
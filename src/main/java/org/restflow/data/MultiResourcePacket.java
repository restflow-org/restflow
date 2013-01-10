package org.restflow.data;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

/**
 * This class represents a unit of information flow between nodes in a workflow.
 * All fields of a Packet instance are immutable and the instance methods entirely passive.
 * Null packet payloads are explicitly allowed by this class and meaningful in RestFlow.
 * 
 * This class is thread safe.  Its superclass is thread safe, and its single field
 * is a final reference to an instance of the synchronized Hashtable class.
 * 
 * @author Timothy McPhillips
 */
@ThreadSafe()
public class MultiResourcePacket extends AbstractPacket {

	///////////////////////////////////////////////////////////////////
	////                  private instance fields                  ////

	private final Map<String,PublishedResource> _resourceMap;

	///////////////////////////////////////////////////////////////////
	////                    public constructors                    ////

	/**
	 * Initializes the instance fields of a new packet.
	 * 
	 * @param resources			The collection of resource represented by the packet.
	 * @param protocol			The protocol by which the packet is passed between nodes.
	 * @param metadataKeys		The names of any metadata items associated with the data.
	 * @param metadataValues	The values of any metadata items associated with the data, and named via
	 * 							via the metadataKeys parameter.
	 */

	public MultiResourcePacket(Collection<PublishedResource> resources,
			Protocol protocol, String[] metadataKeys, Object[] metadataValues) {
		
		super(protocol, metadataKeys, metadataValues);
		
		_resourceMap = new Hashtable<String, PublishedResource>();		
		
		// copy the resources to the internal key-resource map
		for (PublishedResource resource : resources) {
			_resourceMap.put(resource.getKey(), resource);
		}
	}

	///////////////////////////////////////////////////////////////////
	////                      public accessors                    ////

	public Collection<PublishedResource> getResources() {
		return _resourceMap.values();
	}

	@Override
	public PublishedResource getResource(String key) {
		return _resourceMap.get(key);		
	}
}
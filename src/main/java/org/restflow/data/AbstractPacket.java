package org.restflow.data;

import java.util.Arrays;

import net.jcip.annotations.ThreadSafe;

/**
 * This class represents a unit of information flow between nodes in a workflow.
 * All fields of a Packet instance are immutable and the instance methods entirely passive.
 * Null packet payloads are explicitly allowed by this class and meaningful in RestFlow.
 * 
 * This class is thread safe because it's fields are final and both array fields are
 * copies of constructor parameter values.  Note that any objects may be used for the
 * elements of the _metadataValues array, and many themselves not be thread safe.
 *   
 * TODO Consider limiting metadata values to instances of String to ensure thread safety.
 * 
 * @author Timothy McPhillips
 */
@ThreadSafe
public abstract class AbstractPacket implements Packet {

	///////////////////////////////////////////////////////////////////
	////                  private instance fields                  ////

	private Long		   _id;
	private final Protocol _protocol;
	private final String[] _metadataKeys;
	private final Object[] _metadataValues;
	
	///////////////////////////////////////////////////////////////////
	////                   public class fields                     ////

	/* The protocol used when no protocol is given for a packet*/
	public static final Protocol defaultProtocol = new DataProtocol();

	
	///////////////////////////////////////////////////////////////////
	////                  private class fields                     ////

	/** An empty array used when no metadata is associated with a packet */
	protected static final String[] emptyStringArray = new String[] {};

	///////////////////////////////////////////////////////////////////
	////                    public constructors                    ////

	/**
	 * Initializes the instance fields of a new packet.
	 * @param protocol			The protocol by which the packet is passed between nodes.
	 * @param metadataKeys		The names of any metadata items associated with the data.
	 * @param metadataValues	The values of any metadata items associated with the data, and named via
	 * 							via the metadataKeys parameter.
	 */
	public AbstractPacket(Protocol protocol, String[] metadataKeys, Object[] metadataValues) {
		
		// Protocols are immutable so simply store a reference to protocol.
		_protocol = protocol;
		
		// Defensively copy the metadataKeys array. Safe because String is immutable.
		_metadataKeys = Arrays.copyOf(metadataKeys, metadataKeys.length);
		
		// Defensively copy the metadata values array.  Safe if the values are immutable.
		_metadataValues = Arrays.copyOf(metadataValues, metadataValues.length);
	}
	
	///////////////////////////////////////////////////////////////////
	////                      public accessors                    ////
	
	/** @return The names of the metadata items associated with data in the packet. */
	public String[] getMetadataKeys() {
		
		// return a copy of the _metadataKeys array for safety
		return Arrays.copyOf(_metadataKeys, _metadataKeys.length);
	}

	/** @return The values of the metadata items associated with data in the packet. */
	public Object[] getMetadataValues() {

		// return a copy of the _metadataValues array for safety
		return Arrays.copyOf(_metadataValues, _metadataValues.length);
	}

	public Protocol getProtocol() {
		return _protocol;
	}
	
	public synchronized void setID(Long packetID) {
		_id = packetID;
	}
	
	public synchronized Long getID() {
		return _id;
	}
}